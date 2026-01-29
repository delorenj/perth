// Perth Integration Layer - Adapter Trait
// STORY-005: Integration Adapter Framework
//
// Defines the IntegrationAdapter trait that all external CLI integrations
// (Bloodbank, iMi, Jelmore) must implement. Provides a clean abstraction
// for both one-shot calls and long-running subscriptions.

use async_trait::async_trait;
use tokio::sync::mpsc::Receiver;

use super::error::{IntegrationError, IntegrationResult};

/// Trait for external CLI tool integrations.
///
/// Each adapter (Bloodbank, iMi, Jelmore) implements this trait to provide
/// a consistent interface for Dashboard components. The trait supports both
/// one-shot CLI calls and long-running subprocess subscriptions.
///
/// # Error Isolation
///
/// Adapters are designed to fail gracefully. Panics are caught at the adapter
/// boundary and converted to `IntegrationError`. A failing adapter does not
/// crash Zellij; instead, the affected Dashboard pane displays an error message.
///
/// # Example
///
/// ```ignore
/// use crate::integrations::{IntegrationAdapter, IntegrationResult};
///
/// struct iMiAdapter {
///     cli_path: String,
/// }
///
/// #[async_trait]
/// impl IntegrationAdapter for iMiAdapter {
///     async fn call(&self, args: &[&str]) -> IntegrationResult<String> {
///         // Execute `imi list --json` and return output
///     }
///
///     async fn subscribe(&self, args: &[&str]) -> IntegrationResult<Receiver<String>> {
///         // iMi doesn't support streaming, return error
///         Err(IntegrationError::NotRunning)
///     }
///
///     // ... other methods
/// }
/// ```
#[async_trait]
pub trait IntegrationAdapter: Send + Sync {
    /// Execute a one-shot CLI command and return its stdout output.
    ///
    /// This method spawns a subprocess, waits for it to complete, and returns
    /// the stdout content. Suitable for commands like `imi list --json` or
    /// `jelmore start-session <id>`.
    ///
    /// # Arguments
    ///
    /// * `args` - Command-line arguments to pass to the CLI
    ///
    /// # Returns
    ///
    /// * `Ok(String)` - The stdout output from the CLI
    /// * `Err(IntegrationError)` - If the CLI is not found, spawn fails, or process exits non-zero
    ///
    /// # Security
    ///
    /// Arguments are passed via `Command::arg()`, not shell interpolation,
    /// preventing command injection attacks.
    async fn call(&self, args: &[&str]) -> IntegrationResult<String>;

    /// Start a long-running subprocess and return a stream of output lines.
    ///
    /// This method spawns a subprocess that runs continuously (like
    /// `bloodbank subscribe --format json`) and returns a channel receiver
    /// for streaming output lines. The subprocess is monitored for health
    /// and automatically restarted on crash (up to max retries).
    ///
    /// # Arguments
    ///
    /// * `args` - Command-line arguments to pass to the CLI
    ///
    /// # Returns
    ///
    /// * `Ok(Receiver<String>)` - A bounded channel receiver for output lines
    /// * `Err(IntegrationError)` - If spawn fails or max restarts exceeded
    ///
    /// # Channel Behavior
    ///
    /// The returned channel has a capacity of 100 messages. If the consumer
    /// falls behind, oldest messages are dropped to prevent unbounded memory
    /// growth. This is acceptable for real-time event feeds.
    async fn subscribe(&self, args: &[&str]) -> IntegrationResult<Receiver<String>>;

    /// Stop any running subprocess.
    ///
    /// Sends SIGTERM to the subprocess, waits up to 2 seconds for graceful
    /// exit, then sends SIGKILL if still running. This method is idempotent;
    /// calling it when no subprocess is running is a no-op.
    ///
    /// # Returns
    ///
    /// * `Ok(())` - Subprocess stopped or wasn't running
    /// * `Err(IntegrationError)` - If stop operation fails
    async fn stop(&self) -> IntegrationResult<()>;

    /// Check if the subprocess is healthy.
    ///
    /// For long-running subscriptions, returns `true` if the subprocess is
    /// running and responsive. For adapters that only support one-shot calls,
    /// this may always return `true`.
    ///
    /// # Returns
    ///
    /// * `true` - Subprocess is running or adapter is one-shot only
    /// * `false` - Subprocess has crashed or is not responsive
    fn is_healthy(&self) -> bool;

    /// Get the name of this adapter for logging and error messages.
    ///
    /// # Returns
    ///
    /// A human-readable name like "Bloodbank", "iMi", or "Jelmore".
    fn name(&self) -> &str;
}

/// Configuration for adapter behavior
#[derive(Debug, Clone)]
pub struct AdapterConfig {
    /// Maximum number of restart attempts before giving up
    pub max_restarts: u8,

    /// Capacity of the output channel (bounded to prevent memory growth)
    pub channel_capacity: usize,

    /// Health check interval in seconds (for long-running subprocesses)
    pub health_check_interval_secs: u64,

    /// Timeout for one-shot calls in seconds
    pub call_timeout_secs: u64,

    /// Graceful shutdown timeout in seconds
    pub shutdown_timeout_secs: u64,
}

impl Default for AdapterConfig {
    fn default() -> Self {
        Self {
            max_restarts: 3,
            channel_capacity: 100,
            health_check_interval_secs: 5,
            call_timeout_secs: 30,
            shutdown_timeout_secs: 2,
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_adapter_config_defaults() {
        let config = AdapterConfig::default();
        assert_eq!(config.max_restarts, 3);
        assert_eq!(config.channel_capacity, 100);
        assert_eq!(config.health_check_interval_secs, 5);
    }
}
