# 🧪 PROTOCOL: HYPER-INNOVATION (PROPOSAL)

> **Context:** Transitioning VoidTracker to "Bleeding Edge" mode (2026 Standards).
> **Goal:** 60fps UI, Massive Scale Visualization, Native Polish.

## 1. 🌌 VISUALIZATION: Deck.gl (The Void-Map)

**Current:** Leaflet (DOM-based, slows down at >500 markers).
**Proposed:** **Deck.gl** (WebGL-powered).
**Why:**
- **Performance:** Handles 100k+ points/arcs with zero lag (essential for "10k+ daily parcels").
- **Aesthetic:** "Void" style requires 3D Arcs, Glowing Paths, and Hexagon layers for density – Leaflet cannot do this natively.
- **Integration:** Perfect fit for visualizing the `VrpSolution` vectors from the Optimization Engine.
- **Status:** Open Source (Uber/Visgl).

**Implementation:**
- Replace `L.map` with `DeckGL` overlay for heavy layers.
- Use `PathLayer` for Route Plans (Color coded by `DriverTaskEntity.status`).
- Use `ArcLayer` to visualize "Hub -> Spoke" flow in `GraphController`.

## 2. ⚡ INTERACTION: VueUse + AutoAnimate (The Living UI)

**Current:** Static lists, manual CSS transitions.
**Proposed:** **@formkit/auto-animate** + **VueUse**.
**Why:**
- **Zero-Config animation:** Dispatchers move orders between routes constantly. `AutoAnimate` makes these list changes fluid (60fps) with **one line of code** (`v-auto-animate`).
- **Reactive Power:** `VueUse` gives us `useWebSocket` (for internal `EventEmitter` updates), `useVirtualList` (for the 10k order backlog), and `useIdle` (security auto-logout).
- **Wow Factor:** The UI feels "intelligent" because it reacts organically to user input.

## 3. 🚄 BACKEND CORE: Java 21 + Virtual Threads (Project Loom)

**Current:** Java 17 (Thread-per-request model).
**Proposed:** **Java 21 (LTS) + Virtual Threads**.
**Why:**
- **Throughput:** `planning-service` does heavy I/O (Database + Timefold CPU + External Geocoding). Virtual Threads allow handling thousands of concurrent optimization requests without thread-pool exhaustion.
- **Simplicity:** Code looks synchronous (easy to read) but scales like reactive (WebFlux).
- **2026 Standard:** Java 21 is the new baseline for high-performance cloud-native apps.

---

## 🛠 EXECUTION PLAN (Immediate Actions)

1.  **Upgrade Backend:** Bump `pom.xml` to Java 21 & Spring Boot 3.2+ features (`spring.threads.virtual.enabled=true`).
2.  **Install Frontend Core:** `npm install deck.gl @deck.gl/vue @formkit/auto-animate @vueuse/core`.
3.  **Prototype Dispatch:** Create a new `DispatchView.vue` using Deck.gl to render the `OptimizationSolution` returned by `/api/planning/optimization/latest`.
