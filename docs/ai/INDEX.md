# AI Docs Index

This folder contains optional, task-scoped guidance documents.

## Files

- `MEMORY.md` - architecture facts and system behavior.
- `SKILLS.md` - engineering playbook and coding quality standards.

## Read Policy

1. Read `CLAUDE.md` first.
2. Read `AGENTS.md` next for mandatory rules.
3. Read `MEMORY.md` only for architecture, infra, and design decisions.
4. Read `SKILLS.md` only for implementation strategy and code quality guidance.
5. For small fixes, avoid optional files unless needed.

## Maintenance Rules

- Keep facts synchronized with `pom.xml` and source code.
- Avoid duplicating the same rule across multiple docs.
- Prefer concise bullet points over long prose to reduce token usage.
- Update this folder when build profiles, auth flow, or architecture boundaries change.
