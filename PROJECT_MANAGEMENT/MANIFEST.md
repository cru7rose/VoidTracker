# 🌌 VoidTracker Project Management - Manifest

**Ostatnia aktualizacja:** 2026-01-12 12:30  
**Status Projektu:** Hyper-Innovation Phase (Revolution 2026)  
**Model Operacyjny:** The Liquid Enterprise (Event-Driven EAV Architecture)  
**Język Projektu:** Hybrid (PL: Zarządzanie / EN: Implementacja)  
**Agent Role:** Lead Architect & Senior Fullstack Engineer

---

## 💎 STRATEGIC VISION & REVENUE FOCUS (THE PRIME)

- **Cel Nadrzędny:** Stworzenie systemu OMS+CRM+TMS, który jest "Logistycznym Systemem Operacyjnym". Celujemy w **dominację rynkową** poprzez automatyzację i User Experience klasy Premium.
- **Rewolucja:** System łączy twarde dane (TMS) z miękką empatią (CRM). Dla użytkownika "Vanilla" jest prosty, dla "Pro" jest potężny.
- **Profit:** Architektura nastawiona na minimalizację pustych przebiegów i maksymalizację marży (Dynamic Pricing, Marketplace Integration).

---

## 🧠 AGENT MEMORY & CONTEXT STRATEGY

- **Source of Truth:** `MANIFEST.md`, `task.md` oraz `IDEA.md` są dokumentami nadrzędnymi.
- **Language Protocol (Hybrid):**
    - **PL:** Wizja, Biznes, Taski, Komunikacja z Userem.
    - **EN:** Kod, Komentarze, Commity, Baza Danych.
- **Operational Context:** Przed każdą akcją Agent analizuje `task.md` pod kątem zgodności z wizją "Void-Mesh".

---

## 🛠 PROTOKOŁY DZIAŁANIA (AI RULES)

### 1. Zasada "Unified Void" (Integration First)
- **Zero Silos:** Dane z OMS są natychmiast dostępne w TMS i CRM. Używamy Event-Driven Architecture (Kafka).
- **EAV Core:** Struktura zamówień musi być elastyczna (JSONB), aby przyjąć każdy typ ładunku bez zmiany schematu bazy.

### 2. Protokół "5-Star Experience"
- **Proactive CRM:** System musi wykrywać problemy zanim zrobi to klient. Implementuj alerty i automatyczne powiadomienia.
- **UX First:** Interfejs (Ghost/Void-Flow) musi być "przepiękny" i ultra-szybki. Ciemny motyw, płynne animacje.

### 3. Vibe Check: Security & Stability
- **The Gatekeeper:** AI sugeruje, człowiek zatwierdza. Żadne dane nie wychodzą na zewnątrz bez autoryzacji.
- **Offline Mode:** Moduły mobilne (PWA) muszą działać bez sieci.

### 4. Infrastructure Optimization Protocol (Timeout Management)
- **Server Timeouts:** Wszystkie timeouty są zoptymalizowane dla stabilności i niezawodności.
  - Health checks: 180s (3 minuty) z exponential backoff
  - Port checks: 120s (2 minuty) z retry logic
  - Connection pools: 30s timeout, 5-20 connections
  - TCP keepalive: 300s time, 30s interval, 5 probes
- **Startup Scripts:** `start-all.sh` i `start-sup.sh` mają wbudowane retry logic i lepsze raportowanie błędów.
- **System Configuration:** Skrypt `scripts/configure-system-timeouts.sh` konfiguruje TCP, connection tracking i file descriptors.
- **Spring Boot Optimization:** Connection pools (HikariCP), Kafka timeouts i Tomcat settings są zoptymalizowane.

**Reference:** `updates/2026-01-12_1230_Server_Timeout_Optimization.md`

### 5. CI/CD Build Protocol (No Build on Server) ⚠️ CRITICAL
- **Zasada Nadrzędna:** **NIGDY nie budujemy Java/Maven na serwerze** - wszystkie buildy przez GitHub Actions CI/CD.
- **Problem:** Buildy Maven/Java na serwerze powodują zerwanie połączenia SSH z Cursorem z powodu przeciążenia zasobów.
- **Rozwiązanie:** 
  - Wszystkie buildy Java/Maven → GitHub Actions CI/CD
  - Buildy są rozdzielone na moduły (danxils-commons, iam, order, planning) dla łatwego zbierania logów
  - **Buildy mogą być równoległe** - każdy job ma własny GitHub Actions runner (osobna maszyna), więc nie przeciążają serwera
  - Logi buildu są zbierane nawet gdy build failed (upload-artifact zawsze)
  - Na serwerze tylko: uruchamianie infrastruktury Docker i frontend dev server
  - **Różnica:** Buildy (równoległe OK - różne maszyny) vs Restarty (sekwencyjne - ta sama maszyna)
- **Service Restart Protocol (Sequential with Delays):** ⚠️ **KRYTYCZNE - ZAPAMIĘTAĆ**
  - **Problem:** Restartowanie wszystkich usług jednocześnie przeciąża serwer i zrywa połączenie SSH
  - **Rozwiązanie:** 
    - Usługi są restartowane **sekwencyjnie** (jedna po drugiej), nie równolegle
    - **Opóźnienie 15 sekund** między restartami różnych usług
    - To zapobiega przeciążeniu serwera podczas równoczesnego uruchamiania wielu JVM
    - Workflow pokazuje postęp: "Restarting X Service (1/3)..."
    - **Lekcja:** Zawsze restartować usługi z opóźnieniami, nigdy równolegle
- **Automatyczne Zbieranie Logów Buildu:** 🔍 **AGENT MUSI SPRAWDZAĆ TO PRZY KAŻDYM PROBLEMIE Z BUILDEM**
  - Każdy build automatycznie zapisuje pełne logi Maven do plików
  - Logi są uploadowane jako artifacts (zawsze, nawet przy failed build)
  - Artifacts dostępne: `commons-build-logs`, `iam-build-logs`, `order-build-logs`, `planning-build-logs`
  - Job `build-summary` automatycznie analizuje logi i tworzy podsumowanie (Markdown)
  - **Agent powinien:** Poprosić użytkownika o wklejenie `build-summary` lub pobranie artifacts z GitHub Actions
  - **Lokalizacja:** GitHub Actions → Workflow Run → Artifacts
  - **Dokumentacja:** `GITHUB_ACTIONS_LOGS.md` - pełny przewodnik dostępu do logów
- **Frontend:**
  - Dev server (`npm run dev`) uruchamiany na serwerze (lekki, nie powoduje problemów)
  - Frontend NIE jest budowany w CI/CD - tylko restart przez CI/CD po deploy backend services
- **Workflow po restarcie serwera:**
  1. `./start-sup.sh` - uruchamia infrastrukturę Docker (PostgreSQL, Kafka, Neo4j, etc.)
  2. `./start-frontend.sh` - uruchamia frontend dev server (`npm run dev`)
  3. Backend services (IAM, Order, Planning) są deployowane przez CI/CD:
     - Push do GitHub → GitHub Actions buduje moduły
     - Zbudowane JAR-y są uploadowane jako artifacts
     - Deploy job pobiera artifacts i deployuje na serwer przez SCP
     - Serwer restartuje serwisy (nie buduje!)
- **Skrypty na serwerze:**
  - `start-sup.sh` - Docker Compose (infrastruktura) - **JEDYNY skrypt który może uruchamiać buildy (Docker images)**
  - `start-frontend.sh` - Frontend dev server (`npm run dev`) - lekki, nie powoduje problemów
  - `start-iam.sh`, `start-order.sh`, `start-planning.sh` - **TYLKO** uruchamianie już zbudowanych JAR-ów (sprawdzają czy JAR istnieje, jeśli nie - błąd)
  - `stop-*.sh` - zatrzymywanie serwisów

**Reference:** `.github/workflows/build-and-deploy.yml`, `CICD_QUICK_START.md`

---

## 📂 STRUKTURA PLIKÓW
PROJECT_MANAGEMENT/
├── MANIFEST.md                          # [TEN PLIK]
├── task.md                              # Plan wykonawczy
├── IDEA.md                              # Wizja "The Void Protocol"
├── plans/                               # Strategie szczegółowe
├── updates/                             # Raporty aktualizacji (protokół)
└── logs/                                # Logi systemowe

---

## ⚡ INFRASTRUCTURE OPTIMIZATION (2026-01-12)

### Server Timeout Configuration
**Status:** ✅ COMPLETED

**Key Optimizations:**
1. **Script Timeouts:**
   - Health checks: 180s (was 60s)
   - Port checks: 120s (was 60s)
   - Exponential backoff retry logic
   - Enhanced error reporting

2. **System-Level Settings:**
   - TCP keepalive: 300s time, 30s interval
   - Connection tracking: 262144 entries
   - TCP connection queue: 4096
   - File descriptors: 1048576

3. **Spring Boot Services:**
   - HikariCP connection pool: 30s timeout, 5-20 connections
   - Kafka: 30s request timeout, 2min delivery timeout
   - Tomcat: 60s connection timeout, 200 max threads

**Configuration Script:**
```bash
./scripts/configure-system-timeouts.sh  # One-time system setup
```

**Startup Process:**
```bash
./start-sup.sh    # Infrastructure (PostgreSQL, Kafka, Neo4j)
./start-all.sh    # Application services (IAM, Order, Planning)
```

**Benefits:**
- ✅ Eliminated timeout issues during startup
- ✅ Better resilience to slow infrastructure
- ✅ Improved error messages with troubleshooting hints
- ✅ Optimized connection pool management
- ✅ Enhanced network stability

**Documentation:**
- Full details: `updates/2026-01-12_1230_Server_Timeout_Optimization.md`
- Scripts: `start-all.sh`, `start-sup.sh`
- System config: `scripts/configure-system-timeouts.sh`

---