# AgroConnect

Smart workforce and land sharing platform for farmers, workers, and kavulu land seekers. Built with Spring Boot, MySQL, and session-based authentication.

## What it does
- Multi-role flows: FARMER, WORKER, KAVULU, ADMIN.
- Jobs: farmers post, workers apply, farmers select/reject; direct hire supported.
- Land sharing: farmers list land, kavulu submit requests, farmers approve/reject.
- Worker availability & profiles: workers expose availability; all roles can update profile data.
- Admin oversight: user moderation, job/land removal, simple analytics.
- Static landing assets live under `src/main/resources/static` for quick hosting.

## Stack & project layout
- Java 21, Spring Boot 3.4 (web, security, data-jpa), Maven build. See [pom.xml](pom.xml).
- Persistence: MySQL with JPA/Hibernate; schema bootstrapped from [src/main/resources/schema.sql](src/main/resources/schema.sql).
- Security: Spring Security with `BCryptPasswordEncoder`, session-based auth, role/authority checks. Config at [src/main/java/com/agroconnect/config/SecurityConfig.java](src/main/java/com/agroconnect/config/SecurityConfig.java).
- MVC: controllers in `controller`, business logic in `service`, entities in `model`, repositories in `repository`.

## Domain model
- `User`: roles, phone login, location, worker fields (`workType`, `experience`, `availableDays`), kavulu fields (`preferredCrop`), flags (`verified`, `suspended`).
- `Job`: farmer-owned task with `workType`, `durationDays`, `workersRequired`, location, `experienceRequired`.
- `JobApplication`: worker tied to job with `Status` (PENDING/SELECTED/REJECTED) and `Type` (APPLIED/DIRECT).
- `LandListing`: farmer land entry with `area`, `cropType`, `duration`.
- `LandRequest`: kavulu request to a land listing with status lifecycle.

## Security model
- Session-based authentication via `/api/auth/login` and `/api/auth/register`; credentials = phone + password.
- CSRF is ignored for `/api/**`. Static assets are public.
- Authorities enforced per endpoint: FARMER (post jobs/land, manage applications/requests), WORKER (apply, manage availability), KAVULU (request land), ADMIN (moderation and analytics). See [SecurityConfig](src/main/java/com/agroconnect/config/SecurityConfig.java).

## API surface (high-level)
- Auth: `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me`.
- Farm (jobs): `POST /api/farm/jobs`, `GET /api/farm/jobs`, `GET /api/farm/my-jobs`, `POST /api/farm/jobs/{jobId}/apply`, `GET /api/farm/my-applications`, `PUT /api/farm/applications/{appId}/select|reject`.
- Farm (land): `POST /api/farm/land`, `GET /api/farm/land`, `GET /api/farm/my-land`, `POST /api/farm/land/{landId}/request`, `PUT /api/farm/land-requests/{reqId}/approve|reject`.
- Workers: `GET /api/farm/workers`, `POST /api/farm/hire-worker/{workerId}`, `PUT /api/farm/worker/availability`.
- Profiles: `GET /api/farm/profile/current`, `PUT /api/farm/profile`.
- Admin: `GET /api/admin/analytics`, `GET /api/admin/users?role=...`, `PUT /api/admin/users/{userId}/suspend`, `DELETE /api/admin/users/{userId}`, `GET /api/admin/jobs`, `DELETE /api/admin/jobs/{jobId}`, `GET /api/admin/land`, `DELETE /api/admin/land/{landId}`.

## Data layer
Schema auto-applies on startup (`spring.sql.init.mode=always`). Key tables are defined in [schema.sql](src/main/resources/schema.sql). A seed admin is inserted:
- Phone: `0000000000`
- Password: `admin123`

## Configuration
Main settings live in [src/main/resources/application.properties](src/main/resources/application.properties):
- `spring.datasource.url` (default `jdbc:mysql://localhost:3306/agroconnect?...`)
- `spring.datasource.username` / `spring.datasource.password`
- `spring.jpa.hibernate.ddl-auto=update` and `spring.jpa.show-sql=true`
- `server.port=8080`

## Run it locally
1) Prereqs: Java 21, Maven, running MySQL with a database named `agroconnect`.
2) Update DB credentials in application.properties (or override via env vars).
3) Install deps & run: `mvn spring-boot:run`
4) App starts on http://localhost:8080. Static landing page served from `/`.

Build a jar: `mvn clean package` (artifact `target/agroconnect-1.0.0.jar`).

## Quick API tries (session cookies kept by curl `-c`/`-b`)
```bash
# Register a farmer
curl -c cookies.txt -H "Content-Type: application/json" -d '{"name":"Farmer A","phone":"1234567890","password":"pass","role":"FARMER","district":"X"}' http://localhost:8080/api/auth/register

# Login
curl -c cookies.txt -b cookies.txt -H "Content-Type: application/json" -d '{"phone":"1234567890","password":"pass"}' http://localhost:8080/api/auth/login

# Post a job (farmer)
curl -b cookies.txt -H "Content-Type: application/json" -d '{"workType":"Harvest","durationDays":3,"workersRequired":2}' http://localhost:8080/api/farm/jobs
```

## Testing
Run unit tests: `mvn test`.

## Notes
- Authentication is stateful; include session cookies on subsequent requests.
- Role-specific fields are respected on registration/profile updates (e.g., workers set `workType`, `availableDays`).
- Admin endpoints require the seeded admin or a user with `ADMIN` role.