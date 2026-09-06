# Waitless Backend

Spring Boot backend for a restaurant/cafe "call waiter" app.

Customers scan a QR code at their table and call a waiter or request the bill — no login.
accounts manage stores, tables, menus and staff from a Flutter app, authenticated with Firebase Auth.

## Stack

Java 17 · Spring Boot 3.5 · PostgreSQL 16 · Flyway · Spring Security + Firebase Admin SDK · STOMP over WebSocket · Lombok

## Running locally

```bash
docker compose up -d      # PostgreSQL on localhost:5432
mvn spring-boot:run
```

Flyway applies `src/main/resources/db/migration` on startup. Hibernate runs with
`ddl-auto: validate` and never modifies the schema — every change goes in a new migration file.

### Environment variables

All have local defaults, so the app starts with no configuration beyond `docker compose up`.

| Variable | Default                                     | Notes |
| --- |---------------------------------------------| --- |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/waitless` | Matches `docker-compose.yml` |
| `SPRING_DATASOURCE_USERNAME` | `bl2026`                                    | |
| `SPRING_DATASOURCE_PASSWORD` | `bl2026`                                    | |
| `FIREBASE_CREDENTIALS_PATH` | *(empty)*                                   | Path to the Firebase service account JSON |

`FIREBASE_CREDENTIALS_PATH` accepts a plain filesystem path, or a Spring resource URL
(`file:`, `classpath:`). **When it is empty, Firebase verification is disabled**: public
endpoints work normally and authenticated endpoints return `503`. This keeps the app runnable
for customer-side work without a key. Set it before touching any account endpoint.

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
| `POST` | `/api/requests` | Create a service request (`qrToken`, `type`, `paymentMethod`) |

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
