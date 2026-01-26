# Perth Database Migrations

This directory contains SQLx migrations for Perth's PostgreSQL persistence layer.

## Prerequisites

- PostgreSQL 16+
- sqlx-cli: `cargo install sqlx-cli --no-default-features --features postgres`

## Setup

1. Start PostgreSQL (via Docker or local instance):
```bash
docker run -d \
  --name perth-postgres \
  -e POSTGRES_USER=perth \
  -e POSTGRES_PASSWORD=perth \
  -e POSTGRES_DB=perth \
  -p 5432:5432 \
  postgres:16
```

2. Set DATABASE_URL environment variable:
```bash
export DATABASE_URL=postgres://perth:perth@localhost:5432/perth
```

Or copy `.env.example` to `.env` and update credentials.

## Running Migrations

```bash
# From zellij-server directory
cd zellij-server

# Run all pending migrations
sqlx migrate run --source migrations

# Check migration status
sqlx migrate info --source migrations

# Rollback last migration
sqlx migrate revert --source migrations
```

## Offline Mode (Development)

For compile-time query verification without a running database:

```bash
# Prepare offline metadata (run this after adding new queries)
sqlx prepare --database-url postgres://perth:perth@localhost:5432/perth -- --lib
```

## Migration Structure

- `20260125000001_initial_schema.sql`: Initial schema with all tables
- `20260125000001_initial_schema.down.sql`: Rollback for initial schema

## Schema Overview

- **sessions**: Top-level session metadata
- **tabs**: Tab containers within sessions
- **panes**: Individual panes (terminal or custom components)
- **pane_history**: Scrollback buffer chunks
- **templates**: Reusable layout templates (JSONB)
