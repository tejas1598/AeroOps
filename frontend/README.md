# AeroOps frontend

The React frontend supports the core airline operations workflow: sign in, manage aircraft and airports, schedule flights, assign ready aircraft, update flight status, and plan maintenance windows.

## Run locally

```powershell
pnpm install
pnpm dev
```

Open `http://localhost:5173`. Start the Spring Boot API on `http://localhost:8080` first. Vite forwards `/auth` and `/api` requests to the backend, so the browser can use same-origin paths during development without a backend CORS change.

## Screens and API operations

- Sign in through `POST /auth/login`.
- Aircraft: list and register with `/api/v1/aircraft`, edit with `PUT /api/v1/aircraft/{id}`, and change readiness through `PATCH /api/v1/aircraft/{id}/status`.
- Airports: list and add through `/api/v1/airports`, edit with `PUT /api/v1/airports/{id}`.
- Flights: list and schedule through `/api/v1/flights`; assign aircraft with `PUT /api/v1/flights/{id}/aircraft`; move through allowed statuses with `PATCH /api/v1/flights/{id}/status`.
- Maintenance: list and schedule through `/api/v1/maintenance-windows`; cancel a scheduled window with `PATCH /api/v1/maintenance-windows/{id}/cancel`.
- Display backend validation, authentication, permission, and scheduling conflict errors in the relevant screen or form.

The JWT is held in `sessionStorage` for this learning prototype and sent as `Authorization: Bearer <token>` by the API helper. The backend remains responsible for validating permissions and enforcing scheduling rules.

## Request flow

`App.jsx` handles login and keeps the signed-in state. `FlightsPage.jsx` provides the operations shell and flight schedule, while `ResourcePages.jsx` renders aircraft, airport, and maintenance screens. `api.js` makes HTTP requests, attaches the token, parses JSON, and turns API errors into readable messages. The Vite proxy forwards those requests to Spring Boot.
