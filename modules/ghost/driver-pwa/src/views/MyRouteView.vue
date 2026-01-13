<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { fetchDriverRoute, type DriverRoute } from '../services/routeService'
import StopActionSheet from '../components/StopActionSheet.vue'

const authStore = useAuthStore()
const router = useRouter()

const route = ref<DriverRoute | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)
const selectedStop = ref<any | null>(null)
const showActionSheet = ref(false)

// Extract stops from routeData
const stops = computed(() => {
    if (!route.value?.routeData) return []
    
    const activities = route.value.routeData.activities || []
    const stops = route.value.routeData.stops || []
    
    // Prefer activities if available, otherwise use stops
    return (activities.length > 0 ? activities : stops).map((item: any, index: number) => ({
        id: item.id || item.stopId || `stop-${index}`,
        address: item.address || item.deliveryAddress?.address || item.pickupAddress?.address || 'Unknown address',
        type: item.type || (item.deliveryAddress ? 'DELIVERY' : 'PICKUP'),
        status: item.status || 'PENDING',
        lat: item.lat || item.deliveryAddress?.lat || item.pickupAddress?.lat,
        lon: item.lon || item.deliveryAddress?.lon || item.pickupAddress?.lon,
        orderId: item.orderId,
        plannedArrival: item.plannedArrival || item.plannedArrivalTime
    }))
})

onMounted(async () => {
    // Check if driver is authenticated
    if (!authStore.isAuthenticated || !authStore.driverId) {
        router.push('/login')
        return
    }

    try {
        loading.value = true
        error.value = null
        
        const driverRoute = await fetchDriverRoute(authStore.driverId!)
        if (!driverRoute) {
            error.value = 'No active route found. Please contact dispatcher.'
            return
        }
        
        route.value = driverRoute
    } catch (err: any) {
        console.error('Failed to load route:', err)
        error.value = err.message || 'Failed to load route. Please try again.'
    } finally {
        loading.value = false
    }
})

const openNavigation = (lat: number, lon: number, address: string) => {
    // Open in Google Maps or default navigation app
    const url = `https://www.google.com/maps/dir/?api=1&destination=${lat},${lon}`
    window.open(url, '_blank')
}

const formatAddress = (address: string) => {
    // Truncate long addresses
    return address.length > 50 ? address.substring(0, 50) + '...' : address
}

const openStopActions = (stop: any) => {
    selectedStop.value = stop
    showActionSheet.value = true
}

const handleStopCompleted = (stopId: string) => {
    // Update local state
    const stop = stops.value.find(s => s.id === stopId)
    if (stop) {
        stop.status = 'COMPLETED'
    }
    showActionSheet.value = false
    selectedStop.value = null
}

// Workflow config (TODO: fetch from backend)
const workflowConfig = ref({
    scan: {
        barcode: { enabled: true, allowManual: true },
        deliveryCode: { enabled: 'conditional' }
    },
    photo: {
        dmg: { enabled: true, minCount: 0 },
        pod: { enabled: true, minCount: 1 }
    },
    signature: { required: true, captureRecipientName: true },
    statuses: ['IN_TRANSIT', 'ARRIVED', 'LOADING', 'UNLOADING', 'POD', 'ISSUE', 'COMPLETED']
})
</script>

<template>
  <div class="min-h-screen bg-gray-900 text-white p-4">
    <div class="max-w-2xl mx-auto">
      <div class="flex justify-between items-center mb-6">
        <div>
          <h1 class="text-2xl font-bold">{{ route?.routeName || 'My Route' }}</h1>
          <p v-if="route" class="text-sm text-gray-400 mt-1">
            {{ stops.length }} stops • {{ route.totalDistanceKm?.toFixed(1) || 'N/A' }} km
          </p>
        </div>
        <button 
          @click="authStore.logout()" 
          class="text-sm text-gray-400 hover:text-white"
        >
          Logout
        </button>
      </div>
      
      <div v-if="loading" class="text-center py-12">
        <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-white mb-4"></div>
        <p class="text-gray-400">Loading route...</p>
      </div>
      
      <div v-else-if="error" class="bg-red-900/20 border border-red-700 rounded-lg p-4 text-center">
        <p class="text-red-400">{{ error }}</p>
        <button 
          @click="router.push('/login')" 
          class="mt-4 text-sm text-blue-400 hover:text-blue-300 underline"
        >
          Go to Login
        </button>
      </div>
      
      <div v-else-if="stops.length === 0" class="text-center py-12">
        <p class="text-gray-400">No stops in this route</p>
      </div>
      
      <div v-else class="space-y-3">
        <div 
          v-for="(stop, index) in stops" 
          :key="stop.id" 
          class="bg-gray-800 p-4 rounded-lg border border-gray-700"
        >
          <div class="flex justify-between items-start">
            <div class="flex-1">
              <div class="flex items-center gap-2 mb-2">
                <span class="text-xs text-gray-400 font-mono bg-gray-700 px-2 py-1 rounded">
                  STOP #{{ index + 1 }}
                </span>
                <span 
                  :class="stop.type === 'PICKUP' ? 'bg-green-900/30 text-green-400' : 'bg-blue-900/30 text-blue-400'" 
                  class="text-xs font-bold px-2 py-1 rounded"
                >
                  {{ stop.type }}
                </span>
                <span 
                  :class="{
                    'bg-yellow-900/30 text-yellow-400': stop.status === 'PENDING',
                    'bg-green-900/30 text-green-400': stop.status === 'COMPLETED',
                    'bg-blue-900/30 text-blue-400': stop.status === 'IN_PROGRESS'
                  }"
                  class="text-xs px-2 py-1 rounded"
                >
                  {{ stop.status }}
                </span>
              </div>
              <p class="font-medium text-lg mb-1">{{ formatAddress(stop.address) }}</p>
              <p v-if="stop.plannedArrival" class="text-xs text-gray-400">
                ETA: {{ new Date(stop.plannedArrival).toLocaleTimeString() }}
              </p>
            </div>
            
            <div class="ml-4 flex gap-2">
              <button 
                v-if="stop.lat && stop.lon"
                @click="openNavigation(stop.lat, stop.lon, stop.address)"
                class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded text-sm font-semibold transition-colors"
              >
                Navigate
              </button>
              <button
                @click="openStopActions(stop)"
                :class="[
                  'px-4 py-2 rounded text-sm font-semibold transition-colors',
                  stop.status === 'COMPLETED'
                    ? 'bg-green-600 hover:bg-green-700 text-white'
                    : 'bg-gray-700 hover:bg-gray-600 text-white'
                ]"
              >
                {{ stop.status === 'COMPLETED' ? 'Zakończone' : 'Akcje' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Stop Action Sheet -->
    <StopActionSheet
      v-if="showActionSheet && selectedStop"
      :stop="selectedStop"
      :stop-index="stops.findIndex(s => s.id === selectedStop.id)"
      :workflow-config="workflowConfig"
      @stop-completed="handleStopCompleted"
    />
  </div>
</template>
