# Momentum API

Momentum is a full-stack application for building a personal productivity and progress tracking experience across mobile and web. This repository contains the backend service that will power the client apps with APIs, persistence, validation, and future business logic.

The backend is built with Spring Boot, Java 17, Maven, PostgreSQL, and Docker Compose for local app and database setup.

## Requirements

- Docker Desktop
- Java 17 and the Maven wrapper if running outside Docker

## Run With Docker

Build and start the API with PostgreSQL:

```bash
docker compose up --build
```

The API starts on `http://localhost:8080`.

PostgreSQL data is stored in the persistent Docker volume `momentum-postgres-data`.
Use `docker compose down` to stop containers without deleting database data. Do not
use `docker compose down -v` unless you intentionally want to delete the local
database.

## Run Locally Without Docker App

Start only PostgreSQL:

```bash
docker compose up -d postgres
```

Run the API:

```bash
./mvnw spring-boot:run
```

The API starts on `http://localhost:8080`.

## Health Check

```bash
curl http://localhost:8080/health
```

## Documentation

- [Initial plan](docs/initial-plan.md)
- [Digital Wellness runbook](docs/digital-wellness-runbook.md)
- [Personal Finance PRD and Technical Specification](docs/personal-finance-prd-technical-spec.md)
- [Expense Tracking runbook](docs/expense-tracking-runbook.md)
