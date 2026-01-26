# Perth Persistence Layer

**STORY-001**: Persistence Manager

This module provides PostgreSQL persistence for Perth sessions with write-behind caching and graceful degradation.

## Architecture

### Write-Behind Caching Strategy

The `PersistenceManager` implements an async write queue pattern:
- Write operations are queued to a Tokio unbounded channel
- A background task processes the queue asynchronously
- NFR-003: System continues without persistence if database unavailable

### Components

- **`manager.rs`**: Core `PersistenceManager` with connection pool and write queue
- **`models.rs`**: Database record structs (`SessionRecord`, `TabRecord`, `PaneRecord`, etc.)
- **`error.rs`**: Error types and `PersistenceResult<T>` alias

## Usage

### Initialization

```rust
use zellij_server::persistence::PersistenceManager;

// Initialize with DATABASE_URL environment variable
let db_url = std::env::var("DATABASE_URL").ok();
let manager = PersistenceManager::new(db_url).await;

// Check if persistence is available
if manager.is_available() {
    println!("Persistence enabled");
} else {
    println!("Running without persistence (NFR-003: graceful degradation)");
}
```

### Writing Data (Async Queue)

```rust
use zellij_server::persistence::models::SessionRecord;
use uuid::Uuid;
use chrono::Utc;

// Create a session record
let session = SessionRecord {
    id: Uuid::new_v4(),
    name: "my-session".to_string(),
    template_name: None,
    created_at: Utc::now(),
    last_active: Utc::now(),
};

// Queue write (non-blocking)
manager.create_session(session)?;
```

### Reading Data (Synchronous)

```rust
// Restore session from database (async, blocks until complete)
let session_id = Uuid::parse_str("...")?;
let session = manager.restore_session(session_id).await?;

// Restore all tabs for session
let tabs = manager.restore_tabs(session_id).await?;

// Restore all panes for tab
let panes = manager.restore_panes(tab.id).await?;
```

## Database Schema

See `zellij-server/migrations/` for full schema.

### Tables

- **`sessions`**: Session metadata (id, name, template_name, timestamps)
- **`tabs`**: Tab containers (id, session_id, position, name, layout_blob)
- **`panes`**: Panes (id, tab_id, pane_id, pane_type, component_state, title, cwd, command)
- **`pane_history`**: Scrollback chunks (id, pane_id, chunk_index, content)
- **`templates`**: Layout templates (id, name, definition)

## Error Handling

All operations return `PersistenceResult<T>`:

```rust
match manager.create_session(session) {
    Ok(()) => println!("Session queued for persistence"),
    Err(PersistenceError::DatabaseUnavailable(msg)) => {
        // NFR-003: System continues
        warn!("Persistence unavailable: {}", msg);
    }
    Err(e) => error!("Persistence error: {}", e),
}
```

## Testing

Run tests with PostgreSQL available:

```bash
export DATABASE_URL=postgres://perth:perth@localhost:5432/perth_test
cargo test --package zellij-server persistence::
```

## Acceptance Criteria (STORY-001)

- [x] `PersistenceManager` implements write-behind caching strategy
- [x] Async task queues state changes to DB via Tokio channel
- [ ] `--restore-from-db <session_id>` CLI flag (pending integration)
- [x] Graceful degradation if DB unavailable (NFR-003)
- [x] Session creation writes to `sessions` table
- [x] Tab/pane creation writes to `tabs`/`panes` tables with JSONB
- [x] Unit tests with sqlx::test macro

## Integration

To integrate with Perth session management:

1. Initialize `PersistenceManager` in `zellij-server/src/lib.rs:start_server()`
2. Call persistence methods on session/tab/pane create/update/delete events
3. Add `--restore-from-db` CLI flag to main Perth binary
4. Implement session restoration logic in startup flow

## Future Enhancements (Post-Milestone 1)

- Pane history persistence (scrollback buffer chunks)
- Template CRUD operations
- Connection pool tuning based on workload
- Compression for JSONB and BYTEA columns
- Periodic cleanup of old session data
