# 🎯 PLANNING SERVICE - Kompletna Dokumentacja Architektury

**Autor:** Antigravity AI Agent  
**Data:** 2026-01-13  
**Przeznaczenie:** Przekazanie wiedzy o Planning Service dla innego Agenta AI  
**Projekt:** VoidTracker (Moduł FLUX)

---

## 📚 SPIS TREŚCI

1. [Przegląd Systemu](#przegląd-systemu)
2. [Założenia Architektoniczne](#założenia-architektoniczne)
3. [Przepływ Danych](#przepływ-danych)
4. [Implementowane Funkcjonalności](#implementowane-funkcjonalności)
5. [Co Jest Zrobione](#co-jest-zrobione)
6. [Co Jest Do Zrobienia](#co-jest-do-zrobienia)
7. [Integracje](#integracje)
8. [Konfiguracja Driver PWA](#konfiguracja-driver-pwa)

---

## 🌟 PRZEGLĄD SYSTEMU

### Kontekst Biznesowy
Planning Service (FLUX) to moduł optymalizacji tras w systemie VoidTracker. Jego głównym zadaniem jest:
- Automatyczna optymalizacja zamówień do tras za pomocą Timefold Solver
- Zarządzanie przypisaniami tras do kierowców i pojazdów
- Publikacja tras do aplikacji mobilnej dla kierowcy (Ghost PWA)
- Automatyczne skalowanie pojazdów i zarządzanie flotą

### Pozycja w Architekturze
```
┌─────────────────────────────────────────────────────────┐
│                    CONTROL TOWER (Web UI)                │
│              Dyspozytornia - Vue 3 + Deck.gl            │
└────────────────────┬────────────────────────────────────┘
                     │
        ┌────────────┼────────────┐
        │            │            │
┌───────▼──────┐ ┌──▼─────────┐ ┌▼──────────┐
│   NEXUS      │ │   FLUX     │ │   TITAN   │
│   (OMS)      │ │ (Planning) │ │   (TMS)   │
│ order-service│ │   THIS!    │ │ mesh-graph│
└───────┬──────┘ └──┬─────────┘ └┬──────────┘
        │           │             │
        └───────────┼─────────────┘
                    │
            ┌───────▼────────┐
            │     KAFKA      │
            │  Event Stream  │
            └────────────────┘
                    │
            ┌───────▼────────┐
            │   GHOST PWA    │
            │  Driver Mobile │
            └────────────────┘
```

---

## 🏛️ ZAŁOŻENIA ARCHITEKTONICZNE

### 1. Event-Driven Architecture
- **Kafka Topics używane:**
  - `orders.created` - nowe zamówienia z Nexus
  - `routes.optimized` - wynik optymalizacji Timefold
  - `routes.published` - trasa opublikowana do kierowcy
  - `driver.assignment.created` - nowe przypisanie kierowcy

### 2. Persistence Strategy
- **PostgreSQL** jako główna baza danych
- **JSONB** dla dynamicznych danych (route data, workflow config)
- **UUID** jako primary key dla większości encji
- **Hibernate/JPA** z Liquibase dla migracji

### 3. Design Patterns
- **Repository Pattern** - dostęp do danych
- **Service Layer** - logika biznesowa
- **DTO Pattern** - separacja API od modelu domeny
- **Strategy Pattern** - różne profile optymalizacji

---

## 🔄 PRZEPŁYW DANYCH - ORDERS → OPTIMIZATION → ASSIGNMENTS

### KROK 1: Orders → Dyspozytornia
```
┌──────────────┐
│ Control Tower│
│  Orders View │
└──────┬───────┘
       │ User selects 3-5 orders
       │ Clicks "Send to Dispatch"
       ▼
┌──────────────┐
│   Frontend   │
│   Store      │ Stores selected orderIds in Vuex/Pinia
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ Dispatch View│ Redirect to /dispatch with notification
└──────────────┘
```

**Status:** ✅ ZREALIZOWANE

**Pliki:**
- `/modules/web/voidtracker-web/src/views/OrdersView.vue`
- `/modules/web/voidtracker-web/src/views/DispatchView.vue`

---

### KROK 2: Optymalizacja (Dyspozytornia → Timefold)

```
┌────────────────────┐
│  DispatchView.vue  │
│                    │
│ 1. Select Profile  │ (DEFAULT, FAST, ECO)
│ 2. Click Optimize  │
└─────────┬──────────┘
          │
          │ POST /api/planning/optimization/solution
          │ Body: { orderIds: [...], profileId: "DEFAULT" }
          ▼
┌────────────────────────────────────┐
│  OptimizationController.java       │
│  optimizeRoutes()                  │
└─────────┬──────────────────────────┘
          │
          ▼
┌────────────────────────────────────┐
│  VrpOptimizerService.java          │
│                                    │
│  1. Fetch orders from order-service│
│  2. Fetch vehicles from DB         │
│  3. Build Timefold problem         │
│  4. Run solver (30-60s)            │
│  5. Return solution                │
└─────────┬──────────────────────────┘
          │
          │ VehicleRoutingSolution
          ▼
┌────────────────────────────────────┐
│  OptimizationSolutionEntity        │
│  Saved to DB with:                 │
│  - solutionId (UUID)               │
│  - solutionData (JSONB)            │
│  - score                           │
│  - timestamp                       │
└─────────┬──────────────────────────┘
          │
          │ Return enriched solution
          ▼
┌────────────────────────────────────┐
│  Frontend ROUTES Tab               │
│  Displays routes on map            │
│  (Deck.gl PathLayer)               │
└────────────────────────────────────┘
```

**Status:** ✅ ZREALIZOWANE

**Key Classes:**
- `VrpOptimizerService` - główny serwis optymalizacji
- `OptimizationSolutionEntity` - persystencja wyników
- `VoidConstraintProvider` - reguły Timefold (time windows, capacity, SLA)

---

### KROK 3: Auto-Save Routes → Assignments

```
┌────────────────────────────────────┐
│  DispatchView.vue                  │
│  After optimization completes:     │
│                                    │
│  routes.forEach(route => {         │
│    POST /api/planning/assignments  │
│    body: {                         │
│      routeName: "Route A",         │
│      solutionId: UUID,             │
│      vehicleId: null,  // not yet  │
│      driverId: null,   // assigned │
│      routeData: {...stops...},     │
│      status: "DRAFT"               │
│    }                               │
│  })                                │
└─────────┬──────────────────────────┘
          │
          ▼
┌────────────────────────────────────┐
│  RouteAssignmentController         │
│  createBatchAssignments()          │
└─────────┬──────────────────────────┘
          │
          ▼
┌────────────────────────────────────┐
│  RouteAssignmentService            │
│  saveRouteAssignment()             │
└─────────┬──────────────────────────┘
          │
          ▼
┌────────────────────────────────────┐
│  RouteAssignmentEntity (DB)        │
│  Fields:                           │
│  - id (UUID)                       │
│  - routeName (String)              │
│  - solutionId (UUID FK)            │
│  - vehicleId (UUID) - nullable     │
│  - driverId (UUID) - nullable     │
│  - carrierId (UUID) - nullable     │
│  - routeData (JSONB)               │
│  - status (ENUM)                   │
│  - createdAt, updatedAt            │
└────────────────────────────────────┘
```

**Status:** ✅ ZREALIZOWANE (2026-01-12)

**Conversation ID:** `99566e23-c8ea-41d5-b72e-fb12bd4986b6`

---

### KROK 4: Assignment Management (Assignments Tab)

```
┌────────────────────────────────────┐
│  ASSIGNMENTS Tab (DispatchView)    │
│                                    │
│  GET /api/planning/assignments     │
│  → Display list of saved routes    │
│                                    │
│  User clicks route → Modal opens   │
└─────────┬──────────────────────────┘
          │
          ▼
┌────────────────────────────────────┐
│  AssignmentEditModal.vue           │
│                                    │
│  Form fields:                      │
│  - Driver dropdown (from IAM)      │
│  - Vehicle dropdown (from profiles)│
│  - Carrier (auto-detected)         │
│  - Status (DRAFT/ASSIGNED/etc)     │
│                                    │
│  User fills form → Click Save      │
└─────────┬──────────────────────────┘
          │
          │ PUT /api/planning/assignments/{id}
          ▼
┌────────────────────────────────────┐
│  RouteAssignmentController         │
│  updateAssignment()                │
└─────────┬──────────────────────────┘
          │
          ▼
┌────────────────────────────────────┐
│  RouteAssignmentService            │
│  updateRouteAssignment()           │
│                                    │
│  Updates:                          │
│  - driverId = selected UUID        │
│  - vehicleId = selected UUID       │
│  - carrierId = auto from vehicle   │
│  - status = ASSIGNED               │
└────────────────────────────────────┘
```

**Status:** ✅ ZREALIZOWANE

---

### KROK 5: Publish to Driver (Magic Link Generation)

```
┌────────────────────────────────────┐
│  AssignmentEditModal.vue           │
│                                    │
│  After assigning driver:           │
│  User clicks "PUBLISH" button      │
└─────────┬──────────────────────────┘
          │
          │ POST /api/planning/assignments/{id}/publish
          ▼
┌────────────────────────────────────┐
│  RouteAssignmentController         │
│  publishRoute()                    │
└─────────┬──────────────────────────┘
          │
          ▼
┌────────────────────────────────────┐
│  RouteAssignmentService            │
│  publishRouteToDriver()            │
│                                    │
│  1. Validate driverId exists       │
│  2. Generate Magic Link token       │
│  3. Update status → PUBLISHED       │
│  4. Send email (stub)              │
│  5. Return magic link URL          │
└─────────┬──────────────────────────┘
          │
          ▼
┌────────────────────────────────────┐
│  Magic Link Token Store            │
│  (In-memory HashMap)               │
│                                    │
│  Key: UUID token                   │
│  Value: {                          │
│    driverId: UUID,                 │
│    routeId: UUID,                  │
│    expiresAt: Instant (24h)        │
│  }                                 │
└─────────┬──────────────────────────┘
          │
          │ Magic Link URL generated:
          │ https://driver.voidtracker.app/auth?token={UUID}
          ▼
┌────────────────────────────────────┐
│  Email/SMS Service (TODO)          │
│  Send magic link to driver         │
└────────────────────────────────────┘
```

**Status:** ✅ BACKEND DONE | ⚠️ EMAIL INTEGRATION TODO

**Magic Link Format:**
```
https://driver.voidtracker.app/auth?token=550e8400-e29b-41d4-a716-446655440000
```

**Token Expiry:** 24 hours (configurable)

---

## 📦 IMPLEMENTOWANE FUNKCJONALNOŚCI

### ✅ 1. Automatyczne Skalowanie Samochodu z Vehicle Profiles

**Lokalizacja:** `/modules/flux/planning-service/src/main/java/com/example/planning_service/entity/VehicleProfileEntity.java`

**Struktura:**
```java
@Entity
@Table(name = "planning_vehicle_profiles")
public class VehicleProfileEntity {
    @Id
    private String id;  // "VAN_LARGE", "TRUCK_10T"
    
    private String name;
    private Double maxCapacityWeight;  // kg
    private Double maxCapacityVolume;  // m³
    
    @ElementCollection
    private Set<String> capabilities;  // ["REFRIGERATED", "TAIL_LIFT"]
}
```

**Jak działa:**
1. Timefold Solver pobiera dostępne pojazdy z `FleetVehicleRepository`
2. Każdy pojazd ma referencję do `VehicleProfile` (np. "VAN_LARGE")
3. Constraint Provider sprawdza:
   - `totalWeight <= vehicle.profile.maxCapacityWeight`
   - `totalVolume <= vehicle.profile.maxCapacityVolume`
   - `order.requiredSkills ⊆ vehicle.profile.capabilities`

**Status:** ✅ ZAIMPLEMENTOWANE

**Gdzie to widać:**
- `VrpOptimizerService.buildVehicle()` - buduje obiekt pojazdu dla Timefold
- `VoidConstraintProvider.vehicleCapacity()` - hard constraint

---

### ✅ 2. Automatyczne Dodawanie Carrier z Carrier Compliance

**Lokalizacja:** `/modules/flux/planning-service/src/main/java/com/example/planning_service/entity/CarrierComplianceEntity.java`

**Struktura:**
```java
@Entity
@Table(name = "planning_carrier_compliance")
public class CarrierComplianceEntity {
    @Id
    private String carrierId;
    
    private Boolean isInsured;
    private LocalDate insuranceExpiryDate;
    private String complianceStatus;  // "COMPLIANT", "NON_COMPLIANT", "SUSPENDED"
}
```

**Przepływ:**
1. Pojazd (`FleetVehicleEntity`) ma pole `carrierId`
2. Przy przypisaniu pojazdu do trasy, system automatycznie:
   ```java
   FleetVehicle vehicle = vehicleRepo.findById(vehicleId);
   assignment.setCarrierId(vehicle.getCarrierId());
   ```
3. Przed publikacją trasy sprawdzana jest compliance:
   ```java
   CarrierCompliance compliance = complianceRepo.findById(carrierId);
   if (!compliance.getComplianceStatus().equals("COMPLIANT")) {
       throw new IllegalStateException("Carrier not compliant!");
   }
   ```

**Status:** ✅ ENTITY CREATED | ⚠️ VALIDATION LOGIC TODO

---

### ✅ 3. Dodawanie Kierowcy z Integracją IAM

**Flow:**
```
┌────────────────────────────────────┐
│  AssignmentEditModal.vue           │
│                                    │
│  1. Component mounted              │
│  2. Fetch drivers:                 │
│     GET /api/auth/users            │
│     ?role=ROLE_DRIVER              │
└─────────┬──────────────────────────┘
          │
          ▼
┌────────────────────────────────────┐
│  IAM Service                       │
│  UserController.getUsers()         │
│                                    │
│  Returns: [                        │
│    {                               │
│      id: UUID,                     │
│      username: "jan.kowalski",     │
│      email: "jan@example.com",     │
│      roles: ["ROLE_DRIVER"]        │
│    }                               │
│  ]                                 │
└─────────┬──────────────────────────┘
          │
          ▼
┌────────────────────────────────────┐
│  Frontend Dropdown                 │
│  User selects driver               │
│  → driverId = selected UUID        │
└─────────┬──────────────────────────┘
          │
          │ Save assignment
          ▼
┌────────────────────────────────────┐
│  RouteAssignmentEntity             │
│  driverId = UUID from IAM          │
└────────────────────────────────────┘
```

**Status:** ✅ ZAIMPLEMENTOWANE

---

### ✅ 4. Magic Link do PWA dla Kierowcy

**Mechnaizm:**
1. **Token Generation:**
   ```java
   String token = UUID.randomUUID().toString();
   Instant expiresAt = Instant.now().plus(24, ChronoUnit.HOURS);
   MagicLinkToken linkToken = new MagicLinkToken(driverId, routeId, expiresAt);
   tokenStore.put(token, linkToken);
   ```

2. **Email/SMS (TODO):**
   ```
   Subject: Twoja trasa na dzisiaj
   
   Witaj Jan!
   
   Kliknij poniższy link aby zobaczyć trasę:
   https://driver.voidtracker.app/auth?token=550e8400-e29b-41d4-a716-446655440000
   
   Link wygasa za 24h.
   ```

3. **Driver PWA Login:**
   ```javascript
   // Ghost PWA: /auth route
   const token = route.query.token;
   const response = await fetch(`/api/planning/auth/validate?token=${token}`);
   if (response.ok) {
     const { driverId, routeId } = await response.json();
     // Store in IndexedDB
     // Redirect to route view
   }
   ```

**Status:** ✅ BACKEND READY | ⚠️ EMAIL SERVICE TODO | ⚠️ PWA AUTH TODO

---

## 👻 GHOST PWA - Driver Workflow Configuration

### Struktura Konfiguracji Workflow

**Entity:** `DriverWorkflowConfigEntity`

**Przykładowa konfiguracja (workflowJson):**
```json
{
  "configCode": "DEFAULT_DELIVERY",
  "steps": [
    "SCAN_BARCODE",
    "SCAN_DELIVERY_CODE",
    "TAKE_PHOTO_DMG",
    "TAKE_PHOTO_POD",
    "SIGNATURE"
  ],
  "scan": {
    "barcode": {
      "enabled": true,
      "allowManual": true,
      "requireMatch": true
    },
    "deliveryCode": {
      "enabled": "conditional",  // based on address/client
      "source": "client.requiresDeliveryCode"
    }
  },
  "photo": {
    "dmg": {
      "requireLocation": true,
      "minCount": 0,
      "label": "Damage Photo (Optional)"
    },
    "pod": {
      "requireLocation": true,
      "minCount": 1,
      "label": "Proof of Delivery"
    }
  },
  "signature": {
    "required": true,
    "captureRecipientName": true
  },
  "geofence": {
    "radiusMeters": 300,
    "alertOnViolation": true,
    "allowOverride": false
  },
  "statuses": [
    "IN_TRANSIT",
    "ARRIVED",
    "LOADING",
    "UNLOADING",
    "POD",
    "ISSUE",
    "COMPLETED"
  ]
}
```

### Różne Statusy Dostawy

**Supported Statuses:**
- `IN_TRANSIT` - w drodze do lokalizacji
- `ARRIVED` - przyjazd na miejsce (geofence trigger)
- `LOADING` - załadunek (pickup)
- `UNLOADING` - rozładunek (delivery)
- `POD` - Proof of Delivery (zdjęcie + podpis)
- `ISSUE` - problem (brak odbiorcy, uszkodzony towar)
- `COMPLETED` - zakończone

### Skanowanie Kodu Dostawy (Conditional)

**Logika:**
1. Order ma przypisany `deliveryAddress`
2. Address ma pole `requiresDeliveryCode: boolean`
3. Client (Customer) ma pole `scanDeliveryCodePolicy: "ALWAYS" | "NEVER" | "IF_RAMP"`

**Implementacja w PWA:**
```javascript
// Ghost PWA: components/StopActionSheet.vue
async checkIfDeliveryCodeRequired(stop) {
  const address = stop.deliveryAddress;
  const client = stop.order.client;
  
  if (client.scanDeliveryCodePolicy === "ALWAYS") {
    return true;
  }
  if (client.scanDeliveryCodePolicy === "NEVER") {
    return false;
  }
  // IF_RAMP
  return address.requiresDeliveryCode === true;
}
```

**Status:** ⚠️ SCHEMA DEFINED | ❌ PWA IMPLEMENTATION TODO

---

### Zdjęcia DMG i POD

**DMG (Damage):**
- Opcjonalne (minCount: 0)
- Wymagane geolocation w EXIF
- Trigger: Kierowca widzi uszkodzony towar
- Upload do: `/api/planning/media/upload/dmg`

**POD (Proof of Delivery):**
- Wymagane (minCount: 1)
- Geolocation required
- Trigger: Po dostawie towaru
- Upload do: `/api/planning/media/upload/pod`

**Storage:**
```
/uploads/
  /{routeId}/
    /{stopId}/
      dmg_001.jpg
      dmg_002.jpg
      pod_001.jpg
      signature.png
```

**Status:** ⚠️ API ENDPOINTS TODO | ⚠️ PWA CAMERA TODO

---

## 🔧 CO JEST ZROBIONE

### ✅ Backend (Planning Service)

1. **Optimization Engine**
   - Timefold Solver integration
   - Constraint Provider (time windows, capacity, SLA)
   - Multiple optimization profiles (DEFAULT, FAST, ECO)

2. **Route Persistence**
   - `OptimizationSolutionEntity` - optimization results
   - `RouteAssignmentEntity` - route assignments
   - CRUD API for assignments (7 endpoints)

3. **Entities**
   - `VehicleProfileEntity` - vehicle templates
   - `CarrierComplianceEntity` - carrier validation
   - `DriverWorkflowConfigEntity` - PWA workflow
   - `FleetVehicleEntity` - actual vehicles
   - `RouteAssignmentEntity` - route-driver binding

4. **Magic Link System**
   - Token generation (24h expiry)
   - In-memory token store
   - Validation endpoint (stub)

5. **Controllers**
   - `OptimizationController` - run optimizer
   - `RouteAssignmentController` - CRUD assignments
   - `DispatchController` - manual route append

6. **Services**
   - `VrpOptimizerService` - core Timefold logic
   - `RouteAssignmentService` - assignment management
   - `DriverEnrichmentService` - fetch driver data

### ✅ Frontend (Control Tower)

1. **Dispatch View**
   - Map visualization (Deck.gl)
   - 3 tabs: ROUTES, ASSIGNMENTS, VEHICLES
   - Optimization workflow UI

2. **Assignment Management**
   - `AssignmentEditModal.vue` - edit modal
   - Driver/vehicle dropdowns
   - Status lifecycle management
   - Publish button

3. **Orders View**
   - "Send to Dispatch" workflow
   - Batch selection
   - Notification system

---

## ❌ CO JEST DO ZROBIENIA

### 🔴 HIGH PRIORITY

1. **Email/SMS Service dla Magic Links**
   - Integracja z SendGrid lub AWS SES
   - Template dla magic link email
   - SMS gateway (opcjonalnie)

2. **Ghost PWA Authentication**
   - `/auth` route w PWA
   - Token validation endpoint
   - IndexedDB session storage

3. **Ghost PWA Route View**
   - Fetch route by driverId
   - Display stops with map
   - Navigation integration

4. **Ghost PWA Workflow Steps**
   - Barcode scanner (HTML5-QR lub QuaggaJS)
   - Camera module (foto DMG/POD)
   - Signature capture (Canvas API)
   - Status update buttons

5. **Media Upload API**
   - `POST /api/planning/media/upload/{type}` (dmg, pod, signature)
   - S3 storage lub local filesystem
   - EXIF geolocation validation

### 🟡 MEDIUM PRIORITY

6. **Carrier Compliance Validation**
   - Auto-check przed publish
   - Warning UI gdy carrier SUSPENDED
   - Insurance expiry alerts

7. **Driver Enrichment - Real Data**
   - `RouteAssignmentService.enrichResponse()` - obecnie mock
   - Fetch real names z IAM service
   - Fetch vehicle names z mesh

8. **Delivery Code Conditional Logic**
   - Address field: `requiresDeliveryCode`
   - Client policy: `scanDeliveryCodePolicy`
   - PWA conditional rendering

9. **Geofencing w PWA**
   - Watchdog dla GPS position
   - Alert gdy kierowca poza geofence
   - Auto-update status "ARRIVED"

### 🟢 LOW PRIORITY (Future)

10. **Real-time WebSocket Updates**
    - Planning Service → Frontend
    - Live map refresh during solving
    - Driver status broadcast

11. **Advanced Timefold Addons**
    - Elastic Shell (milkrun + ad-hoc)
    - Gatekeeper Agent (n8n LLM approval)
    - High-Fidelity Dashboard (progressive solve)

12. **Analytics Dashboard**
    - Route efficiency metrics
    - Driver performance KPIs
    - Cost per km tracking

---

## 🔌 INTEGRACJE

### 1. Nexus (Order Service)

**Endpoint:** `GET /api/orders?ids={id1,id2,id3}`

**Używane przez:** `VrpOptimizerService.fetchOrdersFromOrderService()`

**Data Contract:**
```json
{
  "id": "uuid",
  "barcode": "ORD12345",
  "pickupAddress": {
    "lat": 52.229676,
    "lon": 21.012229,
    "city": "Warszawa"
  },
  "deliveryAddress": { ... },
  "weight": 150.5,
  "volume": 0.8,
  "requiredSkills": ["TAIL_LIFT"],
  "timeWindow": {
    "start": "2026-01-13T08:00:00Z",
    "end": "2026-01-13T16:00:00Z"
  }
}
```

**Status:** ✅ INTEGRATION WORKING

---

### 2. IAM Service

**Endpoint:** `GET /api/auth/users?role=ROLE_DRIVER`

**Używane przez:** Frontend (AssignmentEditModal)

**Data Contract:**
```json
[
  {
    "id": "uuid",
    "username": "jan.kowalski",
    "email": "jan@example.com",
    "phone": "+48123456789",
    "roles": ["ROLE_DRIVER"]
  }
]
```

**Status:** ✅ INTEGRATION WORKING

---

### 3. Titan (Danxils Mesh - Graph DB)

**Endpoint:** `GET /api/graph/driver/{id}`

**Używane przez:** Real-time tracking (future)

**Status:** ⚠️ NOT YET INTEGRATED in Planning Service

---

### 4. Ghost PWA (Driver App)

**Endpoints Planning Service exposes for PWA:**

1. `GET /api/planning/auth/validate?token={uuid}`
   - Validates magic link
   - Returns: `{ driverId, routeId, expiresAt }`

2. `GET /api/planning/driver/{driverId}/route`
   - Fetch active route for driver
   - Returns route with stops, navigation

3. `POST /api/planning/driver/status`
   - Update stop status (ARRIVED, POD, etc)
   - Body: `{ stopId, status, location, timestamp }`

4. `POST /api/planning/media/upload/{type}`
   - Upload photos/signatures
   - Multipart form data

**Status:** ⚠️ ENDPOINTS TODO

---

## 📊 DATABASE SCHEMA (Planning Service)

### Main Tables

```sql
-- Optimization results
CREATE TABLE optimization_solutions (
    id UUID PRIMARY KEY,
    solution_data JSONB NOT NULL,
    score VARCHAR(255),
    created_at TIMESTAMP,
    profile_id VARCHAR(50)
);

-- Route assignments (NEW - Jan 2026)
CREATE TABLE route_assignments (
    id UUID PRIMARY KEY,
    route_name VARCHAR(255) NOT NULL,
    solution_id UUID REFERENCES optimization_solutions(id),
    vehicle_id UUID,
    driver_id UUID,
    carrier_id UUID,
    route_data JSONB NOT NULL,  -- stops, distances, times
    status VARCHAR(50),  -- DRAFT, ASSIGNED, PUBLISHED, IN_PROGRESS, COMPLETED
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Vehicle profiles (templates)
CREATE TABLE planning_vehicle_profiles (
    id VARCHAR(50) PRIMARY KEY,  -- VAN_LARGE
    name VARCHAR(255),
    max_capacity_weight DOUBLE PRECISION,
    max_capacity_volume DOUBLE PRECISION
);

CREATE TABLE planning_vehicle_profiles_capabilities (
    vehicle_profile_entity_id VARCHAR(50) REFERENCES planning_vehicle_profiles(id),
    capabilities VARCHAR(255)
);

-- Actual vehicles in fleet
CREATE TABLE fleet_vehicles (
    id UUID PRIMARY KEY,
    registration VARCHAR(50),
    vehicle_profile_id VARCHAR(50) REFERENCES planning_vehicle_profiles(id),
    carrier_id UUID,
    current_driver_id UUID,
    status VARCHAR(50)
);

-- Carrier compliance
CREATE TABLE planning_carrier_compliance (
    carrier_id VARCHAR(50) PRIMARY KEY,
    is_insured BOOLEAN,
    insurance_expiry_date DATE,
    compliance_status VARCHAR(50)
);

-- Driver workflow configs
CREATE TABLE driver_workflow_configs (
    id UUID PRIMARY KEY,
    config_code VARCHAR(50) UNIQUE,
    name VARCHAR(255),
    workflow_json TEXT
);
```

---

## 🎯 PODSUMOWANIE DLA INNEGO AGENTA

### Co Agent musi wiedzieć przed rozpoczęciem pracy:

1. **Planning Service = FLUX moduł** - odpowiada za optymalizację tras
2. **Główny flow:** Orders → Optimization → Assignments → Magic Link → Driver PWA
3. **Timefold Solver** - używamy Easy Timefold, nie pełnego Timefold
4. **Route Persistence** - od stycznia 2026 trasy są zapisywane w DB (wcześniej były tylko in-memory)
5. **Magic Links** - 24h expiry, UUID token, stored in HashMap (TODO: Redis)
6. **Ghost PWA** - aplikacja mobilna dla kierowcy, offline-first, IndexedDB

### Priorytet zadań (jeśli Agent ma kontynuować):

1. **Zaimplementuj Ghost PWA Auth** - validate magic link token
2. **Zaimplementuj Ghost PWA Route View** - fetch and display route
3. **Barcode scanner w PWA** - HTML5-QR library
4. **Camera module** - foto DMG/POD
5. **Media upload API** - backend endpoint dla zdjęć
6. **Email service** - wyślij magic link na email

### Kluczowe pliki do edycji:

**Backend:**
- `VrpOptimizerService.java` - logika Timefold
- `RouteAssignmentService.java` - zarządzanie assignments
- `RouteAssignmentController.java` - REST API
- `DriverWorkflowConfigEntity.java` - konfiguracja workflow

**Frontend (Control Tower):**
- `DispatchView.vue` - główny widok dyspozytorni
- `AssignmentEditModal.vue` - modal do przypisania kierowcy

**Ghost PWA:**
- `modules/ghost/driver-pwa/src/views/AuthView.vue` - login z magic link
- `modules/ghost/driver-pwa/src/views/RouteView.vue` - widok trasy
- `modules/ghost/driver-pwa/src/components/Scanner.vue` - barcode scanner

### Protokół językowy (WAŻNE!):
- **Polski:** Opisy zadań, komunikacja z userem, task.md
- **Angielski:** Kod, komentarze, nazwy zmiennych, commit messages

### Manifest Compliance:
- Zero hardcoding - używaj JSONB dla dynamicznych danych
- Event-driven - publikuj eventy do Kafki
- Offline-first - PWA musi działać bez sieci
- Security - Magic Link z expirem, JWT dla API

---

**Koniec dokumentu**  
**Last Updated:** 2026-01-13 by Antigravity Agent
