# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# Purpose

This is the entrypoint file for Claude-style agents.
Keep it short and operational.

## Required Documents

- AGENTS.md - mandatory behavior and non-negotiable guardrails.
- docs/ai/INDEX.md - routing map for optional docs.
- docs/ai/MEMORY.md - architecture and system facts.
- docs/ai/SKILLS.md - optional implementation playbook.

## Document Load Order

1. Read `CLAUDE.md`.
2. Read `docs/ai/INDEX.md`.
3. Read `AGENTS.md`.
4. Read `docs/ai/MEMORY.md` only for architecture/infra/system design tasks.
5. Read `docs/ai/SKILLS.md` only for implementation strategy or code quality decisions.

## Token Usage Rules

- Keep responses scoped to the user request.
- Prefer targeted file reads over broad scans.
- Do not duplicate architecture details from `docs/ai/MEMORY.md`.
- Do not duplicate mandatory rules from `AGENTS.md`.

## Build and Run Commands

```bash
# Compile
mvn compile

# Run tests
mvn test

# Package
mvn clean package

# Run app with local profile
mvn spring-boot:run -Pdev
```
Maven profiles in use: `dev`, `test`, `verify`, `no-tests`.

## Architecture Snapshot

- Stack: Spring Boot + Java 17, MySQL, Redis, RabbitMQ, JWT, AWS S3.
- Layering: Controller -> Service -> Repository -> Database.
- Auth: JWT-based authentication and server-side authorization checks.
- Realtime: WebSocket transport with RabbitMQ for cross-instance async delivery.
- Data ownership: MySQL is source of truth; Redis is transient; S3 stores media binaries.
- API shape: DTO-based APIs with standardized success/error response wrappers.

## Source Layout Snapshot

- Base package: `src/main/java/com/chatapp/synk/`
- Controllers: `src/main/java/com/chatapp/synk/controller/`
- Services: `src/main/java/com/chatapp/synk/service/` and `src/main/java/com/chatapp/synk/service/impl/`
- Repositories: `src/main/java/com/chatapp/synk/repository/`
- Entities: `src/main/java/com/chatapp/synk/entity/`
- DTOs: `src/main/java/com/chatapp/synk/dto/`
- Security: `src/main/java/com/chatapp/synk/security/` and `src/main/java/com/chatapp/synk/security_validator/`
- Realtime: `src/main/java/com/chatapp/synk/chat/`
- Media upload: `src/main/java/com/chatapp/synk/mediaUpload/`
