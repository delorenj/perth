// Perth Integration Layer - Subprocess Manager
// STORY-005: Integration Adapter Framework
//
// Manages the lifecycle of external CLI subprocesses:
// - Spawning processes with proper argument handling
// - Concurrent stdout/stderr reading via tokio::select!
// - Health checks (poll process status every 5 seconds)
// - Automatic restart with exponential backoff (1s, 2s, 4s)
// - Graceful shutdown (SIGTERM, wait 2s, SIGKILL)
// - Bounded channels to prevent memory growth

use std::process::Stdio;
use std::sync::atomic::{AtomicBool, AtomicU8, Ordering};
use std::sync::Arc;
use std::time::Duration;

use tokio::io::{AsyncBufReadExt, BufReader};
use tokio::process::{Child, Command};
use tokio::sync::mpsc::{self, Receiver, Sender};
use tokio::sync::oneshot;
use tokio::time::{interval, sleep, timeout};

use super::adapter::AdapterConfig;
use super::error::{IntegrationError, IntegrationResult};

/// Manages the lifecycle of a subprocess for CLI integrations.
///
/// `SubprocessManager` handles the complex async subprocess management:
/// - Spawning the process with proper stdio configuration
/// - Reading stdout/stderr concurrently
/// - Detecting crashes and automatically restarting
/// - Exponential backoff between restart attempts
/// - Graceful shutdown with SIGTERM/SIGKILL escalation
///
/// # Thread Safety
///
/// The manager is designed to be used from a single async task, but health
/// status can be checked from any thread via atomic flags.
pub struct SubprocessManager {
    /// CLI command to execute (e.g., "bloodbank", "imi")
    command: String,

    /// Configuration for restart behavior, timeouts, etc.
    config: AdapterConfig,

    /// Handle to the running child process
    child: Option<Child>,

    /// Current restart count (reset on successful operation)
    restart_count: Arc<AtomicU8>,

    /// Sender for output lines
    output_tx: Sender<String>,

    /// Flag indicating if subprocess is healthy
    is_healthy: Arc<AtomicBool>,

    /// Channel to signal shutdown
    shutdown_tx: Option<oneshot::Sender<()>>,
}

impl SubprocessManager {
    /// Create a new subprocess manager.
    ///
    /// # Arguments
    ///
    /// * `command` - The CLI command to execute
    /// * `config` - Configuration for restart behavior, channel capacity, etc.
    ///
    /// # Returns
    ///
    /// A tuple of (manager, receiver) where the receiver provides output lines.
    pub fn new(command: &str, config: AdapterConfig) -> (Self, Receiver<String>) {
        let (output_tx, output_rx) = mpsc::channel(config.channel_capacity);

        let manager = Self {
            command: command.to_string(),
            config,
            child: None,
            restart_count: Arc::new(AtomicU8::new(0)),
            output_tx,
            is_healthy: Arc::new(AtomicBool::new(false)),
            shutdown_tx: None,
        };

        (manager, output_rx)
    }

    /// Start a one-shot command and return its output.
    ///
    /// This spawns the subprocess, waits for completion, and returns stdout.
    /// The process is expected to exit after producing output.
    ///
    /// # Arguments
    ///
    /// * `args` - Command-line arguments
    ///
    /// # Returns
    ///
    /// The stdout output as a string.
    pub async fn call(&self, args: &[&str]) -> IntegrationResult<String> {
        // Spawn the process
        let output = timeout(
            Duration::from_secs(self.config.call_timeout_secs),
            Command::new(&self.command)
                .args(args)
                .stdout(Stdio::piped())
                .stderr(Stdio::piped())
                .output(),
        )
        .await
        .map_err(|_| IntegrationError::Timeout {
            operation: format!("{} {}", self.command, args.join(" ")),
            duration_secs: self.config.call_timeout_secs,
        })?
        .map_err(|e| {
            if e.kind() == std::io::ErrorKind::NotFound {
                IntegrationError::CliNotFound(self.command.clone())
            } else {
                IntegrationError::SpawnFailed(e.to_string())
            }
        })?;

        if output.status.success() {
            Ok(String::from_utf8_lossy(&output.stdout).to_string())
        } else {
            Err(IntegrationError::ProcessExited {
                code: output.status.code().unwrap_or(-1),
                stderr: String::from_utf8_lossy(&output.stderr).to_string(),
            })
        }
    }

    /// Start a long-running subprocess with automatic restart.
    ///
    /// This spawns the subprocess and begins streaming output lines to the
    /// channel. The process is monitored for health and restarted on crash
    /// (up to max_restarts with exponential backoff).
    ///
    /// # Arguments
    ///
    /// * `args` - Command-line arguments
    ///
    /// # Returns
    ///
    /// Ok(()) when the subprocess exits normally or after max restarts exceeded.
    pub async fn start(&mut self, args: &[&str]) -> IntegrationResult<()> {
        let (shutdown_tx, shutdown_rx) = oneshot::channel();
        self.shutdown_tx = Some(shutdown_tx);

        self.run_with_restart(args, shutdown_rx).await
    }

    /// Run the subprocess with automatic restart on crash.
    async fn run_with_restart(
        &mut self,
        args: &[&str],
        mut shutdown_rx: oneshot::Receiver<()>,
    ) -> IntegrationResult<()> {
        let mut last_error: Option<String> = None;

        loop {
            let current_restarts = self.restart_count.load(Ordering::Relaxed);

            if current_restarts >= self.config.max_restarts {
                self.is_healthy.store(false, Ordering::Relaxed);
                return Err(IntegrationError::MaxRestartsExceeded {
                    attempts: current_restarts,
                    last_error: last_error.unwrap_or_else(|| "Unknown error".to_string()),
                });
            }

            // Apply exponential backoff if this is a restart
            if current_restarts > 0 {
                let backoff = self.calculate_backoff(current_restarts);
                log::info!(
                    "Subprocess {} crashed, restarting in {:?} (attempt {}/{})",
                    self.command,
                    backoff,
                    current_restarts + 1,
                    self.config.max_restarts
                );
                sleep(backoff).await;
            }

            // Spawn the subprocess
            match self.spawn_subprocess(args).await {
                Ok(child) => {
                    self.child = Some(child);
                    self.is_healthy.store(true, Ordering::Relaxed);

                    // Run the read loop until exit or shutdown
                    match self.read_loop(&mut shutdown_rx).await {
                        Ok(()) => {
                            // Normal exit requested
                            self.is_healthy.store(false, Ordering::Relaxed);
                            return Ok(());
                        }
                        Err(IntegrationError::ShutdownRequested) => {
                            self.is_healthy.store(false, Ordering::Relaxed);
                            return Ok(());
                        }
                        Err(e) => {
                            last_error = Some(e.to_string());
                            self.restart_count.fetch_add(1, Ordering::Relaxed);
                            continue;
                        }
                    }
                }
                Err(e) => {
                    last_error = Some(e.to_string());
                    self.restart_count.fetch_add(1, Ordering::Relaxed);
                    continue;
                }
            }
        }
    }

    /// Spawn the subprocess with proper stdio configuration.
    async fn spawn_subprocess(&self, args: &[&str]) -> IntegrationResult<Child> {
        Command::new(&self.command)
            .args(args)
            .stdout(Stdio::piped())
            .stderr(Stdio::piped())
            .kill_on_drop(true)
            .spawn()
            .map_err(|e| {
                if e.kind() == std::io::ErrorKind::NotFound {
                    IntegrationError::CliNotFound(self.command.clone())
                } else {
                    IntegrationError::SpawnFailed(e.to_string())
                }
            })
    }

    /// Read stdout/stderr concurrently until exit or shutdown.
    async fn read_loop(
        &mut self,
        shutdown_rx: &mut oneshot::Receiver<()>,
    ) -> IntegrationResult<()> {
        let child = self.child.as_mut().ok_or(IntegrationError::NotRunning)?;

        let stdout = child
            .stdout
            .take()
            .ok_or(IntegrationError::IoError("No stdout".to_string()))?;
        let stderr = child
            .stderr
            .take()
            .ok_or(IntegrationError::IoError("No stderr".to_string()))?;

        let mut stdout_reader = BufReader::new(stdout).lines();
        let mut stderr_reader = BufReader::new(stderr).lines();

        let mut health_check_interval =
            interval(Duration::from_secs(self.config.health_check_interval_secs));

        loop {
            tokio::select! {
                // Read stdout line
                line = stdout_reader.next_line() => {
                    match line {
                        Ok(Some(line)) => {
                            // Reset restart count on successful read
                            self.restart_count.store(0, Ordering::Relaxed);

                            // Send to channel (drop if full - bounded channel)
                            if self.output_tx.try_send(line).is_err() {
                                log::warn!("Output channel full, dropping line");
                            }
                        }
                        Ok(None) => {
                            // EOF - process exited
                            return Err(IntegrationError::ProcessExited {
                                code: 0,
                                stderr: "Process ended unexpectedly".to_string(),
                            });
                        }
                        Err(e) => {
                            return Err(IntegrationError::IoError(e.to_string()));
                        }
                    }
                }

                // Read stderr line (log warnings, don't send to output)
                line = stderr_reader.next_line() => {
                    match line {
                        Ok(Some(line)) => {
                            log::warn!("Subprocess {} stderr: {}", self.command, line);
                        }
                        Ok(None) => {
                            // EOF on stderr is normal
                        }
                        Err(e) => {
                            log::warn!("Error reading stderr: {}", e);
                        }
                    }
                }

                // Health check
                _ = health_check_interval.tick() => {
                    if let Some(ref mut child) = self.child {
                        match child.try_wait() {
                            Ok(Some(status)) => {
                                // Process exited
                                return Err(IntegrationError::ProcessExited {
                                    code: status.code().unwrap_or(-1),
                                    stderr: "Process exited during health check".to_string(),
                                });
                            }
                            Ok(None) => {
                                // Still running, healthy
                                self.is_healthy.store(true, Ordering::Relaxed);
                            }
                            Err(e) => {
                                log::warn!("Health check error: {}", e);
                            }
                        }
                    }
                }

                // Shutdown signal
                _ = &mut *shutdown_rx => {
                    return Err(IntegrationError::ShutdownRequested);
                }
            }
        }
    }

    /// Stop the subprocess gracefully.
    ///
    /// Sends SIGTERM, waits up to shutdown_timeout_secs, then SIGKILL if needed.
    pub async fn stop(&mut self) -> IntegrationResult<()> {
        // Signal shutdown to read loop
        if let Some(tx) = self.shutdown_tx.take() {
            let _ = tx.send(());
        }

        if let Some(ref mut child) = self.child {
            // Send SIGTERM (via kill on Unix)
            #[cfg(unix)]
            {
                use nix::sys::signal::{kill, Signal};
                use nix::unistd::Pid;

                if let Some(pid) = child.id() {
                    let _ = kill(Pid::from_raw(pid as i32), Signal::SIGTERM);
                }
            }

            // Wait for graceful exit
            let wait_result = timeout(
                Duration::from_secs(self.config.shutdown_timeout_secs),
                child.wait(),
            )
            .await;

            match wait_result {
                Ok(Ok(_)) => {
                    // Exited gracefully
                    self.is_healthy.store(false, Ordering::Relaxed);
                    self.child = None;
                    Ok(())
                }
                Ok(Err(e)) => {
                    log::warn!("Error waiting for subprocess: {}", e);
                    self.child = None;
                    self.is_healthy.store(false, Ordering::Relaxed);
                    Ok(())
                }
                Err(_) => {
                    // Timeout - force kill
                    log::warn!(
                        "Subprocess {} didn't exit gracefully, sending SIGKILL",
                        self.command
                    );
                    let _ = child.kill().await;
                    self.child = None;
                    self.is_healthy.store(false, Ordering::Relaxed);
                    Ok(())
                }
            }
        } else {
            Ok(())
        }
    }

    /// Calculate exponential backoff duration.
    ///
    /// Returns: 1s, 2s, 4s for restart counts 1, 2, 3 respectively.
    fn calculate_backoff(&self, restart_count: u8) -> Duration {
        // 1 << 0 = 1s, 1 << 1 = 2s, 1 << 2 = 4s
        let secs = 1u64 << restart_count.saturating_sub(1).min(3);
        Duration::from_secs(secs)
    }

    /// Check if the subprocess is currently healthy.
    pub fn is_healthy(&self) -> bool {
        self.is_healthy.load(Ordering::Relaxed)
    }

    /// Get the current restart count.
    pub fn restart_count(&self) -> u8 {
        self.restart_count.load(Ordering::Relaxed)
    }

    /// Reset the restart count (call after successful operation).
    pub fn reset_restart_count(&self) {
        self.restart_count.store(0, Ordering::Relaxed);
    }
}

impl Drop for SubprocessManager {
    fn drop(&mut self) {
        // Attempt to kill child on drop (kill_on_drop should handle this, but be safe)
        if let Some(ref mut child) = self.child {
            let _ = child.start_kill();
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_calculate_backoff() {
        let config = AdapterConfig::default();
        let (manager, _rx) = SubprocessManager::new("test", config);

        // First restart: 1 second
        assert_eq!(manager.calculate_backoff(1), Duration::from_secs(1));

        // Second restart: 2 seconds
        assert_eq!(manager.calculate_backoff(2), Duration::from_secs(2));

        // Third restart: 4 seconds
        assert_eq!(manager.calculate_backoff(3), Duration::from_secs(4));

        // Beyond max: caps at 8 seconds
        assert_eq!(manager.calculate_backoff(4), Duration::from_secs(8));
    }

    #[test]
    fn test_initial_state() {
        let config = AdapterConfig::default();
        let (manager, _rx) = SubprocessManager::new("test", config);

        assert!(!manager.is_healthy());
        assert_eq!(manager.restart_count(), 0);
    }

    #[tokio::test]
    async fn test_call_nonexistent_command() {
        let config = AdapterConfig::default();
        let (manager, _rx) = SubprocessManager::new("nonexistent_command_xyz_123", config);

        let result = manager.call(&["arg1"]).await;
        assert!(matches!(result, Err(IntegrationError::CliNotFound(_))));
    }

    #[tokio::test]
    async fn test_call_echo() {
        let config = AdapterConfig::default();
        let (manager, _rx) = SubprocessManager::new("echo", config);

        let result = manager.call(&["hello", "world"]).await;
        assert!(result.is_ok());
        assert_eq!(result.unwrap().trim(), "hello world");
    }
}
