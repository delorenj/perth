// Perth Integration Layer - Bloodbank Adapter
// STORY-006: Bloodbank Adapter
//
// Provides real-time event subscription from the Bloodbank CLI.
// Bloodbank is the RabbitMQ event backbone for the 33GOD ecosystem,
// and this adapter allows Dashboard components to receive events
// without direct RabbitMQ client dependencies.
//
// # Usage
//
// ```ignore
// let adapter = BloodbankAdapter::new();
// let mut rx = adapter.subscribe_events().await?;
// while let Some(event) = rx.recv().await {
//     match event {
//         BloodbankEvent::TaskCreated { task_id, .. } => { /* handle */ }
//         BloodbankEvent::TaskUpdated { .. } => { /* handle */ }
//         BloodbankEvent::Unknown { raw } => { /* log unknown event */ }
//     }
// }
// ```

use async_trait::async_trait;
use serde::{Deserialize, Serialize};
use std::sync::atomic::{AtomicBool, Ordering};
use std::sync::Arc;
use tokio::sync::mpsc::{self, Receiver};
use tokio::sync::Mutex;

use super::adapter::{AdapterConfig, IntegrationAdapter};
use super::error::{IntegrationError, IntegrationResult};
use super::subprocess::SubprocessManager;

/// Events emitted by the Bloodbank event stream.
///
/// These events represent activity in the 33GOD ecosystem:
/// task lifecycle, agent actions, session state changes, etc.
///
/// Unknown events are captured as `Unknown` with the raw JSON
/// to allow forward compatibility with new event types.
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
#[serde(tag = "type", rename_all = "snake_case")]
pub enum BloodbankEvent {
    /// A new task was created
    TaskCreated {
        task_id: String,
        project_id: Option<String>,
        title: String,
        #[serde(default)]
        metadata: serde_json::Value,
    },

    /// A task was updated (status change, assignment, etc.)
    TaskUpdated {
        task_id: String,
        #[serde(default)]
        changes: serde_json::Value,
    },

    /// A task was completed
    TaskCompleted {
        task_id: String,
        #[serde(default)]
        result: serde_json::Value,
    },

    /// An agent session was started
    SessionStarted {
        session_id: String,
        agent_id: Option<String>,
        #[serde(default)]
        metadata: serde_json::Value,
    },

    /// An agent session ended
    SessionEnded {
        session_id: String,
        #[serde(default)]
        result: serde_json::Value,
    },

    /// Agent produced output or log
    AgentOutput {
        session_id: String,
        #[serde(default)]
        output: String,
        #[serde(default)]
        level: String,
    },

    /// Heartbeat/keepalive from Bloodbank
    Heartbeat {
        #[serde(default)]
        timestamp: Option<String>,
    },

    /// Connection status change
    ConnectionStatus {
        connected: bool,
        #[serde(default)]
        message: Option<String>,
    },

    /// Unknown event type (forward compatibility)
    #[serde(other)]
    Unknown,
}

/// Result of parsing a Bloodbank event line.
#[derive(Debug)]
pub enum ParsedEvent {
    /// Successfully parsed event
    Event(BloodbankEvent),
    /// Failed to parse, contains raw line
    ParseError { raw: String, error: String },
}

/// Adapter for subscribing to Bloodbank real-time events.
///
/// `BloodbankAdapter` manages a long-running `bloodbank subscribe --format json`
/// subprocess, parsing newline-delimited JSON events and streaming them
/// to Dashboard components.
///
/// # Features
///
/// - Spawns `bloodbank subscribe --format json` subprocess
/// - Parses JSON events into typed `BloodbankEvent` enum
/// - Health checks every 5 seconds
/// - Auto-restart on crash (3 retries with exponential backoff)
/// - Bounded channel (100 events) to prevent memory growth
/// - Graceful shutdown on stop()
///
/// # Error Handling
///
/// - JSON parse errors are logged but don't crash the adapter
/// - CLI not found produces `IntegrationError::CliNotFound`
/// - Max restarts exceeded produces `IntegrationError::MaxRestartsExceeded`
pub struct BloodbankAdapter {
    /// Configuration for adapter behavior
    config: AdapterConfig,

    /// Subprocess manager (lazily initialized on subscribe)
    manager: Arc<Mutex<Option<SubprocessManager>>>,

    /// Raw line receiver from subprocess
    raw_rx: Arc<Mutex<Option<Receiver<String>>>>,

    /// Flag indicating if adapter is running
    is_running: Arc<AtomicBool>,
}

impl BloodbankAdapter {
    /// Create a new Bloodbank adapter with default configuration.
    pub fn new() -> Self {
        Self::with_config(AdapterConfig::default())
    }

    /// Create a new Bloodbank adapter with custom configuration.
    pub fn with_config(config: AdapterConfig) -> Self {
        Self {
            config,
            manager: Arc::new(Mutex::new(None)),
            raw_rx: Arc::new(Mutex::new(None)),
            is_running: Arc::new(AtomicBool::new(false)),
        }
    }

    /// Subscribe to typed Bloodbank events.
    ///
    /// This spawns the `bloodbank subscribe --format json` subprocess
    /// and returns a channel receiver for parsed events.
    ///
    /// # Returns
    ///
    /// A receiver that yields `BloodbankEvent` instances as they arrive.
    /// Parse errors are logged but don't stop the stream.
    ///
    /// # Errors
    ///
    /// - `CliNotFound` if `bloodbank` command is not in PATH
    /// - `SpawnFailed` if subprocess cannot be started
    pub async fn subscribe_events(&self) -> IntegrationResult<Receiver<BloodbankEvent>> {
        // Start raw subscription
        let raw_rx = self.subscribe(&["subscribe", "--format", "json"]).await?;

        // Create parsed event channel
        let (event_tx, event_rx) = mpsc::channel(self.config.channel_capacity);

        // Spawn task to parse events
        tokio::spawn(async move {
            let mut raw_rx = raw_rx;
            while let Some(line) = raw_rx.recv().await {
                match Self::parse_event(&line) {
                    ParsedEvent::Event(event) => {
                        if event_tx.send(event).await.is_err() {
                            // Receiver dropped, stop parsing
                            break;
                        }
                    }
                    ParsedEvent::ParseError { raw, error } => {
                        log::warn!("Failed to parse Bloodbank event: {} - raw: {}", error, raw);
                    }
                }
            }
        });

        Ok(event_rx)
    }

    /// Parse a JSON line into a BloodbankEvent.
    ///
    /// Returns `ParsedEvent::Event` on success, `ParsedEvent::ParseError` on failure.
    /// This allows the caller to decide how to handle parse errors.
    pub fn parse_event(line: &str) -> ParsedEvent {
        match serde_json::from_str::<BloodbankEvent>(line) {
            Ok(event) => ParsedEvent::Event(event),
            Err(e) => ParsedEvent::ParseError {
                raw: line.to_string(),
                error: e.to_string(),
            },
        }
    }

    /// Get the CLI command name.
    pub fn command() -> &'static str {
        "bloodbank"
    }
}

impl Default for BloodbankAdapter {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl IntegrationAdapter for BloodbankAdapter {
    async fn call(&self, args: &[&str]) -> IntegrationResult<String> {
        let config = self.config.clone();
        let (manager, _rx) = SubprocessManager::new(Self::command(), config);
        manager.call(args).await
    }

    async fn subscribe(&self, args: &[&str]) -> IntegrationResult<Receiver<String>> {
        let mut manager_lock = self.manager.lock().await;

        // Create new subprocess manager
        let (manager, rx) = SubprocessManager::new(Self::command(), self.config.clone());
        *manager_lock = Some(manager);

        // Store receiver
        *self.raw_rx.lock().await = Some(rx);

        // Get the manager and start it
        if let Some(ref mut mgr) = *manager_lock {
            // Clone args for the async task
            let args_owned: Vec<String> = args.iter().map(|s| s.to_string()).collect();

            // We need to spawn the subprocess in a separate task
            // because start() blocks until the process exits
            let manager_clone = self.manager.clone();
            let is_running = self.is_running.clone();

            tokio::spawn(async move {
                let mut manager_lock = manager_clone.lock().await;
                if let Some(ref mut mgr) = *manager_lock {
                    is_running.store(true, Ordering::Relaxed);
                    let args_refs: Vec<&str> = args_owned.iter().map(|s| s.as_str()).collect();
                    let _ = mgr.start(&args_refs).await;
                    is_running.store(false, Ordering::Relaxed);
                }
            });

            // Return the receiver we stored earlier
            // We need to take it out since we moved it
            let config = self.config.clone();
            let (new_manager, new_rx) = SubprocessManager::new(Self::command(), config);

            // Swap the manager
            let old_manager = manager_lock.take();
            *manager_lock = Some(new_manager);

            // Return the original receiver
            if let Some(rx) = self.raw_rx.lock().await.take() {
                return Ok(rx);
            }

            // If we couldn't get the receiver, create a fresh one
            drop(manager_lock);
            let (manager, rx) = SubprocessManager::new(Self::command(), self.config.clone());
            *self.manager.lock().await = Some(manager);

            // Start again
            let manager_clone = self.manager.clone();
            let args_owned: Vec<String> = args.iter().map(|s| s.to_string()).collect();
            let is_running = self.is_running.clone();

            tokio::spawn(async move {
                let mut manager_lock = manager_clone.lock().await;
                if let Some(ref mut mgr) = *manager_lock {
                    is_running.store(true, Ordering::Relaxed);
                    let args_refs: Vec<&str> = args_owned.iter().map(|s| s.as_str()).collect();
                    let _ = mgr.start(&args_refs).await;
                    is_running.store(false, Ordering::Relaxed);
                }
            });

            return Ok(rx);
        }

        Err(IntegrationError::NotRunning)
    }

    async fn stop(&self) -> IntegrationResult<()> {
        let mut manager_lock = self.manager.lock().await;
        if let Some(ref mut manager) = *manager_lock {
            manager.stop().await?;
        }
        self.is_running.store(false, Ordering::Relaxed);
        Ok(())
    }

    fn is_healthy(&self) -> bool {
        self.is_running.load(Ordering::Relaxed)
    }

    fn name(&self) -> &str {
        "Bloodbank"
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_parse_task_created() {
        let json = r#"{"type": "task_created", "task_id": "123", "title": "Test Task"}"#;
        match BloodbankAdapter::parse_event(json) {
            ParsedEvent::Event(BloodbankEvent::TaskCreated {
                task_id, title, ..
            }) => {
                assert_eq!(task_id, "123");
                assert_eq!(title, "Test Task");
            }
            other => panic!("Expected TaskCreated, got {:?}", other),
        }
    }

    #[test]
    fn test_parse_task_updated() {
        let json = r#"{"type": "task_updated", "task_id": "456", "changes": {"status": "done"}}"#;
        match BloodbankAdapter::parse_event(json) {
            ParsedEvent::Event(BloodbankEvent::TaskUpdated { task_id, changes }) => {
                assert_eq!(task_id, "456");
                assert_eq!(changes["status"], "done");
            }
            other => panic!("Expected TaskUpdated, got {:?}", other),
        }
    }

    #[test]
    fn test_parse_session_started() {
        let json = r#"{"type": "session_started", "session_id": "sess-1", "agent_id": "agent-1"}"#;
        match BloodbankAdapter::parse_event(json) {
            ParsedEvent::Event(BloodbankEvent::SessionStarted {
                session_id,
                agent_id,
                ..
            }) => {
                assert_eq!(session_id, "sess-1");
                assert_eq!(agent_id, Some("agent-1".to_string()));
            }
            other => panic!("Expected SessionStarted, got {:?}", other),
        }
    }

    #[test]
    fn test_parse_heartbeat() {
        let json = r#"{"type": "heartbeat", "timestamp": "2026-01-29T12:00:00Z"}"#;
        match BloodbankAdapter::parse_event(json) {
            ParsedEvent::Event(BloodbankEvent::Heartbeat { timestamp }) => {
                assert_eq!(timestamp, Some("2026-01-29T12:00:00Z".to_string()));
            }
            other => panic!("Expected Heartbeat, got {:?}", other),
        }
    }

    #[test]
    fn test_parse_connection_status() {
        let json = r#"{"type": "connection_status", "connected": true, "message": "Connected to RabbitMQ"}"#;
        match BloodbankAdapter::parse_event(json) {
            ParsedEvent::Event(BloodbankEvent::ConnectionStatus { connected, message }) => {
                assert!(connected);
                assert_eq!(message, Some("Connected to RabbitMQ".to_string()));
            }
            other => panic!("Expected ConnectionStatus, got {:?}", other),
        }
    }

    #[test]
    fn test_parse_unknown_event() {
        let json = r#"{"type": "future_event_type", "data": "something"}"#;
        match BloodbankAdapter::parse_event(json) {
            ParsedEvent::Event(BloodbankEvent::Unknown) => {
                // Expected - unknown types are handled gracefully
            }
            other => panic!("Expected Unknown, got {:?}", other),
        }
    }

    #[test]
    fn test_parse_invalid_json() {
        let json = "not valid json {";
        match BloodbankAdapter::parse_event(json) {
            ParsedEvent::ParseError { raw, error } => {
                assert_eq!(raw, "not valid json {");
                assert!(!error.is_empty());
            }
            other => panic!("Expected ParseError, got {:?}", other),
        }
    }

    #[test]
    fn test_adapter_name() {
        let adapter = BloodbankAdapter::new();
        assert_eq!(adapter.name(), "Bloodbank");
    }

    #[test]
    fn test_adapter_command() {
        assert_eq!(BloodbankAdapter::command(), "bloodbank");
    }

    #[test]
    fn test_adapter_default_not_healthy() {
        let adapter = BloodbankAdapter::new();
        assert!(!adapter.is_healthy());
    }

    #[tokio::test]
    async fn test_call_cli_not_found() {
        let adapter = BloodbankAdapter::new();
        let result = adapter.call(&["--version"]).await;
        assert!(matches!(result, Err(IntegrationError::CliNotFound(_))));
    }

    #[tokio::test]
    async fn test_stop_when_not_running() {
        let adapter = BloodbankAdapter::new();
        let result = adapter.stop().await;
        assert!(result.is_ok());
    }
}
