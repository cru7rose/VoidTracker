# 📝 VOIDTRACKER PROJECT PLAN (MODULAR) - 2026 EDITION

> **⚠️ PROTOKÓŁ AKTUALIZACJI:**
> Agent po każdym tasku: 
> 1. Tworzy plik w `updates/YYYY-MM-DD_HHmm_Topic.md`.
> 2. Aktualizuje status `%` i datę w tym pliku.
> 3. Sprawdza `logs/` pod kątem błędów regresji.

## 🔐 Credentials & Environment
- **Admin Testing:** `cruz` / `meduza91`
- **Stack:** Java 21 Spring Boot + Vue 3 + PostgreSQL/Neo4j + Kafka
- **Automation:** n8n + AI Agents

---

## ⚡ CURRENT CRITICAL PATH (Phase 1: The Trinity Foundation)
1. [x] `[NEXUS]` EAV Data Core Refactor (Clean Slate) <!-- id: 0 -->
   - [x] Implemented `BaseVoidEntity` (UUID, JSONB, Audit) in `danxils-commons`
   - [x] Refactored `OrderEntity` & `ClientEntity` (Void-Core)
   - [x] Implemented `RouteEntity` & `RouteStopEntity` (Titan)
   - [x] Verify Schema Auto-Generation (VERIFIED) **Status: 100%** `#NexusCore`
2. **[TITAN]** TMS Foundation (Sentinel Prime Logic) - **Status: 100%** `#TitanTMS`
3. **[VOID-CRM]** Client Profile & 5-Star Loop - **Status: 100%** `#VoidCRM`

---

## 🚀 STRATEGY: THE REVOLUTION
*Ref: PROJECT_MANAGEMENT/IDEA.md*
- **Unified Void:** Seamless flow between Order -> Client -> Transport.
- **Profit First:** Algorithmic maximization of route profitability.

---

## 1. 💎 MODULE: NEXUS (OMS)
*Quantum Ingestion & Order Management.*

- [ ] **Data Core (EAV Engine)** `#NexusCore`
  - [ ] Refactor `Order` entity: `properties` JSONB for dynamic attributes
  - [x] Implement `IngestionService`: API -> Validation -> DB
- [ ] **Smart Import**
  - [x] API Endpoint for external integrations (ERP hooks)
  - [x] n8n Workflow: Email PDF Parser -> Order Draft

## 2. ❤️ MODULE: VOID-CRM (Customer Success)
*The Empathy Layer.*

- [ ] **Client Digital Twin** `#VoidCRM`
  - [x] [ORDER] Startup Failure: `JwtAuthFilter` NoClassDefFoundError
  - **Root Cause:** Missing runtime dependency for `danxils-commons` security classes.
  - **Fix:** Copied `JwtAuthFilter` and `JwtUtil` to local package and renamed beans to avoid collision.
  - **Follow-up:** Addressed `ClassFormatError` (duplicate methods) by converting `OrderMapper` and `EpodMapper` to abstract classes.
  - [x] Entity: `ClientProfile` with preferences (Ramp hours, Notifications)
  - [x] Logic: Auto-fill order details based on history
- [ ] **5-Star Feedback Loop**
  - [x] Trigger: `DeliveryEvent.COMPLETED` -> Send Rating Request
  - [x] Dashboard: "Client Satisfaction Health"

## 3. 🛰 MODULE: TITAN (TMS)
*Kinetic Execution.*

- [x] **Routing Engine (Timefold)** ✅
- [x] **Planning Service Foundation** (Config, Security, Kafka) ✅ <!-- id: 10 -->
- [x] **Planning Service Constraint Fix** (VoidConstraintProvider NPE) ✅ **2026-01-11**
  - [x] Fixed NullPointerException in SLA time window constraint
  - [x] Verified: 0 errors, 4 successful optimization runs
- [x] **Sentinel Prime (Geofencing)** `#TitanTMS` <!-- id: 11 -->
  - [x] Logic: Predictive Arrival Time (ETA) calculation
  - [x] Alerting: Notify CRM if `PredictedETA > SLA`
- [ ] **Dynamic Slotting**
  - [x] Logic: Match `Order` to `FixedRoute` capacity
- [ ] **Optimizer Advanced Features** `#TitanAI` 🚀
  - [ ] **Addon 1: Elastic Shell Integration** (Milkrun + Ad-hoc)
    - [ ] Implement `FixedRouteShell` as pseudo-vehicle in Timefold
    - [ ] Shadow Variables: `remainingCapacity` & `availableTimeWindow`
    - [ ] Constraint: `detourDistance < maxDetourKm` (user-defined in Vue)
    - [ ] Logic: AI dołącza `RouteStop` tylko jeśli nie przekracza detour limit
  - [/] **Addon 2: Gatekeeper Agent Logic** (n8n Integration) **Status: 50%**
    - [x] Backend: GatekeeperService score monitoring (20% threshold)
    - [x] Warning generation for significant changes
    - [ ] n8n Webhook trigger (code exists but disabled)
    - [ ] LLM Agent generuje uzasadnienie po polsku
    - [ ] User Approval Flow w Vue (Confirm/Reject)
    - [ ] Publikacja do kierowcy dopiero po zatwierdzeniu
  - [/] **Addon 3: High-Fidelity Dashboard** (Spotify Feel) **Status: 40%**
    - [x] Spring WebSockets (STOMP) config ready (/ws-planning endpoint)
    - [ ] Frontend SockJS client implementation
    - [ ] Progressive UI: Mapa update'uje się podczas Timefold solving
    - [ ] `BestSolutionChangedEvent` → Live Map Refresh  
    - [x] Command Bar backend (CommandController in dashboard-service)
    - [ ] Command Bar frontend integration test

## 4. 👻 MODULE: GHOST (PWA Interface)
*Interface is Instruction.*

- [ ] **Driver App Core** `#GhostPWA`
  - [x] Auth & Route View
  - [x] **Offline Sync Engine** (IndexedDB)
  - [x] **Scanner Module** (HTML5-QR)

## 5. 🎨 MODULE: VOID-FLOW (Control Tower)
*Spotify-style Management.*

- [ ] **Lenses UI**
  - [x] Map Overlay: Profitability Heatmap
  - [x] Map Overlay: Risk/Delay Radar `#VoidFlow`
  - [x] **Connect Risk Lens to Live Graph API** `#VoidFlow`
    - [x] Polling logic for `/api/planning/graph/driver/{id}`
    - [x] Dynamic Marker Updates
- [ ] **Command Bar (NLP)**
  - [x] Input component for natural language queries
  - [ ] **Implement NLP Backend Logic** `#VoidFlow`
    - [ ] Create `CommandController` (Dashboard/Planning)
    - [ ] Define Intent Schema (JSON)
    - [ ] Implement Regex/Heuristic Parser (MVP)
    - [ ] Connect Frontend Input to API

---

## 📊 READINESS MATRIX
| Service | Status | Vibe Check |
| :--- | :--- | :--- |
| **NEXUS (OMS)** | 🏗 REFACTOR | Moving to EAV |
| **TITAN (TMS)** | ⚠️ ALPHA | Routing works, Sentinel pending |
| **CRM** | 🌑 CONCEPT | Needs Entity Structure |
| **Void-Mesh** | ✅ RELEASE | Graph Model Expanded (Driver/Route) |