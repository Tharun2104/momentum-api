# Momentum Backend Initial Plan

## Overall Goal

Momentum will be a full-stack productivity and progress tracking app available on iOS and web. The backend goal is to provide a reliable API layer that stores user data, supports client workflows, and keeps future feature development organized around clear domain models.

## Backend Scope

- Provide REST APIs for the Momentum client apps.
- Persist application data in PostgreSQL.
- Keep validation and business rules centralized in the backend.
- Support local development with Docker Compose for the API and database.
- Maintain a simple health endpoint for setup verification.

## Initial Backend Commands

Run the backend and database with Docker:

```bash
docker compose up --build
```

Run only the database for local Maven development:

```bash
docker compose up -d postgres
```

Run the backend service outside Docker:

```bash
./mvnw spring-boot:run
```

## Early Work Plan

- Confirm the core domain model before adding feature endpoints.
- Add API contracts only when the frontend workflow is clear.
- Keep database changes small and easy to verify.
- Add tests around behavior as endpoints and services are introduced.
