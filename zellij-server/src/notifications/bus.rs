// Perth Notification Bus
// STORY-003: Server-side notification routing
//
// Routes notifications to specific panes. Notifications persist until the pane is focused.

use std::collections::HashMap;
use zellij_utils::data::PaneId;
use zellij_utils::notification::Notification;

/// Central notification router for pane-level alerts
pub struct NotificationBus {
    /// Maps pane_id -> Notification (last write wins)
    pending_notifications: HashMap<PaneId, Notification>,
}

impl NotificationBus {
    pub fn new() -> Self {
        Self {
            pending_notifications: HashMap::new(),
        }
    }

    /// Route notification to specific pane
    ///
    /// If pane already has a notification, it will be overwritten (last write wins).
    pub fn notify_pane(&mut self, pane_id: PaneId, notification: Notification) {
        self.pending_notifications.insert(pane_id, notification);
    }

    /// Get notification for pane (consumed on read)
    ///
    /// Returns None if no notification is pending for this pane.
    pub fn get_notification(&mut self, pane_id: &PaneId) -> Option<Notification> {
        self.pending_notifications.remove(pane_id)
    }

    /// Clear notification when pane is focused
    pub fn clear_notification(&mut self, pane_id: &PaneId) {
        self.pending_notifications.remove(pane_id);
    }

    /// Check if pane has pending notification (without consuming it)
    pub fn has_notification(&self, pane_id: &PaneId) -> bool {
        self.pending_notifications.contains_key(pane_id)
    }

    /// Get immutable reference to notification (without consuming it)
    pub fn peek_notification(&self, pane_id: &PaneId) -> Option<&Notification> {
        self.pending_notifications.get(pane_id)
    }
}

impl Default for NotificationBus {
    fn default() -> Self {
        Self::new()
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use zellij_utils::notification::NotificationStyle;

    #[test]
    fn test_notification_routing() {
        let mut bus = NotificationBus::new();
        let pane_id = PaneId::Terminal(1);
        let notif = Notification::error("Test");

        bus.notify_pane(pane_id, notif.clone());
        assert_eq!(bus.get_notification(&pane_id), Some(notif));
    }

    #[test]
    fn test_notification_clear() {
        let mut bus = NotificationBus::new();
        let pane_id = PaneId::Terminal(1);

        bus.notify_pane(pane_id, Notification::error("Test"));
        bus.clear_notification(&pane_id);
        assert_eq!(bus.get_notification(&pane_id), None);
    }

    #[test]
    fn test_multiple_panes() {
        let mut bus = NotificationBus::new();
        let pane1 = PaneId::Terminal(1);
        let pane2 = PaneId::Terminal(2);

        bus.notify_pane(pane1, Notification::error("Pane 1"));
        bus.notify_pane(pane2, Notification::success("Pane 2"));

        assert!(bus.get_notification(&pane1).is_some());
        assert!(bus.get_notification(&pane2).is_some());
    }

    #[test]
    fn test_last_write_wins() {
        let mut bus = NotificationBus::new();
        let pane_id = PaneId::Terminal(1);

        bus.notify_pane(pane_id, Notification::error("First"));
        bus.notify_pane(pane_id, Notification::success("Second"));

        let notif = bus.get_notification(&pane_id).unwrap();
        assert_eq!(notif.style, NotificationStyle::Success);
        assert_eq!(notif.message, "Second");
    }

    #[test]
    fn test_has_notification() {
        let mut bus = NotificationBus::new();
        let pane_id = PaneId::Terminal(1);

        assert!(!bus.has_notification(&pane_id));

        bus.notify_pane(pane_id, Notification::error("Test"));
        assert!(bus.has_notification(&pane_id));

        bus.clear_notification(&pane_id);
        assert!(!bus.has_notification(&pane_id));
    }

    #[test]
    fn test_peek_notification() {
        let mut bus = NotificationBus::new();
        let pane_id = PaneId::Terminal(1);
        let notif = Notification::warning("Test");

        bus.notify_pane(pane_id, notif.clone());

        // Peek doesn't consume
        assert_eq!(bus.peek_notification(&pane_id), Some(&notif));
        assert!(bus.has_notification(&pane_id));

        // Get consumes
        assert_eq!(bus.get_notification(&pane_id), Some(notif));
        assert!(!bus.has_notification(&pane_id));
    }
}
