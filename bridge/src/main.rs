mod zellij_ipc;

use axum::{
    extract::{
        ws::{Message, WebSocket, WebSocketUpgrade},
        State,
    },
    response::IntoResponse,
    routing::get,
    Router,
};
use clap::Parser;
use futures_util::{sink::SinkExt, stream::StreamExt};
use serde::{Deserialize, Serialize};
use std::net::SocketAddr;
use std::sync::Arc;
use tokio::sync::broadcast;
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

#[derive(Debug, Serialize, Deserialize, Clone)]
#[serde(tag = "type", rename_all = "snake_case")]
enum PerthMessage {
    SessionList,
    SessionListUpdate { sessions: Vec<ZellijSessionInfo> },
    SessionAttach { session_name: String },
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

struct AppState {
    tx: broadcast::Sender<PerthMessage>,
    #[allow(dead_code)]
    token: Option<String>,
}

#[tokio::main]
async fn main() {
    // Initialize tracing
    tracing_subscriber::registry()
        .with(tracing_subscriber::EnvFilter::try_from_default_env().unwrap_or_else(|_| "perth_bridge=debug,tower_http=debug".into()))
        .with(tracing_subscriber::fmt::layer())
        .init();

    let args = Args::parse();
    let (tx, _rx) = broadcast::channel(100);

    let state = Arc::new(AppState {
        tx,
        token: args.token,
    });

    let app = Router::new()
        .route("/ws", get(ws_handler))
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

async fn handle_socket(socket: WebSocket, state: Arc<AppState>) {
    let (mut sender, mut receiver) = socket.split();
    let mut rx = state.tx.subscribe();
    let mut attached_session: Option<String> = None;

    // Task for sending messages from broadcast to WebSocket
    let mut send_task = tokio::spawn(async move {
        while let Ok(msg) = rx.recv().await {
            if let Ok(text) = serde_json::to_string(&msg) {
                if sender.send(Message::Text(text)).await.is_err() {
                    break;
                }
            }
        }
    });

    // Task for receiving messages from WebSocket
    let mut recv_task = tokio::spawn(async move {
        while let Some(Ok(Message::Text(text))) = receiver.next().await {
            match serde_json::from_str::<PerthMessage>(&text) {
                Ok(msg) => {
                    debug!("Received message: {:?}", msg);
                    match msg {
                        PerthMessage::SessionList => {
                            let sessions = zellij_ipc::list_sessions().await;
                            let session_info = sessions.into_iter().map(|s| ZellijSessionInfo {
                                name: s.name,
                                is_current: false,
                            }).collect();
                            let _ = state.tx.send(PerthMessage::SessionListUpdate { sessions: session_info });
                        }
                        PerthMessage::SessionAttach { session_name } => {
                            let sessions = zellij_ipc::list_sessions().await;
                            if let Some(session) = sessions.into_iter().find(|s| s.name == session_name) {
                                info!("Attaching to session: {}", session.name);
                                attached_session = Some(session.name.clone());
                                // Start a task to proxy output from this session
                                let tx = state.tx.clone();
                                tokio::spawn(async move {
                                    if let Ok(mut client) = zellij_ipc::ZellijClient::connect(session.socket_path).await {
                                        let mut buf = [0u8; 4096];
                                        while let Ok(n) = client.read_output(&mut buf).await {
                                            if n == 0 { break; }
                                            let data = base64::Engine::encode(&base64::prelude::BASE64_STANDARD, &buf[..n]);
                                            let _ = tx.send(PerthMessage::PaneOutput {
                                                pane_id: "0".to_string(), // TODO: Get actual pane id
                                                data
                                            });
                                        }
                                    }
                                });
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
                                // We send the command followed by Enter (\n)
                                let full_command = format!("{}\n", command);
                                let _ = tokio::process::Command::new("zellij")
                                    .args(["-s", session_name, "action", "write-chars", &full_command])
                                    .spawn();
                            }
                        }
                        _ => {
                            debug!("Message type not yet implemented: {:?}", msg);
                        }
                    }
                }
                Err(e) => {
                    error!("Failed to parse message: {}", e);
                }
            }
        }
    });

    // If any task finishes, abort the other
    tokio::select! {
        _ = (&mut send_task) => recv_task.abort(),
        _ = (&mut recv_task) => send_task.abort(),
    };
}
