# AeroOps build flow

AeroOps is being developed as a learning project for airline operations. The first business workflow is keeping an aircraft fleet ready for scheduled flights.

```mermaid
flowchart LR
    A[1. Aircraft registry<br/>registration, model, capacity, readiness] --> B[2. Airport registry<br/>IATA codes and locations]
    B --> C[3. Flight schedule<br/>route and offset-aware times]
    C --> D[4. Aircraft assignment<br/>ready and no schedule conflict]
    D --> E[5. Flight operations<br/>valid status transitions]
    E --> F[6. Maintenance windows ✓<br/>block unavailable aircraft]
    G[JWT authentication and role permissions] -. protects .-> A
    G -. protects .-> B
    G -. protects .-> C
    G -. protects .-> D
    G -. protects .-> E
    G -. protects .-> F
    T[Tests and API documentation] -. verify each step .-> A
    T -. verify each step .-> E
```

## Milestones

- [x] Clone and map the existing employee-management backend.
- [x] Add aircraft registration, listing, details, updates, and readiness status.
- [x] Add airport records and validation.
- [x] Add flight scheduling and route/time validation.
- [x] Assign aircraft while rejecting unavailable or overlapping schedules.
- [x] Add allowed flight status transitions.
- [x] Add maintenance windows that block overlapping flights and assignments.
- [x] Align permissions and add workflow-focused tests and API documentation.

- [ ] Run integration checks against PostgreSQL, including real row-lock behavior under concurrent assignment requests.

Each milestone should leave the application buildable. Existing employee endpoints remain available during the migration and can be removed or retained after the aviation workflow is established.

## Flight status lifecycle

```mermaid
stateDiagram-v2
    [*] --> SCHEDULED
    SCHEDULED --> BOARDING: aircraft assigned and ready
    SCHEDULED --> CANCELLED
    BOARDING --> IN_AIR
    BOARDING --> CANCELLED
    IN_AIR --> LANDED
    LANDED --> [*]
    CANCELLED --> [*]
```
