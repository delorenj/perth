// Persistence error types
// STORY-001: Persistence Manager

use std::fmt;

/// Errors that can occur during persistence operations
#[derive(Debug)]
pub enum PersistenceError {
    /// Database connection failed
    ConnectionFailed(String),
    /// Database query failed
    QueryFailed(String),
    /// Migration failed
    MigrationFailed(String),
    /// Serialization error (JSONB encoding)
    SerializationError(String),
    /// Session not found
    SessionNotFound(uuid::Uuid),
    /// Database pool exhausted
    PoolExhausted,
    /// Database unavailable (NFR-003: graceful degradation)
    DatabaseUnavailable(String),
}

impl fmt::Display for PersistenceError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::ConnectionFailed(msg) => write!(f, "Database connection failed: {}", msg),
            Self::QueryFailed(msg) => write!(f, "Database query failed: {}", msg),
            Self::MigrationFailed(msg) => write!(f, "Database migration failed: {}", msg),
            Self::SerializationError(msg) => write!(f, "Serialization error: {}", msg),
            Self::SessionNotFound(id) => write!(f, "Session not found: {}", id),
            Self::PoolExhausted => write!(f, "Database connection pool exhausted"),
            Self::DatabaseUnavailable(msg) => {
                write!(f, "Database unavailable (continuing without persistence): {}", msg)
            }
        }
    }
}

impl std::error::Error for PersistenceError {}

impl From<sqlx::Error> for PersistenceError {
    fn from(err: sqlx::Error) -> Self {
        match err {
            sqlx::Error::RowNotFound => Self::QueryFailed("Row not found".to_string()),
            sqlx::Error::PoolTimedOut => Self::PoolExhausted,
            sqlx::Error::PoolClosed => Self::DatabaseUnavailable("Pool closed".to_string()),
            _ => Self::QueryFailed(err.to_string()),
        }
    }
}

impl From<serde_json::Error> for PersistenceError {
    fn from(err: serde_json::Error) -> Self {
        Self::SerializationError(err.to_string())
    }
}

/// Result type for persistence operations
pub type PersistenceResult<T> = Result<T, PersistenceError>;
