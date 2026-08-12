# GreenTrack Backend — Setup Guide

The app is deployed to **Render** (see [Deploying to Render](#deploying-to-render-free) below).
This section covers running it locally against PostgreSQL for development.

## Prerequisites
- Java 17 JDK  →  https://adoptium.net
- Maven 3.9+   →  https://maven.apache.org
- PostgreSQL 14+

---

## Step 1 — Create the database

Open `psql` as the postgres superuser:
```
psql -U postgres
```

Run:
```sql
CREATE DATABASE greentrack_db;
CREATE USER greentrack_user WITH PASSWORD 'GreenTrack@2026';
GRANT ALL PRIVILEGES ON DATABASE greentrack_db TO greentrack_user;
\c greentrack_db
GRANT ALL ON SCHEMA public TO greentrack_user;
```

---

## Step 2 — Create uploads folder

```
mkdir uploads
```

---

## Step 3 — Set required secrets

The JWT signing key and DB password no longer ship with defaults (they used to be
hardcoded in `application.yml`, which is a security hole once committed to a repo).
Set them as environment variables before running the app.

PowerShell:
```powershell
$env:JWT_SECRET = "a-long-random-string-at-least-32-characters"
$env:DB_PASSWORD = "GreenTrack@2026"
```

Bash:
```bash
export JWT_SECRET="a-long-random-string-at-least-32-characters"
export DB_PASSWORD="GreenTrack@2026"
```

Optional overrides: `DB_HOST` (default `localhost`), `DB_PORT` (default `5432`),
`DB_NAME` (default `greentrack_db`), `DB_USERNAME`, `UPLOAD_PATH`, `FIREBASE_CREDENTIALS`,
`CORS_ALLOWED_ORIGINS` (comma-separated list, defaults to
`http://localhost:3000,http://localhost:8080` — irrelevant once the frontend is served
from the same origin as the API, see below).

## Step 4 — Build & Run

```
mvn clean package -DskipTests
mvn spring-boot:run
```

App starts at: http://localhost:8080 — this also serves the frontend
(`src/main/resources/static/index.html`), so opening that URL in a browser gives you
the full app, not just the API.

---

## Step 5 — Seed data

After first run (tables created automatically via Hibernate `ddl-auto: update`), insert seed data:

```
psql -U greentrack_user -d greentrack_db -f src/main/resources/schema.sql
```

Seed accounts (all password: Admin@2026):
- admin@greentrack.app   → ADMIN
- kofi@greentrack.app    → COLLECTOR  (Badge: GT-COL-001)
- ama@greentrack.app     → CITIZEN

---

## Deploying to Render (free)

This deploys the backend **and** the frontend together as one Render Web Service
(the frontend is baked into the jar and served at `/`, so there's no separate
frontend deploy and no CORS to configure).

1. **Push this project to GitHub** (a new repo is fine — private or public).
2. **Create the database**: Render dashboard → New → PostgreSQL → give it a name (e.g. `greentrack-db`) →
   free plan → Create. Once it's up, open it and note these fields from the "Info" tab:
   `Hostname`, `Port`, `Database`, `Username`, `Password`.
3. **Create the web service**: Render dashboard → New → Web Service → connect your GitHub repo →
   set **Root Directory** to `greentrack-backend` (Render will auto-detect the `Dockerfile` there) →
   free plan → Create.
4. **Set environment variables** on the web service (Settings → Environment):
   | Key | Value |
   |---|---|
   | `DB_HOST` | the Hostname from step 2 |
   | `DB_PORT` | the Port from step 2 (usually `5432`) |
   | `DB_NAME` | the Database name from step 2 |
   | `DB_USERNAME` | the Username from step 2 |
   | `DB_PASSWORD` | the Password from step 2 |
   | `JWT_SECRET` | any long random string (32+ characters) |

   Leave `PORT` alone — Render sets it automatically and `application.yml` already reads it.
5. **Deploy** — Render builds the Dockerfile and starts the service. First boot can take a
   few minutes (Maven has to download dependencies inside the build).
6. Once live, seed the database once via `psql` using the **External** connection string
   Render shows on the Postgres instance's Info tab, running the same `schema.sql` from Step 5 above.
7. Open the Render URL (e.g. `https://greentrack-backend.onrender.com`) on your phone — that's the whole app.

**Known limitation:** Render's free web service disk is *ephemeral* — uploaded report/proof
photos are wiped on every redeploy or restart (including the automatic spin-down after 15
minutes of inactivity). This doesn't block getting things connected and tested, but photos
won't persist long-term until image storage is moved to something durable (e.g. Firebase
Storage or Cloudinary). Flagging this now so it isn't a surprise later.

**Also note:** the free web service sleeps after 15 minutes of no traffic. The next request
after that wakes it back up but takes ~30-50 seconds — expected, not a bug.

---

## Step 5 — Test with Postman

Import: GreenTrack_API.postman_collection.json
Run "Login Admin" first — token saves automatically.

---

## Endpoints

| Method | Endpoint                    | Role      |
|--------|-----------------------------|-----------|
| POST   | /api/auth/register          | Public    |
| POST   | /api/auth/login             | Public    |
| POST   | /api/auth/refresh           | Public    |
| GET    | /api/auth/me                | Any auth  |
| GET    | /api/categories             | Public    |
| POST   | /api/reports                | CITIZEN   |
| GET    | /api/reports                | ADMIN     |
| GET    | /api/reports/my             | CITIZEN   |
| GET    | /api/reports/nearby         | Any auth  |
| GET    | /api/reports/{id}           | Any auth  |
| PATCH  | /api/reports/{id}/status    | ADMIN     |
| DELETE | /api/reports/{id}           | ADMIN     |
| POST   | /api/assignments            | ADMIN     |
| GET    | /api/assignments/mine       | COLLECTOR |
| PATCH  | /api/assignments/{id}/status| COLLECTOR |
| POST   | /api/assignments/{id}/proof | COLLECTOR |
| GET    | /api/notifications          | Any auth  |
| GET    | /api/notifications/unread-count | Any auth |
| PATCH  | /api/notifications/{id}/read| Any auth  |
| PATCH  | /api/notifications/read-all | Any auth  |
| GET    | /api/analytics/dashboard    | ADMIN     |
| GET    | /api/users                  | ADMIN     |
| GET    | /api/users/collectors       | ADMIN     |
| GET    | /api/users/{id}             | ADMIN     |
| PATCH  | /api/users/{id}/status      | ADMIN     |

---

## Firebase (Optional)
Place firebase-service-account.json in project root to enable push notifications.
Get it from: Firebase Console → Project Settings → Service Accounts → Generate key
