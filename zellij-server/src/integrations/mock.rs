// Perth Integration Layer - Mock Adapter
// STORY-005: Integration Adapter Framework
//
// Mock implementation of IntegrationAdapter for unit testing.
// Allows tests to control responses, simulate failures, and verify calls.

use async_trait::async_trait;
use std::sync::atomic::{AtomicBool, AtomicUsize, Ordering};
use std::sync::{Arc, Mutex};
use tokio::sync::mpsc::{self, Receiver};

use super::adapter::IntegrationAdapter;
use super::error::{IntegrationError, IntegrationResult};

/// Mock adapter for testing Dashboard components without real CLI tools.
///
/// `MockAdapter` allows tests to:
/// - Control what responses `call()` returns
/// - Simulate errors and failures
/// - Verify that methods were called with expected arguments
/// - Stream predefined data via `subscribe()`
///
/// # Example
///
/// ```ignore
/// use crate::integrations::MockAdapter;
///
/// #[tokio::test]
/// async fn test_dashboard_component() {
///     let mut mock = MockAdapter::new("test");
///
///     // Set up mock responses
///     mock.set_call_response(Ok(r#"{"projects": []}"#.to_string()));
///
///     // Use mock in component
///     let result = mock.call(&["list"]).await;
///     assert!(result.is_ok());
///
///     // Verify call was made
///     assert_eq!(mock.call_count(), 1);
/// }
/// ```
pub struct MockAdapter {
    name: String,

    /// Response to return from `call()`
    call_response: Arc<Mutex<IntegrationResult<String>>>,

    /// Lines to stream from `subscribe()`
    subscribe_lines: Arc<Mutex<Vec<String>>>,

    /// Whether to simulate being healthy
    is_healthy: Arc<AtomicBool>,

    /// Count of `call()` invocations
    call_count: Arc<AtomicUsize>,

    /// Count of `subscribe()` invocations
    subscribe_count: Arc<AtomicUsize>,

    /// Count of `stop()` invocations
    stop_count: Arc<AtomicUsize>,

    /// Last arguments passed to `call()`
    last_call_args: Arc<Mutex<Vec<String>>>,

    /// Last arguments passed to `subscribe()`
    last_subscribe_args: Arc<Mutex<Vec<String>>>,
}

impl MockAdapter {
    /// Create a new mock adapter.
    ///
    /// # Arguments
    ///
    /// * `name` - Name for this mock (used in logs and errors)
    pub fn new(name: &str) -> Self {
        Self {
            name: name.to_string(),
            call_response: Arc::new(Mutex::new(Ok(String::new()))),
            subscribe_lines: Arc::new(Mutex::new(Vec::new())),
            is_healthy: Arc::new(AtomicBool::new(true)),
            call_count: Arc::new(AtomicUsize::new(0)),
            subscribe_count: Arc::new(AtomicUsize::new(0)),
            stop_count: Arc::new(AtomicUsize::new(0)),
            last_call_args: Arc::new(Mutex::new(Vec::new())),
            last_subscribe_args: Arc::new(Mutex::new(Vec::new())),
        }
    }

    /// Set the response that `call()` will return.
    ///
    /// # Arguments
    ///
    /// * `response` - The result to return from `call()`
    pub fn set_call_response(&mut self, response: IntegrationResult<String>) {
        *self.call_response.lock().unwrap() = response;
    }

    /// Set predefined lines for `subscribe()` to stream.
    ///
    /// # Arguments
    ///
    /// * `lines` - Lines to send via the subscription channel
    pub fn set_subscribe_lines(&mut self, lines: Vec<String>) {
        *self.subscribe_lines.lock().unwrap() = lines;
    }

    /// Set whether `is_healthy()` returns true.
    pub fn set_healthy(&self, healthy: bool) {
        self.is_healthy.store(healthy, Ordering::Relaxed);
    }

    /// Get the number of times `call()` was invoked.
    pub fn call_count(&self) -> usize {
        self.call_count.load(Ordering::Relaxed)
    }

    /// Get the number of times `subscribe()` was invoked.
    pub fn subscribe_count(&self) -> usize {
        self.subscribe_count.load(Ordering::Relaxed)
    }

    /// Get the number of times `stop()` was invoked.
    pub fn stop_count(&self) -> usize {
        self.stop_count.load(Ordering::Relaxed)
    }

    /// Get the last arguments passed to `call()`.
    pub fn last_call_args(&self) -> Vec<String> {
        self.last_call_args.lock().unwrap().clone()
    }

    /// Get the last arguments passed to `subscribe()`.
    pub fn last_subscribe_args(&self) -> Vec<String> {
        self.last_subscribe_args.lock().unwrap().clone()
    }

    /// Reset all counters and recorded arguments.
    pub fn reset(&self) {
        self.call_count.store(0, Ordering::Relaxed);
        self.subscribe_count.store(0, Ordering::Relaxed);
        self.stop_count.store(0, Ordering::Relaxed);
        self.last_call_args.lock().unwrap().clear();
        self.last_subscribe_args.lock().unwrap().clear();
    }
}

#[async_trait]
impl IntegrationAdapter for MockAdapter {
    async fn call(&self, args: &[&str]) -> IntegrationResult<String> {
        // Record the call
        self.call_count.fetch_add(1, Ordering::Relaxed);
        *self.last_call_args.lock().unwrap() = args.iter().map(|s| s.to_string()).collect();

        // Return configured response
        let response = self.call_response.lock().unwrap();
        match &*response {
            Ok(s) => Ok(s.clone()),
            Err(e) => Err(match e {
                IntegrationError::CliNotFound(s) => IntegrationError::CliNotFound(s.clone()),
                IntegrationError::SpawnFailed(s) => IntegrationError::SpawnFailed(s.clone()),
                IntegrationError::ProcessExited { code, stderr } => {
                    IntegrationError::ProcessExited {
                        code: *code,
                        stderr: stderr.clone(),
                    }
                }
                IntegrationError::ParseError(s) => IntegrationError::ParseError(s.clone()),
                IntegrationError::Timeout {
                    operation,
                    duration_secs,
                } => IntegrationError::Timeout {
                    operation: operation.clone(),
                    duration_secs: *duration_secs,
                },
                IntegrationError::ChannelClosed => IntegrationError::ChannelClosed,
                IntegrationError::IoError(s) => IntegrationError::IoError(s.clone()),
                IntegrationError::NotRunning => IntegrationError::NotRunning,
                IntegrationError::ShutdownRequested => IntegrationError::ShutdownRequested,
                IntegrationError::MaxRestartsExceeded {
                    attempts,
                    last_error,
                } => IntegrationError::MaxRestartsExceeded {
                    attempts: *attempts,
                    last_error: last_error.clone(),
                },
            }),
        }
    }

    async fn subscribe(&self, args: &[&str]) -> IntegrationResult<Receiver<String>> {
        // Record the call
        self.subscribe_count.fetch_add(1, Ordering::Relaxed);
        *self.last_subscribe_args.lock().unwrap() = args.iter().map(|s| s.to_string()).collect();

        // Create channel and send predefined lines
        let (tx, rx) = mpsc::channel(100);

        let lines = self.subscribe_lines.lock().unwrap().clone();
        tokio::spawn(async move {
            for line in lines {
                if tx.send(line).await.is_err() {
                    break;
                }
            }
        });

        Ok(rx)
    }

    async fn stop(&self) -> IntegrationResult<()> {
        self.stop_count.fetch_add(1, Ordering::Relaxed);
        self.is_healthy.store(false, Ordering::Relaxed);
        Ok(())
    }

    fn is_healthy(&self) -> bool {
        self.is_healthy.load(Ordering::Relaxed)
    }

    fn name(&self) -> &str {
        &self.name
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[tokio::test]
    async fn test_mock_call() {
        let mut mock = MockAdapter::new("test");
        mock.set_call_response(Ok(r#"{"status": "ok"}"#.to_string()));

        let result = mock.call(&["--format", "json"]).await;

        assert!(result.is_ok());
        assert_eq!(result.unwrap(), r#"{"status": "ok"}"#);
        assert_eq!(mock.call_count(), 1);
        assert_eq!(mock.last_call_args(), vec!["--format", "json"]);
    }

    #[tokio::test]
    async fn test_mock_call_error() {
        let mut mock = MockAdapter::new("test");
        mock.set_call_response(Err(IntegrationError::CliNotFound("test".to_string())));

        let result = mock.call(&[]).await;

        assert!(matches!(result, Err(IntegrationError::CliNotFound(_))));
    }

    #[tokio::test]
    async fn test_mock_subscribe() {
        let mut mock = MockAdapter::new("test");
        mock.set_subscribe_lines(vec![
            r#"{"event": "start"}"#.to_string(),
            r#"{"event": "data"}"#.to_string(),
            r#"{"event": "end"}"#.to_string(),
        ]);

        let mut rx = mock.subscribe(&["subscribe"]).await.unwrap();

        let mut lines = Vec::new();
        while let Some(line) = rx.recv().await {
            lines.push(line);
        }

        assert_eq!(lines.len(), 3);
        assert_eq!(mock.subscribe_count(), 1);
    }

    #[tokio::test]
    async fn test_mock_stop() {
        let mock = MockAdapter::new("test");
        assert!(mock.is_healthy());

        mock.stop().await.unwrap();

        assert!(!mock.is_healthy());
        assert_eq!(mock.stop_count(), 1);
    }

    #[tokio::test]
    async fn test_mock_reset() {
        let mut mock = MockAdapter::new("test");
        mock.set_call_response(Ok("ok".to_string()));

        mock.call(&["test"]).await.unwrap();
        assert_eq!(mock.call_count(), 1);

        mock.reset();
        assert_eq!(mock.call_count(), 0);
        assert!(mock.last_call_args().is_empty());
    }

    #[test]
    fn test_mock_name() {
        let mock = MockAdapter::new("MyAdapter");
        assert_eq!(mock.name(), "MyAdapter");
    }
}
