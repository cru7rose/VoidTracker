<template>
  <div class="p-6">
    <div class="mb-6">
      <button @click="$router.back()" class="text-gray-600 hover:text-gray-900 flex items-center gap-2">
        ← Back to Orders
      </button>
    </div>

    <div v-if="loading" class="text-center py-12">Loading...</div>
    <div v-else-if="!order" class="text-center py-12 text-gray-500">Order not found.</div>
    <div v-else class="space-y-6">
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex justify-between items-start mb-6">
          <div>
            <h1 class="text-2xl font-bold text-gray-900">Order Details</h1>
            <p class="text-gray-500 mt-1">ID: {{ order.id }}</p>
          </div>
          <span :class="getStatusClass(order.status)" class="px-3 py-1 text-sm font-semibold rounded-full">
            {{ order.status }}
          </span>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <h3 class="text-sm font-medium text-gray-500 mb-2">Pickup Information</h3>
            <div class="bg-gray-50 rounded-lg p-4">
              <p class="font-medium text-gray-900">{{ order.pickupAddress }}</p>
              <p class="text-sm text-gray-500 mt-1">Time Window: {{ order.pickupTimeWindow }}</p>
            </div>
          </div>

          <div>
            <h3 class="text-sm font-medium text-gray-500 mb-2">Delivery Information</h3>
            <div class="bg-gray-50 rounded-lg p-4">
              <p class="font-medium text-gray-900">{{ order.deliveryAddress }}</p>
              <p class="text-sm text-gray-500 mt-1">Time Window: {{ order.deliveryTimeWindow }}</p>
            </div>
          </div>
        </div>

        <div class="mt-6 grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <p class="text-sm text-gray-500">Weight</p>
            <p class="text-lg font-semibold text-gray-900">{{ order.weight }} kg</p>
          </div>
          <div>
            <p class="text-sm text-gray-500">Volume</p>
            <p class="text-lg font-semibold text-gray-900">{{ order.volume }} m³</p>
          </div>
          <div>
            <p class="text-sm text-gray-500">Created</p>
            <p class="text-lg font-semibold text-gray-900">{{ formatDate(order.createdAt) }}</p>
          </div>
        </div>
      </div>

      <div v-if="order.epod" class="bg-white rounded-lg shadow p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Proof of Delivery</h2>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <p class="text-sm text-gray-500 mb-2">Signature</p>
            <div class="border border-gray-200 rounded-lg p-4 bg-gray-50">
              <img v-if="order.epod.signatureUrl" :src="order.epod.signatureUrl" alt="Signature" class="max-w-full" />
              <p v-else class="text-gray-400">No signature available</p>
            </div>
          </div>
          <div>
            <p class="text-sm text-gray-500 mb-2">Photos</p>
            <div class="grid grid-cols-2 gap-2">
              <img v-for="(photo, index) in order.epod.photos" :key="index" :src="photo" alt="Delivery photo" class="w-full h-32 object-cover rounded-lg" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { format } from 'date-fns';

const route = useRoute();
const loading = ref(true);
const order = ref(null);

onMounted(async () => {
  // Mock data
  order.value = {
    id: route.params.id,
    pickupAddress: 'Warsaw, ul. Marszałkowska 1',
    deliveryAddress: 'Krakow, ul. Floriańska 5',
    pickupTimeWindow: '09:00 - 11:00',
    deliveryTimeWindow: '14:00 - 16:00',
    status: 'DELIVERED',
    weight: 25,
    volume: 0.5,
    createdAt: new Date(),
    epod: {
      signatureUrl: null,
      photos: []
    }
  };
  loading.value = false;
});

function formatDate(date) {
  return format(new Date(date), 'MMM dd, yyyy HH:mm');
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
