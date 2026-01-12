<template>
  <div class="p-6">
    <h1 class="text-2xl font-bold text-gray-900 mb-6">Internal Dashboard</h1>
    
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
      <div class="bg-white rounded-lg shadow p-6">
        <h3 class="text-sm font-medium text-gray-500">Pending Orders</h3>
        <p class="text-3xl font-bold text-yellow-600 mt-2">{{ stats.pendingOrders }}</p>
      </div>
      <div class="bg-white rounded-lg shadow p-6">
        <h3 class="text-sm font-medium text-gray-500">In Transit</h3>
        <p class="text-3xl font-bold text-blue-600 mt-2">{{ stats.inTransit }}</p>
      </div>
      <div class="bg-white rounded-lg shadow p-6">
        <h3 class="text-sm font-medium text-gray-500">Delivered Today</h3>
        <p class="text-3xl font-bold text-green-600 mt-2">{{ stats.deliveredToday }}</p>
      </div>
      <div class="bg-white rounded-lg shadow p-6">
        <h3 class="text-sm font-medium text-gray-500">Active Drivers</h3>
        <p class="text-3xl font-bold text-indigo-600 mt-2">{{ stats.activeDrivers }}</p>
      </div>
    </div>

    <!-- Critical Alerts -->
    <div v-if="alerts.length > 0" class="mb-8 bg-red-50 border border-red-200 rounded-lg p-4">
      <h2 class="text-lg font-bold text-red-800 mb-2 flex items-center gap-2">
        <span>⚠️</span> Critical Alerts
      </h2>
      <div class="space-y-2">
        <div v-for="(alert, index) in alerts" :key="index" class="flex justify-between items-center bg-white p-3 rounded border border-red-100 shadow-sm">
          <div>
            <span class="font-bold text-red-600">{{ alert.type }}</span>
            <span class="text-gray-600 mx-2">-</span>
            <span class="text-gray-800">{{ alert.description || 'Driver reported an issue' }}</span>
          </div>
          <button class="text-sm text-blue-600 hover:underline">View Location</button>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="bg-white rounded-lg shadow p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Recent Orders</h2>
        <div class="space-y-3">
          <div v-for="order in recentOrders" :key="order.id" class="flex justify-between items-center p-3 bg-gray-50 rounded-lg hover:bg-gray-100 cursor-pointer" @click="viewOrder(order.id)">
            <div>
              <p class="font-medium text-gray-900">{{ order.id.substring(0, 8) }}</p>
              <p class="text-sm text-gray-500">{{ order.customerName }}</p>
            </div>
            <span :class="getStatusClass(order.status)" class="px-2 py-1 text-xs font-semibold rounded-full">
              {{ order.status }}
            </span>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-lg shadow p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Active Manifests</h2>
        <div class="space-y-3">
          <div v-for="manifest in activeManifests" :key="manifest.id" class="flex justify-between items-center p-3 bg-gray-50 rounded-lg hover:bg-gray-100 cursor-pointer" @click="viewManifest(manifest.id)">
            <div>
              <p class="font-medium text-gray-900">{{ manifest.driverName }}</p>
              <p class="text-sm text-gray-500">{{ manifest.stops }} stops</p>
            </div>
            <span class="text-sm text-gray-600">{{ manifest.progress }}%</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const stats = ref({
  pendingOrders: 0,
  inTransit: 0,
  deliveredToday: 0,
  activeDrivers: 0
});
const recentOrders = ref([]);
const activeManifests = ref([]);

const alerts = ref([]);

onMounted(async () => {
  // Mock data
  stats.value = {
    pendingOrders: 8,
    inTransit: 12,
    deliveredToday: 24,
    activeDrivers: 5
  };
  
  try {
    // In real app: const response = await axios.get('/api/tracking/alert');
    // alerts.value = response.data;
    // For demo, we can mock or try to fetch if backend is running
    // alerts.value = [{ type: 'BREAKDOWN', description: 'Vehicle WX 12345 engine failure' }];
    
    // Attempt fetch
    const response = await fetch('/api/tracking/alert', { 
        headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') } 
    });
    if (response.ok) {
        alerts.value = await response.json();
    }
  } catch (e) {
    console.warn("Failed to fetch alerts", e);
  }
  
  recentOrders.value = [
    { id: '550e8400-e29b-41d4-a716-446655440000', customerName: 'ABC Company', status: 'OUT_FOR_DELIVERY' },
    { id: '550e8400-e29b-41d4-a716-446655440001', customerName: 'XYZ Corp', status: 'PLANNED' },
    { id: '550e8400-e29b-41d4-a716-446655440002', customerName: 'Tech Solutions', status: 'INGESTED' }
  ];
  
  activeManifests.value = [
    { id: '1', driverName: 'Jan Kowalski', stops: 8, progress: 62 },
    { id: '2', driverName: 'Anna Nowak', stops: 12, progress: 45 },
    { id: '3', driverName: 'Piotr Wiśniewski', stops: 6, progress: 83 }
  ];
});

function viewOrder(id) {
  router.push(`/internal/orders/${id}`);
}

function viewManifest(id) {
  router.push(`/internal/manifests`);
}

function getStatusClass(status) {
  const classes = {
    'INGESTED': 'bg-gray-100 text-gray-800',
    'PLANNED': 'bg-blue-100 text-blue-800',
    'OUT_FOR_DELIVERY': 'bg-yellow-100 text-yellow-800',
    'DELIVERED': 'bg-green-100 text-green-800',
    'EXCEPTION': 'bg-red-100 text-red-800'
  };
  return classes[status] || 'bg-gray-100 text-gray-800';
}
</script>
