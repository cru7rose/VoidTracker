<template>
  <div class="claim-container">
    <div class="card">
      <h2>Claiming Route...</h2>
      <div v-if="loading" class="loader"></div>
      <div v-if="error" class="error">{{ error }}</div>
      <p v-if="!error">Please wait while we set up your manifest.</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useOfflineStore } from '../stores/offlineStore';

const route = useRoute();
const router = useRouter();
const offlineStore = useOfflineStore();

const loading = ref(true);
const error = ref(null);

onMounted(async () => {
  const token = route.query.token;
  if (!token) {
    error.value = "Invalid Link: No token provided.";
    loading.value = false;
    return;
  }

  try {
    const response = await fetch(`/api/planning/routes/claim/${token}`);
    
    if (!response.ok) {
        throw new Error(`Failed to claim route: ${response.statusText}`);
    }

    const routeData = await response.json();
    console.log("Route claimed:", routeData);

    // Map Route Stops to 'Orders' for offline store
    // Ensure we keep sequence and location data
    const orders = routeData.stops.map((stop, index) => ({
        id: stop.orderId || stop.stopId, // Fallback to stopId if orderId is null
        sequence: index + 1,
        type: stop.type,
        address: stop.address || `Stop #${index + 1}`,
        customerName: stop.customerName || 'Unknown Customer',
        status: 'PENDING',
        location: {
            lat: stop.lat,
            lon: stop.lon
        },
        timeWindow: stop.timeWindow,
        packageCount: stop.packageCount || 1,
        stopId: stop.stopId
    }));

    // Save to Offline DB
    await offlineStore.saveOrders(orders);
    
    // Simulate Login (since magic link implies auth for this session)
    localStorage.setItem('driverToken', `magic-${token}`);
    localStorage.setItem('vehicleId', routeData.vehicleId);

    // Redirect to Route View
    router.push('/route');

  } catch (err) {
    console.error(err);
    error.value = err.message || "An unexpected error occurred.";
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.claim-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f3f4f6;
  padding: 20px;
}

.card {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  text-align: center;
  width: 100%;
  max-width: 400px;
}

.loader {
  border: 4px solid #f3f3f3;
  border-radius: 50%;
  border-top: 4px solid #3498db;
  width: 40px;
  height: 40px;
  margin: 20px auto;
  -webkit-animation: spin 2s linear infinite; /* Safari */
  animation: spin 2s linear infinite;
}

.error {
    color: #ef4444;
    font-weight: bold;
    margin-top: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
