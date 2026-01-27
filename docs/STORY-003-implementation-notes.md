# STORY-003: Notification Bus - Implementation Notes

**Status:** In Progress (Foundation Complete, Core Logic Remaining)
**Story Points:** 5
**Branch:** `feature/STORY-INF-001-database-schema` (continue here or create new branch)

## Completed Work

### 1. Notification Type System (`zellij-utils/src/notification.rs`)

**Location:** `/home/delorenj/code/33GOD/perth/zellij-utils/src/notification.rs`

**Implemented:**
- `NotificationStyle` enum (Error, Success, Warning)
- Color codes and icons for each style
- `Notification` struct with style, message, timestamp
- Helper constructors: `Notification::error()`, `::success()`, `::warning()`
- CLI string parsing: `NotificationStyle::from_str()`
- Full unit test coverage

**Usage Example:**
```rust
use zellij_utils::notification::{Notification, NotificationStyle};

let notif = Notification::error("Build failed in workspace");
// Or from CLI: NotificationStyle::from_str("error")
```

### 2. TerminalPane Extension

**Modified:** `/home/delorenj/code/33GOD/perth/zellij-server/src/panes/terminal_pane.rs`

**Changes:**
- Added import: `use zellij_utils::notification::Notification;`
- Added field to `TerminalPane` struct (line ~147): `pub notification: Option<Notification>`
- Initialized in constructor (line ~997): `notification: None`

**Status:** PluginPane not yet extended (may need same treatment if plugin panes support notifications)

---

## Remaining Implementation Tasks

### Task 3: NotificationBus (Server-Side Routing)

**Create:** `/home/delorenj/code/33GOD/perth/zellij-server/src/notifications/mod.rs`
**Create:** `/home/delorenj/code/33GOD/perth/zellij-server/src/notifications/bus.rs`

**Architecture:**
```rust
// notifications/bus.rs
pub struct NotificationBus {
    // Maps pane_id -> Notification
    pending_notifications: HashMap<PaneId, Notification>,
}

impl NotificationBus {
    pub fn new() -> Self { ... }

    // Route notification to specific pane
    pub fn notify_pane(&mut self, pane_id: PaneId, notification: Notification) {
        self.pending_notifications.insert(pane_id, notification);
    }

    // Get notification for pane (consumed on read)
    pub fn get_notification(&mut self, pane_id: PaneId) -> Option<Notification> {
        self.pending_notifications.remove(&pane_id)
    }

    // Clear notification when pane is focused
    pub fn clear_notification(&mut self, pane_id: PaneId) {
        self.pending_notifications.remove(&pane_id);
    }
}
```

**Integration Points:**
1. Add `NotificationBus` to server state (likely in `zellij-server/src/lib.rs` or screen state)
2. Create `ScreenInstruction::Notify` variant for routing notifications
3. Handle notification in screen thread's instruction processor

**Reference Pattern:**
Look at existing `ScreenInstruction` enum in `/home/delorenj/code/33GOD/perth/zellij-server/src/screen/mod.rs` for similar message passing patterns.

---

### Task 4: CLI Integration

**Modify:** `/home/delorenj/code/33GOD/perth/zellij-utils/src/cli.rs`

**Add Subcommand:**
```rust
#[derive(Subcommand)]
pub enum CliAction {
    // ... existing variants

    /// Send notification to a pane
    Notify {
        /// Pane ID to notify (e.g., "1" for Terminal(1))
        #[arg(long, short)]
        pane_id: u32,

        /// Notification style
        #[arg(long, short, value_enum)]
        style: NotificationStyleArg,

        /// Notification message
        #[arg(long, short)]
        message: String,
    },
}

#[derive(ValueEnum, Clone)]
pub enum NotificationStyleArg {
    Error,
    Success,
    Warning,
}
```

**CLI Handler:**
Modify `/home/delorenj/code/33GOD/perth/src/main.rs` or `/home/delorenj/code/33GOD/perth/zellij-client/src/main.rs` to handle `CliAction::Notify`:
```rust
CliAction::Notify { pane_id, style, message } => {
    let notification_style = match style {
        NotificationStyleArg::Error => NotificationStyle::Error,
        NotificationStyleArg::Success => NotificationStyle::Success,
        NotificationStyleArg::Warning => NotificationStyle::Warning,
    };
    let notification = Notification::new(notification_style, message);

    // Send to server via IPC
    send_action_to_server(Action::Notify {
        pane_id: PaneId::Terminal(pane_id),
        notification,
    });
}
```

**Action Enum Extension:**
Add to `/home/delorenj/code/33GOD/perth/zellij-utils/src/input/actions.rs`:
```rust
pub enum Action {
    // ... existing variants
    Notify {
        pane_id: PaneId,
        notification: Notification,
    },
}
```

---

### Task 6: Client-Side Rendering

**Modify:** `/home/delorenj/code/33GOD/perth/zellij-client/src/renderer.rs` or pane frame rendering

**Approach 1: Border Color Override**
Extend `PaneFrame` rendering to check for `notification` field:
```rust
// In pane frame rendering logic
if let Some(notification) = pane.notification {
    // Override border color based on notification.style
    let color = match notification.style {
        NotificationStyle::Error => PaletteColor::Red,
        NotificationStyle::Success => PaletteColor::Green,
        NotificationStyle::Warning => PaletteColor::Orange,
    };
    frame_params.color_override = Some(color);
}
```

**Approach 2: Icon in Frame**
Add icon to pane title/frame when notification present:
```rust
let title = if let Some(notification) = &pane.notification {
    format!("{} {}", notification.style.icon(), pane.title())
} else {
    pane.title()
};
```

**Files to Modify:**
- `/home/delorenj/code/33GOD/perth/zellij-server/src/ui/pane_boundaries_frame.rs` - Frame rendering
- `/home/delorenj/code/33GOD/perth/zellij-server/src/panes/terminal_pane.rs` - Title generation

**Reference:**
Examine existing `pane_frame_color_override` field in TerminalPane (line 141) for similar pattern.

---

### Task 7: Clear Notification on Focus

**Modify:** Focus event handlers in screen/tab logic

**Pattern:**
```rust
// When pane receives focus
fn focus_pane(&mut self, pane_id: PaneId) {
    if let Some(pane) = self.panes.get_mut(&pane_id) {
        pane.notification = None; // Clear notification on focus
    }
    // ... existing focus logic
}
```

**Files to Check:**
- `/home/delorenj/code/33GOD/perth/zellij-server/src/screen/mod.rs` - Screen-level focus
- `/home/delorenj/code/33GOD/perth/zellij-server/src/tab/mod.rs` - Tab-level focus
- Look for existing `switch_active_pane_*` methods

---

### Task 8: Unit Tests

**Create:** `/home/delorenj/code/33GOD/perth/zellij-server/src/notifications/tests.rs`

**Test Coverage:**
```rust
#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_notification_routing() {
        let mut bus = NotificationBus::new();
        let pane_id = PaneId::Terminal(1);
        let notif = Notification::error("Test");

        bus.notify_pane(pane_id, notif.clone());
        assert_eq!(bus.get_notification(pane_id), Some(notif));
    }

    #[test]
    fn test_notification_clear() {
        let mut bus = NotificationBus::new();
        let pane_id = PaneId::Terminal(1);

        bus.notify_pane(pane_id, Notification::error("Test"));
        bus.clear_notification(pane_id);
        assert_eq!(bus.get_notification(pane_id), None);
    }

    #[test]
    fn test_multiple_panes() {
        let mut bus = NotificationBus::new();
        let pane1 = PaneId::Terminal(1);
        let pane2 = PaneId::Terminal(2);

        bus.notify_pane(pane1, Notification::error("Pane 1"));
        bus.notify_pane(pane2, Notification::success("Pane 2"));

        assert!(bus.get_notification(pane1).is_some());
        assert!(bus.get_notification(pane2).is_some());
    }
}
```

---

### Task 9: Integration Testing

**Manual Test Plan:**
```bash
# Terminal 1: Start Zellij
zellij

# Terminal 2: Send notification
zellij action notify --pane-id 1 --style error --message "Build failed"

# Expected: Pane 1 shows red border + ✗ icon
# Action: Switch focus to pane 1
# Expected: Notification clears (border returns to normal)
```

**Test Cases:**
1. Error notification (red border)
2. Success notification (green border)
3. Warning notification (yellow border)
4. Multiple panes with different notifications
5. Notification persists across renders until focus
6. Invalid pane ID handling (error message)

---

## Architecture Decisions

### Notification Persistence Strategy

**Current Decision:** In-memory only (NotificationBus HashMap)
- Notifications **do not** persist to PostgreSQL
- Cleared on pane focus or Zellij restart
- Rationale: Ephemeral alerts for active development session

**Future Enhancement:**
- Store in `panes` table `component_state` JSONB for restoration
- Would require integrating with PersistenceManager (STORY-001)

### Notification Scope

**Current Decision:** Pane-level only
- CLI targets specific `--pane-id`
- No tab-level or session-level notifications

**Future Enhancement:**
- Tab notifications (affect all panes in tab)
- Session-level notifications (global banner)

### Multiple Notifications

**Current Decision:** One notification per pane (last write wins)
- HashMap stores single `Notification` per `PaneId`
- New notification overwrites existing

**Future Enhancement:**
- Notification queue (Vec<Notification>) per pane
- Display multiple notifications stacked

---

## Acceptance Criteria Status

| Criterion | Status | Notes |
|-----------|--------|-------|
| CLI: `zellij notify --pane-id <ID> --style <style> --message <text>` | ⧗ Pending | Task 4 |
| Server: NotificationBus updates pane metadata | ⧗ Pending | Task 3 |
| Client: Renderer interprets metadata for visual effects | ⧗ Pending | Task 6 |
| Notification persists until pane focus | ⧗ Pending | Task 7 |
| 3 styles: error (red), success (green), warning (yellow) | ✓ Complete | Types defined |
| Unit tests for notification routing | ⧗ Pending | Task 8 |

---

## Dependencies & References

**Depends On:**
- None (standalone feature)

**Related Code:**
- Pane trait: `/home/delorenj/code/33GOD/perth/zellij-server/src/tab/mod.rs:282`
- ScreenInstruction enum: `/home/delorenj/code/33GOD/perth/zellij-server/src/screen/mod.rs`
- Action enum: `/home/delorenj/code/33GOD/perth/zellij-utils/src/input/actions.rs`
- PaneFrame rendering: `/home/delorenj/code/33GOD/perth/zellij-server/src/ui/pane_boundaries_frame.rs`

**Similar Patterns:**
- `NotificationEnd` in `/home/delorenj/code/33GOD/perth/zellij-server/src/route.rs:94` (different purpose: action completion)
- `pane_frame_color_override` in TerminalPane (existing color override mechanism)

---

## Estimation

**Remaining Effort:** M-L (3-5 complexity points)

**Breakdown:**
- NotificationBus implementation: S-M (core logic straightforward, integration with screen state moderate)
- CLI integration: S (straightforward clap + action dispatch)
- Client-side rendering: M (requires understanding frame rendering + multiple integration points)
- Focus handling: S (hook into existing focus logic)
- Testing: S-M (unit tests simple, integration testing requires manual verification)

**Risk Areas:**
1. **Client rendering integration** - Multiple potential hook points, need to find cleanest approach
2. **Screen state threading** - NotificationBus needs to live in correct thread with proper synchronization
3. **PluginPane support** - May need to extend PluginPane struct similarly to TerminalPane

---

## Next Session Checklist

Starting STORY-003 implementation:

1. [ ] Create feature branch: `git checkout -b feature/STORY-003-notification-bus`
2. [ ] Create notifications module structure:
   - `mkdir -p zellij-server/src/notifications`
   - `touch zellij-server/src/notifications/mod.rs`
   - `touch zellij-server/src/notifications/bus.rs`
3. [ ] Implement NotificationBus struct (Task 3)
4. [ ] Add to zellij-server lib.rs: `pub mod notifications;`
5. [ ] Extend Action enum with Notify variant (Task 4)
6. [ ] Add CLI subcommand (Task 4)
7. [ ] Integrate with screen instruction processor (Task 3)
8. [ ] Implement client-side rendering (Task 6)
9. [ ] Add focus clearing logic (Task 7)
10. [ ] Write unit tests (Task 8)
11. [ ] Manual integration testing (Task 9)
12. [ ] Commit and update sprint status

**Estimated Completion:** Single focused session (2-3 hours real-time)
