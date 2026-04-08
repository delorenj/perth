use std::path::PathBuf;
use tokio::net::UnixStream;
use tokio::io::{AsyncReadExt, AsyncWriteExt};
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Session {
    pub name: String,
    pub socket_path: PathBuf,
}

pub async fn list_sessions() -> Vec<Session> {
    let mut sessions = Vec::new();
    let uid = unsafe { libc::getuid() };
    let base_path = PathBuf::from(format!("/tmp/zellij-{}", uid));

    if let Ok(mut entries) = tokio::fs::read_dir(&base_path).await {
        while let Ok(Some(entry)) = entries.next_entry().await {
            let path = entry.path();
            if path.is_dir() {
                if let Ok(mut session_entries) = tokio::fs::read_dir(&path).await {
                    while let Ok(Some(s_entry)) = session_entries.next_entry().await {
                        let s_path = s_entry.path();
                        if let Some(name) = s_path.file_name().and_then(|n| n.to_str()) {
                            if name.starts_with("zellij-") {
                                let session_name = name.trim_start_matches("zellij-").to_string();
                                sessions.push(Session {
                                    name: session_name,
                                    socket_path: s_path,
                                });
                            }
                        }
                    }
                }
            }
        }
    }
    sessions
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
