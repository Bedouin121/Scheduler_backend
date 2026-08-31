# Campaign Scheduler Backend, Full Build Spec

## Purpose and framing

Build a small Spring Boot backend for a campaign and messaging scheduler, similar in spirit to Optimizely Campaign. A logged in user creates campaigns, adds recipients to a campaign, schedules a message for it, and can trigger a manual "run due sends now" action that simulates delivery and writes a send log. No real email is sent. This is a portfolio project meant to be finished, deployed, and demoed quickly, not a production system, so favor working, testable code over completeness.

## Tech stack, do not deviate

* Java 21
* Spring Boot 3
* Maven as the build tool
* PostgreSQL as the database
* Spring Data JPA for persistence
* Spring Security with JWT for authentication, no OAuth, no session based login
* Lombok for getters, setters, and constructors
* JUnit 5 and Mockito for tests
* Bean Validation, the jakarta.validation annotations, on request DTOs

## Entities

**User**
id, Long, primary key
email, String, unique, not null
password, String, stores a bcrypt hash, not null
createdAt, Instant

**Campaign**
id, Long, primary key
name, String, not null
status, enum, values DRAFT, ACTIVE, COMPLETED, default DRAFT
owner, many to one User, not null
createdAt, Instant

**Recipient**
id, Long, primary key
name, String, not null
email, String, not null
campaign, many to one Campaign, not null

**Message**
id, Long, primary key
content, String, not null, use a text column type
scheduledTime, Instant, not null
campaign, many to one Campaign, not null
sent, boolean, default false

**SendLog**
id, Long, primary key
recipient, many to one Recipient, not null
message, many to one Message, not null
status, enum, values SENT, FAILED
timestamp, Instant

Cascade delete rules, deleting a Campaign should delete its Recipients and Messages, deleting a Message or Recipient should delete related SendLog rows. Enforce this with cascade settings on the JPA relationships, not manual cleanup code.

## File structure

```
backend/
  pom.xml
  Dockerfile
  render.yaml
  .gitignore
  README.md
  src/
    main/
      java/com/optimizely/scheduler/
        SchedulerApplication.java
        config/
          SecurityConfig.java
          CorsConfig.java
        controller/
          AuthController.java
          CampaignController.java
          RecipientController.java
          MessageController.java
          SendController.java
        dto/
          AuthRequest.java
          AuthResponse.java
          CampaignRequest.java
          CampaignResponse.java
          RecipientRequest.java
          MessageRequest.java
          SendLogResponse.java
        entity/
          User.java
          Campaign.java
          Recipient.java
          Message.java
          SendLog.java
        repository/
          UserRepository.java
          CampaignRepository.java
          RecipientRepository.java
          MessageRepository.java
          SendLogRepository.java
        service/
          AuthService.java
          CampaignService.java
          RecipientService.java
          MessageService.java
          SendService.java
        security/
          JwtUtil.java
          JwtFilter.java
        seed/
          DataSeeder.java
      resources/
        application.yml
        application-prod.yml
    test/
      java/com/optimizely/scheduler/service/
        CampaignServiceTest.java
        SendServiceTest.java
```

Controllers stay thin, they only translate HTTP in and out and call a service. All business logic lives in the service layer so it is unit testable without booting the whole web stack. DTOs are always used at the controller boundary, entities are never returned directly from a controller, so internal fields like a password hash can never leak into a response.

## API endpoints

All endpoints except register and login require a valid JWT in the Authorization header as a Bearer token. Every campaign scoped endpoint must check that the campaign belongs to the authenticated user and return a 403 or 404 if it does not, never leak another user's data.

**Auth**
POST /api/auth/register, body email and password, creates a user, returns 201, no token yet, this is registration only
POST /api/auth/login, body email and password, returns a JWT and its expiry

**Campaigns**
POST /api/campaigns, body name, creates a campaign owned by the current user, status defaults to DRAFT
GET /api/campaigns, lists only the current user's campaigns
GET /api/campaigns/id, returns one campaign with its recipients and messages nested in the response
PUT /api/campaigns/id, body name and or status, updates the campaign
DELETE /api/campaigns/id, deletes the campaign and cascades

**Recipients**
POST /api/campaigns/campaignId/recipients, body name and email, adds a recipient to the campaign
GET /api/campaigns/campaignId/recipients, lists recipients for the campaign
DELETE /api/recipients/id, deletes a single recipient

**Messages**
POST /api/campaigns/campaignId/messages, body content and scheduledTime, schedules a message on the campaign
GET /api/campaigns/campaignId/messages, lists messages for the campaign

**Manual send trigger**
POST /api/send/run, no body, scoped to the current user, finds every message across the user's campaigns where scheduledTime is at or before now and sent is false, for each such message writes one SendLog row per recipient in that message's campaign with status SENT, then flips sent to true on the message, returns the list of SendLog rows created by this call so the frontend can render what just happened

GET /api/send/logs, lists all SendLog rows across the current user's campaigns, most recent first

## Auth and security details

Passwords are hashed with BCryptPasswordEncoder before saving, never store or log a plain password. JwtUtil generates a token containing the user id and email, with a reasonable expiry, 24 hours is fine for a demo. JwtFilter runs once per request, reads the Authorization header, validates the token, and sets the authenticated user on the security context, so controllers and services can fetch the current user without re parsing the token themselves. SecurityConfig marks POST /api/auth/register and POST /api/auth/login as public, everything else requires authentication, and disables CSRF since this is a stateless token based API, not a form based one.

## CORS

CorsConfig must allow requests from the deployed Vercel frontend origin and from localhost for local development. Read the allowed origin from an environment variable so the same code works locally and in production without editing source.

## Seed data

DataSeeder is a CommandLineRunner bean that runs on startup and only inserts data if the users table is empty, so it never duplicates data on every restart. It creates one demo user, for example demo at example dot com with a known password, one demo campaign owned by that user, three or four fake recipients on that campaign, and one message scheduled a few minutes in the past so the manual trigger has something to act on immediately after deployment.

## Configuration files

application.yml is for local development, it points at a local PostgreSQL instance with hardcoded local credentials, this file is fine to commit since it holds no real secrets.

application-prod.yml is the profile that runs on Render, it must read the database URL, username, password, the JWT signing secret, and the allowed CORS origin entirely from environment variables, never hardcode any of these. Render injects the Postgres connection details automatically once a Postgres instance is attached to the web service in its dashboard, and the JWT secret and CORS origin should be set as custom environment variables in the Render dashboard.

## Testing requirements

CampaignServiceTest, using Mockito to mock CampaignRepository, test that creating a campaign sets the correct owner and default status, and that listing campaigns only returns the current user's campaigns.

SendServiceTest, using Mockito to mock the repositories, test that the manual trigger only picks up messages where scheduledTime has passed and sent is false, that it creates exactly one SendLog row per recipient, and that it correctly flips sent to true afterward so a second call does not resend the same message.

## Deployment target, Render

Build command, mvn clean package
Start command, java -jar target/scheduler-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
A Dockerfile is optional, only needed if the default JVM memory settings overshoot Render's free tier 512MB limit, in which case set explicit JVM flags like -Xmx400m in the start command or Dockerfile entrypoint.
render.yaml is optional, it can define the web service and a Postgres instance as code instead of manual dashboard setup, add it once the manual setup is confirmed working.

## Explicit non goals

Do not implement real email sending, the manual trigger only writes SendLog rows.
Do not implement a real time background scheduler, the point of the manual trigger is to make the demo interactive without waiting.
Do not add roles or permissions beyond simple ownership checks, a user can only see and act on their own campaigns, nothing more granular is needed.
Do not add pagination, filtering, or search, the dataset is small and seeded, keep the API surface exactly as listed above.

## Definition of done

The application starts locally against a local Postgres with the seed data present. Every endpoint listed above works correctly when tested with Postman or curl, including the manual send trigger producing new SendLog rows on a fresh deploy. Both service level tests pass. The application deploys successfully to Render with a Postgres instance attached, and the deployed API responds correctly to a login followed by a call to GET /api/campaigns using the returned token.
