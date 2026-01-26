// Database record models
// STORY-001: Persistence Manager

use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};
use sqlx::types::Json;
use uuid::Uuid;

/// Session record corresponding to sessions table
#[derive(Debug, Clone, Serialize, Deserialize, sqlx::FromRow)]
pub struct SessionRecord {
    pub id: Uuid,
    pub name: String,
    pub template_name: Option<String>,
    pub created_at: DateTime<Utc>,
    pub last_active: DateTime<Utc>,
}

/// Tab record corresponding to tabs table
#[derive(Debug, Clone, Serialize, Deserialize, sqlx::FromRow)]
pub struct TabRecord {
    pub id: Uuid,
    pub session_id: Uuid,
    pub position: i32,
    pub name: String,
    #[sqlx(json)]
    pub layout_blob: Json<serde_json::Value>,
    pub created_at: DateTime<Utc>,
    pub updated_at: DateTime<Utc>,
}

/// Pane record corresponding to panes table
#[derive(Debug, Clone, Serialize, Deserialize, sqlx::FromRow)]
pub struct PaneRecord {
    pub id: Uuid,
    pub tab_id: Uuid,
    pub pane_id: String,
    pub pane_type: String,
    #[sqlx(json)]
    pub component_state: Option<Json<serde_json::Value>>,
    pub title: Option<String>,
    pub cwd: Option<String>,
    pub command: Option<String>,
    pub created_at: DateTime<Utc>,
    pub updated_at: DateTime<Utc>,
}

/// Pane history chunk record
#[derive(Debug, Clone, sqlx::FromRow)]
pub struct PaneHistoryRecord {
    pub id: Uuid,
    pub pane_id: Uuid,
    pub chunk_index: i32,
    pub content: Vec<u8>,
    pub created_at: DateTime<Utc>,
}

/// Template record corresponding to templates table
#[derive(Debug, Clone, Serialize, Deserialize, sqlx::FromRow)]
pub struct TemplateRecord {
    pub id: Uuid,
    pub name: String,
    #[sqlx(json)]
    pub definition: Json<serde_json::Value>,
    pub created_at: DateTime<Utc>,
    pub updated_at: DateTime<Utc>,
}
