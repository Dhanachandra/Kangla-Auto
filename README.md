# Kangla Auto

Rickshaw booking for Manipur — riders request rides, drivers accept and complete
them. Ship: FastAPI + PostgreSQL backend with an HTML/CSS/JS web dashboard and a
Kotlin/Jetpack Compose Android app.

## Stack

- **Backend:** FastAPI + SQLAlchemy + PostgreSQL (SQLite for dev), Alembic
  migrations, JWT auth (pyjwt) with bcrypt hashing, gunicorn workers, structured
  request logging, CORS, health checks.
- **Web frontend:** static HTML/CSS/JS served by the app at `/`.
- **Android:** Kotlin + Jetpack Compose in `android/` (see `android/README.md`).

## Quickstart (dev, SQLite)

Requires Python 3.12+.

```bash
python3 -m venv .venv
source .venv/bin/activate
pip install -e ".[dev]"
# replace `[dev]` with pytest httpx if pip doesn't know the dev group
uvicorn app.main:app --reload
```

Open http://127.0.0.1:8000. First startup seeds a SQLite DB at `data/app.db`.

## Production (Docker + PostgreSQL)

```bash
export SECRET_KEY="$(python -c 'import secrets; print(secrets.token_hex(32))')"
docker compose up -d --build
```

- `db` — PostgreSQL 16 (data in a named volume).
- `app` — runs `alembic upgrade head` then gunicorn (4+ workers, UvicornWorker).
- The app refuses to start without a `SECRET_KEY` (compose uses `:?`).
- Health check: `curl http://localhost:8000/health` → `200 {"status":"ok",...}`.

## Configuration

Copy `.env.example` to `.env` for local overrides (env vars win).

| Variable | Purpose |
| --- | --- |
| `APP_ENV` | development / staging / production (disables `/docs` in prod) |
| `SECRET_KEY` | JWT HMAC key; must be ≥32 chars when changed |
| `DATABASE_URL` | `sqlite:///./data/app.db` or `postgresql+psycopg://…` |
| `AUTO_CREATE_TABLES` | dev-only table auto-create; prod uses Alembic |
| `SEED_ON_STARTUP` | seed demo accounts on boot |
| `CORS_ORIGINS` | comma-separated browser origins allowed |

## Migrations

```bash
alembic revision --autogenerate -m "change"   # after model edits
alembic upgrade head                           # apply
```

## API

Auth (`/api/auth`): `POST register`, `POST login`, `GET me`.
Driver list: `GET /api/drivers`.
Bookings (Bearer token): `POST /api/bookings` (riders),
`GET /api/bookings/my`, `GET /api/bookings/pending` (drivers),
`PATCH /api/bookings/{id}` (`accepted|cancelled|completed`, role-rules enforced).
Fare = ₹30 + ₹15/km. List endpoints support `offset`/`limit`.

## Demo accounts

| Role   | Phone      | Password   |
| ------ | ---------- | ---------- |
| rider  | 9876500003 | rider123   |
| driver | 9876500001 | driver123  |
| driver | 9876500002 | driver123  |

## Tests

```bash
pytest
```

CI equivalent runs in `.github/workflows/ci.yml` (install, `alembic upgrade head`
against a fresh DB, then the test suite).