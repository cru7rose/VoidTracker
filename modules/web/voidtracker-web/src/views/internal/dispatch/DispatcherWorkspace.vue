<template>
  <div class="flex h-screen overflow-hidden bg-gray-50 flex-col">
    <!-- TOP PANEL: MAP (60% Height) -->
    <div class="h-[60%] relative bg-gray-200">
        <MapComponent 
            :zoom="12" 
            :center="[52.23, 21.01]" 
            :routes="mapRoutes"
            :markers="mapMarkers"
        />
        
        <!-- Floating Toolbar -->
        <div class="absolute top-4 left-4 z-[1000] bg-white rounded shadow p-2 flex gap-2">
            <button @click="fetchOrders" class="px-3 py-1 bg-white border rounded text-xs hover:bg-gray-50">Refresh Orders</button>
            <button @click="showConfigModal = true" class="px-3 py-1 bg-blue-600 text-white rounded text-xs hover:bg-blue-700 shadow font-bold">Configure & Optimize</button>
            <button @click="publishRoutes" class="px-3 py-1 bg-green-600 text-white rounded text-xs hover:bg-green-700 shadow font-bold">Publish Plan</button>
        </div>

        <!-- KPI Overlay -->
        <div class="absolute top-4 right-4 z-[1000] bg-white/90 rounded shadow p-3 text-xs w-64 backdrop-blur-sm">
            <h3 class="font-bold text-gray-700 border-b pb-1 mb-2">Plan Summary</h3>
            <div class="grid grid-cols-2 gap-y-2">
                <span class="text-gray-500">Total Routes:</span>
                <span class="font-bold text-right">{{ routes.length }}</span>
                <span class="text-gray-500">Unassigned:</span>
                <span class="font-bold text-right" :class="orders.length > 0 ? 'text-red-600' : 'text-green-600'">{{ orders.length }}</span>
                <span class="text-gray-500">Total Distance:</span>
                <span class="font-bold text-right">{{ totalDistance }} km</span>
                <span class="text-gray-500">Total Duration:</span>
                <span class="font-bold text-right">{{ totalDuration }} h</span>
            </div>
        </div>
    </div>

    <!-- BOTTOM PANEL: DATA TABLES (40% Height) -->
    <div class="flex-1 bg-white border-t border-gray-300 flex flex-col">
        <!-- Tabs -->
        <div class="flex border-b border-gray-200 bg-gray-50">
            <button 
                v-for="tab in ['Unplanned Orders', 'Planned Routes']" 
                :key="tab"
                @click="activeTab = tab"
                class="px-6 py-3 text-sm font-medium border-r border-gray-200 hover:bg-white transition-colors"
                :class="activeTab === tab ? 'bg-white text-blue-600 border-t-2 border-t-blue-600' : 'text-gray-600'"
            >
                {{ tab }}
            </button>
        </div>

        <!-- Tab Content -->
        <div class="flex-1 overflow-auto p-0">
            
            <!-- TABLE: UNPLANNED ORDERS -->
            <div v-if="activeTab === 'Unplanned Orders'" class="h-full">
                <table class="w-full text-left border-collapse">
                    <thead class="bg-gray-100 sticky top-0 z-10 text-xs text-gray-500 uppercase">
                        <tr>
                            <th class="p-3 border-b"><input type="checkbox" @change="selectAll"></th>
                            <th class="p-3 border-b">Order ID</th>
                            <th class="p-3 border-b">Priority</th>
                            <th class="p-3 border-b">City</th>
                            <th class="p-3 border-b">Address</th>
                            <th class="p-3 border-b">Weight</th>
                            <th class="p-3 border-b">Time Window</th>
                            <th class="p-3 border-b">Status</th>
                        </tr>
                    </thead>
                    <tbody class="text-sm divide-y divide-gray-100">
                        <tr 
                            v-for="order in orders" 
                            :key="order.orderId" 
                            class="hover:bg-blue-50 cursor-pointer transition-colors" 
                            @click="openOrderDetails(order.orderId)"
                            @mouseenter="hoveredOrderId = order.orderId"
                            @mouseleave="hoveredOrderId = null"
                            :class="{'bg-blue-50': hoveredOrderId === order.orderId}"
                        >
                            <td class="p-3" @click.stop><input type="checkbox" :checked="isSelected(order)" @change="toggleSelection(order)"></td>
                            <td class="p-3 font-mono text-xs">{{ order.orderId.substring(0,8) }}</td>
                            <td class="p-3"><span class="px-2 py-1 rounded text-xs font-bold" :class="getPriorityClass(order.priority)">{{ order.priority }}</span></td>
                            <td class="p-3">{{ order.delivery?.city || order.deliveryAddress?.city }}</td>
                            <td class="p-3 truncate max-w-[200px]">{{ order.delivery?.street || order.deliveryAddress?.street }}</td>
                            <td class="p-3">{{ getWeight(order) }} kg</td>
                            <td class="p-3">{{ formatTime(order.deliveryTimeFrom) }}</td>
                            <td class="p-3"><span class="bg-gray-100 text-gray-600 px-2 py-1 rounded text-xs">NEW</span></td>
                        </tr>
                    </tbody>
                </table>
                <div v-if="orders.length === 0" class="p-8 text-center text-gray-400">No data available</div>
            </div>

            <!-- TABLE: PLANNED ROUTES -->
            <div v-if="activeTab === 'Planned Routes'" class="h-full">
                <table class="w-full text-left border-collapse">
                    <thead class="bg-gray-100 sticky top-0 z-10 text-xs text-gray-500 uppercase">
                        <tr>
                            <th class="p-3 border-b">Route</th>
                            <th class="p-3 border-b">Driver</th>
                            <th class="p-3 border-b">Vehicle</th>
                            <th class="p-3 border-b">From → To</th>
                            <th class="p-3 border-b">Stops</th>
                            <th class="p-3 border-b">Distance</th>
                            <th class="p-3 border-b">Capacity</th>
                            <th class="p-3 border-b">Actions</th>
                        </tr>
                    </thead>
                    <tbody class="text-sm divide-y divide-gray-100">
                        <tr v-for="route in routes" :key="route.id" class="hover:bg-gray-50">
                            <td class="p-3 font-bold text-gray-700">{{ route.name }}</td>
                            <td class="p-3">
                                <span v-if="route.driverId && getDriverName(route)" class="text-sm">{{ getDriverName(route) }}</span>
                                <span v-else class="text-gray-400 italic text-xs">Not assigned</span>
                            </td>
                            <td class="p-3 text-sm">{{ route.vehicleId || 'Auto' }}</td>
                            <td class="p-3 text-xs text-gray-600">{{ route.stops || 0 }} stops</td>
                            <td class="p-3">{{ route.stops }}</td>
                            <td class="p-3">{{ route.distance || '0' }} km</td>
                            <td class="p-3 w-40">
                                <div class="w-full bg-gray-200 rounded-full h-2">
                                    <div class="bg-blue-600 h-2 rounded-full" :style="{width: getCapacityPercentage(route) + '%'}"></div>
                                </div>
                                <div class="text-[10px] text-gray-500 text-right mt-1">{{ getCapacityPercentage(route) }}%</div>
                            </td>
                            <td class="p-3">
                                <button @click="openAssignDriverParams(route)" class="text-blue-600 hover:underline text-xs block mb-1">{{ route.driverId ? 'Reassign' : 'Assign Driver' }}</button>
                                <button v-if="route.driverId" @click="sendMagicLinkForRoute(route)" class="text-green-600 hover:underline text-xs block">📧 Send Link</button>
                            </td>
                        </tr>
                    </tbody>
                </table>
                <div v-if="routes.length === 0" class="p-8 text-center text-gray-400">No routes planned. Configure & Optimize to start.</div>
            </div>

        </div>
    </div>

    <!-- OPTIMIZATION CONFIG MODAL (PROFILE MANAGER) -->
    <div v-if="showConfigModal" class="fixed inset-0 z-[2000] bg-black/50 flex items-center justify-center p-4">
        <div class="bg-white rounded-lg shadow-xl w-full max-w-2xl h-[80vh] overflow-hidden flex flex-col">
            <ProfileManager 
                @cancel="showConfigModal = false"
                @confirm="runOptimizationWithProfile"
            />
        </div>
    </div>

    <!-- DRIVER SELECTION MODAL -->
    <div v-if="showDriverModal" class="fixed inset-0 z-[2000] bg-black/50 flex items-center justify-center p-4">
        <div class="bg-white rounded-lg shadow-xl w-full max-w-lg p-6 animate-fade-in-up">
            <h3 class="font-bold text-lg mb-4">Assign Driver to Route</h3>
            
            <div class="mb-4">
                <input v-model="driverSearch" placeholder="Search drivers..." class="w-full border rounded p-2 text-sm mb-2">
                <div class="max-h-60 overflow-y-auto border rounded divide-y">
                    <div v-for="driver in filteredDrivers" :key="driver.userId" 
                         @click="selectedDriverId = driver.userId"
                         class="p-2 cursor-pointer hover:bg-blue-50"
                         :class="{'bg-blue-100': selectedDriverId === driver.userId}">
                        <div class="font-bold text-sm">{{ driver.username }}</div>
                        <div class="text-xs text-gray-500">{{ driver.email }}</div>
                    </div>
                </div>
            </div>

            <div class="flex justify-end gap-3">
                <button @click="showDriverModal = false" class="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded text-sm">Cancel</button>
                <button 
                    @click="confirmDriverAssignment" 
                    :disabled="!selectedDriverId || assigning"
                    class="px-4 py-2 bg-blue-600 text-white rounded text-sm font-bold shadow hover:bg-blue-700 disabled:opacity-50"
                >
                    {{ assigning ? 'Assigning...' : 'Assign Driver' }}
                </button>
            </div>
        </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useDispatchStore } from '../../../stores/dispatchStore';
import { orderApi } from '../../../api/axios';
import MapComponent from '../../../components/MapComponent.vue';
import ProfileManager from '../../../components/dashboard/ProfileManager.vue';

const dispatchStore = useDispatchStore();

// UI State
const activeTab = ref('Unplanned Orders');
// Optimization Actions
const showConfigModal = ref(false);

// Data
const orders = ref([]);
const loadingOrders = ref(false);
const selectedOrders = ref([]);
const hoveredOrderId = ref(null);
const driverSearch = ref('');
const showDriverModal = ref(false);
const selectedDriverId = ref(null);
const activeRouteId = ref(null);
const assigning = ref(false);

const filteredDrivers = computed(() => {
    if (!driverSearch.value) return dispatchStore.drivers;
    const q = driverSearch.value.toLowerCase();
    return dispatchStore.drivers.filter(d => 
        (d.username && d.username.toLowerCase().includes(q)) || 
        (d.email && d.email.toLowerCase().includes(q))
    );
});

// Computed
const routes = computed(() => dispatchStore.routes);
const totalDistance = computed(() => routes.value.reduce((acc, r) => acc + parseFloat(r.distance || 0), 0).toFixed(1));
const totalDuration = computed(() => routes.value.reduce((acc, r) => acc + parseFloat(r.time || 0), 0).toFixed(1));

// Map Helpers
// Deterministic pseudo-random for consistent positions per ID
const getDeterministicGeo = (id) => {
    let hash = 0;
    for (let i = 0; i < id.length; i++) {
        hash = id.charCodeAt(i) + ((hash << 5) - hash);
    }
    // Warsaw Center approx 52.23, 21.01
    // Spread +/- 0.1 degree
    const latOffset = (hash % 1000) / 5000; // -0.1 to 0.1
    const lonOffset = ((hash >> 16) % 1000) / 5000;
    return {
        lat: 52.23 + latOffset,
        lng: 21.01 + lonOffset
    };
};

const mapRoutes = computed(() => {
    return routes.value.map((route, idx) => {
        // Warehouse
        const warehouse = [52.23, 21.01];
        const path = [warehouse];
        
        route.legs?.forEach(leg => {
             // Use order address geo if available (TODO: Backend should enforce this)
             // For now use deterministic fallbacks
             const geo = getDeterministicGeo(leg.orderId);
             path.push([geo.lat, geo.lng]);
        });
        
        // Return to warehouse? VRP usually implies return.
        path.push(warehouse);

        return {
            name: route.name,
            coordinates: path,
            color: ['blue', 'red', 'green', 'purple', 'orange'][idx % 5]
        };
    });
});

const mapMarkers = computed(() => {
    const markers = [];

    // 1. Depot
    markers.push({
        lat: 52.23,
        lng: 21.01,
        type: 'depot',
        popup: 'Depot (Warsaw)'
    });

    // 2. Planned Stops (Numbered)
    routes.value.forEach((route, routeIdx) => {
        const color = ['blue', 'red', 'green', 'purple', 'orange'][routeIdx % 5];
        route.legs?.forEach((leg, legIdx) => {
            const geo = getDeterministicGeo(leg.orderId);
            markers.push({
                lat: geo.lat,
                lng: geo.lng,
                type: 'numbered',
                number: legIdx + 1,
                color: color,
                popup: `Stop ${legIdx + 1}: Order ${leg.orderId}`,
                isHighlight: hoveredOrderId.value === leg.orderId
            });
        });
    });

    // 3. Unplanned Orders (Gray/Red small dots)
    orders.value.forEach(o => {
        // Check if real geo exists
        let lat = o.delivery?.lat;
        let lng = o.delivery?.lon;
        
        if (!lat || !lng) {
             const geo = getDeterministicGeo(o.orderId);
             lat = geo.lat;
             lng = geo.lng;
        }

        // Only show if NOT planned (i.e. not in routes)
        // Check if orderId is in any route leg
        const isPlanned = routes.value.some(r => r.legs?.some(l => l.orderId === o.orderId));
        
        if (!isPlanned) {
            markers.push({
                lat,
                lng,
                type: 'numbered', // Reuse numbered but maybe 'dot' style later?
                number: '?', // Or just priority
                color: o.priority === 'HIGH' ? 'red' : 'gray',
                popup: `Unplanned: ${o.orderId}`,
                isHighlight: hoveredOrderId.value === o.orderId
            });
        }
    });

    return markers;
});


// Logic ---------------------------------------------------------

const fetchOrders = async () => {
  loadingOrders.value = true;
  try {
    const res = await orderApi.post('/api/internal/orders/query', { statuses: ['NEW'] });
    orders.value = res.data;
  } catch (e) {
    console.error("Fetch failed", e);
    // Mock
    orders.value = Array.from({ length: 15 }).map((_, i) => ({
         orderId: `ORD-${1000 + i}`,
         priority: Math.random() > 0.8 ? 'HIGH' : 'NORMAL',
         deliveryAddress: { city: 'Warsaw', street: `Street ${i + 1}` },
         deliveryTimeFrom: new Date().toISOString(),
         assets: [{ attributes: { weight: Math.floor(Math.random() * 50) + 5 } }]
      }));
  } finally {
    loadingOrders.value = false;
  }
};

const runOptimizationWithProfile = async (profileId) => {
    showConfigModal.value = false;
    // Construct payload with profile ID
    const payload = {
        model: 'TIMEFOLD_VRP',
        orderIds: selectedOrders.value.length > 0 ? selectedOrders.value.map(o => o.orderId) : orders.value.map(o => o.orderId),
        vehicleIds: [], // Logic handled by backend (based on profile)
        profileId: profileId
    };
    await dispatchStore.runOptimization(payload);
    activeTab.value = 'Planned Routes'; // Switch tab to see results
};

const publishRoutes = async () => {
   await dispatchStore.publishRoutes();
   alert("Published!");
};

// Selection -----------------------------------------------------
const isSelected = (o) => selectedOrders.value.some(s => s.orderId === o.orderId);
const toggleSelection = (o) => {
    const idx = selectedOrders.value.findIndex(s => s.orderId === o.orderId);
    if (idx > -1) selectedOrders.value.splice(idx, 1);
    else selectedOrders.value.push(o);
};
const selectAll = () => {
    if (selectedOrders.value.length === orders.value.length) selectedOrders.value = [];
    else selectedOrders.value = [...orders.value];
};

// Utils ---------------------------------------------------------
const getPriorityClass = (p) => p === 'HIGH' ? 'bg-red-100 text-red-800' : 'bg-gray-100 text-gray-600';
const getWeight = (o) => o.aPackage?.weight || o.packageDetails?.weight || o.assets?.[0]?.attributes?.weight || 0;
const formatTime = (iso) => iso ? new Date(iso).toLocaleDateString() : '-';
const getCapacityPercentage = (route) => Math.min(Math.round(((route.totalWeight || 500) / 1000) * 100), 100);

const openOrderDetails = (orderId) => {
    window.open(`/internal/orders/${orderId}`, '_blank');
};

const openDriverView = (route) => {
     window.open('http://localhost:5176/driver/tasks', '_blank');
};

const openAssignDriverParams = (route) => {
    activeRouteId.value = route.id;
    selectedDriverId.value = null; // Reset selection
    showDriverModal.value = true;
    dispatchStore.fetchDrivers();
};

const confirmDriverAssignment = async () => {
    if (!selectedDriverId.value || !activeRouteId.value) return;
    assigning.value = true;
    try {
        await dispatchStore.assignDriverToRoute(activeRouteId.value, selectedDriverId.value);
        showDriverModal.value = false;
    } catch (e) {
        alert("Failed to assign driver");
    } finally {
        assigning.value = false;
    }
};

const sendMagicLink = async (driver) => {
    if (!confirm(`Send Magic Link to ${driver.email}?`)) return;
    
    // Find the route for this driver (we need to get the selected route from context)
    const route = routes.value.find(r => r.driverId === driver.userId);
    if (!route) {
        alert("Please assign this driver to a route first");
        return;
    }
    
    try {
        const res = await dispatchStore.sendMagicLink(driver.userId, route.id, driver.email);
        alert(`Magic Link Sent!\nToken: ${res.token}\nLink: ${res.link}`);
    } catch (e) {
        console.error(e);
        alert("Failed to send magic link");
    }
};



const sendMagicLinkForRoute = async (route) => {
    if (!route.driverId) {
        alert("Please assign a driver to this route first");
        return;
    }
    
    const driver = dispatchStore.drivers.find(d => d.userId === route.driverId);
    if (!driver) {
        alert("Driver not found");
        return;
    }
    
    if (!confirm(`Send Magic Link to ${driver.email} for ${route.name}?`)) return;
    
    try {
        const res = await dispatchStore.sendMagicLink(route.driverId, route.id, driver.email);
        alert(`✅ Magic Link Sent to ${driver.email}!\n\n🔗 Link: ${res.link}\n\nThe driver can access their tasks using this link.`);
    } catch (e) {
        console.error(e);
        alert("❌ Failed to send magic link. Please check the logs.");
    }
};

// Helper to get driver name from route
const getDriverName = (route) => {
    if (!route || !route.driverId) return null;
    const driver = dispatchStore.drivers.find(d => d.userId === route.driverId);
    return driver ? driver.username : null;
};

// Helper to get route description (from -> to)
const getRouteDescription = (route) => {
    if (!route || !route.legs || route.legs.length === 0) {
        return '—';
    }
    if (route.legs.length === 1) {
        return `1 stop`;
    }
    return `${route.legs.length} stops`;
};

onMounted(async () => {
    fetchOrders();
    // Check if we requested a specific plan
    const route = useRoute();
    if (route.query.planId) {
        await dispatchStore.fetchPlan(route.query.planId);
    } else {
        await dispatchStore.fetchLatestPlan();
    }
});
</script>

<style scoped>
/* Scrollbar */
::-webkit-scrollbar { width: 6px; height: 6px; }
::-webkit-scrollbar-thumb { background: #cbd5e0; border-radius: 3px; }
table th { font-weight: 600; letter-spacing: 0.05em; }
</style>
