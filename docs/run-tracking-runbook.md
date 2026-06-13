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

Frontend:

- Flutter runs on iOS Simulator.
- Flutter web is enabled.
- Frontend can communicate with the backend.

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
- `averagePace`

RoutePoint:

- `latitude`
- `longitude`
- `timestamp`

Relationship:

- One `Run` has many `RoutePoint` records.
- Every saved run contains its route points.

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

- `POST /runs`
- `GET /runs`
- `GET /runs/{id}`

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

Build only:

- Backend entities.
- Backend APIs.
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
