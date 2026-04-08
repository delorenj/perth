# Research Spike: Rust-based Zellij Bridge (Perth-Bridge)

## Role: System Architect

## Context
The "Perth" Android app needs a remote bridge to control Zellij sessions. 
This bridge (let's call it `perth-bridge`) will act as a WebSocket/HTTP server that proxies requests from the Android app to the local Zellij IPC (Unix Domain Sockets).

## Goals
1. **IPC Analysis**: Deeply understand how Zellij's Rust-based IPC works. How does `zellij-utils` handle command emission and session state?
2. **Protocol Design**: Design a lightweight JSON-over-WebSocket protocol for:
    - Listing active sessions.
    - Attaching to a session.
    - Streaming pane output (ANSI/UTF-8).
    - Sending input (keyboard events).
    - Executing actions (new tab, close pane, etc.).
3. **Bridge Architecture**: Define the Rust stack for the bridge. 
    - Web framework (Axum? Warp? Actix-web?)
    - Async runtime (Tokio is standard).
    - Zellij Integration (Can we use `zellij-utils` directly or do we wrap the CLI?).
4. **Security**: How to handle authentication (Token-based) and Tailscale-friendly networking.

## Output Requirements
- Technical Specification for `perth-bridge`.
- WebSocket Message Schema.
- Implementation Plan for the Rust team.
