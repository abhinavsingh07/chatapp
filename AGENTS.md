# Agent Guidance & Rules: Chat Application Backend

Welcome to the Spring Boot Chat Backend project. Follow these guidelines strictly for all code generation, refactoring, and terminal executions.

## 🛠 Tech Stack & Environment
- Language: Java 17 and higher
- Framework: Spring Boot 3.x, Spring MVC
- Security: Spring Security (JWT-based authentication)
- Protocol: Spring WebSocket / STOMP (for real-time chat messaging)
- Database & ORM: Mysql, Spring Data JPA / Hibernate
- Build & Package Manager: Maven
- Cache: Redis
- Message Broker: RabbitMQ

## Project Architecture & Directory Mapping
Always place new files into these exact directory structures inside `src/main/java/com/chatapp/synk/chat/`:
- Entities & DB Schemas: `/entity/`
- Repositories: `repository/`
- Service Layer: `service/` (Interfaces) & `service/impl/` (Implementations)
- REST Controllers: `controller/`
- Data Transfer Objects: `dto/`
- Configuration Classes: `config/`
- Entity classes: `entity/
- Response classes: `response/

## DB Schemas & Entity-First Rules
1. **Source of Truth**: The database schema is strictly derived from Java Entity classes using Hibernate. Do not write raw SQL schemas unless writing migration scripts.
2. **Entity Generation**: When creating database models, use standard JPA annotations (`@Entity`, `@Table`, `@Id`, `@GeneratedValue`).

## Coding Rules & Style Conventions
1. **Clean Architecture**: Controllers must never talk directly to Repositories or Entities. They must interact only with Services and DTOs.
2. **DTO Mapping**: Always map Entities to DTOs before sending them over REST. Do not expose raw `@Entity` classes to the client.Use @Mapper.java class to map dtos to entity and entity to dtos
3. **Global Error Handling**: Handle exceptions globally using `@GlobalExceptionHandler` and return a standard payload: using @ErrorResponse.java.

## Execution Commands
You are permitted to execute the following terminal commands to build, test, and verify your work locally:
- Compile Project: `mvn compile`
- Run Unit Tests: `mvn test`
- Package & Verify Application: `mvn clean package`
- Run Application Locally: `mvn spring-boot:run -Pdev`