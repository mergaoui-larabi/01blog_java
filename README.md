# 01Blog

A social blogging platform for students to share what they are learning. Users can publish posts with media, comment, like, follow each other, receive realtime notifications, and report inappropriate content; admins get user and content moderation tools.

## Overview

The project is a microservice application: an Angular SPA talks to a Spring Cloud Gateway, which routes to six independent Spring Boot services. Each service owns its PostgreSQL database and talks to the others over HTTP. Redis backs gateway rate limiting and caching, and JWTs are signed by the auth service.

```
Angular (4200) ──► API Gateway (5000) ──┬──► user-service          (5001) ──► PostgreSQL 5432
                                        ├──► auth-service          (5002)
                                        ├──► media-service         (5003) ──► PostgreSQL 5433
                                        ├──► post-service          (5004) ──► PostgreSQL 5434
                                        ├──► notification-service  (5005) ──► PostgreSQL 5435
                                        └──► moderation-service    (5006) ──► PostgreSQL 5436
                                                          Redis (6379)
```

| Component | Stack | Responsibility |
| --- | --- | --- |
| `frontend/blog` | Angular 22, TypeScript, Bootstrap, Vitest | SPA: feed, profiles, posts, notifications, admin panel |
| `backend/api-gateway` | Spring Cloud Gateway, WebFlux, Redis | Single entry point, routing, CORS, rate limiting, JWT checks |
| `backend/auth-service` | Spring Boot, RSA/JWT | Signup/signin, JWKS endpoint, token issuance |
| `backend/user-service` | Spring Boot, JPA, Flyway | Accounts, profiles, follows, bans |
| `backend/media-service` | Spring Boot, JPA, Flyway | Avatar/post media upload and serving |
| `backend/post-service` | Spring Boot, JPA, Flyway | Posts, comments, likes, feed |
| `backend/notification-service` | Spring Boot, WebSocket | Notifications and realtime delivery |
| `backend/moderation-service` | Spring Boot, JPA, Flyway | Content/user reports and moderation actions |
| `docker/` | Docker Compose | Infrastructure-only and full-stack stacks |

API and design documentation lives in [`docs/`](docs) — start with [`docs/subject.md`](docs/subject.md), [`docs/backend/api/index.md`](docs/backend/api/index.md), and [`docs/database/db.dbml`](docs/database/db.dbml).

## Prerequisites

- **Docker path:** Docker Engine/Desktop with Compose v2, `make`, `openssl`, `bash`.
- **Local dev path:** Java 21, Node.js 22 with npm, `make`, `openssl`, `bash`, plus Docker for the databases and Redis.
- The Maven wrapper (`./mvnw`) is committed, so Maven itself is not required.

## Path A — Run everything with Docker

### 1. Create the environment file

```bash
cp .env.example .env
```

Open `.env` and delete the first line (`[TEMPLATE]`), which is a placeholder and not valid shell. The rest of the file already contains the Docker-internal service URLs and database credentials the Compose stack expects. `.env` is gitignored.

### 2. Generate the JWT signing keys

```bash
make key-gen
```

This writes `backend/auth-service/keys/private.pem` and `public.pem`. The auth service will not start without them, and the Compose stack mounts that directory read-only. The target refuses to overwrite existing keys.

### 3. Build and start the stack

```bash
make docker-up
```

The first run builds seven images (six services plus the nginx-served frontend), so it takes a few minutes. Check status and logs with:

```bash
docker compose -f docker/docker-compose.yml ps
docker compose -f docker/docker-compose.yml logs -f user-service
```

Flyway applies each service's schema on startup automatically; the databases start empty apart from that schema.

### 4. Open the app

| URL | What |
| --- | --- |
| http://localhost:4200 | Frontend |
| http://localhost:5000 | API Gateway (`/api/v1/...`) |
| http://localhost:8080 | Dozzle — live container logs |

Dozzle's compose definition mounts the Podman socket (`${XDG_RUNTIME_DIR}/podman/podman.sock`). On Docker Desktop or plain Docker Engine that path does not exist and Dozzle will fail; it is an optional log viewer, so remove or comment out the `dozzle` service in `docker/docker-compose.yml` if you do not need it.

### 5. (Optional) Load seed data

```bash
./tests/seed.sh
```

This loads 100 users, posts, comments, notifications, and reports into the running database containers, then writes the placeholder media files. Seeded accounts all use the password `Password123!`; the first three are admins. For example: `swift_nebula_1@mailinator.com` / username `swift_nebula_1`.

### Stop / reset

```bash
make docker-down     # stop containers, keep data
make docker-clean-up # stop, delete volumes, and start again (empty databases)
```

## Path B — Local development (services on the host, infra in Docker)

Use this when you want hot reload and debuggers for the Java services and Angular.

### 1. Create the environment file

```bash
cp .env.example .env
```

Delete the `[TEMPLATE]` line, then **replace the Docker-internal hosts with ports published on your machine** (Compose maps each database to a different host port):

```dotenv
# JWT RSA key location
JWT_PRIVATE_KEY_PATH=keys/private.pem
JWT_PUBLIC_KEY_PATH=keys/public.pem

INTERNAL_SERVICE_TOKEN=local-dev-internal-token

# Databases (host ports exposed by docker/docker-compose.dev.yml)
USER_DB_HOST=localhost:5432
USER_DB_NAME=blog_users
USER_DB_USERNAME=blog_user
USER_DB_PASSWORD=secret

MEDIA_DB_HOST=localhost:5433
MEDIA_DB_NAME=blog_medias
MEDIA_DB_USERNAME=blog_media
MEDIA_DB_PASSWORD=secret

POST_DB_HOST=localhost:5434
POST_DB_NAME=blog_post
POST_DB_USERNAME=blog_post
POST_DB_PASSWORD=secret

NOTIF_DB_HOST=localhost:5435
NOTIF_DB_NAME=blog_notifications
NOTIF_DB_USERNAME=blog_notification
NOTIF_DB_PASSWORD=secret

MOD_DB_HOST=localhost:5436
MOD_DB_NAME=blog_moderations
MOD_DB_USERNAME=blog_moderation
MOD_DB_PASSWORD=secret

MEDIA_STORAGE_LOCATION=./media-storage

# Service URLs (localhost, not container names)
USER_SERVICE_URL=http://localhost:5001
AUTH_SERVICE_URL=http://localhost:5002
MEDIA_SERVICE_URL=http://localhost:5003
POST_SERVICE_URL=http://localhost:5004
NOTIFICATION_SERVICE_URL=http://localhost:5005
MODERATION_SERVICE_URL=http://localhost:5006

REDIS_PASSWORD=secret
```

The Makefile sources this `.env` before launching every service. If you keep the container-name hosts (`postgres-users:5432`, `auth-service:5002`, ...), host processes cannot resolve them.

### 2. Start PostgreSQL and Redis

```bash
make infra-up
```

This runs `docker/docker-compose.dev.yml` (five PostgreSQL 17 instances, Redis 8, and Dozzle). Verify with `docker ps`.

### 3. Generate the JWT keys

```bash
make key-gen
```

### 4. Start the backend services and frontend

```bash
make start
```

This starts the gateway, all six services, and the Angular dev server in the background, writing logs to `logs/`. Each Java service runs `./mvnw install` and then `spring-boot:run`. The first start downloads dependencies, so give it a minute or two before the gateway is reachable. Follow progress with:

```bash
tail -f logs/gateway.log
tail -f logs/user-service.log
tail -f logs/angular.log
```

Open http://localhost:4200. Angular calls the gateway at `http://localhost:5000/api/v1` (see `frontend/blog/src/environments/environments.ts`); the notification WebSocket connects directly to `ws://localhost:5005`.

### 5. (Optional) Load seed data

```bash
./tests/seed.sh
```

The `infra-up` containers have the same names as the full stack, so the script works here too.

### Stop

```bash
make stop       # stop the seven host processes and the frontend
make infra-down # stop PostgreSQL/Redis
```

Individual targets are also available, e.g. `make dev-post-service`, `make stop-post-service`, `make front-down`.

## Common commands

| Command | Effect |
| --- | --- |
| `make key-gen` | Generate the RSA key pair for JWT signing |
| `make infra-up` / `make infra-down` | Start/stop only databases + Redis (dev compose) |
| `make docker-up` / `make docker-down` | Start/stop the full Docker stack |
| `make docker-clean-up` | Recreate the stack with empty volumes |
| `make start` / `make stop` | Start/stop all services + frontend on the host |
| `make dev-<service>` / `make stop-<service>` | One service (`gateway`, `user-service`, `auth-service`, `media-service`, `post-service`, `notification-service`, `moderation-service`) |
| `make front-up` / `make front-down` | Angular dev server only |
| `./tests/seed.sh` | Load mock data into the database containers |

Frontend-only commands (from `frontend/blog`): `npm start`, `npm run build`, `npm test` (Vitest).
Backend-only commands (from a service directory): `./mvnw spring-boot:run`, `./mvnw test`.

## Ports and health checks

| Service | Port | Health endpoint |
| --- | --- | --- |
| Frontend (dev / Docker) | 4200 | — |
| API Gateway | 5000 | `/actuator/health` |
| User | 5001 | `/actuator/health` |
| Auth | 5002 | `/actuator/health`, `/.well-known/jwks.json` |
| Media | 5003 | `/actuator/health` |
| Post | 5004 | `/actuator/health` |
| Notification | 5005 | `/actuator/health` |
| Moderation | 5006 | `/actuator/health` |
| PostgreSQL (users/media/posts/notifications/moderation) | 5432 / 5433 / 5434 / 5435 / 5436 | — |
| Redis | 6379 | — |
| Dozzle | 8080 | — |

## Troubleshooting

- **Auth service exits on startup** — `make key-gen` was not run, or the keys were generated after the container was created. Generate them, then `make docker-up` again.
- **`relation "..." does not exist` / schema validation errors** — the databases were created before/without Flyway migrations. Check the service log; if the volume predates a migration, run `make docker-clean-up` (destroys data) and recreate.
- **Services cannot reach each other locally** — the `.env` still has Docker hostnames. Use the local `.env` block above.
- **`[TEMPLATE]: command not found`** — remove the first line of `.env`.
- **Gateway returns 429** — the Redis rate limiter (50 req/s replenish, 100 burst) is active; wait or relax it in `backend/api-gateway/src/main/resources/application.yml`.
- **Port already in use** — `make stop` and `make infra-down` free the app ports; the Makefile's `stop-*` targets kill by port.
- **Dozzle fails in the full stack** — the Podman socket mount does not exist on Docker; remove the optional `dozzle` service.

## Project layout

```
backend/            # api-gateway + six Spring Boot services (each with its own Dockerfile)
frontend/blog/      # Angular application
docker/             # docker-compose.dev.yml (infra only) and docker-compose.yml (full stack)
docs/               # architecture, API reference, database schema and seed data
tests/seed.sh       # loads docs/database/seed into the running containers
Makefile            # key generation, Docker stacks, and per-service dev start/stop
```
