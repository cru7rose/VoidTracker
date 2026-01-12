<template>
  <div class="min-h-screen bg-gray-100 pb-20">
    <!-- Header -->
    <header class="bg-white shadow-sm sticky top-0 z-10">
      <div class="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
        <h1 class="text-lg font-bold text-gray-900">My Route</h1>
        <button @click="logout" class="text-sm text-red-600 font-medium">Logout</button>
      </div>
    </header>

    <!-- Route List -->
    <main class="max-w-7xl mx-auto px-4 py-4 space-y-4">
      <div v-if="loading" class="text-center py-8">
        <p class="text-gray-500">Loading route...</p>
      </div>

      <div v-else-if="error" class="text-center py-8 text-red-600">
        {{ error }}
      </div>

      <div v-else-if="orders.length === 0" class="text-center py-8 text-gray-500">
        No orders assigned for today.
      </div>

      <div v-else v-for="(order, index) in orders" :key="order.id" 
        class="bg-white rounded-lg shadow overflow-hidden relative">
        
        <!-- Stop Number Badge -->
        <div class="absolute top-0 left-0 bg-blue-600 text-white text-xs font-bold px-2 py-1 rounded-br-lg">
          #{{ index + 1 }}
        </div>

        <div class="p-4 pt-6">
          <div class="flex justify-between items-start mb-2">
            <div>
              <h3 class="text-lg font-bold text-gray-900">{{ order.customer?.name || 'Unknown Customer' }}</h3>
              <p class="text-sm text-gray-500">{{ order.type === 'DELIVERY' ? 'Delivery' : 'Pickup' }}</p>
            </div>
            <span :class="{
              'bg-green-100 text-green-800': order.status === 'COMPLETED',
              'bg-yellow-100 text-yellow-800': order.status === 'PENDING',
              'bg-blue-100 text-blue-800': order.status === 'IN_TRANSIT'
            }" class="px-2 py-1 text-xs font-semibold rounded-full">
              {{ order.status }}
            </span>
          </div>

          <div class="space-y-2 text-sm text-gray-600">
            <div class="flex items-start">
              <svg class="h-5 w-5 text-gray-400 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              <span>{{ formatAddress(order.deliveryAddress) }}</span>
            </div>
            <div class="flex items-center">
              <svg class="h-5 w-5 text-gray-400 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
              </svg>
              <span>{{ order.assets?.length || 0 }} Packages</span>
            </div>
          </div>

          <div class="mt-4 flex space-x-3">
            <button class="flex-1 bg-blue-600 text-white py-2 rounded-md font-medium hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
              Navigate
            </button>
            <button class="flex-1 bg-gray-100 text-gray-700 py-2 rounded-md font-medium hover:bg-gray-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500">
              Details
            </button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import authStore from '../stores/authStore';

const router = useRouter();
const orders = ref([]);
const loading = ref(true);
const error = ref(null);

onMounted(async () => {
  if (!authStore.state.token) {
    router.push('/login');
    return;
  }

  try {
    // Fetch assigned orders
    // Note: Assuming /api/orders returns all orders for now, filtering by driver should be done in backend
    // or we use a specific endpoint like /api/orders/assigned
    const response = await authStore.api.get('/api/orders');
    // Backend returns Page<OrderResponseDto>, so we access .content
    orders.value = response.data.content || [];
  } catch (err) {
    console.error('Failed to fetch orders:', err);
    error.value = 'Failed to load route.';
  } finally {
    loading.value = false;
  }
});

const logout = () => {
  authStore.logout();
  router.push('/login');
};

const formatAddress = (addr) => {
  if (!addr) return 'No address';
  return `${addr.street}, ${addr.city}`;
};
</script>
