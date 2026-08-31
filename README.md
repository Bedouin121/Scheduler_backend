# Campaign Scheduler Backend

A small Spring Boot backend for a campaign and messaging scheduler, in the spirit of Optimizely Campaign. A logged-in user creates campaigns, adds recipients, schedules messages, and triggers a manual "run due sends now" action that simulates delivery by writing send logs. No real email is sent; everything is designed to be run, tested, and demoed quickly.

## Tech stack

- Java 21, Spring Boot 3
- Maven build
- PostgreSQL database with Spring Data JPA
- Spring Security with JWT authentication (stateless, no OAuth, no sessions)
- Lombok, Bean Validation (Jakarta), JUnit 5 + Mockito

## Getting started locally

1. **Database** — create a local PostgreSQL database named `campaign_scheduler`, or change `src/main/resources/application.yml` to match your local credentials. The local config uses `postgres` / `postgres` on `localhost:5432`.

2. **Run** the application:

   ```bash
   mvn spring-boot:run
   ```

   On startup the `DataSeeder` inserts a demo dataset if the `users` table is empty:
   - User: `demo@example.com` / `password`
   - One campaign ("Product Launch") with four recipients
   - One message scheduled a few minutes in the past, so `POST /api/send/run` has something to send immediately

3. **Test** the API:

   ```bash
   curl -s -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"demo@example.com","password":"password"}'
   ```

   Use the returned token in an `Authorization: Bearer <token>` header for all other endpoints.

## API

All endpoints except `POST /api/auth/register` and `POST /api/auth/login` require a valid JWT in the `Authorization` header. Every campaign-scoped endpoint checks that the campaign belongs to the authenticated user and returns `404` if it does not, so one user can never read or modify another's data.

### Auth

| Method | Path | Body | Description |
| ------ | ---- | ---- | ----------- |
| POST | `/api/auth/register` | `{ "email", "password" }` | Create an account, returns the user (no token) |
| POST | `/api/auth/login` | `{ "email", "password" }` | Returns `{ "token", "expiresIn" }` |

### Campaigns

| Method | Path | Body | Description |
| ------ | ---- | ---- | ----------- |
| POST | `/api/campaigns` | `{ "name", "status"? }` | Create a campaign, default status `DRAFT` |
| GET | `/api/campaigns` | — | List the current user's campaigns |
| GET | `/api/campaigns/{id}` | — | One campaign with its recipients and messages nested |
| PUT | `/api/campaigns/{id}` | `{ "name", "status"? }` | Update name and/or status |
| DELETE | `/api/campaigns/{id}` | — | Delete the campaign and cascade |

### Recipients

| Method | Path | Body | Description |
| ------ | ---- | ---- | ----------- |
| POST | `/api/campaigns/{campaignId}/recipients` | `{ "name", "email" }` | Add a recipient |
| GET | `/api/campaigns/{campaignId}/recipients` | — | List recipients for a campaign |
| DELETE | `/api/recipients/{id}` | — | Delete a single recipient |

### Messages

| Method | Path | Body | Description |
| ------ | ---- | ---- | ----------- |
| POST | `/api/campaigns/{campaignId}/messages` | `{ "content", "scheduledTime" }` | Schedule a message |
| GET | `/api/campaigns/{campaignId}/messages` | — | List messages for a campaign |

### Manual send trigger

| Method | Path | Body | Description |
| ------ | ---- | ---- | ----------- |
| POST | `/api/send/run` | — | Find every unsent message with `scheduledTime <= now`, write one `SENT` send log per recipient, mark messages sent. Returns the logs created. |
| GET | `/api/send/logs` | — | All send logs across the user's campaigns, most recent first |

`scheduledTime` is an ISO-8601 instant, e.g. `"2026-08-31T12:00:00Z"`.

## Configuration

- `application.yml` — local development defaults. Postgres credentials and the JWT secret are readable from environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `ALLOWED_ORIGIN`) with convenient local-only fallbacks baked in, so nothing sensitive is hardcoded. Safe to commit and run anywhere. Schema is `create-drop`.
- `application-prod.yml` — production profile (used on Render). Reads the database, JWT secret, and CORS origin entirely from environment variables: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `ALLOWED_ORIGIN`. It builds the JDBC URL (`jdbc:postgresql://...`) from Render's individual database fields, so it never has to parse a combined `postgres://...` connection string. Schema is `update` so the first deploy creates the tables.
- CORS allows the origin from `allowed-origin` plus `http://localhost:3000`, so the same build serves local development and the deployed frontend.

## Tests

Service-layer unit tests use Mockito with repositories mocked, so no database is required:

```bash
mvn test
```

- `CampaignServiceTest` — creating a campaign assigns the correct owner and default status; listing returns only the current user's campaigns.
- `SendServiceTest` — the manual trigger only processes due, unsent messages; writes exactly one log per recipient; flips `sent` so a second call does not resend.

## Deploying to Render

The repository includes a `Dockerfile` and a `render.yaml` blueprint.

**Recommended: Blueprint deploy**

1. Push the repository to GitHub and connect it to Render.
2. Create a new service and choose **Blueprint** — Render provisions the Docker web service and the PostgreSQL instance from `render.yaml`.
3. Set `ALLOWED_ORIGIN` to your deployed frontend origin.
4. No database string work needed: the web service pulls `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, and `DB_PASSWORD` from Render's individual database fields, and `application-prod.yml` assembles the JDBC URL from them.

**Manual setup**

1. Create a PostgreSQL instance and attach it to the web service in the dashboard.
2. Set the start command: `java -Xms128m -Xmx384m -jar target/scheduler-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod`
3. Set custom env vars: `JWT_SECRET`, `ALLOWED_ORIGIN`, and the database credentials.

Either way, after the first deploy logs in with `demo@example.com` / `password`, calls `GET /api/campaigns` with the returned token to confirm it works. The seeded message is already in the past, so `POST /api/send/run` produces send logs on the very first call.

> **JVM memory.** The Dockerfile and manual start command cap the heap with `-Xmx384m` so the JVM stays within Render's free-tier 512 MB limit.

## Project structure

```
src/main/java/com/optimizely/scheduler/
  SchedulerApplication.java
  config/        Security, CORS, global exception handling
  controller/    Thin HTTP layer, calls services
  dto/           Request/response objects used at the boundary
  entity/        JPA entities with cascade delete rules
  exception/     NotFoundException and error mapping
  repository/    Spring Data repositories
  security/      JWT generation and filter
  seed/          DataSeeder startup demo data
  service/       Business logic, unit-testable
src/test/java/com/optimizely/scheduler/service/
  CampaignServiceTest.java
  SendServiceTest.java
```

## Non-goals

This is a working, testable demo, not a production mailer. It does not send real email, run background schedulers, or add roles beyond simple ownership checks. The dataset is small and seeded, so the API surface is intentionally kept minimal.
