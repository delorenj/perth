// Perth Integration Layer
// STORY-005: Integration Adapter Framework
//
// This module provides a clean abstraction for integrating external CLI tools
// (Bloodbank, iMi, Jelmore) into the Perth Dashboard. It handles:
//
// - Subprocess lifecycle management (spawn, monitor, restart)
// - Error isolation (adapter failures don't crash Zellij)
// - Bounded channels for backpressure management
// - Exponential backoff for restart attempts
// - Graceful shutdown (SIGTERM, wait, SIGKILL)
//
// # Architecture
//
// ```text
// Dashboard Component
//         │
//         ▼
// ┌───────────────────┐
// │ IntegrationAdapter│  ← Trait defining the interface
// └───────────────────┘
//         │
//         ▼
// ┌───────────────────┐
// │ SubprocessManager │  ← Handles process lifecycle
// └───────────────────┘
//         │
//         ▼
// ┌───────────────────┐
// │   CLI Process     │  ← bloodbank, imi, jelmore
// └───────────────────┘
// ```
//
// # Usage
//
// For one-shot commands (iMi, Jelmore):
// ```ignore
// let adapter = iMiAdapter::new();
// let output = adapter.call(&["list", "--json"]).await?;
// let projects: Vec<Project> = serde_json::from_str(&output)?;
// ```
//
// For streaming subscriptions (Bloodbank):
// ```ignore
// let adapter = BloodbankAdapter::new();
// let mut rx = adapter.subscribe(&["subscribe", "--format", "json"]).await?;
// while let Some(line) = rx.recv().await {
//     let event: Event = serde_json::from_str(&line)?;
//     // Process event...
// }
// ```
//
// For testing:
// ```ignore
// let mut mock = MockAdapter::new("test");
// mock.set_call_response(Ok(r#"{"projects": []}"#.to_string()));
// // Use mock in component tests...
// ```

mod adapter;
mod error;
mod mock;
mod subprocess;

// Re-export public API
pub use adapter::{AdapterConfig, IntegrationAdapter};
pub use error::{IntegrationError, IntegrationResult};
pub use mock::MockAdapter;
pub use subprocess::SubprocessManager;

// Future: Specific adapter implementations will be added in separate stories
// pub mod bloodbank;  // STORY-006
// pub mod imi;        // STORY-007
// pub mod jelmore;    // STORY-007

#[cfg(test)]
mod tests {
    use super::*;

    /// Integration test verifying the full adapter workflow
    #[tokio::test]
    async fn test_adapter_workflow() {
        // Create mock adapter
        let mut mock = MockAdapter::new("test-adapter");

        // Set up responses
        mock.set_call_response(Ok(r#"{"status": "ok", "data": [1, 2, 3]}"#.to_string()));
        mock.set_subscribe_lines(vec![
            r#"{"event": "connected"}"#.to_string(),
            r#"{"event": "data", "payload": "test"}"#.to_string(),
        ]);

        // Test one-shot call
        let result = mock.call(&["status"]).await;
        assert!(result.is_ok());
        assert!(result.unwrap().contains("status"));

        // Test subscription
        let mut rx = mock.subscribe(&["events"]).await.unwrap();
        let first = rx.recv().await;
        assert!(first.is_some());
        assert!(first.unwrap().contains("connected"));

        // Test stop
        mock.stop().await.unwrap();
        assert!(!mock.is_healthy());

        // Verify call counts
        assert_eq!(mock.call_count(), 1);
        assert_eq!(mock.subscribe_count(), 1);
        assert_eq!(mock.stop_count(), 1);
    }

    /// Test that SubprocessManager can execute simple commands
    #[tokio::test]
    async fn test_subprocess_echo() {
        let config = AdapterConfig::default();
        let (manager, _rx) = SubprocessManager::new("echo", config);

        let result = manager.call(&["Hello", "Integration", "Layer"]).await;
        assert!(result.is_ok());
        assert_eq!(result.unwrap().trim(), "Hello Integration Layer");
    }

    /// Test error handling for non-existent commands
    #[tokio::test]
    async fn test_subprocess_not_found() {
        let config = AdapterConfig::default();
        let (manager, _rx) =
            SubprocessManager::new("this_command_definitely_does_not_exist_xyz", config);

        let result = manager.call(&[]).await;
        assert!(matches!(result, Err(IntegrationError::CliNotFound(_))));
    }

    /// Test bounded channel capacity
    #[tokio::test]
    async fn test_channel_capacity() {
        let config = AdapterConfig {
            channel_capacity: 5, // Small capacity for testing
            ..Default::default()
        };

        let (manager, _rx) = SubprocessManager::new("echo", config);

        // Channel exists with configured capacity
        // (actual capacity testing would require mocking the subprocess)
        assert!(!manager.is_healthy()); // Not started yet
    }
}
