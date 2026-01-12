# Offline Mode Implementation Plan (Driver PWA)

## Goal Description
Implement a robust "Offline-First" architecture for the Driver PWA ("The Ghost").
The system must allow drivers to perform scans and updates (e.g., "I'm Here" events, POD uploads) even without network connectivity. Data will be stored locally in IndexedDB and synchronized automatically when the connection is restored, adhering to the "Liquid Interface" protocol (never block the user).

## User Review Required
> [!NOTE]
> This implementation assumes the use of `vite-plugin-pwa` with `injectManifest` strategy to give us full control over the Service Worker logic.

## Proposed Changes

### Ghost Module (Driver PWA)
All changes within `modules/ghost/driver-pwa`.

#### [MODIFY] [vite.config.ts](file:///Users/VoidTracker/modules/ghost/driver-pwa/vite.config.ts)
- Configure `VitePWA` plugin:
    - Strategy: `injectManifest`
    - SrcDir: `src`
    - Filename: `sw.ts`
    - RegisterType: `autoUpdate`
    - Manifest configuration (Name, Icons, Theme Color).

#### [NEW] [src/sw.ts](file:///Users/VoidTracker/modules/ghost/driver-pwa/src/sw.ts)
- Implement Service Worker logic:
    - Precache static assets (Vue bundle, CSS).
    - Runtime caching for basic resources (fonts, images).
    - *Note: API syncing logic will largely live in the main thread (IndexedDB) to avoid complexity with SW-to-Client communication, unless Background Sync API is strictly required (simpler to start with online/offline listeners in app).*

#### [NEW] [src/services/offline-storage.ts](file:///Users/VoidTracker/modules/ghost/driver-pwa/src/services/offline-storage.ts)
- Initialize `idb` database (`voidtracker-ghost`).
- Define stores:
    - `outbox`: For queued requests (method, url, body).
    - `orders`: For caching order details for offline viewing.

#### [NEW] [src/services/sync-manager.ts](file:///Users/VoidTracker/modules/ghost/driver-pwa/src/services/sync-manager.ts)
- **QueueRequest(request)**: Save failed request to `outbox`.
- **FlushQueue()**: Iterate `outbox` and retry requests sequentially.
- **Listeners**: Listen for `window.addEventListener('online')` to trigger flush.

#### [MODIFY] [src/main.ts](file:///Users/VoidTracker/modules/ghost/driver-pwa/src/main.ts)
- Register the Service Worker.
- Initialize `SyncManager`.

#### [MODIFY] [src/App.vue](file:///Users/VoidTracker/modules/ghost/driver-pwa/src/App.vue)
- Add a global "Network Status" indicator (Toast or Icon).
    - Shows "Offline - Changes saving locally" when offline.
    - Shows "Syncing..." when connectivity restores.

## Verification Plan

### Automated Tests
- Currently, no unit tests for PWA/SW logic are planned (requires browser mocking).
- We can write a simple unit test for `offline-storage.ts` logic if `vitest` is set up.

### Manual Verification
1. **Build & Preview**:
   ```bash
   cd modules/ghost/driver-pwa
   npm run build
   npm run preview
   ```
2. **Offline Simulation**:
   - Open Browser DevTools -> Network -> Select "Offline".
   - Perform an action (e.g., if a dummy form exists, or just verify the 'Offline' indicator appears).
   - Check `Application` tab -> `IndexedDB` -> `voidtracker-ghost` -> `outbox` (Should see entry if we trigger a request).
3. **Restore Connection**:
   - Switch Network to "No throttling" (Online).
   - Verify Console logs: "Syncing items...", "Item synced".
   - Verify `outbox` is empty.
4. **Lighthouse Audit**:
   - Run Lighthouse in Chrome DevTools.
   - Verify "PWA" category is green (Installable, Service Worker registered).
