<template>
  <div class="h-screen flex flex-col relative bg-gray-100 overflow-hidden">
    <!-- Map Container -->
    <div id="tracking-map" class="absolute inset-0 z-0"></div>

    <!-- Loading State -->
    <div v-if="loading" class="absolute inset-0 z-50 flex items-center justify-center bg-white/80 backdrop-blur-sm">
      <div class="text-center">
        <div class="w-12 h-12 border-4 border-brand-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
        <p class="text-gray-600 font-medium">Locating your package...</p>
      </div>
    </div>

    <!-- Error State -->
    <div v-if="error" class="absolute inset-0 z-50 flex items-center justify-center bg-white">
      <div class="text-center max-w-md px-6">
        <div class="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4 text-red-500 text-2xl">
          ⚠️
        </div>
        <h2 class="text-xl font-bold text-gray-900 mb-2">Tracking Unavailable</h2>
        <p class="text-gray-500">{{ error }}</p>
      </div>
    </div>

    <!-- Tracking Card (Bottom Sheet) -->
    <div v-if="!loading && !error" class="absolute bottom-0 left-0 right-0 z-10 p-4 pointer-events-none">
      <div class="max-w-md mx-auto bg-white rounded-2xl shadow-2xl overflow-hidden pointer-events-auto transition-transform duration-300 ease-out transform translate-y-0">
        <!-- Status Header -->
        <div class="bg-brand-600 px-6 py-4 flex justify-between items-center">
          <div>
            <p class="text-brand-100 text-xs font-bold uppercase tracking-wider">Status</p>
            <h2 class="text-white font-bold text-lg">{{ order.status || 'In Transit' }}</h2>
          </div>
          <div class="text-right">
            <p class="text-brand-100 text-xs font-bold uppercase tracking-wider">ETA</p>
            <p class="text-white font-mono font-bold text-xl">{{ eta || '--:--' }}</p>
          </div>
        </div>

        <!-- Driver Info -->
        <div class="p-6">
          <div class="flex items-center gap-4 mb-6">
            <div class="w-12 h-12 bg-gray-200 rounded-full flex items-center justify-center text-2xl overflow-hidden">
              <img v-if="driver.avatar" :src="driver.avatar" class="w-full h-full object-cover" />
              <span v-else>👨‍✈️</span>
            </div>
            <div>
              <h3 class="font-bold text-gray-900">{{ driver.name || 'Your Driver' }}</h3>
              <p class="text-sm text-gray-500">{{ driver.vehicle || 'Delivery Vehicle' }}</p>
            </div>
            <div class="ml-auto">
              <a :href="'tel:' + driver.phone" class="w-10 h-10 bg-green-100 rounded-full flex items-center justify-center text-green-600 hover:bg-green-200 transition-colors">
                📞
              </a>
            </div>
          </div>

          <!-- Progress Bar -->
          <div class="relative h-2 bg-gray-100 rounded-full mb-2 overflow-hidden">
            <div class="absolute top-0 left-0 h-full bg-brand-500 transition-all duration-1000" :style="{ width: progress + '%' }"></div>
          </div>
          <p class="text-center text-xs text-gray-400 mb-6">{{ stopsRemaining }} stops away</p>

          <!-- Delivery Details -->
          <div class="border-t border-gray-100 pt-4">
            <div class="flex items-start gap-3">
              <div class="mt-1 text-gray-400">📍</div>
              <div>
                <p class="text-xs text-gray-400 uppercase font-bold">Delivering To</p>
                <p class="text-gray-900 font-medium">{{ order.address }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRoute } from 'vue-router';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import axios from 'axios';

const route = useRoute();
const loading = ref(true);
const error = ref(null);
const map = ref(null);
const driverMarker = ref(null);
const order = ref({});
const driver = ref({});
const eta = ref(null);
const progress = ref(0);
const stopsRemaining = ref(0);

// Mock Data for fallback
const mockOrder = {
  status: 'Arriving Soon',
  address: '123 Innovation Blvd, Tech City',
  lat: 52.2297,
  lng: 21.0122
};

const mockDriver = {
  name: 'Alex Driver',
  vehicle: 'Mercedes Sprinter • WX 12345',
  phone: '+48123456789',
  lat: 52.2350,
  lng: 21.0150
};

onMounted(async () => {
  const token = route.params.token;
  if (!token) {
    error.value = 'Invalid tracking link.';
    loading.value = false;
    return;
  }

  try {
    const response = await axios.get(`/api/public/tracking/${token}`);
    const data = response.data;
    
    // Map backend event to frontend model
    order.value = {
        status: 'In Transit', // Backend event doesn't have status yet, assume In Transit
        address: `Order #${data.orderId}`, // We don't have address in event, would need another call or enrichment
        lat: data.latitude,
        lng: data.longitude
    };
    
    driver.value = {
        name: data.driverId.startsWith('TERM:') ? 'Terminal Processing' : 'Driver ' + data.driverId,
        vehicle: data.driverId.startsWith('TERM:') ? data.driverId.replace('TERM:', '') : 'Vehicle',
        phone: '',
        lat: data.latitude,
        lng: data.longitude,
        isTerminal: data.driverId.startsWith('TERM:')
    };
    
    // Calculate ETA (Mock for now as backend doesn't provide it yet)
    eta.value = data.driverId.startsWith('TERM:') ? 'Processing' : 'Calculating...';
    
    initMap();
    startPolling(token);
  } catch (e) {
    console.error(e);
    error.value = 'Unable to load tracking info.';
  } finally {
    loading.value = false;
  }
});

const startPolling = (token) => {
  setInterval(async () => {
    try {
        const response = await axios.get(`/api/public/tracking/${token}`);
        const data = response.data;
        if (data && driverMarker.value) {
            driver.value.lat = data.latitude;
            driver.value.lng = data.longitude;
            updateDriverMarker();
        }
    } catch (e) {
        console.error("Polling failed", e);
    }
  }, 5000);
};
</script>
