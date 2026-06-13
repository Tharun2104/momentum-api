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
- Run deletion API is implemented.

Frontend:

- Flutter runs on iOS Simulator.
- Flutter web is enabled.
- Frontend can communicate with the backend.
- Flutter runs on a physical iPhone with Xcode signing configured.
- Frontend can save runs from Chrome, iOS Simulator, and physical iPhone.
- Physical iPhone local backend testing uses the Mac LAN IP through `API_BASE_URL`.
- Physical iPhone remote local testing works through ngrok by passing the tunnel URL as `API_BASE_URL`.
- Saved run history supports deleting runs from the UI.
- Saved Run Detail supports viewing a completed route on a map.
- Run screen supports a live map while tracking.
- Run screen shows pre-start GPS status and GPS diagnostics.

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
- `DELETE /api/runs/{id}`

Backend implementation style chosen:

- DTOs handle field-level request validation.
- Service handles business rules and orchestration.
- Entities are created with constructors instead of broad public setters.
- The `Run` entity owns adding/removing route points so the JPA relationship stays consistent.
- Keep comments small and only on important flows.

Frontend Phase 1A and Phase 1B are complete:

- Home screen has a `Run` entry point.
- Run screen has `Start` and `Stop` states.
- Live timer exists.
- Phase 1A fake/manual route payload works for end-to-end save testing.
- Phase 1B real GPS tracking works on physical iPhone.
- `geolocator` is installed.
- iOS When-In-Use location permission text is configured.
- Real route points are collected in memory while tracking.
- Route points are filtered by accuracy and minimum movement.
- Live distance and pace are calculated from accepted route points.
- Completed runs are sent once to `POST /api/runs` on `Stop`.
- Saved run summary is shown after backend persistence succeeds.

Frontend Phase 1C and Phase 2A are complete:

- History screen lists saved runs from `GET /api/runs`.
- Run Detail screen loads a saved run from `GET /api/runs/{id}`.
- Run Detail shows saved run stats and route point count.
- Run Detail renders saved route points on an OpenStreetMap-based `flutter_map` map.
- Saved route map draws the route polyline.
- Saved route map shows start and end markers.
- Saved route map shows an empty state when fewer than 2 valid route points exist.
- History screen supports deleting a run with a confirmation dialog.
- Deleting a run calls `DELETE /api/runs/{id}` and refreshes history after success.

Frontend Phase 2B and Phase 2C are complete:

- Run screen shows a live route map while tracking.
- Live route map uses OpenStreetMap through `flutter_map`.
- Live route polyline is drawn from accepted GPS route points only.
- Live route map shows start and current-location markers while running.
- Live route map can show an end marker after stop.
- Live map auto-centering follows the latest accepted point while running.
- A `GpsSignalStatus` model exists with `dead`, `searching`, `weak`, and `good`.
- Run screen checks GPS service and permission status before Start.
- Start is enabled for Searching, Weak, and Good GPS.
- Start is disabled only for Dead/unavailable GPS states.
- GPS status/helper messages are visible before and during runs.
- Accepted GPS point count, rejected GPS point count, and latest accuracy are displayed.
- Accepted route points are filtered for valid coordinates, accuracy, near-duplicate movement, and unrealistic speed.
- Rejected GPS points can update status/accuracy diagnostics but do not affect distance, pace, live map, sequence numbers, or saved payload.
- Save flow still posts the completed run once on Stop.

Local backend URL configuration is complete:

- App backend URL is configured through `API_BASE_URL`.
- Default local backend URL is `http://localhost:8080`.
- Chrome and iOS Simulator can use `http://localhost:8080`.
- Physical iPhone on same Wi-Fi can use `http://<MAC_IP>:8080`.
- Physical iPhone off Wi-Fi can use an ngrok HTTPS tunnel URL.

Important testing note:

- Current backend integration tests use the configured local PostgreSQL database and clear run data in setup.
- Do not run backend tests against a local database containing manual run history you want to keep.
- Before future backend test work, prefer an isolated test database/profile or confirm that deleting local dev data is acceptable.

## Next Task

Next recommended task: choose the next post-MVP run tracking improvement.

Phase 1C, Phase 2A, Phase 2B, Phase 2C, and Phase 2D are complete. Saved GPS runs can now be reviewed in the app with a saved route map, active runs show a live route map, GPS status/quality filtering is in place, and active runs can be paused and resumed.

Good next candidates:

- Better release/profile testing workflow for physical iPhone performance.
- Isolated backend test database/profile so tests do not mutate local manual history.
- Route map polish and interaction improvements.
- More detailed manual QA pass for long real-world runs.

Do not change the backend unless absolutely required.

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
- Basic run delete API support.
- Saved route viewing on Run Detail.
- Live map while running.
- GPS quality filters and pre-start GPS status.
- Pause and resume run tracking.

Do not build yet:

- Authentication.
- Friends.
- Social features.
- Challenges.
- Achievements.
- Notifications.
- AI coaching.
- Watch support.
- Route replay.
- Route editing.

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

- Use `LocationAccuracy.bestForNavigation` for foreground tracking.
- Use `distanceFilter = 5`.
- Ignore poor accuracy readings above `20` meters.
- Ignore duplicate or near-duplicate accepted points below `3` meters.
- Collect route points only while the run is active.
- Store route points in memory while tracking.
- Do not continuously send location updates to the backend.
- Send the completed run only when the user presses `Stop`.
- Do not request Always location permission in Phase 1.
- Do not implement background tracking in Phase 1.

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
- `DELETE /api/runs/{id}`

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

Status: Completed.

- Run screen.
- Start and stop functionality.
- Timer.
- Fake/manual route payload.
- Save completed run.
- Show run summary.

Phase 1A is done when the Flutter app can save a completed fake/manual run through `POST /api/runs` and show the saved summary.

### Frontend Phase 1B: Real GPS Tracking

Status: Completed.

- Add `geolocator`.
- Add iOS When-In-Use GPS permission text.
- Collect real route points.
- Calculate real distance and pace.
- Save completed run.
- Show saved run summary.
- Keep Chrome available as a sample-route smoke-test mode.
- Keep real GPS verification focused on physical iPhone.

Phase 1B is done when the app can record a real route, calculate real run stats, save it, and show the saved summary.

### Frontend Phase 1C: Run History And Run Detail

Status: Completed.

- Basic Run History screen using `GET /api/runs`.
- Show saved runs in a simple list.
- Keep sorting and display simple.
- Add navigation from Home or Run summary to history.
- Run Detail screen using `GET /api/runs/{id}`.
- Show saved run details and route point count.

### Frontend Phase 2A: Saved Route Map

Status: Completed.

- Saved route map on Run Detail.
- OpenStreetMap tiles through `flutter_map`.
- `latlong2` route point conversion.
- Route points sorted by `sequenceNumber` before drawing.
- Route polyline.
- Start and end markers.
- Empty state for runs with fewer than 2 valid route points.

### Frontend Phase 2B: Live Map While Running

Status: Completed.

- Add a live map to the Run screen.
- Use `flutter_map` and `latlong2`; do not introduce a new map provider.
- Use the same accepted GPS route points for distance, pace, live polyline, and final backend save.
- Show a placeholder before GPS starts and while waiting for the first accepted point.
- Draw a route polyline when at least 2 accepted points exist.
- Show a start marker at the first accepted point.
- Show a current location marker at the latest accepted point while running.
- Show an end marker after stop if simple.
- Keep auto-centering simple: center on the first accepted point, then follow the latest accepted point while running.
- Preserve existing timer, distance, pace, GPS filtering, and one-shot save flow.
- Do not send live GPS points to the backend.

### Frontend Phase 2C: GPS Quality Filters And Pre-Start GPS Status

Status: Completed.

- Add a simple GPS signal status model: Dead, Searching, Weak, Good.
- On Run screen load, check location service and permission status before Start.
- Keep Start enabled for Searching, Weak, and Good.
- Disable Start only for Dead/unavailable GPS states such as disabled service, denied permission, denied forever permission, or fatal location setup errors.
- Show clear GPS status/helper text before and during runs.
- Show accepted point count, rejected point count, and latest accuracy.
- Reject route point candidates with null/non-finite accuracy or accuracy greater than `20` meters.
- Reject near-duplicate candidates less than `3` meters from the last accepted point.
- Reject impossible GPS jumps where elapsed time is not positive or speed is greater than `8.0` meters per second.
- Reject invalid coordinates outside valid latitude/longitude ranges.
- Let rejected points update GPS status/accuracy, but never distance, pace, live map, sequence numbers, or saved payload.
- Preserve the completed live map and one-shot save flow.
- Do not change backend schema or APIs.

### Frontend Phase 2D: Pause And Resume Run Tracking

Status: Completed.

- Add a simple run tracking state model if useful: Idle, Running, Paused, Saving, Completed.
- Idle shows Start.
- Running shows Pause and Stop.
- Paused shows Resume and Stop.
- Saving shows a disabled/loading state.
- Pause must stop active-duration accumulation.
- Resume must continue active duration from the previous total.
- Paused time must not count toward saved duration or average pace.
- GPS updates may still arrive while paused, but must not affect route points, distance, pace, map, sequence numbers, or saved payload.
- Do not clear route points on Pause.
- Do not reset timer or distance on Pause.
- Do not call the backend on Start, Pause, or Resume.
- Call `POST /api/runs` only on Stop.
- Prevent distance jumps after Resume so movement during Pause is not counted.
- Keep the existing in-memory accepted route point list for MVP.
- Preserve live map and save flow behavior.
- Do not change backend schema or APIs.

Completed behavior:

- Run screen now uses a simple run tracking state model: Idle, Running, Paused, Saving, Completed.
- Running state shows Pause and Stop controls.
- Paused state shows Resume and Stop controls.
- Active duration excludes paused time.
- Average pace uses active duration only.
- GPS status can still update while paused.
- Paused GPS points are not accepted and do not affect route, distance, pace, map, sequence numbers, or saved payload.
- Resume skips distance for the first accepted point after pause so paused movement is not counted.
- Route points remain in memory for the active run.
- Backend is still called only once on Stop.

### History Delete

Status: Completed.

- `DELETE /api/runs/{id}` backend API.
- History delete icon on each saved run.
- Confirmation dialog before deletion.
- Per-run loading indicator while deleting.
- Success and failure snackbar feedback.
- History refresh after successful deletion.

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
- `geolocator` for foreground iPhone GPS tracking.

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
- `DELETE /api/runs/{id}`
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

Frontend status:

- `momentum-app` implements Phase 1A fake route save flow.
- `momentum-app` implements Phase 1B real GPS tracking on physical iPhone.
- `momentum-app` implements Phase 1C run history and run detail.
- `momentum-app` implements Phase 2A saved route map on Run Detail.
- `momentum-app` implements Phase 2B live map while running.
- `momentum-app` implements Phase 2C GPS quality filters and pre-start GPS status.
- `momentum-app` implements Phase 2D pause and resume run tracking.
- `momentum-app` implements deleting runs from History.
- `tool/run_phone.sh` runs the app on the connected iPhone and passes `API_BASE_URL`.
- `tool/run_chrome.sh` runs the app in Chrome.
- `tool/run_simulator.sh` runs the app in the iOS Simulator.
- Physical iPhone testing requires Apple development signing in Xcode.
- Real phone local backend testing currently assumes the iPhone can reach the Mac backend over the network.
- When phone and Mac are on the same Wi-Fi, use the Mac LAN IP, for example `http://192.168.1.9:8080`.
- When phone and Mac are not on the same Wi-Fi, use an ngrok HTTPS tunnel and pass it as `API_BASE_URL`.
- Chrome and iOS Simulator can use `http://localhost:8080`.

Frontend commands:

```bash
cd "../momentum-api"
docker compose up -d
```

```bash
cd "../momentum-app"
./tool/run_phone.sh
```

```bash
./tool/run_chrome.sh
```

```bash
./tool/run_simulator.sh
```

```bash
flutter analyze
flutter test
```

Frontend next task:

- Work on `momentum-app`.
- Choose the next post-MVP run tracking improvement.
- Recommended candidates: release/profile phone testing workflow, isolated backend test DB, route map polish, or longer real-world run QA.
- Keep the backend API contract unchanged.

Important guardrails for the next chat:

- Do not add authentication yet.
- Do not add friends, social features, challenges, achievements, notifications, AI coaching, or watch support.
- Do not build route maps in Phase 1A or 1B.
- Saved route maps are complete for Run Detail, and Phase 2B live Run screen maps are complete.
- Phase 2C GPS quality filters and pre-start GPS status are complete.
- Phase 2D pause/resume is complete and does not add backend partial-save APIs or call the backend on Pause/Resume.
- Keep UI and code simple.
- Avoid over-engineering.
- Follow existing project structure.
- For backend changes, follow TDD and keep controllers thin.
- For frontend changes, preserve the completed Phase 1A, Phase 1B, Phase 1C, Phase 2A, Phase 2B, Phase 2C, and Phase 2D behavior.

## Completed Versus Not Completed

Completed:

- Backend run persistence API.
- Backend validation and exception handling.
- PostgreSQL Docker local development.
- Flyway schema ownership.
- Frontend Home screen with Run entry point.
- Frontend Run screen.
- Start/Stop flow.
- Live timer.
- Fake route save flow for Chrome/testing.
- Real foreground GPS tracking on physical iPhone.
- Location permission request/check flow.
- GPS point filtering.
- Live distance and average pace.
- One-shot run save on Stop.
- Saved run summary.
- Basic run history screen.
- Run detail screen.
- Saved route map on Run Detail.
- Live map while running.
- Run deletion from History.
- Pre-start GPS status.
- GPS signal status model.
- Pause and resume run tracking.
- Helper scripts for phone, Chrome, and simulator testing.
- Frontend README local setup docs.

Not completed:

- Route replay.
- Route editing.
- Isolated backend test database/profile.
- Background GPS tracking.
- Always location permission.
- Authentication.
- Social features.
- Challenges.
- Achievements.
- Notifications.
- AI coaching.
- Watch support.
- Android-specific validation.

## Local Phone Testing Notes

Current local phone setup:

- Backend runs on the Mac.
- iPhone runs the Flutter app.
- The iPhone cannot use `localhost` to reach the Mac backend.
- The iPhone must use a reachable backend URL, usually the Mac Wi-Fi IP.

Same Wi-Fi testing:

1. Put Mac and iPhone on the same Wi-Fi.
2. Start backend:

```bash
cd "../momentum-api"
docker compose up -d
```

3. Verify backend from Mac:

```bash
curl http://localhost:8080/health
```

4. Run phone app:

```bash
cd "../momentum-app"
./tool/run_phone.sh
```

Testing without same Wi-Fi:

- Use Chrome or iOS Simulator on the Mac with `./tool/run_chrome.sh` or `./tool/run_simulator.sh`.
- Deploy the backend to a real remote environment and point the app to that URL.
- USB alone does not automatically let the iPhone reach the Mac's `localhost:8080`.

## Guardrails

- Stay MVP-focused.
- Do not add auth.
- Do not add social features.
- Do not add map drawing to Phase 1 flows beyond the completed saved route map and the Phase 2B live run map.
- Do not optimize before the basic flow works.
- Prefer simple code over clever abstractions.
- Keep variable names clear and domain-specific.
- Avoid unnecessary branching and nested `if` statements.
- Keep implementation easy to read and easy to change.
