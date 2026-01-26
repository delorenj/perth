// Perth Persistence Layer
// STORY-001: Persistence Manager
//
// This module provides async PostgreSQL persistence for Perth sessions,
// tabs, and panes with write-behind caching and graceful degradation.

mod error;
mod manager;
mod models;

pub use error::{PersistenceError, PersistenceResult};
pub use manager::PersistenceManager;
pub use models::{PaneRecord, SessionRecord, TabRecord};
