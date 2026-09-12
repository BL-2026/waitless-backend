# Waitless Backend

Spring Boot backend for a restaurant/cafe "call waiter" app.

Customers scan a QR code at their table and call a waiter or request the bill — no login.
accounts manage stores, tables, menus and staff from a Flutter app, authenticated with Firebase Auth.

## Stack

Java 17 · Spring Boot 3.5 · PostgreSQL 16 · Flyway · Spring Security + Firebase Admin SDK · STOMP over WebSocket · Lombok

## Running locally

```bash
docker compose up -d      # PostgreSQL on localhost:5432
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The `local` profile is what enables mock auth, permissive CORS and debug logging. Those used
to be the defaults, but that meant a deployment which forgot to set `FIREBASE_MOCK_ENABLED`
silently accepted a hardcoded bearer token as the venue owner. The defaults now fail closed
and development opts in. Without the profile, authenticated endpoints return `503` until you
configure real Firebase credentials.

Flyway applies `src/main/resources/db/migration` on startup. Hibernate runs with
`ddl-auto: validate` and never modifies the schema — every change goes in a new migration file.

### Environment variables

All have local defaults, so the app starts with no configuration beyond `docker compose up`.

| Variable | Default                                     | Notes |
| --- |---------------------------------------------| --- |
| `PORT` | `8081`                                      | Managed hosts inject this |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/waitless` | Matches `docker-compose.yml` |
| `SPRING_DATASOURCE_USERNAME` | `bl2026`                                    | |
| `SPRING_DATASOURCE_PASSWORD` | `bl2026`                                    | |
| `DB_POOL_MAX` | `5`                                         | Free Postgres plans allow few connections |
| `FIREBASE_CREDENTIALS_JSON` | *(empty)*                                   | The service account JSON, raw or base64 |
| `FIREBASE_CREDENTIALS_PATH` | *(empty)*                                   | Path to the same JSON; `JSON` wins if both are set |
| `FIREBASE_MOCK_ENABLED` | `false`                                     | `true` accepts a fixed bearer token as the owner |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173`                     | Comma-separated; patterns allowed |
| `LOG_LEVEL` | `INFO`                                      | For the `com.bl2026` package |

`FIREBASE_CREDENTIALS_PATH` accepts a plain filesystem path, or a Spring resource URL
(`file:`, `classpath:`). `FIREBASE_CREDENTIALS_JSON` carries the file's contents instead,
which is what managed hosts need since they give you environment variables rather than a
filesystem to drop a key onto. It accepts base64 as well as raw JSON, because the private
key's newlines rarely survive being pasted into a dashboard field.

**With neither set, Firebase verification is disabled**: public endpoints work normally and
authenticated endpoints return `503`. This keeps the app runnable for customer-side work
without a key.

### Tests

```bash
mvn test
```

`SchemaMigrationTest` starts an embedded PostgreSQL (downloaded once, ~50 MB) and boots the
full context, so Flyway migrations and JPA entities are checked against each other. Delete the
test and the `io.zonky.test:embedded-postgres` dependency if you'd rather not have that.

## API

Public — called by the customer web app, no `Authorization` header:

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/tables/{qrToken}` | Resolve a QR token to its table, store and menu |
| `POST` | `/api/requests` | Create a service request (`storeId`, `tableNumber`, `qrToken`, `type`, `paymentMethod`) |

`POST /api/requests` is unauthenticated, so `storeId` and `tableNumber` are treated as a
claim, not as identity: the store and table are resolved from `qrToken` alone and the
request is rejected with `403` when the body disagrees. Store ids are visible to any
customer, so accepting them as identity would let one tenant's guests create requests
against another's tables.

Account-facing — require `Authorization: Bearer <Firebase ID token>`:

| Method | Path                                    | Purpose |
| --- |-----------------------------------------| --- |
| `POST` | `/api/accounts`                                | Create the account on first login (idempotent on `firebaseUid`) |
| `GET` | `/api/account/me`                       | Current account |
| `POST` / `GET` | `/api/stores`                           | Create / list the account's stores |
| `POST` / `GET` | `/api/stores/{storeId}/tables`          | Add a table (`qrToken` generated server-side) / list |
| `POST` / `GET` | `/api/stores/{storeId}/menu-items`      | Add / list menu items |
| `POST` / `GET` | `/api/stores/{storeId}/staff`           | Add (hashes the PIN) / list staff |
| `POST` | `/api/staff/{staffId}/verify-pin`       | Check a PIN against the stored bcrypt hash |
| `GET` | `/api/stores/{storeId}/requests/active` | Requests still `OPEN` or `ACKNOWLEDGED` |
| `POST` | `/api/requests/{requestId}/acknowledge` | Body: `{ "staffId": "<uuid>" }` |
| `POST` | `/api/requests/{requestId}/resolve`     | |

Every `/api/stores/{storeId}/**` route resolves the store through
`StoreService.requireOwnedStore`, which scopes it to the authenticated account and returns `404`
for a store belonging to someone else.

## Real-time

STOMP endpoint at `/ws` (SockJS enabled). `ServiceRequestBroadcaster` publishes to
`/topic/stores/{storeId}/requests` on create, acknowledge and resolve:

```json
{ "event": "CREATED", "request": { "id": "…", "tableNumber": 4, "type": "CALL_WAITER", "status": "OPEN" } }
```

This is a stub: the handshake is currently unauthenticated and there is no client wiring yet.
Because `enableSimpleBroker("/topic")` applies no destination authorization, anyone who can
reach `/ws` can subscribe to another venue's `storeId` and watch its floor. Authenticate the
handshake before exposing this publicly — the mobile app polls instead, so nothing depends on
it yet.

## Deploying for free

The app is a Docker image plus a Postgres URL, so any host that runs containers will do.
`render.yaml` is a ready blueprint; the environment variables are the same anywhere.

**Database.** Don't use Render's free Postgres: it is deleted 30 days after creation. Two
options that keep your data:

| | Storage | Catch |
| --- | --- | --- |
| Supabase free | 500 MB | Pauses after ~7 days of no activity |
| Neon free | 0.5 GB | 100 compute-hours/month, sleeps after 5 min idle |

Neon's compute cap interacts badly with this app: the staff app polls every 3 seconds, so the
database never gets to sleep while anyone is on shift. At the free tier's 0.25 CU that is
400 active hours a month against a 730-hour month — fine for a 12-hour service day, but it
will suspend if you leave the app open around the clock. Supabase doesn't meter compute, so
prefer it unless you want branching.

On Supabase, use the **session pooler** connection string (port 5432), not the direct one:
direct connections are IPv6-only on new projects, and the transaction pooler on 6543 breaks
Flyway's prepared statements. Append `?sslmode=require` to the JDBC URL.

**Steps.**

1. Create the database, then run the two SQL files against it in order: the Flyway migration
   happens automatically on first boot, so afterwards apply `seed-demo-data.sql`.
2. Base64 the Firebase service account key and set it as `FIREBASE_CREDENTIALS_JSON`:
   ```bash
   base64 -i path/to/service-account.json | tr -d '\n' | pbcopy
   ```
3. Deploy with the variables from the table above. `FIREBASE_MOCK_ENABLED` must be `false`.
4. Set `CORS_ALLOWED_ORIGINS` to your Netlify origin, and `VITE_API_BASE_URL` in Netlify to
   the backend's URL. Both sides need to know about each other.
5. **Re-point the demo venue at your real account.** With mock auth off you sign in as a real
   Firebase user whose uid is not `mock-user-001`, so `GET /api/stores` comes back empty and
   the staff app's PIN screen has no venue to unlock. `seed-demo-data.sql` ends with the
   `UPDATE` for this; run it with your own uid.

**Cold starts.** A free instance sleeps after 15 minutes idle, and a JVM waking on a shared
core takes a while — long enough that a customer scanning a QR code gives up. Render's free
tier allows 750 hours a month, which covers one service running continuously, so an external
cron hitting `/actuator/health` every 10 minutes keeps it warm within budget.

## Package layout

Domain-based — each package owns its entity, repository, service and controller.

```
com.bl2026
├── auth/        Firebase token verification filter, Spring Security config
├── common/      Exceptions + handler, WebSocket/STOMP config
├── account/     Account
├── store/       Store  (owns requireOwnedStore, the authorization primitive)
├── storetable/  StoreTable, QR token generation and public resolution
├── menu/        MenuItem
├── staff/       StaffMember (PIN only, no Firebase identity)
└── request/     ServiceRequest, enums, broadcaster
```
# waitless-backend
