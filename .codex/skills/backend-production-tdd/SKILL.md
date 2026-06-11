---
name: backend-production-tdd
description: Use for Momentum backend work that adds or changes Spring Boot API behavior, services, persistence, validation, or production code. Enforces a simple TDD workflow, production-level structure, clear naming, minimal branching, and focused tests before implementation.
---

# Backend Production TDD

## Core Rule

Always use TDD for backend changes:

1. Write or update a failing test that describes the desired behavior.
2. Run the focused test and confirm it fails for the expected reason.
3. Implement the simplest production code that makes the test pass.
4. Refactor only after tests pass.
5. Run the focused test again, then the broader relevant test suite.

Do not add feature code before the test unless the change is documentation, configuration, or mechanical cleanup.

## Code Style

- Keep code simple and direct.
- Prefer clear names over comments.
- Use small methods with one responsibility.
- Avoid unnecessary abstractions, helpers, inheritance, or premature patterns.
- Avoid unnecessary `if` blocks; use guard clauses, validation annotations, or clearer data flow where they simplify behavior.
- Avoid nested branching when an early return or validation boundary is clearer.
- Keep controller logic thin; put business rules in services.
- Keep persistence logic in repositories or data access boundaries.
- Keep DTOs/request/response models explicit and easy to read.
- Do not hide important behavior behind vague names like `process`, `handle`, `data`, `obj`, or `result` when a domain name is available.

## Spring Boot Structure

Prefer this flow unless the existing codebase establishes a better local pattern:

- Controller: HTTP mapping, request validation, response status.
- Service: business rules and orchestration.
- Repository: persistence.
- DTO/request/response: API contract.
- Entity/model: persisted domain shape.
- Configuration: framework wiring only.

## Test Expectations

- Add controller tests for HTTP behavior and validation.
- Add service tests for business rules.
- Add repository/integration tests only when persistence behavior matters.
- Name tests by behavior, not implementation.
- Test important edge cases, but do not chase low-value permutations.
- Keep tests readable with clear arrange/act/assert sections when useful.

## Before Finishing

- Run the smallest relevant test first.
- Run `./mvnw test` before calling backend work complete.
- Confirm Docker startup if the change touches Docker, datasource config, ports, or app startup:

```bash
docker compose up --build -d
curl http://localhost:8080/health
```

## Bias

When in doubt, choose the boring, readable solution that a future engineer can understand quickly.
