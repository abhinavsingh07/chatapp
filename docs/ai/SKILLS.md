# SKILLS.md

## Purpose

This file provides optional implementation tactics and review heuristics.
Use it when the task requires design trade-offs, refactoring strategy, or code quality guidance.

---

## When To Read

Read this file for:

- non-trivial feature implementation
- refactoring strategy
- test strategy decisions
- performance trade-offs

Skip this file for very small fixes when AGENTS + local code context are enough.

---

## Problem-Solving Workflow

1. Clarify requirement and constraints.
2. Inspect existing implementation paths.
3. Reuse current services/utilities where possible.
4. Evaluate edge cases and failure modes.
5. Implement smallest correct change.
6. Verify behavior and regressions.

---

## Implementation Heuristics

- Prefer simple, explicit code over clever abstractions.
- Keep methods small and purpose-driven.
- Name symbols by business intent.
- Extract duplication only when it improves clarity.
- Use composition before inheritance.

---

## Refactoring Heuristics

- Preserve behavior first, improve structure second.
- Refactor only the scope needed by the request.
- Remove dead branches and unclear naming while touching code.
- Avoid broad rewrites unless requested.

---

## Testing Heuristics

- Prioritize tests for changed business logic.
- Cover one success path and relevant failure paths.
- Keep tests deterministic and isolated.
- Mock external systems when unit testing.

---

## Performance Heuristics

- Measure first when possible.
- Optimize bottlenecks, not guesses.
- Watch for N+1 patterns and repeated remote calls.
- Prefer pagination and batch operations for large data paths.

---

## Review Heuristics

Before finishing, quickly verify:

- Is naming clear?
- Is logic duplicated?
- Are edge cases handled?
- Is there any avoidable complexity?
- Are tests or validation updates needed?

---

## Communication Heuristics

- Explain important trade-offs briefly.
- Call out security/performance implications when relevant.
- Mention assumptions explicitly.
- Prefer practical guidance over theory.
