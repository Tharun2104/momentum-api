# Momentum Run Tracking Runbook

## Purpose

This runbook captures the first real Momentum feature: run tracking. Use it as the reference point when implementation starts so the work stays MVP-focused, simple, and aligned with the current product direction.

Do not implement future optimizations until Phase 1 is complete and working end to end.

## Product Direction

Momentum is a running and fitness tracking platform similar to Strava, but intentionally simpler and easier to use.

Primary platforms:

- iOS as the first-class platform.
- Web.
- Android later through the same Flutter codebase.

Tech stack:

- Frontend: Flutter.
- Backend: Spring Boot with Java 17.
- Database: PostgreSQL.
- Local development: PostgreSQL in Docker.

## Current Foundation

Backend:

- Spring Boot app runs.
- PostgreSQL is connected.
- Docker is configured.
- Health endpoint exists.
- Flyway is configured and owns schema creation.
- Swagger/OpenAPI is available for local endpoint testing.
- Run tracking persistence API is implemented.

Frontend:

- Flutter runs on iOS Simulator.
- Flutter web is enabled.
- Frontend can communicate with the backend.

## Completed So Far

Backend Phase 1 foundation is complete:

- Created Flyway migration `V1__create_runs_and_route_points.sql`.
- Created `runs` and `route_points` tables.
- Added indexes for run start time and route point lookup/order.
- Added JPA entities: `Run` and `RoutePoint`.
- Added repositories: `RunRepository` and `RoutePointRepository`.
- Added DTOs for create requests and API responses.
- Added Jakarta Bean Validation on request DTOs.
- Added controller-level `@Valid` request validation.
- Added `RunService` with the cross-field business rule: `endTime` must be after `startTime`.
- Added global exception handling for validation, bad request, and not found errors.
- Added Swagger/OpenAPI documentation.
- Added backend tests for service behavior and controller validation.
- Verified Docker startup, Flyway validation, Swagger, and run API smoke tests.

Backend APIs currently available:

- `POST /api/runs`
- `GET /api/runs`
- `GET /api/runs/{id}`

Backend implementation style chosen:

- DTOs handle field-level request validation.
- Service handles business rules and orchestration.
- Entities are created with constructors instead of broad public setters.
- The `Run` entity owns adding/removing route points so the JPA relationship stays consistent.
- Keep comments small and only on important flows.

## Next Task

Next recommended task: Frontend Phase 1A Run Screen.

Build the smallest frontend flow that can use the completed backend API:

- Add a `Run` entry point.
- Create a Run screen.
- Add `Start` and `Stop` states.
- Add a live timer.
- Use a fake/manual route payload.
- Submit the completed run to `POST /api/runs`.
- Show a simple saved run summary.

Do Phase 1A before real GPS. Add `geolocator`, permissions, real route collection, and real distance/pace calculations in Phase 1B.

## Current Goal

Implement the first MVP feature: Run Tracking.

Target user flow:

1. User opens Momentum.
2. User taps `Run`.
3. Run screen opens.
4. User taps `Start`.
5. App starts a timer.
6. App starts collecting GPS locations.
7. App continuously tracks the route while the run is active.
8. User taps `Stop`.
9. App calculates duration, distance, and average pace.
10. App sends the completed run to the backend.
11. Backend stores the run and its route points.
12. User sees a run summary.

## MVP Scope

Build only:

- Start run.
- Stop run.
- GPS tracking.
- Route point collection.
- Distance calculation.
- Duration calculation.
- Average pace calculation.
- Run persistence.
- Basic run history API support.

Do not build yet:

- Authentication.
- Friends.
- Social features.
- Challenges.
- Achievements.
- Notifications.
- AI coaching.
- Watch support.
- Route map drawing.
- Saved route viewing.

## Data Model

Run:

- `id`
- `startTime`
- `endTime`
- `distanceMeters`
- `durationSeconds`
- `averagePaceSecondsPerKm`
- `createdAt`
- `updatedAt`

RoutePoint:

- `id`
- `runId`
- `latitude`
- `longitude`
- `recordedAt`
- `accuracyMeters`
- `sequenceNumber`
- `createdAt`

Relationship:

- One `Run` has many `RoutePoint` records.
- Every saved run contains its route points.
- Route points are ordered by `sequenceNumber`.

## GPS Requirements

Use Flutter package:

- `geolocator`

Rules:

- Use high accuracy tracking.
- Ignore poor accuracy readings.
- Collect route points only while the run is active.
- Store route points in memory while tracking.
- Do not continuously send location updates to the backend.
- Send the completed run only when the user presses `Stop`.

## Backend Requirements

Create:

- `Run` entity.
- `RoutePoint` entity.
- `RunRepository`.
- `RunService`.
- `RunController`.
- Request and response DTOs.

APIs:

- `POST /api/runs`
- `GET /api/runs`
- `GET /api/runs/{id}`

Backend rules:

- Use JPA relationships.
- Keep controllers thin.
- Put run persistence and validation orchestration in the service layer.
- Use DTOs at API boundaries.
- Follow TDD before adding production code.

## Frontend Requirements

Create a Run Screen with:

- Start button.
- Stop button.
- Live timer.
- Live distance.
- Live pace.

While running:

- Track route points in memory.
- Update duration, distance, and pace live.

After stop:

- Calculate final run statistics.
- Call backend API.
- Save the run.
- Show run summary.

## Phases

### Backend Phase 1: Completed

Completed:

- Backend entities.
- Backend APIs.
- Flyway schema migration.
- DTO validation and clean error responses.
- Swagger endpoint documentation.
- Basic backend tests.

### Frontend Phase 1A: Fake Route End-to-End

- Run screen.
- Start and stop functionality.
- Timer.
- Fake/manual route payload.
- Save completed run.
- Show run summary.

Phase 1A is done when the Flutter app can save a completed fake/manual run through `POST /api/runs` and show the saved summary.

### Frontend Phase 1B: Real GPS Tracking

- Add `geolocator`.
- Add GPS permissions.
- Collect real route points.
- Calculate real distance and pace.
- Save completed run.
- Show saved run summary.

Phase 1B is done when the app can record a real route, calculate real run stats, save it, and show the saved summary.

### Phase 2: Basic Run History

- Basic Run History screen using `GET /api/runs`.
- Show saved runs in a simple list.
- Keep sorting and display simple.

### Phase 3: Route Map And Run Detail

- Route map.
- Run detail screen.
- Saved route viewing.

## Implementation Checklist

Before generating code for each phase:

1. Explain the design.
2. Identify files to create.
3. Identify files to modify.
4. Write tests first for backend behavior.
5. Generate implementation code.
6. Verify integration end to end.

## New Chat Handoff

Use this section when continuing in a new chat.

Current project:

- Product: Momentum, a simple running and fitness tracking app.
- First feature: Run Tracking MVP.
- Backend repository: `momentum-api`.
- Backend package: `com.mttauto.momentum_api`.
- Frontend repository: `momentum-app`.
- Backend runs locally on `http://localhost:8080`.
- PostgreSQL runs in Docker.
- Local PostgreSQL host port is `5433`.
- Local database name is `momentum`.

Backend stack:

- Java 17.
- Spring Boot.
- Maven.
- Spring Data JPA.
- PostgreSQL.
- Flyway.
- Docker Compose.
- Swagger/OpenAPI.

Frontend stack:

- Flutter.
- iOS first.
- Web enabled.
- Android later from the same Flutter codebase.

Backend status:

- Backend run tracking persistence is implemented.
- Flyway migration creates `runs` and `route_points`.
- Hibernate schema mode is `validate`; do not use `ddl-auto=update`.
- DTOs are used at API boundaries.
- Jakarta Bean Validation is used on create request DTOs.
- `@Valid` is used in the controller.
- `RunService` handles orchestration and the cross-field rule that `endTime` must be after `startTime`.
- Entities use constructors for required state instead of broad public setters.
- `Run.addRoutePoint()` owns keeping the bidirectional JPA relationship in sync.
- Small comments exist only on important flows.

Backend API contract:

- `POST /api/runs`
- `GET /api/runs`
- `GET /api/runs/{id}`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health check: `GET /health`

Example `POST /api/runs` payload:

```json
{
  "startTime": "2026-06-13T10:00:00Z",
  "endTime": "2026-06-13T10:20:00Z",
  "distanceMeters": 3000,
  "durationSeconds": 1200,
  "averagePaceSecondsPerKm": 400,
  "routePoints": [
    {
      "latitude": 40.7128,
      "longitude": -74.006,
      "recordedAt": "2026-06-13T10:00:10Z",
      "accuracyMeters": 5,
      "sequenceNumber": 1
    }
  ]
}
```

Backend commands:

```bash
docker compose up --build
```

```bash
docker compose up -d postgres
```

```bash
./mvnw spring-boot:run
```

```bash
./mvnw test
```

Frontend next task:

- Work on `momentum-app`.
- Implement Frontend Phase 1A first.
- Do not add real GPS yet.
- Create the Run screen and start/stop/timer flow.
- Use a fake/manual route payload to verify the frontend can call `POST /api/runs`.
- Show the saved run summary from the backend response.

Important guardrails for the next chat:

- Do not add authentication yet.
- Do not add friends, social features, challenges, achievements, notifications, AI coaching, or watch support.
- Do not build route maps in Phase 1A or 1B.
- Keep UI and code simple.
- Avoid over-engineering.
- Follow existing project structure.
- For backend changes, follow TDD and keep controllers thin.
- For frontend changes, keep Phase 1A focused on proving the end-to-end save flow before adding real GPS.

## Guardrails

- Stay MVP-focused.
- Do not add auth.
- Do not add social features.
- Do not add map drawing in Phase 1.
- Do not optimize before the basic flow works.
- Prefer simple code over clever abstractions.
- Keep variable names clear and domain-specific.
- Avoid unnecessary branching and nested `if` statements.
- Keep implementation easy to read and easy to change.
