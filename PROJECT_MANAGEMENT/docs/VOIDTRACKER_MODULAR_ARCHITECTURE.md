# VOIDTRACKER MODULAR SYSTEM ARCHITECTURE
> **Standard:** Enterprise Modular Monolith (Java 21 / Vue 3 PWA)
> **Philosophy:** "The Liquid Enterprise" - Configuration over Code.

## 1. THE TRINITY MODULES
We split the "Danxils" monolith into three distinct, loosely coupled bounded contexts.

### A. MODULE: OMS (Order Management System) - "The Nexus"
*   **Role:** The Static Truth. Handles Data, Pricing, and Users.
*   **Core Entities:**
    *   `AssetDefinition`: The JSON Schema registry (e.g., "Medical Sample" vs "Pallet").
    *   `Order/Shipment`: The transactional record.
    *   `RateCard`: The pricing logic engine.
*   **Key Capability:** **EAV (Entity-Attribute-Value)**.
    *   *Requirement:* Adding a new field "Is Radioactive?" to a shipment type MUST NOT require a code deployment. It is a JSON config change in the Admin Panel.

### B. MODULE: TMS (Transportation Management System) - "Titan"
*   **Role:** The Kinetic Truth. Handles Movement, Drivers, and Events.
*   **Core Entities:**
    *   `DriverProfile`: Identity and credentials.
    *   `Manifest`: A grouped list of stops for a vehicle.
    *   `StopEvent`: The atomic unit of work (Scan, Photo, Signature).
*   **Key Capability:** **Geofence Guard**.
    *   *Requirement:* 300m Tolerance Rule. If a scan happens outside the fence, it is flagged as an anomaly automatically.

### C. MODULE: OPTIMIZER - "Flux"
*   **Role:** The Intelligence. Handles Route Planning.
*   **Core Entities:**
    *   `Zone/District`: Polygon definitions.
    *   `RouteParams`: Constraints (Time windows, Vehicle capacity).
    *   `Solution`: The output plan.
*   **Key Capability:** **Timefold / Vroom Integration**.
    *   *Requirement:* "Continuous Optimization". Routes can be re-calculated mid-day if a new high-priority order arrives.

---

## 2. THE "GHOST" PWA (Progressive Web App)
> **Concept:** The interface IS the instruction.

*   **Technology:** Vue 3 + Vite + Tailwind CSS.
*   **Features:**
    1.  **Offline-First:** Uses `IndexedDB` to store the manifest. Can operate fully without 5G/LTE. Syncs when connectivity returns.
    2.  **Universal Scanner:** Built-in `html5-qrcode`. No need for native external apps.
    3.  **Magic Link Auth:** No passwords. Driver receives an SMS link that auto-logs them in for 12 hours.

---

## 3. EVENT-DRIVEN NERVOUS SYSTEM
We stop using direct HTTP calls between modules for side effects. We use Events.

*   **Workflow:**
    1.  `TMS` emits `event: shipment.delivered`.
    2.  `N8N` (Workflow Engine) listens.
    3.  `N8N` triggers:
        *   Send SMS to Customer.
        *   Update `OMS` status to "BILLED".
        *   Post to Slack "Control Tower".

---

## 4. DIRECTORY STANDARD & REORGANIZATION
To achieve this, we will restructure the project source code:

```
/src
  /main/java/com/voidtracker
    /nucleus   (Shared Kernel: Auth, Commons, Config)
    /oms       (Order Logic, Asset Definitions, Pricing)
    /tms       (Driver Logic, Geofencing, Scanning)
    /optimizer (Routing Solvers)
```

**Next Steps:**
1.  Verify strict separation of packages.
2.  Implement the PWA "Ghost" interface in `api-frontend` or dedicated repo.
