<template>
  <div class="relative w-full h-screen bg-void-black overflow-hidden flex">
    
    <!-- LEFT SIDEBAR: Intelligent List -->
    <div class="w-96 flex-shrink-0 z-20 h-full flex flex-col glass-panel border-r border-void-gray/30 transition-all duration-500 ease-in-out"
         :class="{ '-ml-96': !sidebarOpen }">
        
        <div class="p-6 border-b border-white/5 bg-gradient-to-r from-void-darker to-transparent">
            <h1 class="text-2xl font-bold neon-text tracking-tighter flex items-center gap-2">
                <span class="text-3xl">🌌</span> VOID-FLOW
            </h1>
            <p class="text-xs text-void-cyan-400/70 font-mono mt-1">OPERATIONAL CONTROL TOWER v2.0</p>
        </div>

        <!-- OPTIMIZATION PROFILE SELECTOR -->
        <div class="p-4 border-b border-white/5">
            <label class="block text-xs uppercase tracking-widest text-gray-500 mb-2">Optimization Profile</label>
            <select v-model="selectedProfile" class="w-full bg-void-darker border border-white/10 rounded p-2 text-sm text-gray-300 focus:ring-2 focus:ring-void-cyan-500">
                <option value="">Default Profile</option>
                <option value="standard">Standard Delivery</option>
                <option value="express">Express Priority</option>
                <option value="eco">Eco-Friendly Routes</option>
            </select>
            <div v-if="pendingOrderIds.length > 0" class="mt-2 text-xs text-void-cyan-400">
                📦 {{ pendingOrderIds.length }} orders queued from Orders view
            </div>
        </div>

        <!-- ACTIONS -->
        <div class="p-4 grid grid-cols-2 gap-3">
            <button @click="triggerOptimization" 
                    :disabled="isOptimizing"
                    class="col-span-2 group relative overflow-hidden rounded-lg bg-void-cyan-500/10 border border-void-cyan-500/50 p-3 hover:bg-void-cyan-500/20 transition-all">
                <div v-if="isOptimizing" class="absolute inset-0 bg-void-cyan-500/20 animate-pulse"></div>
                <div class="relative flex items-center justify-center gap-2 text-void-cyan-400 font-bold tracking-wider group-hover:text-glow">
                    <span v-if="isOptimizing">CALCULATING...</span>
                    <span v-else>RUN OPTIMIZER PRIME</span>
                </div>
            </button>
            
             <button @click="publishRoutes" 
                    :disabled="!solutionData || isPublishing"
                    class="col-span-1 rounded-lg bg-void-pink-500/10 border border-void-pink-500/50 p-2 text-xs font-bold text-void-pink-400 hover:bg-void-pink-500/20 transition-all text-center">
                {{ isPublishing ? 'PUBLISHING...' : 'PUBLISH TO GHOST' }}
            </button>

             <button @click="fetchData" 
                    class="col-span-1 rounded-lg bg-white/5 border border-white/10 p-2 text-xs font-bold text-gray-400 hover:bg-white/10 transition-all text-center">
                SYNC DATA
            </button>
        </div>

        <!-- STATS GRID -->
        <div class="grid grid-cols-2 gap-px bg-void-gray/30 border-y border-white/5">
            <div class="p-4 bg-void-darker/50 hover:bg-void-darker transition">
                <div class="text-[10px] uppercase tracking-widest text-gray-500">Predicted Profit</div>
                <div class="text-xl font-bold text-void-cyan-400 font-mono mt-1">
                    {{ formatCurrency(stats.profit) }}
                </div>
            </div>
             <div class="p-4 bg-void-darker/50 hover:bg-void-darker transition">
                <div class="text-[10px] uppercase tracking-widest text-gray-500">Payload Cost</div>
                 <div class="text-xl font-bold text-void-pink-400 font-mono mt-1">
                    {{ formatCurrency(stats.cost) }}
                </div>
            </div>
             <div class="p-4 bg-void-darker/50 hover:bg-void-darker transition">
                <div class="text-[10px] uppercase tracking-widest text-gray-500">Active Routes</div>
                 <div class="text-xl font-bold text-white font-mono mt-1">
                    {{ stats.routesCount }}
                </div>
            </div>
             <div class="p-4 bg-void-darker/50 hover:bg-void-darker transition">
                <div class="text-[10px] uppercase tracking-widest text-gray-500">Unassigned</div>
                 <div class="text-xl font-bold text-void-amber-400 font-mono mt-1">
                    {{ unassignedOrders.length }}
                </div>
            </div>
        </div>

        <!-- TABS -->
        <div class="flex border-b border-white/5 text-xs font-bold tracking-wider bg-void-darker">
            <button @click="activeTab = 'orders'" 
                :class="activeTab === 'orders' ? 'text-void-cyan-400 border-b-2 border-void-cyan-400 bg-white/5' : 'text-gray-500 hover:text-gray-300'"
                class="flex-1 p-3 transition-all">
                UNASSIGNED ORDERS
            </button>
             <button @click="activeTab = 'routes'"
                :class="activeTab === 'routes' ? 'text-void-cyan-400 border-b-2 border-void-cyan-400 bg-white/5' : 'text-gray-500 hover:text-gray-300'"
                 class="flex-1 p-3 transition-all">
                OPTIMIZED ROUTES
            </button>
            <button @click="activeTab = 'assignments'"
                :class="activeTab === 'assignments' ? 'text-void-cyan-400 border-b-2 border-void-cyan-400 bg-white/5' : 'text-gray-500 hover:text-gray-300'"
                 class="flex-1 p-3 transition-all">
                ASSIGNMENTS
            </button>
        </div>

        <!-- LIST CONTENT -->
        <div class="flex-1 overflow-y-auto custom-scrollbar p-2 space-y-2">
            
            <!-- ORDERS LIST -->
            <div v-if="activeTab === 'orders'">
                <div v-if="unassignedOrders.length === 0" class="p-8 text-center text-gray-600 text-sm">
                    No unassigned orders found.
                </div>
                <div v-for="order in unassignedOrders" :key="order.orderId" 
                     class="group p-3 rounded bg-white/5 border border-white/5 hover:border-void-cyan-400/50 hover:bg-white/10 transition-all cursor-pointer"
                     @click="flyToOrder(order)">
                    <div class="flex justify-between items-start">
                        <span class="text-xs font-mono text-void-cyan-600 group-hover:text-void-cyan-400">#{{ order.orderId?.substring(0,8) || 'N/A' }}</span>
                        <span class="text-[10px] px-1.5 py-0.5 rounded bg-void-amber-500/20 text-void-amber-400 border border-void-amber-500/30">{{ order.status }}</span>
                    </div>
                    <div class="mt-2 text-sm font-semibold text-gray-200">{{ order.delivery?.customer || order.delivery?.name || 'Unknown Client' }}</div>
                    <div class="text-xs text-gray-500 mt-1 truncate">{{ order.delivery?.city || 'No Address' }}</div>
                    <div class="mt-2 flex items-center justify-between text-[10px] text-gray-400 border-t border-white/5 pt-2">
                        <span>{{ formatDate(order.timestamps?.created) }}</span>
                        <span>{{ order.aPackage?.weight || 0 }}kg</span>
                    </div>
                </div>
            </div>

            <!-- ROUTES LIST -->
            <div v-if="activeTab === 'routes'">
                 <div v-if="!solutionData" class="p-8 text-center text-gray-600 text-sm">
                    No optimization solution loaded.
                </div>
                <template v-else>
                     <div v-for="(route, idx) in solutionData.routes" :key="idx" 
                        class="p-3 mb-2 rounded bg-white/5 border border-white/5 hover:border-void-pink-400/50 hover:bg-white/10 transition-all"
                        @mouseover="highlightRoute(route)" @mouseleave="clearHighlight">
                        <div class="flex justify-between items-center mb-2">
                             <span class="text-xs font-bold text-void-pink-400">ROUTE {{ idx + 1 }}</span>
                             <span class="text-[10px] text-gray-400">{{ (route.totalDistance / 1000).toFixed(1) }} km</span>
                        </div>
                        <div class="flex items-center gap-2 mb-2">
                            <div class="w-2 h-2 rounded-full bg-green-500" :class="{'animate-pulse': !route.vehicle}"></div>
                            <span class="text-sm font-mono text-gray-300">
                                {{ route.vehicle ? route.vehicle.id : 'UNASSIGNED VEHICLE' }}
                            </span>
                        </div>
                        
                        <!-- Timeline -->
                        <div class="relative pl-3 border-l border-white/10 space-y-2 mt-3">
                            <div v-for="(act, aIdx) in route.activities.slice(0, 3)" :key="aIdx" class="relative text-xs">
                                <span class="absolute -left-[17px] top-1 w-2 h-2 rounded-full bg-void-gray border border-slate-600"></span>
                                <div class="text-gray-400">{{ act.type }} <span class="text-gray-500 text-[10px]">expected</span></div>
                                <div class="text-gray-300 truncate w-48">{{ act.name }}</div>
                            </div>
                            <div v-if="route.activities.length > 3" class="text-[10px] text-gray-600 pl-1">
                                + {{ route.activities.length - 3 }} more stops
                            </div>
                        </div>
                     </div>
                </template>
            </div>

            <!-- ASSIGNMENTS LIST -->
            <div v-if="activeTab === 'assignments'">
                 <div v-if="assignments.length === 0" class="p-8 text-center text-gray-600 text-sm">
                    No route assignments yet.
                 </div>
                 <div v-for="assignment in assignments" :key="assignment.id" 
                      class="p-3 mb-2 rounded bg-white/5 border border-white/5 hover:border-void-pink-400/50 hover:bg-white/10 transition-all cursor-pointer"
                      @click="viewAssignment(assignment)">
                     <div class="flex justify-between items-center mb-2">
                          <span class="text-xs font-bold text-void-pink-400">{{ assignment.routeName }}</span>
                          <span class="text-[10px] px-1.5 py-0.5 rounded" 
                                :class="getStatusClass(assignment.status)">{{ assignment.status }}</span>
                     </div>
                     <div class="text-xs text-gray-500 mt-1">
                         Driver: <span class="text-gray-300">{{ assignment.driverName || 'Unassigned' }}</span>
                     </div>
                     <div class="text-xs text-gray-500">
                         Vehicle: <span class="text-gray-300">{{ assignment.vehicleName || 'Unassigned' }}</span>
                     </div>
                     <div class="text-xs text-gray-500 mt-2 flex justify-between items-center border-t border-white/5 pt-2">
                         <span>{{ assignment.stopCount || 0 }} stops</span>
                         <span>{{ (assignment.totalDistanceKm || 0).toFixed(1) }} km</span>
                     </div>
                 </div>
            </div>

        </div>
    </div>

    <!-- MAIN: DeckGL Map -->
    <div class="flex-1 relative bg-black">
        <canvas id="deck-canvas" class="w-full h-full cursor-crosshair"></canvas>
        
        <!-- Map Overlay Controls -->
        <div class="absolute bottom-6 left-6 flex flex-col gap-2 z-10 pointer-events-none">
             <div class="pointer-events-auto glass-panel p-2 rounded flex flex-col gap-2">
                <label class="flex items-center gap-2 text-xs text-gray-300 cursor-pointer hover:text-white">
                    <input type="checkbox" v-model="layers.arcs" class="accent-void-cyan-500 rounded bg-white/10 border-white/20">
                    Show Quantum Arcs
                </label>
                 <label class="flex items-center gap-2 text-xs text-gray-300 cursor-pointer hover:text-white">
                    <input type="checkbox" v-model="layers.paths" class="accent-void-pink-500 rounded bg-white/10 border-white/20">
                    Show Route Paths
                </label>
                 <label class="flex items-center gap-2 text-xs text-gray-300 cursor-pointer hover:text-white">
                    <input type="checkbox" v-model="layers.heatmap" class="accent-void-amber-500 rounded bg-white/10 border-white/20">
                    Risk Heatmap
                </label>
            </div>
        </div>

        <!-- Sidebar Toggle -->
        <div class="absolute top-4 left-4 z-30">
            <button @click="sidebarOpen = !sidebarOpen" class="bg-void-darker/80 text-white p-2 rounded border border-white/10">
                <span v-if="sidebarOpen">◀</span>
                <span v-else>▶</span>
            </button>
        </div>

         <!-- Tooltip -->
        <div v-if="tooltip.visible" 
             :style="{ top: tooltip.y + 'px', left: tooltip.x + 'px' }"
             class="fixed z-50 pointer-events-none bg-void-darker/90 backdrop-blur border border-void-cyan-500/30 text-xs p-3 rounded shadow-2xl transform -translate-y-full -translate-x-1/2 -mt-2">
             <div class="font-bold text-void-cyan-400 mb-1">{{ tooltip.title }}</div>
             <div class="text-gray-300">{{ tooltip.text }}</div>
        </div>
        <!-- Magic Links Modal -->
        <div v-if="publishedLinks" class="fixed inset-0 z-[100] flex items-center justify-center bg-black/80 backdrop-blur-sm p-4">
             <div class="glass-panel border-void-pink-500/50 p-6 rounded-xl w-full max-w-2xl shadow-2xl relative">
                <button @click="publishedLinks = null" class="absolute top-4 right-4 text-gray-400 hover:text-white">✕</button>
                <h2 class="text-xl font-bold text-void-pink-400 mb-4 flex items-center gap-2">
                    <span>🚀</span> READY TO DISPATCH
                </h2>
                <div class="space-y-3 max-h-[60vh] overflow-y-auto custom-scrollbar">
                    <div v-for="(link, idx) in publishedLinks" :key="idx" class="bg-white/5 p-4 rounded border border-white/10 hover:border-void-pink-400/30 transition">
                         <div class="flex justify-between items-center mb-2">
                            <span class="font-bold text-gray-200">Vehicle {{ link.vehicle }}</span>
                            <span class="text-xs text-void-cyan-400 font-mono">ACTIVE</span>
                        </div>
                        <div class="flex gap-2">
                            <input type="text" readonly :value="link.link" class="bg-black/50 border border-white/10 rounded px-3 py-2 text-xs font-mono text-gray-400 flex-1 select-all" />
                            <a :href="link.link" target="_blank" class="bg-void-pink-500/20 hover:bg-void-pink-500/40 text-void-pink-400 px-3 py-2 rounded text-xs font-bold border border-void-pink-500/50 transition">
                                OPEN
                            </a>
                        </div>
                    </div>
                </div>
             </div>
        </div>
    </div>
    
    <!-- Gatekeeper Approval Modal -->
    <GatekeeperApprovalModal
        :show="showGatekeeperModal"
        :solution-id="pendingApproval?.solutionId"
        :score-change="pendingApproval?.scoreChange"
        :justification="pendingApproval?.justification"
        :warnings="pendingApproval?.warnings"
        :vehicle-count="pendingApproval?.vehicleCount"
        @close="closeGatekeeperModal"
        @approved="onApprovalApproved"
        @rejected="onApprovalRejected"
    />
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, reactive, watch, computed } from 'vue';
import { Deck } from '@deck.gl/core';
import { ScatterplotLayer, ArcLayer, PathLayer, PolygonLayer } from '@deck.gl/layers';
import { TileLayer } from '@deck.gl/geo-layers';
import { BitmapLayer } from '@deck.gl/layers';
import { orderApi, planningApi } from '../../api/axios';
import { useAuthStore } from '../../stores/authStore';
import webSocketService from '../../services/WebSocketService';
import AssignmentEditModal from '../../components/AssignmentEditModal.vue';
import GatekeeperApprovalModal from '../../components/GatekeeperApprovalModal.vue';
import { useWebSocketStore } from '../../stores/websocketStore';
import { useGatekeeperStore } from '../../stores/gatekeeperStore';

// --- STATE ---
const authStore = useAuthStore();
const websocketStore = useWebSocketStore();
const gatekeeperStore = useGatekeeperStore();

const sidebarOpen = ref(true);
const activeTab = ref('orders');
const isOptimizing = ref(false);
const isPublishing = ref(false);
const publishedLinks = ref(null);
const lastError = ref(null);

const solutionData = ref(null);
const unassignedOrders = ref([]);
const deckInstance = ref(null);
const selectedProfile = ref('');
const pendingOrderIds = ref([]);
const assignments = ref([]);
const showEditModal = ref(false);
const selectedAssignment = ref(null);

// Gatekeeper approval modal
const showGatekeeperModal = ref(false);
const pendingApproval = ref(null);

const layers = reactive({
    arcs: true,
    paths: true,
    heatmap: false
});

const tooltip = reactive({ visible: false, x: 0, y: 0, title: '', text: '' });

// --- ROUTING CONFIGURATION ---
// OSRM (Free, Open Source) - Currently Active
const ROUTING_PROVIDER = 'OSRM';
const OSRM_BASE_URL = 'https://router.project-osrm.org';

// MapBox Directions API (Requires API Key) - Future Option
// Uncomment and set API key when ready to use MapBox
// const ROUTING_PROVIDER = 'MAPBOX';
// const MAPBOX_API_KEY = 'YOUR_MAPBOX_API_KEY_HERE';
// const MAPBOX_BASE_URL = 'https://api.mapbox.com/directions/v5/mapbox/driving';

// Route geometry cache to avoid redundant API calls
const routeGeometryCache = new Map();

// 🔴 FIX #3: Reactive computed for order count
const unassignedCount = computed(() => {
    return unassignedOrders.value?.length || 0;
});

// Stats (Computed from solution)
const stats = computed(() => {
    if (!solutionData.value) return { profit: 0, cost: 0, routesCount: 0 };
    
    // Mock Logic for Profit Calculation until backend provides it
    const totalDistKm = solutionData.value.routes.reduce((acc, r) => acc + (r.totalDistance/1000), 0);
    const cost = totalDistKm * 1.5; // 1.5 EUR/km
    const revenue = totalDistKm * 2.2; // Mock revenue margin
    
    return {
        cost: cost,
        profit: revenue - cost,
        routesCount: solutionData.value.routes.length
    };
});

// --- LIFECYCLE ---
onMounted(() => {
    // Check for orders sent from OrderList view
    const stored = sessionStorage.getItem('pendingOrders');
    if (stored) {
        try {
            pendingOrderIds.value = JSON.parse(stored);
            sessionStorage.removeItem('pendingOrders');
            console.log(`📦 Received ${pendingOrderIds.value.length} orders from Orders view`);
        } catch (e) {
            console.error('Failed to parse pending orders:', e);
        }
    }
    
    initDeck();
    fetchData();
    
    // Auto-refresh every 30s
    const interval = setInterval(fetchData, 30000);
    onUnmounted(() => clearInterval(interval));
});

onUnmounted(() => {
    if (deckInstance.value) deckInstance.value.finalize();
    webSocketService.disconnect();
    websocketStore.disconnect();
});

watch(layers, () => updateLayers());

// --- ROUTING HELPERS ---

/**
 * Fetch route geometry from routing provider (OSRM or MapBox)
 * @param {Array} from - [lon, lat] starting coordinate
 * @param {Array} to - [lon, lat] ending coordinate
 * @returns {Promise<Array>} Array of [lon, lat] coordinates along the route
 */
async function fetchRouteGeometry(from, to) {
    // Generate cache key
    const cacheKey = `${from[0]},${from[1]}-${to[0]},${to[1]}`;
    
    // Check cache first
    if (routeGeometryCache.has(cacheKey)) {
        return routeGeometryCache.get(cacheKey);
    }
    
    try {
        let geometry;
        
        if (ROUTING_PROVIDER === 'OSRM') {
            // OSRM Request Format: /route/v1/{profile}/{coordinates}
            const url = `${OSRM_BASE_URL}/route/v1/driving/${from[0]},${from[1]};${to[0]},${to[1]}?geometries=geojson&overview=full`;
            const response = await fetch(url);
            
            if (!response.ok) {
                throw new Error(`OSRM API error: ${response.status}`);
            }
            
            const data = await response.json();
            
            if (data.code !== 'Ok' || !data.routes || data.routes.length === 0) {
                throw new Error('OSRM: No route found');
            }
            
            // Extract GeoJSON coordinates
            geometry = data.routes[0].geometry.coordinates;
            
        } else if (ROUTING_PROVIDER === 'MAPBOX') {
            // MapBox Directions API (commented out - requires API key)
            // const url = `${MAPBOX_BASE_URL}/${from[0]},${from[1]};${to[0]},${to[1]}?geometries=geojson&access_token=${MAPBOX_API_KEY}`;
            // const response = await fetch(url);
            // const data = await response.json();
            // geometry = data.routes[0].geometry.coordinates;
            
            console.warn('MapBox routing not configured. Falling back to straight line.');
            geometry = [from, to];
        } else {
            // Fallback: straight line
            geometry = [from, to];
        }
        
        // Cache the result
        routeGeometryCache.set(cacheKey, geometry);
        
        return geometry;
        
    } catch (error) {
        console.error(`Routing API error (${from} → ${to}):`, error.message);
        // Fallback to straight line on error
        const fallback = [from, to];
        routeGeometryCache.set(cacheKey, fallback);
        return fallback;
    }
}

// --- API ACTIONS ---


async function fetchData() {
    // 1. Fetch Orders (Created/New) - INDEPENDENT
    try {
        console.log("🔄 Fetching Unassigned Orders...");
        const orderRes = await orderApi.get('/api/orders', {
            params: { statuses: ['NEW'] }
        });
        unassignedOrders.value = orderRes.data.content || orderRes.data || [];
        console.log(`✅ Loaded ${unassignedOrders.value.length} unassigned orders`);
    } catch (e) {
        console.error("❌ Failed to fetch orders:", e);
        unassignedOrders.value = [];
    }
    
    // 2. Fetch Latest Solution - INDEPENDENT (OK to fail)
    try {
        console.log("🔄 Fetching Latest Optimization...");
        // Use /solution endpoint which returns proper DTO format
        const solRes = await planningApi.post('/api/planning/optimization/solution', {
            orderIds: [],
            vehicleIds: []
        });
        solutionData.value = parseSolution(solRes.data);
        console.log(`✅ Loaded ${solutionData.value?.routes?.length || 0} routes`);
    } catch (e) {
        console.warn("⚠️ No optimization solution available (expected on first load):", e.message);
        // DON'T overwrite if solution already exists (from manual optimization)
        if (!solutionData.value) {
            solutionData.value = null;
        }
    }
    
    // Always update map layers (even if planning failed)
    updateLayers();

    // Connect WS using Service (legacy)
    webSocketService.connect(
        () => {
            webSocketService.subscribe('/topic/optimization-updates', handleOptimizationUpdate);
        },
        (err) => console.error("WS Connect Error", err)
    );
    
    // Connect using new WebSocket Store
    websocketStore.connect();
    
    // Listen for optimization updates from WebSocket store
    window.addEventListener('optimization-update', (event) => {
        const update = event.detail;
        handleOptimizationUpdate(update);
        
        // Check if approval is required
        if (update.requiresApproval) {
            pendingApproval.value = {
                solutionId: update.solutionId || 'unknown',
                scoreChange: update.scoreChangePercent || 0,
                justification: update.justification || '',
                warnings: update.warnings || [],
                vehicleCount: update.routes?.length || 0
            };
            showGatekeeperModal.value = true;
        }
    });
    
    // Fetch saved assignments
    await fetchAssignments();
}

/**
 * Fetch all saved route assignments from backend
 */
async function fetchAssignments() {
    try {
        const res = await planningApi.get('/api/planning/assignments');
        assignments.value = res.data || [];
        console.log(`✅ Loaded ${assignments.value.length} route assignments`);
    } catch (e) {
        console.error("❌ Failed to fetch assignments:", e);
        assignments.value = [];
    }
}

// Function connectWebSocket removed - functionality moved to WebSocketService

function handleOptimizationUpdate(update) {
    console.log("⚡ Real-time Update:", update.score);
    // Map DTO to View Model
    if (!update.routes) return;
    
    const newRoutes = update.routes.map(r => ({
        vehicle: { id: r.vehicleId },
        totalDistance: r.totalDistanceMeters,
        activities: r.path.map(p => ({
            type: p.type === 'DEPOT' ? 'DEPOT' : 'DELIVERY', 
            location: { coordinate: { x: p.lon, y: p.lat } },
            name: p.type === 'DEPOT' ? 'Hub' : (p.visitId ? `Order ${p.visitId.substring(0,8)}` : 'Stop')
        }))
    }));
    
    solutionData.value = { routes: newRoutes };
    updateLayers();
}


async function triggerOptimization() {
    isOptimizing.value = true;
    try {
        // Use pending orders from OrderList if available, otherwise empty (= all unassigned)
        const orderIds = pendingOrderIds.value.length > 0 
            ? pendingOrderIds.value 
            : [];
        
        const payload = {
            orderIds,
            vehicleIds: []
        };
        
        // Add profile ID if selected
        if (selectedProfile.value) {
            payload.profileId = selectedProfile.value;
        }
        
        // Trigger FULL solution optimization (includes all Timefold metadata)
        const res = await planningApi.post('/api/planning/optimization/solution', payload);
        solutionData.value = parseSolution(res.data);
        
        // Auto-save routes as assignments to database
        await saveRoutesAsAssignments(res.data);
        
        updateLayers();
        activeTab.value = 'routes';
        
        // Clear pending orders after successful optimization
        pendingOrderIds.value = [];
        
        console.log('✅ Optimization complete, routes generated and saved');
    } catch (e) {
        console.error("Optimization Failed:", e);
        alert("Optimization Trigger Failed: " + (e.response?.data?.message || e.message));
    } finally {
        isOptimizing.value = false;
    }
}

/**
 * Auto-save optimized routes to database as route assignments
 */
async function saveRoutesAsAssignments(solutionData) {
    if (!solutionData?.routes || solutionData.routes.length === 0) {
        console.log('No routes to save');
        return;
    }
    
    try {
        const headers = { Authorization: `Bearer ${authStore.token}` };
        
        // Map solution routes to assignment DTOs
        const assignments = solutionData.routes.map((route, idx) => ({
            optimizationSolutionId: solutionData.id || null,
            vehicleId: route.vehicle?.id || null,
            driverId: null, // Not assigned yet
            carrierId: null,
            routeName: `Route ${idx + 1}`,
            routeData: {
                activities: route.activities || [],
                totalDistance: route.totalDistance || 0,
                vehicle: route.vehicle
            },
            status: 'DRAFT'
        }));
        
        // Batch save to backend
        const response = await planningApi.post('/api/planning/assignments/batch', assignments);
        console.log(`✅ Saved ${response.data.length} route assignments to database`);
        
        // Refresh assignments list to show newly created routes
        await fetchAssignments();
    } catch (e) {
        console.error('Failed to auto-save routes:', e);
        // Don't block optimization flow on save failure
    }
}

async function publishRoutes() {
    if (!solutionData.value) return;
    isPublishing.value = true;
    try {
        const publishedLinks = [];

        // Publish each route individually
        const promises = solutionData.value.routes.map(async (route, index) => {
            if (!route.vehicle) return null;

            try {
                const res = await planningApi.post('/api/planning/routes/publish', {
                    vehicleId: route.vehicle.id,
                    driverName: route.vehicle.name || `Driver ${index+1}`
                });

                return {
                    vehicle: route.vehicle.id,
                    link: res.data.magicLink
                };
            } catch (err) {
                console.error(`Failed to publish route for ${route.vehicle.id}`, err);
                return null;
            }
        });

        const results = await Promise.all(promises);
        const validLinks = results.filter(r => r !== null);

        if (validLinks.length > 0) {
            publishedLinks.value = validLinks;
        } else {
            alert("No routes could be published. Check console.");
        }

    } catch (e) {
        alert("Publish Failed: " + (e.message));
    } finally {
        isPublishing.value = false;
    }
}

function initDeck() {
    deckInstance.value = new Deck({
        canvas: 'deck-canvas',
        initialViewState: {
            longitude: 19.1451, // Poland Center
            latitude: 51.9194,
            zoom: 6,
            pitch: 45,
            bearing: 0
        },
        controller: true,
        layers: [],
        getTooltip: ({object}) => object && object.message
    });
}

function updateLayers() {
    if (!deckInstance.value) return;

    const deckLayers = [];
    
    // 0. Base Map Tiles (OpenStreetMap)
    deckLayers.push(new TileLayer({
        id: 'osm-tiles',
        data: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
        minZoom: 0,
        maxZoom: 19,
        tileSize: 256,
        renderSubLayers: props => {
            const {
                bbox: {west, south, east, north}
            } = props.tile;

            return new BitmapLayer(props, {
                data: null,
                image: props.data,
                bounds: [west, south, east, north]
            });
        }
    }));
    
    // 1. Hub (Dropzone) - Very Low Opacity
    const hub = [21.0122, 52.2297]; // Warsaw hub location
    deckLayers.push(new ScatterplotLayer({
        id: 'hub-marker',
        data: [{ position: hub }],
        getPosition: d => d.position,
        getFillColor: [100, 100, 255, 30], // Bardzo niskie opacity (alpha=30)
        getRadius: 1500, // Większy rozmiar dla widoczności dropzone
        stroked: true,
        getLineColor: [150, 150, 255, 80],
        getLineWidth: 100,
        lineWidthMinPixels: 1,
        pickable: true,
        onHover: info => setTooltip(info, 'Hub (Dropzone)', 'Warsaw Distribution Center')
    }));
    
    // 2. Unassigned Orders (Red Scatterplot)
    const orderPoints = unassignedOrders.value
        .filter(o => (o.delivery && o.delivery.lat && o.delivery.lon) || (o.deliveryAddress && o.deliveryAddress.coordinates))
        .map(o => {
            const lat = o.delivery ? o.delivery.lat : (o.deliveryAddress?.coordinates?.latitude || 0);
            const lon = o.delivery ? o.delivery.lon : (o.deliveryAddress?.coordinates?.longitude || 0);
            return {
                position: [lon, lat],
                color: [255, 80, 80, 200], // Subtle Red with alpha
                radius: 150, // Mniejszy rozmiar dla unassigned orders
                data: o
            }
        });

    deckLayers.push(new ScatterplotLayer({
        id: 'unassigned-orders',
        data: orderPoints,
        getPosition: d => d.position,
        getFillColor: d => d.color,
        getRadius: d => d.radius,
        pickable: true,
        autoHighlight: true,
        highlightColor: [255, 255, 255, 255],
        onHover: info => setTooltip(info, 'Unassigned Order', info.object?.data?.customer?.name)
    }));

    // 2. Solution Routes
    if (solutionData.value) {

        solutionData.value.routes.forEach((route, idx) => {
            const color = getRouteColor(idx);
            
            // Extract stops positions
            const stops = route.activities
                .filter(a => a.location && a.location.coordinate)
                .map(a => [a.location.coordinate.x, a.location.coordinate.y]);
            
            if (stops.length === 0) return;

            // Arcs (Hub to First Stop)
            if (layers.arcs) {
                deckLayers.push(new ArcLayer({
                    id: `arc-${idx}`,
                    data: [{ source: hub, target: stops[0] }],
                    getSourcePosition: d => d.source,
                    getTargetPosition: d => d.target,
                    getSourceColor: [0, 255, 204, 180],
                    getTargetColor: color,
                    getWidth: 6 // Grubsze linie arcov
                }));
            }

            // Individual Route Segments with Street Routing via OSRM
            // Using PathLayer with real routing geometry from OSRM API
            if (layers.paths) {
                const allPoints = [hub, ...stops];
                
                // Fetch routing geometry for each segment asynchronously
                const routePromises = [];
                for (let i = 0; i < allPoints.length - 1; i++) {
                    routePromises.push(
                        fetchRouteGeometry(allPoints[i], allPoints[i + 1])
                            .then(geometry => ({ segmentIndex: i, geometry }))
                    );
                }
                
                // Wait for all route geometries and render PathLayers
                Promise.all(routePromises).then(routes => {
                    routes.forEach(({ segmentIndex, geometry }) => {
                        deckLayers.push(new PathLayer({
                            id: `route-${idx}-seg-${segmentIndex}`,
                            data: [{ path: geometry }],
                            getPath: d => d.path,
                            getColor: color,
                            getWidth: 8, // Grubsze linie tras
                            widthUnits: 'pixels',
                            widthMinPixels: 4,
                            widthMaxPixels: 12,
                            rounded: true,
                            pickable: true,
                            onHover: info => setTooltip(
                                info,
                                `Route ${idx+1} - Segment ${segmentIndex+1}`,
                                segmentIndex === 0 ? 'From Hub' : `Stop ${segmentIndex} → ${segmentIndex+1}`
                            )
                        }));
                    });
                    
                    // Update deck layers after all routes are fetched
                    deckInstance.value.setProps({ layers: deckLayers });
                });
            }
            
            // Stop Points with Enhanced Styling
            route.activities.forEach((activity, stopIdx) => {
                const pos = [activity.location.coordinate.x, activity.location.coordinate.y];
                
                deckLayers.push(new ScatterplotLayer({
                    id: `stop-${idx}-${stopIdx}`,
                    data: [{ position: pos }],
                    getPosition: d => d.position,
                    getFillColor: color,
                    getRadius: 200, // Znacznie mniejsze punkty dostawy
                    getLineColor: [255, 255, 255],
                    getLineWidth: 80,
                    lineWidthMinPixels: 1,
                    pickable: true,
                    onHover: info => setTooltip(
                        info,
                        `Stop ${stopIdx + 1}`,
                        activity.name || 'Delivery'
                    )
                }));
            });
        });
    }

    deckInstance.value.setProps({ layers: deckLayers });
}

// --- ASSIGNMENT HELPERS ---

function viewAssignment(assignment) {
    selectedAssignment.value = assignment;
    showEditModal.value = true;
}

function closeEditModal() {
    showEditModal.value = false;
    selectedAssignment.value = null;
}

async function onAssignmentSaved() {
    // Refresh assignments list
    await fetchAssignments();
    console.log('✅ Assignment saved and list refreshed');
}

async function onAssignmentDeleted() {
    // Refresh assignments list
    await fetchAssignments();
    console.log('✅ Assignment deleted and list refreshed');
}

async function onAssignmentPublished() {
    // Refresh assignments list to update status
    await fetchAssignments();
    console.log('✅ Assignment published and list refreshed');
}

function closeGatekeeperModal() {
    showGatekeeperModal.value = false;
    pendingApproval.value = null;
}

async function onApprovalApproved(solutionId) {
    console.log('✅ Solution approved:', solutionId);
    // Solution is now approved, can proceed with publishing
    showGatekeeperModal.value = false;
    pendingApproval.value = null;
}

async function onApprovalRejected(solutionId) {
    console.log('❌ Solution rejected:', solutionId);
    // Solution rejected, don't allow publishing
    showGatekeeperModal.value = false;
    pendingApproval.value = null;
}

function getStatusClass(status) {
    const classes = {
        'DRAFT': 'bg-gray-500/20 text-gray-400 border border-gray-500/30',
        'ASSIGNED': 'bg-blue-500/20 text-blue-400 border border-blue-500/30',
        'PUBLISHED': 'bg-void-cyan-500/20 text-void-cyan-400 border border-void-cyan-500/30',
        'IN_PROGRESS': 'bg-void-amber-500/20 text-void-amber-400 border border-void-amber-500/30',
        'COMPLETED': 'bg-green-500/20 text-green-400 border border-green-500/30',
        'CANCELLED': 'bg-red-500/20 text-red-400 border border-red-500/30'
    };
    return classes[status] || 'bg-gray-500/20 text-gray-400';
}

// --- UTILS ---

function getRouteColor(index) {
    const palette = [
        [0, 255, 204], // Cyan
        [255, 0, 110], // Pink
        [255, 170, 0], // Amber
        [138, 43, 226], // Purple
        [0, 140, 255]  // Blue
    ];
    return palette[index % palette.length];
}

function setTooltip(info, title, text) {
    if (info.object) {
        tooltip.visible = true;
        tooltip.x = info.x;
        tooltip.y = info.y;
        tooltip.title = title;
        tooltip.text = text || '';
    } else {
        tooltip.visible = false;
    }
}

function flyToOrder(order) {
    // Backend uses delivery.lat/lon, not deliveryAddress.coordinates
    if (order.delivery?.lat && order.delivery?.lon) {
        deckInstance.value.setProps({
            initialViewState: {
                ...deckInstance.value.props.initialViewState,
                longitude: order.delivery.lon,
                latitude: order.delivery.lat,
                zoom: 12,
                transitionDuration: 1000
            }
        });
    }
}

function highlightRoute(route) {
    // Ideally highlight on map
}
function clearHighlight() {}

// --- TIME CHAIN RECONSTRUCTION ---
function parseSolution(sol) {
    // Handle both response formats:
    // 1. Old format from /latest: { routes: [...] } (VehicleRoutingSolution interface)
    // 2. New format from /solution: { vehicles: [...], stops: [...] } (VehicleRoutingSolutionDto)
    
    if (!sol) {
        console.warn("parseSolution: Null solution");
        return { routes: [] };
    }

    // If already in routes format (old API), return as-is
    if (sol.routes && Array.isArray(sol.routes)) {
        console.log("parseSolution: Using direct routes format with", sol.routes.length, "routes");
        return sol;
    }

    // Otherwise, reconstruct from vehicles+stops (new DTO format)
    if (!sol.vehicles || !sol.stops) {
        console.warn("parseSolution: Invalid solution structure", sol);
        return { routes: [] };
    }

    console.log("parseSolution: Parsing solution with", sol.vehicles.length, "vehicles and", sol.stops.length, "stops");

    const routes = [];
    const stopMap = new Map();
    
    // Index all stops by ID for quick lookup
    sol.stops.forEach(s => stopMap.set(s.id, s));

    sol.vehicles.forEach(vehicle => {
        const activities = [];
        let currentStop = null;
        
        // Find first stop for this vehicle
        // Look for stop where previousStandstill.type === "Vehicle" AND previousStandstill.id === vehicle.id
        currentStop = sol.stops.find(s => {
            const prev = s.previousStandstill;
            return prev && prev.type === "Vehicle" && prev.id === vehicle.id;
        });

        if (!currentStop) {
            // No stops assigned to this vehicle
            return;
        }

        // Traverse chain using previousStandstill references
        const maxStops = sol.stops.length + 1; // Safety limit
        let count = 0;
        const visitedIds = new Set(); // Prevent infinite loops
        
        while (currentStop && count < maxStops) {
            if (visitedIds.has(currentStop.id)) {
                console.error("Circular reference detected at stop", currentStop.id);
                break;
            }
            visitedIds.add(currentStop.id);

            // Extract location data
            const order = currentStop.order;
            const delivery = order ? (order.delivery || order.deliveryAddress) : null;
            
            const lat = currentStop.latitude || (delivery ? delivery.lat : 0);
            const lon = currentStop.longitude || (delivery ? delivery.lon : 0);

            // Only add if valid coordinates
            if (lat && lon) {
                activities.push({
                    name: delivery ? (delivery.customer || delivery.name || delivery.city || 'Unknown') : 'Stop',
                    type: 'DELIVERY',
                    location: {
                        coordinate: { x: lon, y: lat } // DeckGL format
                    },
                    orderId: order ? (order.orderId || order.id) : currentStop.id
                });
            }

            // Find next stop: search for stop where previousStandstill.id === currentStop.id
            const currentId = currentStop.id;
            currentStop = sol.stops.find(s => {
                const prev = s.previousStandstill;
                return prev && prev.type === "RouteStop" && prev.id === currentId;
            });

            count++;
        }

        if (activities.length > 0) {
            console.log("Vehicle", vehicle.id, "has", activities.length, "activities");
            routes.push({
                vehicle: vehicle,
                activities: activities,
                totalDistance: activities.length * 5000 // Mock: 5km per stop avg
            });
        }
    });

    console.log("parseSolution: Generated", routes.length, "routes");
    return { routes };
}

function formatCurrency(val) {
    return new Intl.NumberFormat('en-IE', { style: 'currency', currency: 'EUR' }).format(val);
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleString();
}

</script>

<style scoped>
.glass-panel {
    background: rgba(5, 5, 10, 0.7);
    backdrop-filter: blur(16px);
    -webkit-backdrop-filter: blur(16px);
}

.custom-scrollbar::-webkit-scrollbar {
    width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
    background: #334;
    border-radius: 4px;
}

.text-glow {
    text-shadow: 0 0 10px currentColor;
}
</style>
