# How the AeroOps frontend talks to the backend

The browser never connects directly to PostgreSQL. It sends HTTP requests to the AeroOps API, and the backend reads or changes database records.

## During local development

```text
Browser (React at localhost:5173)
  ├─ page files, styles, and frontend logic ── served by Vite
  └─ /auth and /api requests ── Vite proxy to localhost:8080
                                      │
                                      ▼
                            Spring Boot API
                                      │
                         Controller → Service → Repository
                                      │
                                      ▼
                                  PostgreSQL
```

Vite's proxy is configured in `frontend/vite.config.js`. It lets the browser request paths such as `/auth/login` and `/api/v1/flights` from its own origin while Vite forwards them to the Spring Boot server on port 8080. This avoids needing a separate browser CORS setup for local development.

## With Docker Compose

```text
Browser (localhost:3000)
        │ page and same-origin /auth, /api requests
        ▼
Frontend container (Nginx serves the built React files)
        │ Nginx forwards /auth and /api to app:8080
        ▼
Backend container (Spring Boot)
        │ Controller → Service → Repository
        ▼
PostgreSQL container (db:5432, persisted in a named volume)
```

In this setup Nginx replaces Vite's development proxy. `app` is the backend's Docker Compose service name, so Docker's internal network resolves it to the backend container. The browser uses `localhost:3000`; it does not need to know the backend container address.

## Example: signing in and scheduling a flight

1. `App.jsx` calls `login()` in `api.js` with the username and password.
2. `api.js` sends `POST /auth/login` with a JSON body. Vite forwards it in development; Nginx forwards it in Compose.
3. Spring Security allows the login route, and the authentication code checks the credentials and returns a JWT.
4. `App.jsx` stores the token in browser `sessionStorage` and renders the operations screen.
5. The screen calls functions in `api.js`, such as `getFlights()` or `scheduleFlight()`. For protected requests, `api.js` adds `Authorization: Bearer <token>`.
6. Spring Security validates the token and checks the required authority. The controller passes valid input to a service, where business rules are checked before a repository accesses PostgreSQL.
7. The backend returns JSON and an HTTP status. `api.js` parses the response; React updates the screen or displays an error such as validation failure, permission denial, or a scheduling conflict.

For example, `scheduleFlight()` sends a `POST /api/v1/flights` request. The API returns the created flight as JSON. The frontend then reloads the flight data so the new schedule appears. The same pattern is used for aircraft, airports, assignment, flight status, and maintenance operations.

## Important addresses

| Address | Used by |
|---|---|
| `http://localhost:5173` | Vite frontend during development |
| `http://localhost:3000` | Nginx frontend in Docker Compose |
| `http://localhost:8080` | Spring Boot API from the host machine |
| `db:5432` | PostgreSQL address used by the backend inside Compose |
| `localhost:5433` | Optional host connection to the Compose PostgreSQL container |

The Docker PostgreSQL container is separate from the PostgreSQL server already installed on the computer. The Compose volume preserves the container database data across container restarts and rebuilds. It does not automatically copy or replace data in the existing local database.
