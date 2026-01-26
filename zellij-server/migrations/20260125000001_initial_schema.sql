-- Initial Perth Database Schema
-- STORY-INF-001: Database Schema Setup
--
-- This migration creates the foundational tables for Perth's session persistence system.
--
-- Tables:
--   - sessions: Top-level session metadata and template association
--   - tabs: Tab containers within sessions
--   - panes: Individual panes (terminals or custom components) within tabs
--   - pane_history: Scrollback buffer storage for panes
--   - templates: Reusable layout templates

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- Sessions Table
-- ============================================================================
-- Stores top-level session metadata including associated template
CREATE TABLE sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    template_name TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_active TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for fast session lookup by name
CREATE INDEX idx_sessions_name ON sessions(name);

-- Index for finding recently active sessions
CREATE INDEX idx_sessions_last_active ON sessions(last_active DESC);

-- ============================================================================
-- Tabs Table
-- ============================================================================
-- Stores tab containers within sessions with layout information
CREATE TABLE tabs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    name TEXT NOT NULL,
    layout_blob JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(session_id, position)
);

-- Index for fast tab lookup by session
CREATE INDEX idx_tabs_session_id ON tabs(session_id);

-- ============================================================================
-- Panes Table
-- ============================================================================
-- Stores individual panes with support for both terminal and custom component types
CREATE TABLE panes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tab_id UUID NOT NULL REFERENCES tabs(id) ON DELETE CASCADE,
    pane_id TEXT NOT NULL,
    pane_type TEXT NOT NULL DEFAULT 'terminal',
    component_state JSONB,
    title TEXT,
    cwd TEXT,
    command TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tab_id, pane_id),
    CHECK (pane_type IN ('terminal', 'bloodbank-feed', 'imi-browser', 'zdrive-browser'))
);

-- Index for fast pane lookup by tab
CREATE INDEX idx_panes_tab_id ON panes(tab_id);

-- Index for pane type filtering
CREATE INDEX idx_panes_pane_type ON panes(pane_type);

-- ============================================================================
-- Pane History Table
-- ============================================================================
-- Stores scrollback buffer content for panes in compressed chunks
CREATE TABLE pane_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    pane_id UUID NOT NULL REFERENCES panes(id) ON DELETE CASCADE,
    chunk_index INTEGER NOT NULL,
    content BYTEA NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(pane_id, chunk_index)
);

-- Index for fast history retrieval by pane and chunk order
CREATE INDEX idx_pane_history_pane_id_chunk_index ON pane_history(pane_id, chunk_index);

-- ============================================================================
-- Templates Table
-- ============================================================================
-- Stores reusable layout templates as JSONB definitions
CREATE TABLE templates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT UNIQUE NOT NULL,
    definition JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Unique index on template name for fast template lookup
CREATE UNIQUE INDEX idx_templates_name ON templates(name);
