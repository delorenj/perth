# CLAUDE.md

## Project Overview

**Perth** - Voice-driven mobile-first Zellij client 

## Ticket Management (MANDATORY)

No code changes without an active Plane ticket.

```
Board: https://plane.delo.sh/33god/
```

- Move ticket to "In Progress" before first code change
- Branch names must include ticket reference
- Commit messages must reference tickets
- Emergency bypass: `ALLOW_NO_TICKET=1`

## Development

```bash
# Load environment
mise trust

# Run tasks
mise tasks  # list available tasks
mise run setup  # initial setup
```

## BMAD Methodology

This project follows BMAD. All methodology files are in `_bmad/`.

- Strict BMAD adherence for prompts and tasks
- Component work delegated to specialized agents
- Maintain parity between BMAD documents and Plane boards

## Principles

- Work with full autonomy toward task goals
- Make well-informed decisions when judgment calls arise
- Speed prioritized over perfection for non-critical paths
