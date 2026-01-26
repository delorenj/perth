// Persistence Manager with write-behind caching
// STORY-001: Persistence Manager

use super::{
    error::{PersistenceError, PersistenceResult},
    models::{PaneRecord, SessionRecord, TabRecord},
};
use log::{error, info, warn};
use sqlx::{postgres::PgPoolOptions, PgPool};
use std::time::Duration;
use tokio::sync::mpsc::{self, UnboundedReceiver, UnboundedSender};
use uuid::Uuid;

/// Write operations to be queued for async processing
#[derive(Debug, Clone)]
pub enum WriteOperation {
    CreateSession(SessionRecord),
    UpdateSession {
        id: Uuid,
        last_active: chrono::DateTime<chrono::Utc>,
    },
    CreateTab(TabRecord),
    UpdateTab(TabRecord),
    DeleteTab(Uuid),
    CreatePane(PaneRecord),
    UpdatePane(PaneRecord),
    DeletePane(Uuid),
}

/// Persistence Manager implementing write-behind caching strategy
pub struct PersistenceManager {
    pool: Option<PgPool>,
    write_queue_tx: UnboundedSender<WriteOperation>,
    is_available: bool,
}

impl PersistenceManager {
    /// Create a new PersistenceManager with database connection
    ///
    /// NFR-003: Gracefully degrades if database is unavailable
    pub async fn new(database_url: Option<String>) -> Self {
        let (write_queue_tx, write_queue_rx) = mpsc::unbounded_channel();

        let pool = match database_url {
            Some(url) => {
                match PgPoolOptions::new()
                    .max_connections(10)
                    .min_connections(2)
                    .acquire_timeout(Duration::from_secs(5))
                    .connect(&url)
                    .await
                {
                    Ok(pool) => {
                        info!("Perth: PostgreSQL connection pool established");
                        // Run migrations
                        match sqlx::migrate!("./migrations").run(&pool).await {
                            Ok(_) => info!("Perth: Database migrations applied successfully"),
                            Err(e) => {
                                error!("Perth: Migration failed: {}", e);
                                warn!("Perth: Continuing without persistence");
                                return Self {
                                    pool: None,
                                    write_queue_tx,
                                    is_available: false,
                                };
                            }
                        }
                        Some(pool)
                    }
                    Err(e) => {
                        warn!("Perth: Failed to connect to PostgreSQL: {}", e);
                        warn!("Perth: Continuing without persistence (NFR-003: graceful degradation)");
                        None
                    }
                }
            }
            None => {
                info!("Perth: No DATABASE_URL provided, persistence disabled");
                None
            }
        };

        let is_available = pool.is_some();

        // Spawn write queue processor
        if let Some(pool_clone) = pool.clone() {
            tokio::spawn(async move {
                Self::process_write_queue(pool_clone, write_queue_rx).await;
            });
        }

        Self {
            pool,
            write_queue_tx,
            is_available,
        }
    }

    /// Check if persistence is available
    pub fn is_available(&self) -> bool {
        self.is_available
    }

    /// Queue a write operation for async processing (write-behind caching)
    pub fn queue_write(&self, operation: WriteOperation) -> PersistenceResult<()> {
        if !self.is_available {
            // Silently ignore writes when DB unavailable (NFR-003)
            return Ok(());
        }

        self.write_queue_tx
            .send(operation)
            .map_err(|e| PersistenceError::QueryFailed(format!("Failed to queue write: {}", e)))
    }

    /// Create a new session (queued async write)
    pub fn create_session(&self, session: SessionRecord) -> PersistenceResult<()> {
        self.queue_write(WriteOperation::CreateSession(session))
    }

    /// Update session last_active timestamp (queued async write)
    pub fn update_session_activity(&self, session_id: Uuid) -> PersistenceResult<()> {
        self.queue_write(WriteOperation::UpdateSession {
            id: session_id,
            last_active: chrono::Utc::now(),
        })
    }

    /// Create a new tab (queued async write)
    pub fn create_tab(&self, tab: TabRecord) -> PersistenceResult<()> {
        self.queue_write(WriteOperation::CreateTab(tab))
    }

    /// Update tab (queued async write)
    pub fn update_tab(&self, tab: TabRecord) -> PersistenceResult<()> {
        self.queue_write(WriteOperation::UpdateTab(tab))
    }

    /// Delete tab (queued async write)
    pub fn delete_tab(&self, tab_id: Uuid) -> PersistenceResult<()> {
        self.queue_write(WriteOperation::DeleteTab(tab_id))
    }

    /// Create a new pane (queued async write)
    pub fn create_pane(&self, pane: PaneRecord) -> PersistenceResult<()> {
        self.queue_write(WriteOperation::CreatePane(pane))
    }

    /// Update pane (queued async write)
    pub fn update_pane(&self, pane: PaneRecord) -> PersistenceResult<()> {
        self.queue_write(WriteOperation::UpdatePane(pane))
    }

    /// Delete pane (queued async write)
    pub fn delete_pane(&self, pane_id: Uuid) -> PersistenceResult<()> {
        self.queue_write(WriteOperation::DeletePane(pane_id))
    }

    /// Restore session from database (synchronous read)
    pub async fn restore_session(&self, session_id: Uuid) -> PersistenceResult<SessionRecord> {
        let pool = self
            .pool
            .as_ref()
            .ok_or_else(|| PersistenceError::DatabaseUnavailable("No pool".to_string()))?;

        let session = sqlx::query_as::<_, SessionRecord>(
            "SELECT id, name, template_name, created_at, last_active FROM sessions WHERE id = $1",
        )
        .bind(session_id)
        .fetch_one(pool)
        .await
        .map_err(|e| match e {
            sqlx::Error::RowNotFound => PersistenceError::SessionNotFound(session_id),
            _ => e.into(),
        })?;

        Ok(session)
    }

    /// Restore all tabs for a session (synchronous read)
    pub async fn restore_tabs(&self, session_id: Uuid) -> PersistenceResult<Vec<TabRecord>> {
        let pool = self
            .pool
            .as_ref()
            .ok_or_else(|| PersistenceError::DatabaseUnavailable("No pool".to_string()))?;

        let tabs = sqlx::query_as::<_, TabRecord>(
            "SELECT id, session_id, position, name, layout_blob, created_at, updated_at
             FROM tabs WHERE session_id = $1 ORDER BY position",
        )
        .bind(session_id)
        .fetch_all(pool)
        .await?;

        Ok(tabs)
    }

    /// Restore all panes for a tab (synchronous read)
    pub async fn restore_panes(&self, tab_id: Uuid) -> PersistenceResult<Vec<PaneRecord>> {
        let pool = self
            .pool
            .as_ref()
            .ok_or_else(|| PersistenceError::DatabaseUnavailable("No pool".to_string()))?;

        let panes = sqlx::query_as::<_, PaneRecord>(
            "SELECT id, tab_id, pane_id, pane_type, component_state, title, cwd, command, created_at, updated_at
             FROM panes WHERE tab_id = $1",
        )
        .bind(tab_id)
        .fetch_all(pool)
        .await?;

        Ok(panes)
    }

    /// Process write queue (background task)
    async fn process_write_queue(pool: PgPool, mut rx: UnboundedReceiver<WriteOperation>) {
        info!("Perth: Write queue processor started");

        while let Some(operation) = rx.recv().await {
            if let Err(e) = Self::execute_write_operation(&pool, operation).await {
                error!("Perth: Write operation failed: {}", e);
                // Continue processing (NFR-003: don't crash on DB errors)
            }
        }

        warn!("Perth: Write queue processor stopped");
    }

    /// Execute a single write operation
    async fn execute_write_operation(
        pool: &PgPool,
        operation: WriteOperation,
    ) -> PersistenceResult<()> {
        match operation {
            WriteOperation::CreateSession(session) => {
                sqlx::query(
                    "INSERT INTO sessions (id, name, template_name, created_at, last_active)
                     VALUES ($1, $2, $3, $4, $5)
                     ON CONFLICT (id) DO NOTHING",
                )
                .bind(session.id)
                .bind(session.name)
                .bind(session.template_name)
                .bind(session.created_at)
                .bind(session.last_active)
                .execute(pool)
                .await?;
            }
            WriteOperation::UpdateSession { id, last_active } => {
                sqlx::query("UPDATE sessions SET last_active = $1 WHERE id = $2")
                    .bind(last_active)
                    .bind(id)
                    .execute(pool)
                    .await?;
            }
            WriteOperation::CreateTab(tab) => {
                sqlx::query(
                    "INSERT INTO tabs (id, session_id, position, name, layout_blob, created_at, updated_at)
                     VALUES ($1, $2, $3, $4, $5, $6, $7)
                     ON CONFLICT (id) DO NOTHING",
                )
                .bind(tab.id)
                .bind(tab.session_id)
                .bind(tab.position)
                .bind(tab.name)
                .bind(&tab.layout_blob)
                .bind(tab.created_at)
                .bind(tab.updated_at)
                .execute(pool)
                .await?;
            }
            WriteOperation::UpdateTab(tab) => {
                sqlx::query(
                    "UPDATE tabs SET position = $1, name = $2, layout_blob = $3, updated_at = $4
                     WHERE id = $5",
                )
                .bind(tab.position)
                .bind(tab.name)
                .bind(&tab.layout_blob)
                .bind(tab.updated_at)
                .bind(tab.id)
                .execute(pool)
                .await?;
            }
            WriteOperation::DeleteTab(tab_id) => {
                sqlx::query("DELETE FROM tabs WHERE id = $1")
                    .bind(tab_id)
                    .execute(pool)
                    .await?;
            }
            WriteOperation::CreatePane(pane) => {
                sqlx::query(
                    "INSERT INTO panes (id, tab_id, pane_id, pane_type, component_state, title, cwd, command, created_at, updated_at)
                     VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
                     ON CONFLICT (id) DO NOTHING",
                )
                .bind(pane.id)
                .bind(pane.tab_id)
                .bind(pane.pane_id)
                .bind(pane.pane_type)
                .bind(&pane.component_state)
                .bind(pane.title)
                .bind(pane.cwd)
                .bind(pane.command)
                .bind(pane.created_at)
                .bind(pane.updated_at)
                .execute(pool)
                .await?;
            }
            WriteOperation::UpdatePane(pane) => {
                sqlx::query(
                    "UPDATE panes SET pane_type = $1, component_state = $2, title = $3, cwd = $4, command = $5, updated_at = $6
                     WHERE id = $7",
                )
                .bind(pane.pane_type)
                .bind(&pane.component_state)
                .bind(pane.title)
                .bind(pane.cwd)
                .bind(pane.command)
                .bind(pane.updated_at)
                .bind(pane.id)
                .execute(pool)
                .await?;
            }
            WriteOperation::DeletePane(pane_id) => {
                sqlx::query("DELETE FROM panes WHERE id = $1")
                    .bind(pane_id)
                    .execute(pool)
                    .await?;
            }
        }

        Ok(())
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[tokio::test]
    async fn test_manager_without_db_gracefully_degrades() {
        let manager = PersistenceManager::new(None).await;
        assert!(!manager.is_available());

        // Should not panic on write operations
        let session = SessionRecord {
            id: Uuid::new_v4(),
            name: "test".to_string(),
            template_name: None,
            created_at: chrono::Utc::now(),
            last_active: chrono::Utc::now(),
        };

        assert!(manager.create_session(session).is_ok());
    }
}
