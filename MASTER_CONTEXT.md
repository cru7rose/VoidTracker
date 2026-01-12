# VOIDTRACKER: MASTER CONTEXT & RULES OF ENGAGEMENT
> **START HERE.** This file is the "Context Injection" for every AI session. SYSTEM HAVE TO BE CAPABLE TO MOVE OVER 100 000 ++ ORDERS PER MONTH.
WITHOUT ANY LAGS OR USER CRASHES/ERRORS.
I AM GOING TO PRESENT IT IN FRONT OF THE DIRECTORS BOARD IN 1-2 MONTHS. I AMING IN SUBSCRIPTION BASES SYTEM,SO I CAN HAVE SOME PICE OF THE CAKE.

## 1. THE MISSION
We are building **VoidTracker (OMNI-NEXUS)**: A modular, enterprise-grade logistics operating system.
*   **Goal:** Replace legacy Transportation Management Systems (TMS) with a "Liquid Enterprise" architecture.
*   **Philosophy:** Configuration over Code. Business logic is defined in JSON Schemas, not hardcoded Java/Vue.

## 2. THE MODULAR ARCHITECTURE (The Trinity)
The system is strictly divided into three modules (Bounded Contexts):
1.  **OMS "NEXUS"** (Order Management): Handles Asset Definitions (EAV), Pricing (Rate Cards), and Users.
2.  **TMS "TITAN"** (Execution): Handles Drivers, Manifests, and Geofencing (300m Guard Rule).
3.  **OPTIMIZER "FLUX"** (Planning): Logic engine for Routes (Timefold/Vroom).

**The Frontend:**
*   **PWA "GHOST"**: A single Vue 3 + Tailwind app for drivers. Offline-first (IndexedDB). No App Store.

## 3. PROJECT STRUCTURE (Canonical)
All work happens inside `/Users/VoidTracker/PROJECT_MANAGEMENT`:
*   `task.md`: **The Central Project Plan.** Always check this first.
*   `updates/`: **The History.** Every task completion = New file here (`YYYY-MM-DD_HHmm_Topic.md`).
*   `plans/`: **The Blueprint Archive.** Archive every approved `implementation_plan.md` here (`YYYY-MM-DD_HHmm_Topic.md`).
*   `docs/reference/`: Deep technical specs, legacy concepts, and architectural decisions.
*   `infrastructure/`: Docker, Kafka, Connectors.
*   `tools/`: Python seeds, verification scripts.

## 4. THE AI PROTOCOL (Rules for You)
1.  **Check `task.md` first.** Never start work without knowing the current phase.
2.  **Log your work.** Before you finish, you MUST create an update log in `updates/`.
3.  **Respect the Architecture.** Do not put business logic in the PWA. Emit Events.
4.  **No "Guessing".** If a requirement is missing, ask.
5.  **Clean Code.** Use the existing patterns (EAV, Event-Driven).

---
*Last Updated: 2025-12-25 12:30*
