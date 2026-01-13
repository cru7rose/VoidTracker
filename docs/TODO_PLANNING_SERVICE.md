# 📋 TODO - Planning Service (FLUX)

**Ostatnia aktualizacja:** 2026-01-13  
**Status:** Backend skalowalność ✅ | Ghost PWA ⚠️ | Integracje ⚠️

---

## ✅ CO ZOSTAŁO ZROBIONE (2026-01-13)

### Backend - Skalowalność i Wydajność
- ✅ **Paginacja i filtrowanie** - RouteAssignmentController z wieloma kryteriami
- ✅ **Specification Pattern** - Zaawansowane dynamiczne zapytania
- ✅ **Database Indexes** - Skrypt SQL z indeksami dla route_assignment
- ✅ **Batch Operations** - Zoptymalizowane bulk insert (50 batch size)
- ✅ **Ghost PWA Endpoints** - DriverAuthController z validate i getRoute

### Backend - Podstawowe Funkcjonalności
- ✅ Optimization Engine (Timefold Solver)
- ✅ Route Persistence (OptimizationSolutionEntity, RouteAssignmentEntity)
- ✅ Magic Link System (token generation, validation)
- ✅ CRUD API dla assignments (7 endpoints)

---

## 🔴 HIGH PRIORITY - Do Zrobienia Natychmiast

### 1. Email/SMS Service dla Magic Links ⚠️
**Status:** Backend ready, brak integracji z providerem

**Co zrobić:**
- [ ] Wybrać provider (SendGrid / AWS SES / Mailgun)
- [ ] Dodać dependency do `pom.xml` (np. `spring-boot-starter-mail` lub SendGrid SDK)
- [ ] Stworzyć `EmailService.java` z metodą `sendMagicLink(driverEmail, magicLink)`
- [ ] Stworzyć email template (HTML) z magic link button
- [ ] Zintegrować z `RouteAssignmentService.publishRouteToDriver()`
- [ ] (Opcjonalnie) SMS gateway (Twilio) dla driverów bez email

**Pliki do modyfikacji:**
- `RouteAssignmentService.java` - metoda `sendNotification()`
- Nowy: `EmailService.java`
- Nowy: `templates/magic-link-email.html`

**Szacowany czas:** 2-3h

---

### 2. Ghost PWA Authentication - Dokończenie ⚠️
**Status:** Backend endpointy gotowe, PWA wymaga integracji

**Co zrobić:**
- [ ] Sprawdzić czy `LoginView.vue` w Ghost PWA używa endpointu `/api/planning/driver/auth/validate`
- [ ] Zaimplementować `authStore.loginWithToken()` w Ghost PWA
- [ ] Dodać IndexedDB storage dla session (driverId, routeId)
- [ ] Dodać route guard w router (sprawdza czy driver zalogowany)
- [ ] Obsługa expired token (redirect do login)

**Pliki do modyfikacji:**
- `modules/ghost/driver-pwa/src/stores/auth.ts` - integracja z backend
- `modules/ghost/driver-pwa/src/router/index.ts` - route guard
- `modules/ghost/driver-pwa/src/views/LoginView.vue` - poprawić flow

**Szacowany czas:** 2-3h

---

### 3. Ghost PWA Route View - Wyświetlanie Trasy ⚠️
**Status:** Backend endpoint gotowy (`GET /api/planning/driver/{driverId}/route`), PWA wymaga implementacji

**Co zrobić:**
- [ ] Stworzyć `RouteView.vue` w Ghost PWA
- [ ] Fetch route z endpointu `/api/planning/driver/{driverId}/route`
- [ ] Wyświetlić listę stops z mapą (Leaflet lub Google Maps)
- [ ] Integracja z nawigacją (Google Maps Navigation / Waze)
- [ ] Status indicators dla każdego stop (pending, in-progress, completed)
- [ ] Offline storage w IndexedDB (cache route data)

**Pliki do stworzenia/modyfikacji:**
- `modules/ghost/driver-pwa/src/views/RouteView.vue` (lub `MyRouteView.vue`)
- `modules/ghost/driver-pwa/src/stores/routeStore.ts` - state management
- `modules/ghost/driver-pwa/src/services/routeService.ts` - API calls

**Szacowany czas:** 4-6h

---

### 4. Ghost PWA Workflow Steps - Skanowanie i Statusy ⚠️
**Status:** Schema zdefiniowane, implementacja PWA TODO

**Co zrobić:**
- [ ] **Barcode Scanner:**
  - [ ] Zainstalować bibliotekę (HTML5-QR lub QuaggaJS)
  - [ ] Stworzyć `Scanner.vue` component
  - [ ] Walidacja kodu (sprawdzenie czy kod pasuje do order barcode)
  - [ ] Manual entry fallback (jeśli skan nie działa)

- [ ] **Camera Module:**
  - [ ] HTML5 Camera API (`navigator.mediaDevices.getUserMedia()`)
  - [ ] Capture foto DMG (damage) - opcjonalne
  - [ ] Capture foto POD (proof of delivery) - wymagane
  - [ ] EXIF geolocation extraction
  - [ ] Preview przed upload

- [ ] **Signature Capture:**
  - [ ] Canvas API dla podpisu
  - [ ] Touch events support (mobile)
  - [ ] Save jako base64 lub PNG
  - [ ] Capture recipient name (text input)

- [ ] **Status Update Buttons:**
  - [ ] IN_TRANSIT, ARRIVED, LOADING, UNLOADING, POD, ISSUE, COMPLETED
  - [ ] Integracja z `POST /api/planning/driver/status`

**Pliki do stworzenia:**
- `modules/ghost/driver-pwa/src/components/Scanner.vue`
- `modules/ghost/driver-pwa/src/components/CameraCapture.vue`
- `modules/ghost/driver-pwa/src/components/SignaturePad.vue`
- `modules/ghost/driver-pwa/src/components/StopActionSheet.vue`

**Szacowany czas:** 8-12h

---

### 5. Media Upload API - Backend ⚠️
**Status:** Endpoint stub w DriverAuthController, brak implementacji

**Co zrobić:**
- [ ] Stworzyć `MediaUploadController.java` z endpointem:
  - `POST /api/planning/media/upload/{type}` (dmg, pod, signature)
- [ ] Multipart file handling (Spring `@RequestParam MultipartFile`)
- [ ] EXIF geolocation validation (sprawdzenie czy foto ma GPS coordinates)
- [ ] Storage strategy:
  - Opcja A: Local filesystem (`/uploads/{routeId}/{stopId}/`)
  - Opcja B: AWS S3 (lepsze dla produkcji)
- [ ] Database entity `MediaUploadEntity` (zapisz metadata: routeId, stopId, type, filePath, uploadedAt)
- [ ] File size limits (max 10MB per foto)
- [ ] Image compression (opcjonalnie, dla oszczędności miejsca)

**Pliki do stworzenia:**
- `MediaUploadController.java`
- `MediaUploadService.java`
- `MediaUploadEntity.java`
- `MediaUploadRepository.java`

**Szacowany czas:** 3-4h

---

## 🟡 MEDIUM PRIORITY

### 6. Carrier Compliance Validation ⚠️
**Status:** Entity istnieje, brak logiki walidacji

**Co zrobić:**
- [ ] W `RouteAssignmentService.publishRouteToDriver()` dodać check:
  ```java
  CarrierCompliance compliance = complianceRepo.findById(carrierId);
  if (!"COMPLIANT".equals(compliance.getComplianceStatus())) {
      throw new IllegalStateException("Carrier not compliant!");
  }
  ```
- [ ] Warning UI w frontend (gdy carrier SUSPENDED, ale pozwól publish)
- [ ] Insurance expiry alerts (scheduled job sprawdzający daty)

**Szacowany czas:** 2h

---

### 7. Driver Enrichment - Real Data ⚠️
**Status:** Obecnie mock data w `enrichResponse()`

**Co zrobić:**
- [ ] Feign Client do IAM Service: `GET /api/auth/users/{driverId}`
- [ ] Feign Client do Titan/Mesh: `GET /api/graph/vehicle/{vehicleId}`
- [ ] Cache wyników (Caffeine cache, 5min TTL)
- [ ] Fallback na mock jeśli service unavailable

**Szacowany czas:** 3h

---

### 8. Delivery Code Conditional Logic ⚠️
**Status:** Schema zdefiniowane, brak implementacji

**Co zrobić:**
- [ ] Dodać pole `requiresDeliveryCode: boolean` do Address entity (Order Service)
- [ ] Dodać pole `scanDeliveryCodePolicy: "ALWAYS" | "NEVER" | "IF_RAMP"` do Customer entity
- [ ] W Ghost PWA: `StopActionSheet.vue` - conditional rendering scannera
- [ ] Logika: `checkIfDeliveryCodeRequired(stop)` - sprawdza policy

**Szacowany czas:** 4h

---

### 9. Geofencing w PWA ⚠️
**Status:** Brak implementacji

**Co zrobić:**
- [ ] GPS Watchdog w Ghost PWA (Web Geolocation API)
- [ ] Sprawdzenie czy driver w geofence (300m radius z config)
- [ ] Alert gdy poza geofence (browser notification)
- [ ] Auto-update status "ARRIVED" gdy w geofence
- [ ] Background geolocation (Service Worker)

**Szacowany czas:** 6-8h

---

## 🟢 LOW PRIORITY (Future Enhancements)

### 10. Real-time WebSocket Updates
- Planning Service → Frontend (Control Tower)
- Live map refresh podczas Timefold solving
- Driver status broadcast

### 11. Advanced Timefold Addons
- Elastic Shell (milkrun + ad-hoc)
- Gatekeeper Agent (n8n LLM approval) - **50% done**
- High-Fidelity Dashboard (progressive solve)

### 12. Analytics Dashboard
- Route efficiency metrics
- Driver performance KPIs
- Cost per km tracking

---

## 📊 PRIORYTET IMPLEMENTACJI (Rekomendacja)

### Faza 1: Ghost PWA Core (Krytyczne dla MVP)
1. ✅ Backend endpoints (DONE)
2. ⚠️ Ghost PWA Authentication (2-3h)
3. ⚠️ Ghost PWA Route View (4-6h)
4. ⚠️ Media Upload API (3-4h)

**Total:** ~10-13h

### Faza 2: Workflow Steps (Funkcjonalność)
5. ⚠️ Ghost PWA Workflow Steps (8-12h)
6. ⚠️ Email/SMS Service (2-3h)

**Total:** ~10-15h

### Faza 3: Polish & Optimization ✅
7. ✅ Carrier Compliance Validation (2h) - DONE
8. ✅ Driver Enrichment Real Data (3h) - DONE
9. ✅ Delivery Code Logic (4h) - DONE (PWA ready, backend schema TODO)

**Total:** ~9h - **COMPLETED**

---

## 🎯 NAJWAŻNIEJSZE - Co Teraz?

**Jeśli chcesz mieć działający Ghost PWA dla kierowców:**

1. **Ghost PWA Authentication** (2-3h) - bez tego kierowcy nie mogą się zalogować
2. **Ghost PWA Route View** (4-6h) - bez tego kierowcy nie widzą tras
3. **Media Upload API** (3-4h) - bez tego nie można uploadować zdjęć POD

**Razem: ~9-13h pracy** aby mieć podstawowy flow: Login → View Route → Upload Photos

---

**Ostatnia aktualizacja:** 2026-01-13  
**Status:** Faza 1 ✅ | Faza 2 ✅ | Faza 3 ✅  
**Następny review:** Po testach end-to-end
