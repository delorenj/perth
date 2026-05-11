mod zellij_ipc;

use std::collections::HashMap;
use std::net::SocketAddr;
use std::sync::Arc;

use axum::{
    extract::{
        ws::{Message, WebSocket, WebSocketUpgrade},
        Json, State,
    },
    http::StatusCode,
    response::IntoResponse,
    routing::{get, post},
    Router,
};
use clap::Parser;
use futures_util::{sink::SinkExt, stream::StreamExt};
use serde::{Deserialize, Serialize};
use tokio::sync::{broadcast, RwLock};
use tracing::{debug, error, info};
use tracing_subscriber::{layer::SubscriberExt, util::SubscriberInitExt};

#[derive(Parser, Debug)]
#[command(author, version, about, long_about = None)]
struct Args {
    /// Port to listen on
    #[arg(short, long, default_value_t = 7800)]
    port: u16,

    /// Host to bind to
    #[arg(short, long, default_value = "0.0.0.0")]
    host: String,

    /// Authorization token (optional)
    #[arg(short, long)]
    token: Option<String>,
}

/// Wire shape for messages exchanged with the Perth Android client.
/// `serde(tag = "type", rename_all = "snake_case")` keeps the JSON keys
/// matching the kotlinx.serialization defaults on the client side.
#[derive(Debug, Serialize, Deserialize, Clone)]
#[serde(tag = "type", rename_all = "snake_case")]
enum PerthMessage {
    SessionList,
    SessionListUpdate { sessions: Vec<ZellijSessionInfo> },
    SessionAttach { session_name: String },
    /// New for M2: bridge sends the full tab/pane snapshot for a session in
    /// response to a `SessionAttach`. Drives the phone client's tab pager.
    SessionAttached {
        session_name: String,
        tabs: Vec<TabInfoWire>,
    },
    PaneInput { pane_id: String, input: String },
    PaneCommand { pane_id: String, command: String },
    PaneOutput { pane_id: String, data: String },
    Error { message: String },
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct ZellijSessionInfo {
    pub name: String,
    pub is_current: bool,
}

/// Wire shape of a single tab in a SessionAttached message. Mirrors the
/// fields the plugin reports.
#[derive(Debug, Serialize, Deserialize, Clone)]
struct TabInfoWire {
    position: usize,
    name: String,
    is_active: bool,
    active_pane_id: Option<u32>,
    panes: Vec<PaneInfoWire>,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
struct PaneInfoWire {
    id: u32,
    title: String,
    is_active: bool,
}

/// Wire shape posted by the zellij plugin to `POST /internal/state`. Must
/// match `StateSnapshot` in `bridge/plugin/src/lib.rs`.
#[derive(Debug, Deserialize, Clone)]
struct PluginStateSnapshot {
    session_name: String,
    tabs: Vec<TabInfoWire>,
    #[serde(default)]
    pane_renders: HashMap<String, String>,
}

/// In-memory cache of the most recent snapshot per session name. Updated by
/// the `/internal/state` endpoint and read by WebSocket handlers when a
/// client attaches.
#[derive(Default)]
struct SessionCache {
    snapshots: HashMap<String, PluginStateSnapshot>,
}

struct AppState {
    /// Broadcast channel for fanning out PerthMessage frames to all attached
    /// WebSocket clients.
    tx: broadcast::Sender<PerthMessage>,
    /// Latest state from the plugin, keyed by session name.
    cache: RwLock<SessionCache>,
    #[allow(dead_code)]
    token: Option<String>,
}

#[tokio::main]
async fn main() {
    tracing_subscriber::registry()
        .with(
            tracing_subscriber::EnvFilter::try_from_default_env()
                .unwrap_or_else(|_| "perth_bridge=debug,tower_http=debug".into()),
        )
        .with(tracing_subscriber::fmt::layer())
        .init();

    let args = Args::parse();
    let (tx, _rx) = broadcast::channel(256);

    let state = Arc::new(AppState {
        tx,
        cache: RwLock::new(SessionCache::default()),
        token: args.token,
    });

    let app = Router::new()
        .route("/ws", get(ws_handler))
        .route("/internal/state", post(internal_state_handler))
        .with_state(state);

    let addr: SocketAddr = format!("{}:{}", args.host, args.port)
        .parse()
        .expect("Invalid host/port");

    info!("Perth Bridge listening on {}", addr);
    let listener = tokio::net::TcpListener::bind(addr).await.unwrap();
    axum::serve(listener, app).await.unwrap();
}

async fn ws_handler(
    ws: WebSocketUpgrade,
    State(state): State<Arc<AppState>>,
) -> impl IntoResponse {
    ws.on_upgrade(move |socket| handle_socket(socket, state))
}

/// Internal endpoint the zellij plugin POSTs state snapshots to. The plugin
/// runs on the same host as the bridge, so the route is intentionally not
/// authenticated; bind to localhost or firewall externally to keep it private.
async fn internal_state_handler(
    State(state): State<Arc<AppState>>,
    Json(snapshot): Json<PluginStateSnapshot>,
) -> impl IntoResponse {
    debug!(
        "plugin snapshot: session={} tabs={} pane_renders={}",
        snapshot.session_name,
        snapshot.tabs.len(),
        snapshot.pane_renders.len(),
    );

    // Push pane render deltas to any attached clients before swapping the
    // cache, so a client attaching at the same moment doesn't race ahead of
    // the broadcast.
    for (pane_id, data) in &snapshot.pane_renders {
        let _ = state.tx.send(PerthMessage::PaneOutput {
            pane_id: pane_id.clone(),
            data: data.clone(),
        });
    }

    // Also fan out the new SessionAttached so clients already attached to
    // this session see live tab changes (e.g., a new tab opened in zellij).
    let _ = state.tx.send(PerthMessage::SessionAttached {
        session_name: snapshot.session_name.clone(),
        tabs: snapshot.tabs.clone(),
    });

    state
        .cache
        .write()
        .await
        .snapshots
        .insert(snapshot.session_name.clone(), snapshot);

    StatusCode::NO_CONTENT
}

async fn handle_socket(socket: WebSocket, state: Arc<AppState>) {
    let (mut sender, mut receiver) = socket.split();
    let mut rx = state.tx.subscribe();
    let mut attached_session: Option<String> = None;

    // Forward broadcasted PerthMessages out to this client.
    let mut send_task = tokio::spawn(async move {
        while let Ok(msg) = rx.recv().await {
            if let Ok(text) = serde_json::to_string(&msg) {
                if sender.send(Message::Text(text)).await.is_err() {
                    break;
                }
            }
        }
    });

    let state_recv = state.clone();
    let mut recv_task = tokio::spawn(async move {
        while let Some(Ok(Message::Text(text))) = receiver.next().await {
            match serde_json::from_str::<PerthMessage>(&text) {
                Ok(msg) => {
                    debug!("Received message: {:?}", msg);
                    match msg {
                        PerthMessage::SessionList => {
                            let sessions = zellij_ipc::list_sessions().await;
                            let session_info = sessions
                                .into_iter()
                                .map(|s| ZellijSessionInfo {
                                    name: s.name,
                                    is_current: false,
                                })
                                .collect();
                            let _ = state_recv.tx.send(PerthMessage::SessionListUpdate {
                                sessions: session_info,
                            });
                        }
                        PerthMessage::SessionAttach { session_name } => {
                            info!("Attach request: {}", session_name);
                            attached_session = Some(session_name.clone());
                            // Replay the latest plugin snapshot for this session
                            // so the client sees tabs immediately without waiting
                            // for the next plugin event.
                            let cache = state_recv.cache.read().await;
                            if let Some(snap) = cache.snapshots.get(&session_name) {
                                let _ = state_recv.tx.send(PerthMessage::SessionAttached {
                                    session_name: snap.session_name.clone(),
                                    tabs: snap.tabs.clone(),
                                });
                                for (pane_id, data) in &snap.pane_renders {
                                    let _ = state_recv.tx.send(PerthMessage::PaneOutput {
                                        pane_id: pane_id.clone(),
                                        data: data.clone(),
                                    });
                                }
                            } else {
                                debug!(
                                    "No cached state for session {}; client will wait for plugin",
                                    session_name
                                );
                            }
                        }
                        PerthMessage::PaneInput { input, .. } => {
                            if let Some(ref session_name) = attached_session {
                                debug!("Sending input to session {}: {}", session_name, input);
                                let _ = tokio::process::Command::new("zellij")
                                    .args(["-s", session_name, "action", "write-chars", &input])
                                    .spawn();
                            }
                        }
                        PerthMessage::PaneCommand { command, .. } => {
                            if let Some(ref session_name) = attached_session {
                                debug!("Sending command to session {}: {}", session_name, command);
                                let full_command = format!("{}\n", command);
                                let _ = tokio::process::Command::new("zellij")
                                    .args(["-s", session_name, "action", "write-chars", &full_command])
                                    .spawn();
                            }
                        }
                        _ => {
                            debug!("Message type not handled: {:?}", msg);
                        }
                    }
                }
                Err(e) => {
                    error!("Failed to parse message: {}", e);
                }
            }
        }
    });

    tokio::select! {
        _ = (&mut send_task) => recv_task.abort(),
        _ = (&mut recv_task) => send_task.abort(),
    };
}
