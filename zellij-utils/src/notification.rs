// Perth Notification System
// STORY-003: Notification Bus
//
// Visual notification types for pane-level alerts (build failures, task completions, etc.)

use serde::{Deserialize, Serialize};
use std::fmt;

/// Visual notification style determining color and icon
#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum NotificationStyle {
    /// Error notification (red border, ✗ icon)
    Error,
    /// Success notification (green border, ✓ icon)
    Success,
    /// Warning notification (yellow border, ⚠ icon)
    Warning,
}

impl NotificationStyle {
    /// Get ANSI color code for this style
    pub fn color_code(&self) -> &'static str {
        match self {
            NotificationStyle::Error => "\x1b[31m",   // Red
            NotificationStyle::Success => "\x1b[32m", // Green
            NotificationStyle::Warning => "\x1b[33m", // Yellow
        }
    }

    /// Get icon character for this style
    pub fn icon(&self) -> &'static str {
        match self {
            NotificationStyle::Error => "✗",
            NotificationStyle::Success => "✓",
            NotificationStyle::Warning => "⚠",
        }
    }

    /// Parse from CLI string argument
    pub fn from_str(s: &str) -> Option<Self> {
        match s.to_lowercase().as_str() {
            "error" => Some(NotificationStyle::Error),
            "success" => Some(NotificationStyle::Success),
            "warning" => Some(NotificationStyle::Warning),
            _ => None,
        }
    }
}

impl fmt::Display for NotificationStyle {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            NotificationStyle::Error => write!(f, "error"),
            NotificationStyle::Success => write!(f, "success"),
            NotificationStyle::Warning => write!(f, "warning"),
        }
    }
}

/// Pane notification with style and message
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub struct Notification {
    pub style: NotificationStyle,
    pub message: String,
    /// Timestamp when notification was created (milliseconds since epoch)
    pub timestamp: u64,
}

impl Notification {
    pub fn new(style: NotificationStyle, message: String) -> Self {
        let timestamp = std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .unwrap_or_default()
            .as_millis() as u64;

        Self {
            style,
            message,
            timestamp,
        }
    }

    /// Create error notification
    pub fn error(message: impl Into<String>) -> Self {
        Self::new(NotificationStyle::Error, message.into())
    }

    /// Create success notification
    pub fn success(message: impl Into<String>) -> Self {
        Self::new(NotificationStyle::Success, message.into())
    }

    /// Create warning notification
    pub fn warning(message: impl Into<String>) -> Self {
        Self::new(NotificationStyle::Warning, message.into())
    }
}

impl Default for Notification {
    fn default() -> Self {
        Self::error("") // Default to empty error notification
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_notification_style_from_str() {
        assert_eq!(
            NotificationStyle::from_str("error"),
            Some(NotificationStyle::Error)
        );
        assert_eq!(
            NotificationStyle::from_str("ERROR"),
            Some(NotificationStyle::Error)
        );
        assert_eq!(
            NotificationStyle::from_str("success"),
            Some(NotificationStyle::Success)
        );
        assert_eq!(
            NotificationStyle::from_str("warning"),
            Some(NotificationStyle::Warning)
        );
        assert_eq!(NotificationStyle::from_str("invalid"), None);
    }

    #[test]
    fn test_notification_creation() {
        let notif = Notification::error("Build failed");
        assert_eq!(notif.style, NotificationStyle::Error);
        assert_eq!(notif.message, "Build failed");
        assert!(notif.timestamp > 0);
    }

    #[test]
    fn test_notification_style_display() {
        assert_eq!(NotificationStyle::Error.to_string(), "error");
        assert_eq!(NotificationStyle::Success.to_string(), "success");
        assert_eq!(NotificationStyle::Warning.to_string(), "warning");
    }
}
