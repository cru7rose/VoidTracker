# The Ghost: Scanner & Offline Core Implementation Plan

## Goal
Integrate QR code scanning and robust **Offline Capabilities**.
Ensure drivers can scan packages, take photos, and log status updates even without network coverage. Data will be queued and synced when connectivity returns.

## User Review Required
> [!IMPORTANT]
> **Offline Strategy**:
> *   **Storage**: We will use **IndexedDB** (via `idb` library) to store high-volume data like photos/scans locally. LocalStorage is insufficient for images.
> *   **Queue Pattern**: All "Write" actions (Scan, Status Update) go to an `OfflineQueue` store first.
> *   **Background Sync**: The app will listen for `online` events and attempt to flush the queue automatically.

## Proposed Changes

### [MODIFY] [modules/ghost/driver-pwa](file:///modules/ghost/driver-pwa)

#### 1. Dependencies
*   Install `html5-qrcode` (Scanner).
*   Install `idb` (Promised-based IndexedDB wrapper).

#### 2. Components
*   **[NEW] `src/components/Scanner.vue`**:
    *   Wraps `Html5Qrcode`.
    *   Emits raw scan data.
*   **[NEW] `src/components/NetworkStatus.vue`**:
    *   Visual indicator (Green/Red dot) showing connection status and pending sync items.

#### 3. Stores (State Management)
*   **[NEW] `src/stores/offline-queue.ts`**:
    *   `queue`: Array of `{ id, type, payload, timestamp, status }`.
    *   `addAction(action)`: Persists to IndexedDB.
    *   `processQueue()`: Iterates through pending items and sends to API.

#### 4. Views
*   **[NEW] `src/views/ScanView.vue`**:
    *   Uses `Scanner.vue`.
    *   On success, calls `offlineQueue.addAction({ type: 'SCAN_EVENT', ... })`.

## Verification Plan

### Manual Verification
*   **Offline Test**:
    1. Turn off WiFi/Network.
    2. Scan a QR code.
    3. Verify item appears in "Pending Queue" UI.
    4. Turn on WiFi.
    5. Verify item is sent to backend and removed from queue.
