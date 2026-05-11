# perth-reporter (zellij plugin)

Mirrors the active zellij session's tab and pane state to the [Perth bridge](../README.md) via HTTP POST. One plugin instance per session.

## Why a plugin

The zellij plugin sandbox is the only first-class way to read tab/pane state and live pane content. External processes can `zellij list-sessions` and `zellij action write-chars` but cannot inspect tab structure or read scrollback. The plugin runs *inside* zellij and uses the official host calls (`get_tab_info`, `get_pane_scrollback`, etc.) to access that state, then POSTs JSON snapshots to the bridge.

## Build

```bash
rustup target add wasm32-wasip1   # one-time
cd bridge/plugin
cargo build --release --target wasm32-wasip1
```

Artifact: `target/wasm32-wasip1/release/perth_reporter.wasm` (~1 MB).

## Install

Copy the wasm to your zellij plugins dir:

```bash
mkdir -p ~/.config/zellij/plugins
cp target/wasm32-wasip1/release/perth_reporter.wasm ~/.config/zellij/plugins/
```

Then declare it in your zellij config (`~/.config/zellij/config.kdl`):

```kdl
plugins {
    perth-reporter location="file:~/.config/zellij/plugins/perth_reporter.wasm" {
        // Optional: override the default bridge URL.
        bridge_url "http://127.0.0.1:7800/internal/state"
    }
}

// Auto-load the plugin in every new tab so reporting kicks in immediately.
load_plugins {
    "file:~/.config/zellij/plugins/perth_reporter.wasm"
}
```

On first launch zellij will prompt for the three permissions the plugin requests:

- `ReadApplicationState` — enumerate tabs and panes
- `ReadPaneContents` — read viewport text
- `WebAccess` — POST snapshots to the bridge

Approve them once; subsequent launches reuse the granted scope.

## Wire format

Each POST body is a single JSON snapshot:

```json
{
  "session_name": "Workspace",
  "tabs": [
    {"position": 0, "name": "perth", "is_active": true, "active_pane_id": 1, "panes": []},
    {"position": 1, "name": "33god", "is_active": false, "active_pane_id": 2, "panes": []}
  ],
  "pane_renders": {
    "1": "$ claude --resume\n> hello world\n"
  }
}
```

`pane_renders` keys are zellij terminal pane IDs (as strings). Values are the rendered viewport with ANSI escape codes preserved. The bridge keys its in-memory cache by `session_name` and replays the latest snapshot to phone clients on `session_attach`.

## Events the plugin listens to

| Event | What triggers it | What the plugin does |
|-------|-----------------|----------------------|
| `SessionUpdate` | Session metadata changes | Updates the cached session name |
| `TabUpdate` | Tab added/removed/renamed/focused | Replaces the tab snapshot |
| `PaneUpdate` | Pane added/removed/focused | Re-POSTs current state |
| `PaneRenderReportWithAnsi` | Pane scrollback changes | Replaces the pane's viewport text |
| `PaneClosed` | Pane closed | Drops the pane from the cache |

After any of these the plugin POSTs the full current snapshot. The bridge dedupes on its side.

## Smoke test

After installation, in a new zellij session:

```bash
curl http://127.0.0.1:7800/internal/state -i      # should 405 (POST-only)
# Open a new tab in zellij, then:
curl http://127.0.0.1:7800/healthz                # if/when implemented
```

For now the easiest verification is to attach a WebSocket client (see [bridge/README.md](../README.md)) and confirm the `session_attached` frame fires with non-empty `tabs`.
