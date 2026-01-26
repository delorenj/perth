-- Rollback for Initial Perth Database Schema
-- STORY-INF-001: Database Schema Setup

-- Drop tables in reverse dependency order
DROP TABLE IF EXISTS pane_history;
DROP TABLE IF EXISTS panes;
DROP TABLE IF EXISTS tabs;
DROP TABLE IF EXISTS templates;
DROP TABLE IF EXISTS sessions;

-- Drop UUID extension (only if no other tables depend on it)
-- Note: Keeping the extension is generally safer for production systems
-- DROP EXTENSION IF EXISTS "uuid-ossp";
