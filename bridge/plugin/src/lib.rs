//! perth-reporter — a zellij plugin that mirrors the active session's tab
//! and pane state to the Perth bridge over HTTP.
//!
//! Architecture: one plugin instance per zellij session (we expect a single
//! `Workspace` session in practice). The plugin subscribes to the events that
//! capture tab/pane state churn and POSTs a JSON snapshot to the bridge each
//! time the relevant state changes. The bridge holds the snapshot in memory
//! and replays it to phone clients over the existing WebSocket protocol.
//!
//! The plugin cannot open WebSocket connections from the zellij plugin
//! sandbox (the API does not expose them), so we use plain HTTP POST. The
//! bridge accepts these on a separate internal route and never exposes that
//! route to the phone client.

use std::collections::BTreeMap;

use serde::Serialize;
use zellij_tile::prelude::*;

/// Default bridge ingress URL. The plugin manifest may override this via
/// the `bridge_url` configuration key; see README for setup.
const DEFAULT_BRIDGE_URL: &str = "http://127.0.0.1:7800/internal/state";

/// Configuration key the plugin manifest can use to override the bridge URL.
const CFG_BRIDGE_URL: &str = "bridge_url";

#[derive(Default)]
struct State {
    bridge_url: String,
    /// Most recent tab snapshot. Kept so we can pair it with pane render
    /// reports when emitting to the bridge.
    tabs: Vec<TabInfo>,
    /// Most recent rendered pane contents, keyed by pane id.
    pane_renders: BTreeMap<u32, String>,
    /// Session name reported by the host on the first state update.
    session_name: Option<String>,
}

register_plugin!(State);

impl ZellijPlugin for State {
    fn load(&mut self, configuration: BTreeMap<String, String>) {
        self.bridge_url = configuration
            .get(CFG_BRIDGE_URL)
            .cloned()
            .unwrap_or_else(|| DEFAULT_BRIDGE_URL.to_string());

        // Request the permissions we need. The user will be prompted on first
        // launch; subsequent launches reuse the granted scope.
        request_permission(&[
            PermissionType::ReadApplicationState,
            PermissionType::ReadPaneContents,
            PermissionType::WebAccess,
        ]);

        // Subscribe to the events that move the cache. SessionUpdate gives us
        // the session name; TabUpdate gives the tab list; PaneRenderReport
        // pushes new pane contents (with ANSI preserved so the phone client
        // can decide what to render).
        subscribe(&[
            EventType::SessionUpdate,
            EventType::TabUpdate,
            EventType::PaneUpdate,
            EventType::PaneRenderReportWithAnsi,
            EventType::PaneClosed,
        ]);
    }

    fn update(&mut self, event: Event) -> bool {
        // Returning false keeps the plugin invisible — it has no UI of its own.
        match event {
            Event::SessionUpdate(sessions, _resurrectable) => {
                if let Some(current) = sessions.iter().find(|s| s.is_current_session) {
                    self.session_name = Some(current.name.clone());
                }
                self.post_state();
            }
            Event::TabUpdate(tabs) => {
                self.tabs = tabs;
                self.post_state();
            }
            Event::PaneUpdate(_manifest) => {
                // PaneUpdate carries the full pane manifest. We don't store it
                // separately because TabInfo already enumerates panes per tab;
                // the manifest is mostly useful as a "something changed,
                // re-report" signal.
                self.post_state();
            }
            Event::PaneRenderReportWithAnsi(report) => {
                for (pane_id, contents) in report {
                    // PaneId may be either Terminal(u32) or Plugin(u32); only
                    // terminal panes are interesting for mirroring.
                    if let PaneId::Terminal(id) = pane_id {
                        // The viewport is what's visible on screen right now.
                        // Scrollback above/below is available but we send only
                        // the viewport for the mobile use case to keep payloads
                        // small. ANSI escape codes are preserved.
                        let rendered = contents.viewport.join("\n");
                        self.pane_renders.insert(id, rendered);
                    }
                }
                self.post_state();
            }
            Event::PaneClosed(PaneId::Terminal(id)) => {
                self.pane_renders.remove(&id);
                self.post_state();
            }
            _ => {}
        }
        false
    }
}

impl State {
    fn post_state(&self) {
        let payload = StateSnapshot {
            session_name: self.session_name.as_deref().unwrap_or("Workspace"),
            tabs: self.tabs.iter().map(TabWire::from).collect(),
            pane_renders: self
                .pane_renders
                .iter()
                .map(|(id, text)| (id.to_string(), text.clone()))
                .collect(),
        };
        let body = match serde_json::to_vec(&payload) {
            Ok(b) => b,
            Err(_) => return,
        };
        let mut headers = BTreeMap::new();
        headers.insert("Content-Type".to_string(), "application/json".to_string());
        web_request(
            &self.bridge_url,
            HttpVerb::Post,
            headers,
            body,
            BTreeMap::new(),
        );
    }
}

/// Wire shape of a single state snapshot sent to the bridge.
#[derive(Serialize)]
struct StateSnapshot<'a> {
    session_name: &'a str,
    tabs: Vec<TabWire>,
    pane_renders: BTreeMap<String, String>,
}

#[derive(Serialize)]
struct TabWire {
    position: usize,
    name: String,
    is_active: bool,
    active_pane_id: Option<u32>,
    panes: Vec<PaneWire>,
}

#[derive(Serialize)]
struct PaneWire {
    id: u32,
    title: String,
    is_active: bool,
}

impl From<&TabInfo> for TabWire {
    fn from(t: &TabInfo) -> Self {
        TabWire {
            position: t.position,
            name: t.name.clone(),
            is_active: t.active,
            active_pane_id: None,
            // TabInfo does not embed pane data; the bridge merges TabUpdate with
            // PaneUpdate/PaneRenderReport using session/tab indices on its side.
            // We leave panes empty here intentionally.
            panes: Vec::new(),
        }
    }
}
