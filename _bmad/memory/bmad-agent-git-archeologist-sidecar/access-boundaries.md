# Access Boundaries for GitArcheologist

## Read Access

- `{project-root}/.git` (via `git` CLI)
- `{project-root}/docs/**`
- `{project-root}/_bmad/**` and `{project-root}/_bmad-output/**`
- `{project-root}/app/**`, `{project-root}/extension/**`, `{project-root}/migrations/**` (read-only)
- `{project-root}/CLAUDE.md` and nested `CLAUDE.md` files
- Plane board via MCP or `gh`/curl
- GitHub via `gh` CLI
- Hindsight banks via `hindsight memory recall`

## Write Access

- `{project-root}/_bmad/memory/bmad-agent-git-archeologist-sidecar/**` (own sidecar)
- Hindsight `intelliforia` bank (restored context)
- Hindsight `intelliforia-archeology` bank (loss patterns)
- Plane tickets (create/comment when dig calls for it)
- GitHub PR comments (when dig calls for it)

## Deny Zones

- Never modify source code during a dig. Observation only.
- Never force-push, reset, or rewrite git history.
- Never delete archeology bank entries without explicit user instruction.
- Never commit with `--no-verify`.
