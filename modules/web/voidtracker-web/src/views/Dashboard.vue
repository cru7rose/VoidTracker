<script setup>
import { ref, onMounted } from 'vue';
import { Package, Truck, CheckCircle, AlertTriangle } from 'lucide-vue-next';
import MapComponent from '../components/MapComponent.vue';

const stats = ref([
  { name: 'Total Orders', stat: '71,897', icon: Package, change: '12%', changeType: 'increase' },
  { name: 'Active Deliveries', stat: '58', icon: Truck, change: '2.1%', changeType: 'increase' },
  { name: 'On-Time Rate', stat: '98.5%', icon: CheckCircle, change: '4.05%', changeType: 'decrease' }, // decrease means bad here? usually green/red logic needed
  { name: 'SLA Warnings', stat: '3', icon: AlertTriangle, change: '0%', changeType: 'neutral' },
]);

// In a real app, fetch stats from API
onMounted(async () => {
  // const response = await api.get('/analytics/dashboard');
  // stats.value = response.data;
});
</script>

<template>
  <div>
    <h1 class="text-2xl font-semibold text-gray-900">Dashboard</h1>
    
    <div class="mt-4 grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
      <div v-for="item in stats" :key="item.name" class="bg-white overflow-hidden shadow rounded-lg">
        <div class="p-5">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <component :is="item.icon" class="h-6 w-6 text-gray-400" aria-hidden="true" />
            </div>
            <div class="ml-5 w-0 flex-1">
              <dl>
                <dt class="text-sm font-medium text-gray-500 truncate">
                  {{ item.name }}
                </dt>
                <dd>
                  <div class="text-lg font-medium text-gray-900">
                    {{ item.stat }}
                  </div>
                </dd>
              </dl>
            </div>
          </div>
        </div>
        <div class="bg-gray-50 px-5 py-3">
          <div class="text-sm">
            <a href="#" class="font-medium text-indigo-600 hover:text-indigo-500">
              View all
            </a>
          </div>
        </div>
      </div>
    </div>

    <!-- Recent Activity / Map -->
    <div class="mt-8 bg-white shadow rounded-lg p-6">
      <h2 class="text-lg font-medium text-gray-900 mb-4">Live Operations Map</h2>
      <div class="bg-gray-100 h-96 rounded-lg overflow-hidden">
        <MapComponent :markers="[
          { lat: 52.2297, lng: 21.0122, popup: 'HQ' },
          { lat: 52.2350, lng: 21.0150, popup: 'Truck 1' },
          { lat: 52.2250, lng: 21.0200, popup: 'Truck 2' }
        ]" />
      </div>
    </div>
  </div>
</template>
