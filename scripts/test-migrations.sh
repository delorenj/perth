#!/usr/bin/env bash
# Test script for Perth database migrations
# STORY-INF-001: Database Schema Setup

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Perth Migration Test Script${NC}"
echo "================================"

# Check if DATABASE_URL is set
if [ -z "${DATABASE_URL:-}" ]; then
    echo -e "${RED}ERROR: DATABASE_URL not set${NC}"
    echo "Set it with: export DATABASE_URL=postgres://perth:perth@localhost:5432/perth"
    exit 1
fi

# Check if sqlx-cli is installed
if ! command -v sqlx &> /dev/null; then
    echo -e "${RED}ERROR: sqlx-cli not installed${NC}"
    echo "Install with: cargo install sqlx-cli --no-default-features --features postgres"
    exit 1
fi

# Navigate to zellij-server directory
cd "$(dirname "$0")/../zellij-server" || exit 1

echo -e "\n${YELLOW}Step 1: Running migrations${NC}"
sqlx migrate run --source migrations

echo -e "\n${YELLOW}Step 2: Verifying migrations${NC}"
sqlx migrate info --source migrations

echo -e "\n${YELLOW}Step 3: Testing rollback${NC}"
sqlx migrate revert --source migrations --target-version 0

echo -e "\n${YELLOW}Step 4: Re-running migrations${NC}"
sqlx migrate run --source migrations

echo -e "\n${YELLOW}Step 5: Final verification${NC}"
sqlx migrate info --source migrations

echo -e "\n${GREEN}✓ All migration tests passed${NC}"
