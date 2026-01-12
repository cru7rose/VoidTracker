<template>
  <div class="p-6 h-full flex flex-col bg-gray-50 overflow-hidden">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-2xl font-bold text-gray-800">Plan Assignments</h2>
      <button @click="fetchPlans" class="px-3 py-1 bg-white border rounded shadow hover:bg-gray-50 text-sm">🔄 Refresh</button>
    </div>

    <div class="bg-white rounded-lg shadow flex-1 overflow-hidden flex flex-col">
       <!-- Table Header -->
       <div class="grid grid-cols-7 gap-4 p-4 border-b bg-gray-50 text-sm font-semibold text-gray-600">
           <div class="col-span-2">Plan Name</div>
           <div>Created At</div>
           <div>Status</div>
           <div>Routes</div>
           <div>Stats</div>
           <div class="text-right">Action</div>
       </div>

       <!-- Table Body -->
       <div class="overflow-y-auto flex-1 p-2">
           <div v-if="loading" class="text-center py-8 text-gray-500">Loading plans...</div>
           <div v-else-if="plans.length === 0" class="text-center py-8 text-gray-400">No history found.</div>

           <div v-for="plan in plans" :key="plan.id" class="border-b">
               <!-- Main Row -->
               <div class="grid grid-cols-7 gap-4 p-4 hover:bg-blue-50 transition-colors items-center text-sm cursor-pointer" @click="togglePlanDetails(plan.id)">
                   <div class="col-span-2 font-medium text-gray-800">
                       {{ plan.name || `Optimization ${plan.id.substring(0, 8)}` }}
                       <div class="text-xs text-gray-400 font-mono">{{ plan.id.substring(0, 23) }}...</div>
                   </div>
                   <div class="text-gray-600">{{ formatDate(plan.createdAt) }}</div>
                   <div>
                       <span :class="getStatusClass(plan.status)" class="px-2 py-1 rounded-full text-xs font-bold">{{ plan.status }}</span>
                   </div>
                   <div class="text-gray-700">{{ plan.routes?.length || plan.totalRoutes || 0 }} Routes / {{ plan.totalStops || 0 }} Stops</div>
                   <div class="text-gray-500 text-xs">
                       <div>📏 {{ formatDistance(plan.totalDistanceMeters || plan.totalDistance) }} km</div>
                       <div>⏱️ {{ formatDuration(plan.totalDurationSeconds || plan.totalTimeSeconds) }} h</div>
                   </div>
                   <div class="text-right flex gap-2 justify-end">
                       <button @click.stop="togglePlanDetails(plan.id)" class="px-2 py-1 text-blue-600 hover:underline text-xs">
                           {{ expandedPlan === plan.id ? '▲' : '▼' }} Details
                       </button>
                       <button @click.stop="loadPlan(plan.id)" class="px-3 py-1.5 bg-blue-600 text-white rounded hover:bg-blue-700 shadow-sm font-medium">
                           View Plan
                       </button>
                   </div>
               </div>
               
               <!-- Expanded Details -->
               <div v-if="expandedPlan === plan.id" class="px-6 py-4 bg-gray-50 border-t">
                   <h4 class="font-semibold text-sm text-gray-700 mb-3">Routes:</h4>
                   <div v-if="plan.routes && plan.routes.length > 0" class="space-y-2">
                       <div v-for="(route, idx) in plan.routes" :key="route.vehicleId || idx" class="bg-white p-3 rounded border text-xs">
                           <div class="grid grid-cols-5 gap-3">
                               <div><span class="font-semibold text-gray-600">Vehicle:</span> <span class="text-gray-800">{{ route.vehicleId ? route.vehicleId.substring(0, 8) : `Auto ${idx + 1}` }}</span></div>
                               <div><span class="font-semibold text-gray-600">Driver:</span> <span class="text-gray-800">{{ route.driverId ? route.driverId.substring(0, 8) : 'Unassigned' }}</span></div>
                               <div><span class="font-semibold text-gray-600">Stops:</span> <span class="text-gray-800">{{ route.activities?.length || 0 }}</span></div>
                               <div><span class="font-semibold text-gray-600">Distance:</span> <span class="text-gray-800">{{ formatDistance(route.totalDistance) }} km</span></div>
                               <div><span class="font-semibold text-gray-600">Duration:</span> <span class="text-gray-800">{{ formatDuration(route.totalTimeSeconds) }} h</span></div>
                           </div>
                       </div>
                   </div>
                   <div v-else class="text-gray-400 text-sm">No route details</div>
               </div>
           </div>
       </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { planningApi } from '@/api/axios';

const router = useRouter();
const plans = ref([]);
const loading = ref(false);
const expandedPlan = ref(null);

const fetchPlans = async () => {
    loading.value = true;
    try {
        const res = await planningApi.get('/solutions');
        // Handle Spring Page response
        if (res.data && res.data.content) {
            plans.value = res.data.content;
        } else if (Array.isArray(res.data)) {
            plans.value = res.data;
        }
    } catch (e) {
        console.error("Failed to fetch plans", e);
    } finally {
        loading.value = false;
    }
};

const loadPlan = (id) => {
    // Navigate to Dispatch Workspace with planId query param
    router.push({ path: '/internal/dispatch', query: { planId: id } });
};

const togglePlanDetails = (id) => {
    expandedPlan.value = expandedPlan.value === id ? null : id;
};

const formatDate = (dateStr) => {
    if (!dateStr) return '—';
    const date = new Date(dateStr);
    if (isNaN(date.getTime())) return 'Invalid Date';
    return date.toLocaleString('en-US', { month: '2-digit', day: '2-digit', year: '2-digit', hour: '2-digit', minute: '2-digit' });
};

const formatDistance = (meters) => {
    if (!meters) return '0.0';
    return (meters / 1000).toFixed(1);
};

const formatDuration = (seconds) => {
    if (!seconds) return '0.0';
    return (seconds / 3600).toFixed(1);
};

const getStatusClass = (status) => {
    switch (status) {
        case 'PUBLISHED': return 'bg-green-100 text-green-800';
        case 'DRAFT': return 'bg-yellow-100 text-yellow-800';
        default: return 'bg-gray-100 text-gray-800';
    }
};

onMounted(fetchPlans);
</script>
