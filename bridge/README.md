# Perth Bridge

WebSocket gateway between the Perth Android app and a host running Zellij.

## What it does

- Listens on `:7800/ws` for the [`PerthMessage`](../app/src/main/kotlin/sh/delo/perth/core/network/PerthProtocol.kt) wire protocol.
- Answers `session_list` by shelling out to `zellij list-sessions --short` and filtering to sessions whose Unix sockets exist on disk (i.e. actually-running sessions).
- Forwards `pane_input` and `pane_command` to `zellij action write-chars` against the attached session.
- Streams `pane_output` from the attached session's Unix socket. **(M2 — currently raw bytes, not yet MessagePack-decoded.)**

For zellij ≥ 0.44 the session sockets live at `/run/user/{uid}/zellij/contract_version_1/{session}`; older builds at `/tmp/zellij-{uid}/{session}`. Resolution is automatic via `XDG_RUNTIME_DIR`.

## Run locally

```bash
cd bridge
cargo run --release -- --host 0.0.0.0 --port 7800
```

The bridge inherits the user environment, so:

- It must run as the same user that owns the zellij sessions (`uid` matters).
- `zellij` must be on `PATH`.
- `XDG_RUNTIME_DIR` must point at a directory containing the session sockets.

## Smoke-test the wire

```bash
python3 -c '
import asyncio, json, websockets
async def main():
    async with websockets.connect("ws://127.0.0.1:7800/ws") as ws:
        await ws.send(json.dumps({"type": "session_list"}))
        print(await ws.recv())
asyncio.run(main())
'
```

Expected response shape:

```json
{
  "type": "session_list_update",
  "sessions": [
    { "name": "Workspace", "is_current": false }
  ]
}
```

## Containerized deployment

Build:

```bash
docker build -t perth-bridge:latest bridge/
```

The container needs three things from the host:

1. The `zellij` binary on `PATH` (the image installs it via the official release tarball).
2. Read access to the user's session sockets (mount `/run/user/$(id -u)/zellij`).
3. Match the host UID via `user:` in compose so socket permissions line up.

A starter compose file lives at `~/docker/stacks/perth-bridge/compose.yml`. It exposes the bridge through Traefik at `perth-bridge.delo.sh`.

## Status by milestone

| Milestone | Status |
|-----------|--------|
| M1: real session names returned over WebSocket | ✅ Done |
| M2: tab structure, attach to session, read-only output stream | ⚠ Partial — `SessionAttach` opens the socket but raw bytes are not parsed as zellij MessagePack frames yet |
| M3: bidirectional input/output with the active pane | Not started |

## Wire protocol open questions

The Kotlin client encodes `pane_id` as a string. The bridge currently hardcodes `"0"` because zellij's per-pane addressing isn't surfaced through the CLI. M2 needs a research spike on either zellij's plugin API or parsing the cached session state to derive real pane IDs.
