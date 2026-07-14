# MEMORY.md

## Purpose

This file stores architecture and system facts for Synk.
Keep it factual and implementation-oriented.
Do not place mandatory coding policy here.

---

## System Overview

- Project: Synk Chat Application.
- Backend: Spring Boot + Java 17.
- Core domains: auth, users, conversations, messages, contacts, media.
- Primary non-functional goals: security, scalability, maintainability, low-latency message flow.

---

## Technology Stack

### Backend

- Spring Boot (Web, Security, Data JPA, WebSocket, AMQP, Cache, Actuator)
- MySQL
- Redis
- RabbitMQ
- JWT
- AWS S3 SDK

### Infrastructure

- Docker Compose
- Nginx reverse proxy

---

## Architectural Boundaries

- Controller -> Service -> Repository -> Database.
- Controllers expose DTO-based APIs.
- Services contain business workflows and transaction boundaries.
- Repositories handle persistence concerns only.

---

## Source Package Map

- Base package: `src/main/java/com/chatapp/synk/`
- Controllers: `controller/`
- Service contracts: `service/`
- Service implementations: `service/impl/`
- Repositories: `repository/`
- Entities: `entity/`
- DTOs: `dto/`
- Security/auth: `security/`
- Input validation/sanitization: `security_validator/`
- Realtime messaging: `chat/`
- Media upload: `mediaUpload/`
- Shared utilities: `util/`

---

## Authentication and Authorization Facts

- Authentication is JWT-based.
- Backend validates token and builds security context for protected requests.
- Authorization is enforced server-side.
- Protected resources require ownership/membership checks.

---

## Messaging and Realtime Facts

- Realtime transport uses WebSocket.
- Asynchronous cross-instance delivery uses RabbitMQ.
- Message persistence remains in MySQL.
- Redis is used for transient session/presence/cache state.

---

## Data Ownership Facts

- MySQL is the source of truth for persistent business data.
- Redis is not a system of record.
- Media binaries are stored in S3; metadata is stored in database.

---

## API and Error Facts

- APIs use standardized success/error response wrappers.
- DTOs are returned to clients, not JPA entities.
- Unexpected failures are captured through centralized exception handling.

---

## Update Triggers

Update this file when any of the following changes:

- Authentication/authorization flow
- Messaging flow (WebSocket/RabbitMQ)
- Data ownership boundaries (MySQL/Redis/S3)
- Layer responsibilities (controller/service/repository)
- Response contract conventions
