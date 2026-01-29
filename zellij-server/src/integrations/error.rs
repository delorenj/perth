// Perth Integration Layer - Error Types
// STORY-005: Integration Adapter Framework
//
// Error types for external CLI tool integrations (Bloodbank, iMi, Jelmore).
// Designed for error isolation: adapter failures don't crash Zellij.

use std::fmt;
use std::io;

/// Errors that can occur during integration adapter operations
#[derive(Debug)]
pub enum IntegrationError {
    /// CLI executable not found in PATH
    CliNotFound(String),

    /// Process spawn failed
    SpawnFailed(String),

    /// Process exited with non-zero code
    ProcessExited {
        code: i32,
        stderr: String,
    },

    /// Maximum restart attempts exceeded
    MaxRestartsExceeded {
        attempts: u8,
        last_error: String,
    },

    /// JSON parsing error from CLI output
    ParseError(String),

    /// Timeout waiting for process response
    Timeout {
        operation: String,
        duration_secs: u64,
    },

    /// Channel closed unexpectedly
    ChannelClosed,

    /// I/O error during subprocess communication
    IoError(String),

    /// Subprocess not running when expected
    NotRunning,

    /// Shutdown requested
    ShutdownRequested,
}

impl fmt::Display for IntegrationError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::CliNotFound(cli) => {
                write!(f, "CLI not found: '{}'. Is it installed and in PATH?", cli)
            }
            Self::SpawnFailed(msg) => write!(f, "Failed to spawn subprocess: {}", msg),
            Self::ProcessExited { code, stderr } => {
                write!(f, "Process exited with code {}: {}", code, stderr)
            }
            Self::MaxRestartsExceeded {
                attempts,
                last_error,
            } => {
                write!(
                    f,
                    "Max restarts exceeded after {} attempts. Last error: {}",
                    attempts, last_error
                )
            }
            Self::ParseError(msg) => write!(f, "Failed to parse CLI output: {}", msg),
            Self::Timeout {
                operation,
                duration_secs,
            } => {
                write!(
                    f,
                    "Timeout after {}s waiting for: {}",
                    duration_secs, operation
                )
            }
            Self::ChannelClosed => write!(f, "Internal channel closed unexpectedly"),
            Self::IoError(msg) => write!(f, "I/O error: {}", msg),
            Self::NotRunning => write!(f, "Subprocess is not running"),
            Self::ShutdownRequested => write!(f, "Shutdown requested"),
        }
    }
}

impl std::error::Error for IntegrationError {}

impl From<io::Error> for IntegrationError {
    fn from(err: io::Error) -> Self {
        match err.kind() {
            io::ErrorKind::NotFound => Self::CliNotFound(err.to_string()),
            io::ErrorKind::TimedOut => Self::Timeout {
                operation: "I/O".to_string(),
                duration_secs: 0,
            },
            _ => Self::IoError(err.to_string()),
        }
    }
}

impl From<serde_json::Error> for IntegrationError {
    fn from(err: serde_json::Error) -> Self {
        Self::ParseError(err.to_string())
    }
}

impl<T> From<tokio::sync::mpsc::error::SendError<T>> for IntegrationError {
    fn from(_: tokio::sync::mpsc::error::SendError<T>) -> Self {
        Self::ChannelClosed
    }
}

impl From<tokio::sync::oneshot::error::RecvError> for IntegrationError {
    fn from(_: tokio::sync::oneshot::error::RecvError) -> Self {
        Self::ChannelClosed
    }
}

/// Result type for integration operations
pub type IntegrationResult<T> = Result<T, IntegrationError>;

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_error_display() {
        let err = IntegrationError::CliNotFound("bloodbank".to_string());
        assert!(err.to_string().contains("bloodbank"));
        assert!(err.to_string().contains("not found"));

        let err = IntegrationError::MaxRestartsExceeded {
            attempts: 3,
            last_error: "connection refused".to_string(),
        };
        assert!(err.to_string().contains("3 attempts"));
    }

    #[test]
    fn test_io_error_conversion() {
        let io_err = io::Error::new(io::ErrorKind::NotFound, "file not found");
        let int_err: IntegrationError = io_err.into();
        matches!(int_err, IntegrationError::CliNotFound(_));
    }
}
