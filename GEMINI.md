# Zellij

## Project Overview

Zellij is a terminal workspace (multiplexer) aimed at developers and ops-oriented people. It is designed to be simple to use out of the box while offering advanced features like deep customizability, layouts, and a plugin system.

The project is written in **Rust** and consists of several key components:

*   **`zellij-server`**: The core logic, handling the screen state, pty management (via `PtyBus`), and plugin orchestration.
*   **`zellij-client`**: The frontend that connects to the server, handles user input, and renders the state to the terminal.
*   **`zellij-utils`**: Shared utilities and error handling used across the workspace.
*   **`zellij-tile`**: The API crate for developing plugins in Rust (compiles to WebAssembly).
*   **`default-plugins/`**: A collection of built-in plugins (e.g., `status-bar`, `tab-bar`, `strider`) that provide the default UI.
*   **`xtask/`**: A custom build system and task runner implementation.

## Building and Running

The project uses `cargo xtask` as its primary task runner. You generally do **not** need to install extra dependencies besides standard Rust tools and `protoc` (Protocol Buffers compiler).

### Common Commands

*   **Build**:
    ```bash
    cargo xtask build
    ```
*   **Run (Debug)**:
    ```bash
    cargo xtask run
    # With arguments:
    cargo xtask run -- -l strider
    ```
*   **Format Code**:
    ```bash
    cargo xtask format
    ```
*   **Run Linter (Clippy)**:
    ```bash
    cargo xtask clippy
    ```
*   **Run Unit Tests**:
    ```bash
    cargo xtask test
    ```

### End-to-End (E2E) Tests

E2E tests run in a Docker container to ensure a consistent environment.

1.  Start the container:
    ```bash
    docker-compose up -d
    ```
2.  Build the linux binary (shared with container):
    ```bash
    cargo xtask ci e2e --build
    ```
3.  Run the tests:
    ```bash
    cargo xtask ci e2e --test
    ```

## Development Conventions

### Code Style & Quality

*   **Formatting**: Always run `cargo xtask format` before submitting changes.
*   **Linting**: Use `cargo xtask clippy`. Note that CI is configured to report only `clippy::correctness` as errors, but fixing warnings is encouraged.
*   **Error Handling**:
    *   Prefer returning `Result<T>` instead of using `.unwrap()` or `.expect()`.
    *   Use `zellij_utils::errors::prelude::*`.
    *   Attach context to errors using `.context("message")`.
    *   For non-fatal errors in void functions, use `.non_fatal()` logging.

### Architecture Notes

*   **Screen**: Manages the layout and relationship of panes (`zellij-server/src/screen.rs`).
*   **TerminalPane**: Represents a pane connected to a PTY (`zellij-server/src/panes/terminal_pane.rs`).
*   **Plugins**: Written in Rust (or other WASM-compilable languages) and interact via the `zellij-tile` API. They run in a sandboxed WASM environment.

### Toolchain

*   The project tracks the stable Rust toolchain (currently `1.92`).
*   `protoc` is required for building `zellij-client` and `zellij-server` communication assets.
