<template>
  <div class="min-h-screen bg-white dark:bg-spotify-black text-gray-900 dark:text-white p-8 transition-colors">
    <!-- Header -->
    <div class="mb-8">
      <h1 class="text-4xl font-bold mb-2">
        Operational Dashboard
      </h1>
      <p class="text-gray-500 dark:text-spotify-gray-400 text-sm">Real-time System Status</p>
    </div>

    <!-- Stats Grid with Suspense -->
    <Suspense>
      <template #default>
        <StatsGrid :stats="stats" />
      </template>
      <template #fallback>
        <StatsGridSkeleton />
      </template>
    </Suspense>

    <!-- Critical Alerts -->
    <TransitionGroup name="alert-list" tag="div">
      <div 
        v-if="alerts.length > 0" 
        key="alerts-container"
        class="mb-8 spotify-card rounded-xl overflow-hidden border border-red-500/30"
      >
        <div class="p-6 bg-red-500/10 border-b border-red-500/30">
          <h2 class="text-xl font-bold text-red-400 mb-2 flex items-center gap-3">
            <span class="text-2xl">⚠️</span>
            <span>Critical Alerts</span>
          </h2>
        </div>
        <div class="p-4 space-y-3">
          <div 
            v-for="(alert, index) in alerts" 
            :key="alert.id || index"
            class="p-4 bg-spotify-darker border border-red-500/20 rounded-lg hover:border-red-500/40 transition-colors cursor-pointer"
            @click="handleAlertClick(alert)"
          >
            <div class="flex justify-between items-start">
              <div class="flex-1">
                <span class="font-bold text-red-400 text-sm uppercase">{{ alert.type }}</span>
                <span class="text-spotify-gray-400 mx-2">-</span>
                <span class="text-white">{{ alert.description || 'Driver reported an issue' }}</span>
              </div>
              <button class="text-sm text-spotify-green-400 hover:text-spotify-green-500 transition-colors">
                View →
              </button>
            </div>
          </div>
        </div>
      </div>
    </TransitionGroup>

    <!-- Content Grid -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Recent Orders -->
      <Suspense>
        <template #default>
          <RecentOrdersCard :orders="recentOrders" @order-click="viewOrder" />
        </template>
        <template #fallback>
          <CardSkeleton />
        </template>
      </Suspense>

      <!-- Active Manifests -->
      <Suspense>
        <template #default>
          <ActiveManifestsCard :manifests="activeManifests" @manifest-click="viewManifest" />
        </template>
        <template #fallback>
          <CardSkeleton />
        </template>
      </Suspense>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Suspense, TransitionGroup } from 'vue';
import StatsGrid from '@/components/dashboard/StatsGrid.vue';
import StatsGridSkeleton from '@/components/dashboard/StatsGridSkeleton.vue';
import RecentOrdersCard from '@/components/dashboard/RecentOrdersCard.vue';
import ActiveManifestsCard from '@/components/dashboard/ActiveManifestsCard.vue';
import CardSkeleton from '@/components/dashboard/CardSkeleton.vue';

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
  await loadDashboardData();
});

async function loadDashboardData() {
  await new Promise(resolve => setTimeout(resolve, 500));
  
  stats.value = {
    pendingOrders: 8,
    inTransit: 12,
    deliveredToday: 24,
    activeDrivers: 5
  };
  
  try {
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
}

function viewOrder(id) {
  router.push(`/internal/orders/${id}`);
}

function viewManifest(id) {
  router.push(`/internal/manifests`);
}

function handleAlertClick(alert) {
  console.log('Alert clicked:', alert);
}
</script>

<style scoped>
.alert-list-enter-active,
.alert-list-leave-active {
  transition: all 0.3s ease;
}

.alert-list-enter-from,
.alert-list-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.alert-list-move {
  transition: transform 0.3s ease;
}
</style>
