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

Next recommended task: Frontend Phase 1 Run Screen.

Build the smallest frontend flow that can use the completed backend API:

- Add a `Run` entry point.
- Create a Run screen.
- Add `Start` and `Stop` states.
- Add a live timer.
- Track route points in memory.
- Calculate duration, distance, and average pace.
- Submit the completed run to `POST /api/runs`.
- Show a simple saved run summary.

Start with a simple UI and fake/manual route data if needed, then add real GPS with `geolocator` once the API contract is proven from the app.

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

### Phase 1: End-to-End MVP

Backend completed:

- Backend entities.
- Backend APIs.
- Flyway schema migration.
- DTO validation and clean error responses.
- Swagger endpoint documentation.
- Basic backend tests.

Frontend remaining:

- Run screen.
- Start and stop functionality.
- Timer.
- GPS collection.
- Save completed run.
- Show run summary.

Phase 1 is done when a user can complete a run on the app and see it saved through the backend.

### Phase 2: Route Map

Build later:

- Draw route on a map.
- View saved route.

### Phase 3: Run History

Build later:

- Run history screen.
- Run detail screen improvements.
- Better summaries and sorting.

## Implementation Checklist

Before generating code for each phase:

1. Explain the design.
2. Identify files to create.
3. Identify files to modify.
4. Write tests first for backend behavior.
5. Generate implementation code.
6. Verify integration end to end.

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
