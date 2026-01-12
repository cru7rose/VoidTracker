# Flux: Routing Engine (Control Tower) Implementation Plan

## Goal
Implement "Control Tower" functionality for the Routing Engine (`Flux`).
Enable dispatchers to:
1.  Manage **Standard Routes** (Fixed templates for regular service).
2.  Perform **Ad-hoc** modifications (Inject orders into running routes).
3.  Maintain **100% Control** (Lock routes from auto-optimization).

## User Review Required
> [!IMPORTANT]
> **Ad-hoc Strategy**:
> *   **Manual Injection**: User calls `/routes/{id}/inject`. logic: Add stop -> Re-sequence (Nearest Neighbor) -> Recalculate ETA.
> *   **Locking**: If a route is manually modified, `autoOptimized` flag is set to `false` to prevent Timefold from scrambling it during global optimization.

## Proposed Changes

### [MODIFY] [modules/flux/planning-service](file:///modules/flux/planning-service)

#### 1. Domain / Entities
*   **[NEW] `StandardRouteEntity`**:
    *   `name`: "Warsaw Center - Morning".
    *   `defaultVehicleId`: Preferred truck.
    *   `fixedStops`: JSONB list of mandatory stops (e.g. cross-docks).
*   **[NEW] `PlannedRouteEntity`**:
    *   Persisted result of optimization.
    *   `isLocked`: boolean (Prevent auto-optimization).

#### 2. Service
*   **[NEW] `RouteManagementService.java`**:
    *   `instantiateStandardRoutes(LocalDate date)`: Creates empty `PlannedRoute`s from templates.
    *   `injectOrder(UUID routeId, OrderResponseDto order)`:
        *   Adds order to route.
        *   Calls `TimefoldOptimizer.resequenceRoutes` (Partial optimization).
        *   Sets `isLocked = true`.

#### 3. Controller
*   **[NEW] `ControlTowerController.java`**:
    *   `POST /routes/standard/instantiate`: Manual trigger for day start.
    *   `POST /routes/{id}/inject`: Ad-hoc support.

## Verification Plan

### Automated Tests
*   **Standard Route**: Create Template -> Instantiate -> Verify Route Exists.
*   **Ad-hoc**: Create Route with A->B. Inject C. Verify A->C->B or A->B->C (Nearest Neighbor).
