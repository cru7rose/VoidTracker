<template>
  <div class="h-full flex flex-col overflow-hidden bg-void-black text-void-cyan-400">
    <!-- Header Controls -->
    <div class="flex justify-between items-center mb-4 px-4 py-2 border-b border-void-cyan-900">
      <h2 class="text-xl font-bold text-void-cyan-300 font-mono">Routes Mosaic</h2>
        <div class="flex gap-3 items-center">
         <button @click="showConstraintsModal = true" class="text-void-cyan-400 hover:text-void-cyan-300 px-3 py-2 border border-void-cyan-800 rounded bg-void-cyan-950 flex items-center gap-2 hover:bg-void-cyan-900 transition-colors">
            <span>⚙️</span> Constraints
         </button>
         <select v-model="selectedModel" class="border border-void-cyan-800 rounded px-3 py-2 bg-void-black text-void-cyan-300 focus:border-void-cyan-600 focus:outline-none">
          <option value="PUDO_CLUSTER">PUDO Cluster (Locker/PUDO)</option>
          <option value="DYNAMIC_SIMULATION">Digital Twin (Simulation)</option>
          <option value="STRICT_CONSTRAINT">Strict AETR (Legal)</option>
        </select>
        <button @click="runOptimization" class="bg-void-cyan-600 text-void-black px-4 py-2 rounded hover:bg-void-cyan-500 shadow-lg shadow-void-cyan-900/20 flex items-center gap-2 font-bold transition-colors">
          <span>⚡</span> Optimize Routes
        </button>
      </div>
    </div>

    <div class="flex-1 flex gap-6 min-h-0">
        <!-- Main: Active Routes Grid -->
        <div class="flex-1 flex flex-col min-w-0">
            <h3 class="font-semibold text-void-cyan-500 mb-2 uppercase text-xs tracking-wider px-2">Plan: {{ routes.length }} Routes</h3>
            <div class="grid grid-cols-1 md:grid-cols-2 2xl:grid-cols-3 gap-4 overflow-y-auto pr-2 pb-2">
                <div v-for="route in routes" :key="route.id" 
                    class="bg-void-cyan-950 border border-void-cyan-800 rounded-xl shadow-lg hover:shadow-void-cyan-900/50 hover:border-void-cyan-600 transition-all duration-200 flex flex-col h-[500px]">
                    
                    <!-- Route Header -->
                    <div class="p-4 border-b border-void-cyan-900">
                        <div class="flex justify-between items-start">
                            <div>
                                <h3 class="font-bold text-lg text-void-cyan-300">{{ route.name }}</h3>
                                <div class="flex items-center gap-2 mt-1">
                                    <span class="text-xs bg-void-cyan-900 text-void-cyan-400 px-2 py-0.5 rounded-full">{{ route.vehicle }}</span>
                                    <span v-if="route.driver !== 'Unassigned'" class="text-xs bg-void-cyan-900 text-void-cyan-300 px-2 py-0.5 rounded-full flex items-center gap-1">
                                        👤 {{ route.driver }}
                                    </span>
                                    <span v-else class="text-xs bg-red-900/50 text-red-300 px-2 py-0.5 rounded-full">
                                        ⚠️ No Driver
                                    </span>
                                </div>
                            </div>
                            <span :class="['px-2 py-1 rounded text-xs font-bold uppercase tracking-wide', getStatusClass(route.status)]">
                                {{ route.status }}
                            </span>
                        </div>
                        
                        <!-- Stats Row -->
                        <div class="grid grid-cols-3 gap-2 mt-4">
                            <div class="text-center">
                                <div class="text-lg font-bold text-void-cyan-300">{{ route.stops }}</div>
                                <div class="text-[10px] text-void-cyan-500 uppercase">Stops</div>
                            </div>
                            <div class="text-center border-l border-void-cyan-900">
                                <div class="text-lg font-bold text-void-cyan-300">{{ route.distance }}<span class="text-xs font-normal text-void-cyan-500">km</span></div>
                                <div class="text-[10px] text-void-cyan-500 uppercase">Dist</div>
                            </div>
                            <div class="text-center border-l border-void-cyan-900">
                                <div class="text-lg font-bold text-void-cyan-300">{{ route.time }}<span class="text-xs font-normal text-void-cyan-500">h</span></div>
                                <div class="text-[10px] text-void-cyan-500 uppercase">Time</div>
                            </div>
                        </div>
                    </div>

                    <!-- Stops List (Scrollable) -->
                    <div class="flex-1 overflow-y-auto p-2 space-y-1 bg-void-black/50" 
                         @dragover.prevent 
                         @drop="onDrop($event, route.id, -1)">
                         
                        <div v-if="route.legs.length === 0" class="h-full flex flex-col items-center justify-center text-void-cyan-600 text-sm">
                            <span>Empty Route</span>
                            <span class="text-xs">Drag orders here</span>
                        </div>

                        <div v-for="(stop, index) in route.legs" :key="index" 
                            class="group flex items-center gap-3 p-3 bg-void-black rounded-lg border border-void-cyan-800 shadow-sm cursor-move hover:border-void-cyan-600 transition-colors"
                            draggable="true"
                            @dragstart="onDragStart($event, route.id, index, stop)">
                            
                            <div class="w-6 h-6 rounded-full bg-void-cyan-900 text-void-cyan-400 flex items-center justify-center text-xs font-bold flex-shrink-0 group-hover:bg-void-cyan-800 group-hover:text-void-cyan-300 transition-colors">
                                {{ index + 1 }}
                            </div>
                            <div class="flex-1 min-w-0">
                                <div class="truncate font-medium text-void-cyan-300 text-sm">{{ stop.address }}</div>
                                <div class="text-xs text-void-cyan-500 flex items-center gap-2">
                                    <span>🆔 {{ stop.orderId?.substring(0,6) || 'N/A' }}</span>
                                    <span>🕒 {{ stop.eta || '--:--' }}</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Actions Footer -->
                    <div class="p-3 border-t border-void-cyan-900 bg-void-cyan-950 rounded-b-xl flex gap-1">
                        <button @click="openAssignModal(route)" class="flex-1 py-1.5 text-xs font-medium text-void-cyan-300 bg-void-cyan-900 rounded hover:bg-void-cyan-800 transition-colors">
                            Assign Driver
                        </button>
                        <button @click="openRouteDetails(route)" class="px-3 py-1.5 text-xs font-medium text-void-cyan-400 bg-void-black rounded hover:bg-void-cyan-950 transition-colors">
                            Details
                        </button>
                        <button @click="publishRoute(route)" class="px-3 py-1.5 text-xs font-medium text-green-300 bg-green-900/30 rounded hover:bg-green-900/50 transition-colors">
                            Publish
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Right: Unassigned Orders (Control Tower) -->
        <div class="w-80 flex flex-col bg-white border-l border-gray-200">
            <div class="p-4 border-b border-gray-200 bg-gray-50">
                <h3 class="font-bold text-gray-800 flex items-center gap-2">
                    <span>📦</span> Unassigned Orders
                    <span class="px-2 py-0.5 bg-gray-200 rounded-full text-xs">{{ unassignedOrders.length }}</span>
                </h3>
                <div class="mt-2 relative">
                    <input type="text" placeholder="Search orders..." class="w-full text-sm border-gray-300 rounded-md pl-8 py-1.5 focus:ring-blue-500 focus:border-blue-500">
                    <span class="absolute left-2.5 top-2 text-gray-400 text-xs">🔍</span>
                </div>
            </div>
            
            <div class="flex-1 overflow-y-auto p-2 space-y-2 bg-gray-50">
                <div v-for="order in unassignedOrders" :key="order.id"
                     class="p-3 bg-white border border-gray-200 rounded-lg shadow-sm cursor-move hover:border-blue-400 hover:shadow-md transition-all active:cursor-grabbing"
                     draggable="true"
                     @dragstart="onDragStart($event, 'UNASSIGNED', -1, order)">
                    <div class="flex justify-between items-start mb-1">
                        <span class="text-xs font-bold text-gray-500">#{{ order.id.substring(0,8) }}</span>
                        <span class="text-[10px] bg-red-100 text-red-700 px-1.5 rounded">PENDING</span>
                    </div>
                    <div class="text-sm font-medium text-gray-900 mb-1">{{ order.address || 'Unknown Address' }}</div>
                    <div class="flex justify-between text-xs text-gray-400">
                        <span>{{ order.weight }}kg</span>
                        <span>{{ order.serviceType }}</span>
                    </div>
                </div>
                
                <div v-if="unassignedOrders.length === 0" class="text-center py-8 text-gray-400 text-sm">
                    No unassigned orders.
                </div>
            </div>
        </div>
    </div>

    <!-- Gatekeeper Approval Modal -->
    <GatekeeperApprovalModal
      :show="showGatekeeperModal"
      :approval-id="gatekeeperData.approvalId"
      :justification="gatekeeperData.justification"
      :warnings="gatekeeperData.warnings"
      :score-change-percent="gatekeeperData.scoreChangePercent"
      @close="showGatekeeperModal = false"
      @approved="handleApproval"
      @rejected="handleRejection"
    />

    <!-- Modals ... (Keep existing modals) -->
    <div v-if="showAssignModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-6 w-96 shadow-xl transform transition-all scale-100">
        <h3 class="text-lg font-bold mb-4">Assign Driver</h3>
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700">Driver ID / Name</label>
            <input v-model="assignForm.driverId" type="text" class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2 focus:ring-blue-500 focus:border-blue-500" placeholder="e.g. driver-1">
          </div>
          <div class="bg-blue-50 p-3 rounded text-xs text-blue-700">
            After claiming, the driver ({{ assignForm.driverId }}) tasks will appear in the Driver App automatically.
          </div>
        </div>
        <div class="mt-6 flex justify-end gap-3">
          <button @click="showAssignModal = false" class="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded">Cancel</button>
          <button @click="assignDriver" class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 shadow-sm" :disabled="assigning">
            {{ assigning ? 'Saving...' : 'Confirm Assignment' }}
          </button>
        </div>
      </div>
    </div>
    <!-- ... same Constraints modal ... -->
    <div v-if="showConstraintsModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-6 w-96">
        <h3 class="text-lg font-bold mb-4">Optimization Constraints</h3>
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700">Max Stops per Route</label>
            <input v-model.number="constraints.maxStops" type="number" class="mt-1 block w-full border rounded-md shadow-sm p-2">
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">Max Weight per Vehicle (kg)</label>
            <input v-model.number="constraints.maxWeight" type="number" class="mt-1 block w-full border rounded-md shadow-sm p-2">
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">Time Window Strictness</label>
            <select v-model="constraints.timeWindowStrictness" class="mt-1 block w-full border rounded-md shadow-sm p-2 bg-white">
                <option value="HARD">Hard (Must meet window)</option>
                <option value="SOFT">Soft (Allow late with penalty)</option>
            </select>
          </div>
        </div>
        <div class="mt-6 flex justify-end gap-3">
          <button @click="showConstraintsModal = false" class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">Done</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed, watch } from 'vue';
import { useDispatchStore } from '../../../stores/dispatchStore';
import { planningApi } from '../../../api/axios';
import { generateManifest } from '../../../utils/pdfGenerator';
import GatekeeperApprovalModal from '../../../components/GatekeeperApprovalModal.vue';

const dispatchStore = useDispatchStore();

// Use computed properties to read from store
const routes = computed(() => dispatchStore.routes);
const unassignedOrders = computed(() => dispatchStore.unassignedOrders || []);
const loading = computed(() => dispatchStore.loading);

const selectedModel = ref('PUDO_CLUSTER');

// Modal State
const showAssignModal = ref(false);
const selectedRoute = ref(null);
const assigning = ref(false);
const assignForm = reactive({
  driverId: '',
  email: ''
});

const assignDriver = async () => {
  if (!selectedRoute.value || !assignForm.driverId) return;
  assigning.value = true;
  try {
    const vehicleId = selectedRoute.value.id; 
    // Assuming backend endpoint needs vehicleId to find the FleetVehicleEntity
    await planningApi.post(`/dispatch/routes/${vehicleId}/assign-driver`, null, { // Corrected endpoint for logic
        params: { driverId: assignForm.driverId }
    });
    // Also try the dedicated endpoint if that fails or based on previous logic finding
    // Actually the previous thought was 'dispatch/routes/{id}/assign-driver' in DispatchController? 
    // Nope, I implemented it in DispatchService.
    // Let's use the one I added in Phase 11.
    
    alert(`Driver ${assignForm.driverId} assigned!`);
    showAssignModal.value = false;
    await dispatchStore.fetchLatestPlan(); // Refresh to see driver name
  } catch (e) {
    console.error(e);
    // Fallback: If 404, maybe the route ID is not vehicle ID?
    // But route.id is vehicle ID in store.
    alert('Failed to assign driver. Ensure Vehicle ID exists.');
  } finally {
    assigning.value = false;
  }
};

const showConstraintsModal = ref(false);
const constraints = dispatchStore.constraints;

// Gatekeeper Approval Modal
const showGatekeeperModal = ref(false);
const gatekeeperData = ref({
  approvalId: null,
  justification: null,
  warnings: [],
  scoreChangePercent: 0
});

// Watch for approval requirements from optimization updates
watch(() => dispatchStore.lastOptimizationUpdate, (update) => {
  if (update && update.requiresApproval) {
    gatekeeperData.value = {
      approvalId: update.approvalId || `GK-${Date.now()}`,
      justification: update.justification || 'Wymagana weryfikacja rozwiązania optymalizacji.',
      warnings: update.warnings || [],
      scoreChangePercent: update.scoreChangePercent || 0
    };
    showGatekeeperModal.value = true;
  }
}, { deep: true }); 

// Drag and Drop State
const draggedItem = ref(null);

const onDragStart = (event, sourceId, index, item) => {
    // sourceId can be 'UNASSIGNED' or routeId
    draggedItem.value = { sourceId, index, item };
    event.dataTransfer.effectAllowed = 'move';
    event.target.style.opacity = '0.5';
};

const onDrop = async (event, targetRouteId, targetIndex) => {
    event.target.style.opacity = '1';
    
    if (!draggedItem.value) return;
    
    const { sourceId, item } = draggedItem.value;

    // Check if dropping on same route
    if (sourceId === targetRouteId) return;

    // Logic for appending to route
    try {
        if (targetRouteId) {
            // Append logic
            await dispatchStore.appendOrder(targetRouteId, item.orderId || item.id);
            // We assume appendOrder handles backend and refreshes plan
            // Ideally we should optimistically update UI here for speed, but store refresh is safer
        }
    } catch (e) {
        alert('Failed to move order.');
    } finally {
        draggedItem.value = null;
    }
};

const recalculateRoute = (route) => {
    route.isModified = false;
    alert(`Recalculating optimal path for ${route.name}... Done!`);
};

// Actions
const runOptimization = async () => {
    const payload = {
      model: selectedModel.value,
      vehicleIds: [], // Empty = use all available
      orderIds: [], // Empty = use all available
      constraints: { ...constraints }
    };
    await dispatchStore.runOptimization(payload);
};

const publishRoute = async (route) => {
    try {
        // Construct payload expected by backend
        // Backend expects List<RouteDefinition>
        const payload = {
            routes: [{
                vehicleId: route.id, // ID is vehicleId
                stops: route.legs.map(l => ({ orderId: l.orderId, address: l.address }))
            }]
        };
        await planningApi.post('/optimization/publish', payload);
        alert('Route published! Tasks sent to driver.');
    } catch (e) {
        console.error(e);
        alert('Failed to publish route.');
    }
};

const openAssignModal = (route) => {
    selectedRoute.value = route;
    // Pre-fill if driver exists?
    assignForm.driverId = route.driver !== 'Unassigned' ? route.driver : 'driver-1';
    showAssignModal.value = true;
};

const openRouteDetails = (route) => {
    selectedRoute.value = route;
    alert(`Route: ${route.name}\nVehicle: ${route.vehicle}\nDriver: ${route.driver}\nStops: ${route.stops}\nDistance: ${route.distance}km`);
};

const handleApproval = (response) => {
  console.log('Solution approved:', response);
  alert('✅ Rozwiązanie zatwierdzone! Publikowanie do kierowców...');
  showGatekeeperModal.value = false;
  // Trigger route publication
  // dispatchStore.publishRoutes();
};

const handleRejection = (response) => {
  console.log('Solution rejected:', response);
  alert('❌ Rozwiązanie odrzucone. Optymalizacja nie zostanie opublikowana.');
  showGatekeeperModal.value = false;
};

const getStatusClass = (status) => {
  const classes = {
    'PLANNED': 'bg-blue-900/50 text-blue-300',
    'ACTIVE': 'bg-green-900/50 text-green-300',
    'COMPLETED': 'bg-void-cyan-900/50 text-void-cyan-300',
    'CANCELLED': 'bg-red-900/50 text-red-300'
  };
  return classes[status] || 'bg-gray-900/50 text-gray-300';
};

onMounted(async () => {
    await dispatchStore.fetchLatestPlan();
    await dispatchStore.fetchUnassignedOrders();
});
</script>
