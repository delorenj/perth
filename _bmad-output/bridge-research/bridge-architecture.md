# Perth-Bridge Architecture Specification

**Role:** System Architect
**Topic:** Remote Zellij Control Bridge
**Status:** Draft

## 1. Executive Summary
`perth-bridge` is a lightweight Rust-based service designed to expose Zellij's local Unix Domain Socket (UDS) IPC over a secure, Tailscale-friendly WebSocket/HTTP interface. This enables the Perth Android app to remotely list sessions, attach to panes, and send voice-to-text derived commands.

## 2. Technical Stack
- **Language:** Rust (Stable)
- **Runtime:** `tokio` (Multi-threaded)
- **Web Framework:** `axum` (Leveraging `tower` ecosystem for middleware)
- **Zellij Integration:** `zellij-utils` (for IPC message types) + `tokio::net::UnixStream`
- **Serialization:** `serde` + `serde_json`
- **Logging:** `tracing` + `tracing-subscriber`

## 3. Bridge Components

### 3.1 Zellij IPC Handler
Responsible for managing the connection to the local Zellij UDS.
- **Path Discovery:** Locates Zellij sockets (usually in `/tmp/zellij-<uid>/`).
- **Stream Management:** Handles asynchronous reading/writing of Zellij IPC messages.
- **State Sync:** Periodically polls or listens for session/tab/pane updates.

### 3.2 WebSocket Proxy
Translates Perth's JSON-based WebSocket protocol into Zellij IPC commands.
- **Multiplexing:** Handles multiple concurrent Android clients (if needed).
- **Framing:** Encapsulates terminal output into WebSocket messages.

### 3.3 Security & Auth
- **Tailscale Integration:** Binds to the Tailscale IP/interface by default.
- **Token Auth:** Uses a simple Pre-Shared Key (PSK) or Bearer Token in the `Authorization` header for WebSocket handshakes.

## 4. Protocol Specification (Draft)

### 4.1 Connection
**URL:** `ws://<server-ip>:7800/ws`
**Headers:** `Authorization: Bearer <token>`

### 4.2 Message Types (Client -> Server)
```json
// List active Zellij sessions
{ "type": "session.list" }

// Attach to a specific session
{ "type": "session.attach", "session_name": "my-dev-session" }

// Send raw input to a pane
{ "type": "pane.input", "pane_id": 123, "input": "ls -la\n" }

// Execute a high-level command (Command Mode)
{ "type": "pane.command", "pane_id": 123, "command": "cargo build" }
```

### 4.3 Message Types (Server -> Client)
```json
// Session List Update
{
  "type": "session.list",
  "sessions": [
    { "name": "perth-dev", "active": true, "tabs": [...] }
  ]
}

// Terminal Output Stream
{
  "type": "pane.output",
  "pane_id": 123,
  "data": "G2lsIC1sYQo..." // Base64 encoded ANSI/UTF-8
}
```

## 5. Implementation Roadmap

### Phase 1: Prototype (Solo Rust Dev)
- [ ] Initialize Rust project with `axum` and `tokio`.
- [ ] Implement manual UDS connection to a running Zellij session.
- [ ] Echo raw terminal output to a WebSocket.

### Phase 2: Session Management
- [ ] Implement `session.list` using `zellij --list-sessions` or IPC.
- [ ] Support switching between active sessions.

### Phase 3: Android Integration
- [ ] Update `WebSocketZellijTransport.kt` in Android app to match the new protocol.
- [ ] Test real-time terminal rendering with ANSI sequences.

## 6. Critical Considerations
- **Latency:** Must minimize overhead between UDS and WebSocket.
- **Terminal Parsing:** Android app needs a robust ANSI parser (e.g., `term.js` equivalent or native view).
- **Zellij Versioning:** IPC protocol may change; `zellij-utils` must be kept in sync.
