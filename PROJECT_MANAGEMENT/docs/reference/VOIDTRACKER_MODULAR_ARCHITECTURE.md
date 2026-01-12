# VOIDTRACKER MODULAR ARCHITECTURE (THE TRINITY)

> **Philosophy:** "Decompose by Subdomain, not by Noun."

## 1. The Trinity (Bounded Contexts)
The system is divided into three strict Bounded Contexts. Code dependencies *between* these contexts must be loosely coupled (Event-Driven or API-based), never direct database sharing.

### 🔵 NEXUS (Order Management System - OMS)
**Goal:** The "Brain" of the operation.
*   **Responsibilities:** Order Intake, Customer Management, Billing, User Identity (IAM), Analytics.
*   **Services:** `order-service`, `iam-service`, `analytics-service`, `dashboard-service`, `admin-panel-service`.
*   **Location:** `/modules/nexus/`

### 🔴 TITAN (Transport Management System - TMS)
**Goal:** The "Muscle" of the operation.
*   **Responsibilities:** Execution, Driver Management, Tracking, Geofencing, Mobile App Backend.
*   **Services:** `tracking-service`, `driver-app-service`, `audit-service`.
*   **Location:** `/modules/titan/`

### 🟢 FLUX (Optimization Engine)
**Goal:** The "Logic" of the operation.
*   **Responsibilities:** Route Planning, Solving VRP (Vehicle Routing Problems), Time & Distance matrix.
*   **Services:** `planning-service`.
*   **Location:** `/modules/flux/`

---

## 2. The Frontend Elements

### 👻 GHOST (Driver PWA)
**Goal:** Invisible, lightweight interface for drivers.
*   **Stack:** Vue 3, Vite, Tailwind.
*   **Location:** `/modules/ghost/`

### 🖥️ VOIDTRACKER WEB (Admin Console)
**Goal:** Mission Control.
*   **Stack:** Vue 3 (Legacy Dashboard).
*   **Location:** `/modules/web/`

---

## 3. Directory Structure Rules
```
/modules
  /nexus      # OMS Services
  /titan      # TMS Services
  /flux       # Optimization Services
  /ghost      # Driver App
  /web        # Admin Website
  /shared     # Shared Libraries (if any)
```

## 4. Communication Protocol
1.  **Inter-Module:** REST (Feign Clients) or Kafka Events.
2.  **Database:** Each Service owns its own Schema/DB. NO Shared Tables.
