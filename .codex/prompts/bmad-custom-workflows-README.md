# CUSTOM Workflows

## Available Workflows in custom

**ticket-lifecycle**
- Path: `_bmad/custom/workflows/ticket-lifecycle/workflow.md`
- Autonomous multi-agent ticket lifecycle: triage, AC refinement, implementation, and QA verification via Plane + Bloodbank (tri-modal: create, validate, edit)


## Execution

When running any workflow:
1. LOAD {project-root}/_bmad/core/tasks/workflow.xml
2. Pass the workflow path as 'workflow-config' parameter
3. Follow workflow.xml instructions EXACTLY
4. Save outputs after EACH section

## Modes
- Normal: Full interaction
- #yolo: Skip optional steps
