# Titan: Geofence Guard (The Sentinel) Implementation Plan

## Goal
Implement backend logic to accept driver scans/events and verify them against the target location (Geofence).
If `distance(gps_scan, gps_target) > 300m`, trigger a `GEOFENCE_BREACH` event. Provide overrides for drivers.

## User Review Required
> [!IMPORTANT]
> **Logic Location**: Implemented in `driver-app-service` (module `titan`).
> **Threshold**: 300 meters (hardcoded initially, configurable later).
> **Override**: If breach detected, return `406 Not Acceptable` with `BREACH` code. Driver can retry with `override=true` flag to force acceptance.

## Proposed Changes

### [MODIFY] [modules/titan/driver-app-service](file:///modules/titan/driver-app-service)

#### 1. Controller
*   **[NEW] `ScanController.java`**:
    *   `POST /api/scan`: Accepts `{ barcode, lat, lon, timestamp, override }`.
    *   Calls `GeofencingService`.

#### 2. Service
*   **[NEW] `GeofencingService.java`**:
    *   Fetches Order/Stop location (Mocked or synced from `order-service`).
    *   Calculates Haversine distance.
    *   If > 300m and `!override`: Throws `GeofenceException`.
    *   If valid or `override`: Emits event to Kafka (`scan.accepted`).

#### 3. Domain
*   **`ScanRequestDto`**: `{ barcode, lat, lon, timestamp, override }`.

## Verification Plan

### Automated Tests
*   **Unit Test**:
    *   Mock `OrderLocation` (52.2297, 21.0122).
    *   Test Scan at (52.2297, 21.0122) -> Success.
    *   Test Scan at (50.0000, 20.0000) -> Fails (>300m).
    *   Test Scan at (50.0000, 20.0000) + Override -> Success.
