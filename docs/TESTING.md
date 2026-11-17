# Testing

Run the test suite from `spring-core-demo`:

```powershell
./mvnw.cmd test
```

The suite includes service-level tests for aircraft, airports, flights, and maintenance, plus a database-backed MockMvc workflow test. The workflow test uses H2 in PostgreSQL compatibility mode so it can exercise Spring Security, JWT login, validation, JPA persistence, HTTP responses, and the full operational path without requiring a running database server.

The integration scenario registers aircraft and airports, schedules a flight, verifies maintenance and flight overlap conflicts, assigns the aircraft, advances the flight through boarding, in-air, and landed, then verifies when maintenance can be scheduled.

H2 compatibility mode does not reproduce every PostgreSQL behavior. PostgreSQL-specific integration checks, especially simultaneous assignments and pessimistic row-lock behavior, remain to be run against a real PostgreSQL instance.