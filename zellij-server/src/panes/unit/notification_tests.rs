// Perth STORY-003: Unit tests for notification routing in TerminalPane
// Tests the set_notification and clear_notification methods

use crate::panes::sixel::SixelImageStore;
use crate::panes::terminal_pane::TerminalPane;
use crate::panes::LinkHandler;
use crate::tab::Pane;
use std::cell::RefCell;
use std::collections::HashMap;
use std::rc::Rc;
use zellij_utils::{
    data::{Palette, Style},
    notification::{Notification, NotificationStyle},
    pane_size::PaneGeom,
};

fn create_test_pane() -> TerminalPane {
    let pid = 1;
    let mut geom = PaneGeom::default();
    geom.cols.set_inner(80);
    geom.rows.set_inner(24);
    let style = Style::default();
    let sixel_image_store = Rc::new(RefCell::new(SixelImageStore::default()));
    let terminal_emulator_colors = Rc::new(RefCell::new(Palette::default()));
    let terminal_emulator_color_codes = Rc::new(RefCell::new(HashMap::new()));

    TerminalPane::new(
        pid,
        geom,
        style,
        0,
        String::from("test_pane"),
        Rc::new(RefCell::new(LinkHandler::new())),
        Default::default(),
        sixel_image_store,
        terminal_emulator_colors,
        terminal_emulator_color_codes,
        None,
        None,
        false,
        true,
        true,
        true,
        false,
        None,
    )
}

    #[test]
    fn test_set_notification_stores_notification() {
        let mut pane = create_test_pane();
        let notification = Notification::error("Test error");

        pane.set_notification(notification.clone());

        // Verify notification was stored
        assert!(pane.notification.is_some());
        let stored = pane.notification.as_ref().unwrap();
        assert_eq!(stored.message, "Test error");
        assert_eq!(stored.style, NotificationStyle::Error);
    }

    #[test]
    fn test_set_notification_applies_frame_color_override() {
        let mut pane = create_test_pane();
        let notification = Notification::success("Build succeeded");

        pane.set_notification(notification);

        // Verify frame color override was applied
        assert!(pane.pane_frame_color_override.is_some());
        let (_, text) = pane.pane_frame_color_override.as_ref().unwrap();
        assert!(text.is_some());
        let text_str = text.as_ref().unwrap();
        assert!(text_str.contains("✓")); // Success icon
        assert!(text_str.contains("Build succeeded"));
    }

    #[test]
    fn test_set_notification_error_style() {
        let mut pane = create_test_pane();
        let notification = Notification::error("Connection failed");

        pane.set_notification(notification);

        let (_, text) = pane.pane_frame_color_override.as_ref().unwrap();
        let text_str = text.as_ref().unwrap();
        assert!(text_str.contains("✗")); // Error icon
        assert!(text_str.contains("Connection failed"));
    }

    #[test]
    fn test_set_notification_warning_style() {
        let mut pane = create_test_pane();
        let notification = Notification::warning("Disk space low");

        pane.set_notification(notification);

        let (_, text) = pane.pane_frame_color_override.as_ref().unwrap();
        let text_str = text.as_ref().unwrap();
        assert!(text_str.contains("⚠")); // Warning icon
        assert!(text_str.contains("Disk space low"));
    }

    #[test]
    fn test_clear_notification_removes_notification() {
        let mut pane = create_test_pane();
        let notification = Notification::error("Test");

        // Set notification
        pane.set_notification(notification);
        assert!(pane.notification.is_some());

        // Clear notification
        pane.clear_notification();
        assert!(pane.notification.is_none());
    }

    #[test]
    fn test_clear_notification_removes_frame_override() {
        let mut pane = create_test_pane();
        let notification = Notification::success("Test");

        // Set notification
        pane.set_notification(notification);
        assert!(pane.pane_frame_color_override.is_some());

        // Clear notification
        pane.clear_notification();
        assert!(pane.pane_frame_color_override.is_none());
    }

    #[test]
    fn test_clear_notification_when_no_notification() {
        let mut pane = create_test_pane();

        // Clearing when no notification should not panic
        pane.clear_notification();
        assert!(pane.notification.is_none());
    }

    #[test]
    fn test_multiple_notifications_override() {
        let mut pane = create_test_pane();

        // Set first notification
        pane.set_notification(Notification::error("First error"));
        assert_eq!(pane.notification.as_ref().unwrap().message, "First error");

        // Set second notification (should override)
        pane.set_notification(Notification::success("Second success"));
        let stored = pane.notification.as_ref().unwrap();
        assert_eq!(stored.message, "Second success");
        assert_eq!(stored.style, NotificationStyle::Success);
    }

    #[test]
    fn test_notification_triggers_render() {
        let mut pane = create_test_pane();

        // Reset render flag
        pane.set_should_render(false);
        assert!(!pane.should_render());

        // Setting notification should trigger render
        pane.set_notification(Notification::error("Render test"));
        assert!(pane.should_render());
    }

    #[test]
    fn test_clear_notification_triggers_render_when_notification_exists() {
        let mut pane = create_test_pane();

        // Set notification first
        pane.set_notification(Notification::error("Test"));
        pane.set_should_render(false);
        assert!(!pane.should_render());

        // Clearing should trigger render
        pane.clear_notification();
        assert!(pane.should_render());
    }

    #[test]
    fn test_clear_notification_no_render_when_no_notification() {
        let mut pane = create_test_pane();

        // No notification set
        pane.set_should_render(false);
        assert!(!pane.should_render());

        // Clearing when no notification shouldn't trigger render
        pane.clear_notification();
        assert!(!pane.should_render());
    }
