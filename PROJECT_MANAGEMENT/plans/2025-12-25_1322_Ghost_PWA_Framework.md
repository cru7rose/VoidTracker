# The Ghost: Driver PWA Framework Implementation Plan

## Goal
Establish a modern, zero-technical-debt Progressive Web Application (PWA) for drivers.
Technology Stack: **Vue 3 (Script Setup) + Vite + Tailwind CSS + Pinia**.
Key Requirements: Offline-first architecture (Service Worker), Mobile-first design, "One-Time" Identity integration.

## User Review Required
> [!TIP]
> **Zero Tech Debt Strategy**:
> *   **Strict TypeScript**: No `any` types.
> *   **Component Library**: We will use a headless UI library (e.g., Headless UI or Radix) + Tailwind for maximum control and performance, implementing the "Premium" aesthetic requested.
> *   **State Management**: Pinia for strictly typed stores.

## Proposed Changes

### [NEW] [modules/ghost/driver-pwa](file:///modules/ghost/driver-pwa)
Initialize a new Vite project.

#### 1. Project Structure
*   `src/stores/auth.ts`: Auth logic using Magic Link JWT.
*   `src/views/LoginView.vue`: Minimalist, numeric keypad or simple link request UI.
*   `src/sw.ts`: Service Worker using `vite-plugin-pwa` for caching static assets and API requests (offline mode).

#### 2. Core Features
*   **Authentication**: Integrate with `titan/event-emitter-service` (Magic Link Exchange).
*   **Offline Storage**: IndexedDB wrapper (using generic `idb` or `localforage`) for storing Route/Stop data.

## Verification Plan

### Manual Verification
*   **Build**: Run `npm run build` to verify zero type errors.
*   **Dev Server**: Run `npm run dev` and open in browser.
*   **Lighthouse Audit**: Verify PWA scores (aiming for 100/100).
