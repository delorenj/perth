//! Zellij IPC layer for the Perth Bridge.
//!
//! For session-listing we shell out to `zellij list-sessions --short` rather
//! than walking the socket directory. The socket layout has churned across
//! zellij versions (e.g. 0.44.0 moved sockets from `/tmp/zellij-{uid}/` to
//! `/run/user/{uid}/zellij/contract_version_1/{session}`); the CLI is the
//! one stable contract zellij itself maintains.
//!
//! Socket-path resolution is still exposed for downstream consumers that need
//! to attach to a session, but it now reads from the correct directory and
//! filters to entries that actually exist.
use std::path::PathBuf;
use tokio::io::{AsyncReadExt, AsyncWriteExt};
use tokio::net::UnixStream;
use tokio::process::Command;
use tracing::{debug, warn};
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Session {
    pub name: String,
    pub socket_path: PathBuf,
}

/// Returns currently-running zellij sessions on the host.
///
/// Uses `zellij list-sessions --short`, which prints one session name per line
/// and excludes ANSI formatting. Sessions in EXITED state are also returned by
/// `--short`; we filter to those whose socket file exists, matching what we
/// could actually attach to.
pub async fn list_sessions() -> Vec<Session> {
    let names = match run_list_sessions_short().await {
        Some(n) => n,
        None => return Vec::new(),
    };

    let socket_dir = session_socket_dir();
    names
        .into_iter()
        .map(|name| Session {
            socket_path: socket_dir.join(&name),
            name,
        })
        // Only keep sessions whose socket actually exists. EXITED sessions in
        // `zellij list-sessions --short` have their socket cleaned up, so this
        // filter narrows to live sessions the client can interact with.
        .filter(|s| s.socket_path.exists())
        .collect()
}

async fn run_list_sessions_short() -> Option<Vec<String>> {
    let output = match Command::new("zellij")
        .args(["list-sessions", "--short"])
        .output()
        .await
    {
        Ok(o) => o,
        Err(e) => {
            warn!("zellij list-sessions failed to spawn: {}", e);
            return None;
        }
    };

    // zellij returns non-zero ("No active zellij sessions found") when there
    // are zero sessions; treat that as success-with-empty-list rather than an
    // error. Other non-zero outcomes we log but continue with empty.
    let stderr = String::from_utf8_lossy(&output.stderr);
    if !output.status.success() && !stderr.contains("No active zellij sessions") {
        warn!("zellij list-sessions exited {}: {}", output.status, stderr.trim());
    }

    let stdout = String::from_utf8_lossy(&output.stdout);
    let names: Vec<String> = stdout
        .lines()
        .map(|l| l.trim().to_string())
        .filter(|l| !l.is_empty())
        .collect();
    debug!("zellij list-sessions --short returned {} names", names.len());
    Some(names)
}

/// Resolves the directory zellij keeps its session sockets in for the current
/// user. Layout for zellij >= 0.44 is `/run/user/{uid}/zellij/contract_version_1/`.
/// Falls back to `/tmp/zellij-{uid}` for older builds.
fn session_socket_dir() -> PathBuf {
    let uid = unsafe { libc::getuid() };
    let xdg_runtime = std::env::var("XDG_RUNTIME_DIR")
        .unwrap_or_else(|_| format!("/run/user/{}", uid));
    let new_layout = PathBuf::from(format!("{}/zellij/contract_version_1", xdg_runtime));
    if new_layout.exists() {
        return new_layout;
    }
    PathBuf::from(format!("/tmp/zellij-{}", uid))
}

pub struct ZellijClient {
    stream: UnixStream,
}

impl ZellijClient {
    pub async fn connect(path: PathBuf) -> tokio::io::Result<Self> {
        let stream = UnixStream::connect(path).await?;
        Ok(Self { stream })
    }

    pub async fn send_input(&mut self, input: &[u8]) -> tokio::io::Result<()> {
        self.stream.write_all(input).await?;
        Ok(())
    }

    pub async fn read_output(&mut self, buf: &mut [u8]) -> tokio::io::Result<usize> {
        self.stream.read(buf).await
    }
}
