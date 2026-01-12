<template>
  <div class="min-h-screen bg-surface-950 flex flex-col relative overflow-hidden">
    <!-- Background Elements -->
    <div class="absolute top-0 right-0 w-64 h-64 bg-brand-500/10 rounded-full blur-3xl -translate-y-1/2 translate-x-1/2 pointer-events-none"></div>

    <!-- Header -->
    <header class="glass sticky top-0 z-20 px-6 py-4 flex justify-between items-center border-b border-white/5">
      <div>
        <h1 class="text-xl font-bold text-white tracking-tight">{{ $t('my_route') }}</h1>
        <p class="text-xs text-brand-400 font-medium">{{ $t('active') }} • {{ remainingStops }} {{ $t('stops_remaining') }}</p>
      </div>
      <div class="flex items-center gap-3">
        <LanguageSwitcher />
        <button @click="logout" class="w-10 h-10 rounded-full bg-surface-800 flex items-center justify-center text-surface-400 hover:text-white hover:bg-surface-700 transition-colors">
          <span class="text-lg">🚪</span>
        </button>
      </div>
    </header>

    <!-- Content -->
    <main class="flex-1 p-6 overflow-y-auto z-10 pb-24">
      <!-- Geofence Alert -->
      <div v-if="isNearStop" class="mb-6 bg-green-500/20 border border-green-500/50 rounded-xl p-4 flex items-center gap-4 animate-bounce-in">
        <div class="w-10 h-10 rounded-full bg-green-500 flex items-center justify-center text-white text-xl shadow-lg shadow-green-500/30">
          📍
        </div>
        <div>
          <h3 class="text-white font-bold">{{ $t('you_have_arrived') }}</h3>
          <p class="text-green-200 text-sm">{{ $t('within_300m', { customer: currentStop?.customer }) }}</p>
        </div>
      </div>

      <div v-if="loading" class="flex justify-center py-12">
        <div class="w-8 h-8 border-2 border-brand-500 border-t-transparent rounded-full animate-spin"></div>
      </div>
      
      <div v-else class="space-y-8">
        <!-- Quick Actions Grid -->
        <div class="grid grid-cols-2 gap-4">
          <!-- Start Route Tile -->
          <div v-if="config.showStartRoute" 
               class="col-span-2 bg-gradient-to-br from-brand-600 to-brand-800 p-6 rounded-2xl shadow-lg shadow-brand-900/50 flex items-center justify-between active:scale-[0.98] transition-transform cursor-pointer group border border-white/10"
               @click="startRoute">
            <div>
              <h3 class="text-xl font-bold text-white mb-1">{{ $t('start_route') }}</h3>
              <p class="text-brand-100 text-sm">{{ $t('ready_to_begin') }}</p>
            </div>
            <div class="w-12 h-12 bg-white/10 rounded-xl flex items-center justify-center text-2xl group-hover:bg-white/20 transition-colors">
              🚀
            </div>
          </div>

          <!-- Scan Package Tile -->
          <div v-if="config.showScanner" 
               class="glass-card p-6 flex flex-col items-center justify-center gap-3 active:scale-[0.98] transition-all cursor-pointer hover:bg-surface-800/80 group aspect-square"
               @click="openScanner">
            <div class="w-14 h-14 bg-surface-800 rounded-full flex items-center justify-center text-2xl text-brand-400 group-hover:scale-110 transition-transform shadow-inner shadow-black/50">
              📷
            </div>
            <span class="font-medium text-surface-200">{{ $t('scan') }}</span>
          </div>

          <!-- Navigation Tile -->
          <div v-if="config.showNavigation" 
               class="glass-card p-6 flex flex-col items-center justify-center gap-3 active:scale-[0.98] transition-all cursor-pointer hover:bg-surface-800/80 group aspect-square"
               @click="openNavigation">
            <div class="w-14 h-14 bg-surface-800 rounded-full flex items-center justify-center text-2xl text-blue-400 group-hover:scale-110 transition-transform shadow-inner shadow-black/50">
              🗺️
            </div>
            <span class="font-medium text-surface-200">{{ $t('map') }}</span>
          </div>

          <!-- Cash Collection Tile -->
          <div v-if="config.enableCashCollection" 
               class="glass-card p-6 flex flex-col items-center justify-center gap-3 active:scale-[0.98] transition-all cursor-pointer hover:bg-surface-800/80 group aspect-square">
            <div class="w-14 h-14 bg-surface-800 rounded-full flex items-center justify-center text-2xl text-green-400 group-hover:scale-110 transition-transform shadow-inner shadow-black/50">
              💰
            </div>
            <span class="font-medium text-surface-200">{{ $t('cash') }}</span>
          </div>

          <!-- AR Loading Mode Tile -->
          <div class="glass-card p-6 flex flex-col items-center justify-center gap-3 active:scale-[0.98] transition-all cursor-pointer hover:bg-surface-800/80 group aspect-square"
               @click="openARLoading">
            <div class="w-14 h-14 bg-surface-800 rounded-full flex items-center justify-center text-2xl text-purple-400 group-hover:scale-110 transition-transform shadow-inner shadow-black/50">
              📦
            </div>
            <span class="font-medium text-surface-200">{{ $t('load') }}</span>
          </div>

          <!-- Optimize Route Tile -->
          <div class="glass-card p-6 flex flex-col items-center justify-center gap-3 active:scale-[0.98] transition-all cursor-pointer hover:bg-surface-800/80 group aspect-square"
               @click="optimizeRoute">
            <div class="w-14 h-14 bg-surface-800 rounded-full flex items-center justify-center text-2xl text-yellow-400 group-hover:scale-110 transition-transform shadow-inner shadow-black/50">
              ⚡
            </div>
            <span class="font-medium text-surface-200" v-if="!isOptimizing">{{ $t('optimize') }}</span>
            <span class="font-medium text-surface-200 text-xs" v-else>{{ $t('optimizing') }}...</span>
          </div>
        </div>

        <!-- Route Timeline -->
        <div class="pt-4">
          <h2 class="text-white font-bold mb-6 flex items-center gap-2">
            <span class="w-1 h-6 bg-brand-500 rounded-full"></span>
            {{ $t('todays_stops') }}
          </h2>
          
          <div class="relative pl-4 space-y-8">
            <!-- Vertical Line -->
            <div class="absolute left-[27px] top-2 bottom-2 w-0.5 bg-surface-800"></div>

            <div v-for="(stop, index) in route" :key="stop.id" class="relative pl-8 group">
              <!-- Dot -->
              <div class="absolute left-[19px] top-1 w-4 h-4 rounded-full border-2 transition-colors z-10"
                   :class="getStopStatusClass(stop.status)"></div>
              
              <!-- Card -->
              <div class="glass-card p-4 hover:bg-surface-800/80 transition-colors cursor-pointer border-l-4"
                   :class="stop.status === 'COMPLETED' ? 'border-l-brand-500 opacity-60' : 'border-l-transparent'"
                   @click="openDetails(stop)">
                <div class="flex justify-between items-start mb-1">
                  <span class="text-xs font-mono text-surface-400 bg-surface-900 px-2 py-0.5 rounded">{{ stop.time }}</span>
                  <span class="text-xs font-bold px-2 py-0.5 rounded" 
                        :class="stop.type === 'PICKUP' ? 'bg-orange-500/20 text-orange-400' : 'bg-blue-500/20 text-blue-400'">
                    {{ stop.type }}
                  </span>
                </div>
                <h3 class="text-white font-medium text-lg leading-tight mb-1">{{ stop.customer }}</h3>
                <p class="text-surface-400 text-sm flex items-center gap-1">
                  <span>📍</span> {{ stop.address }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Scanner Modal -->
      <div v-if="showScanner" class="fixed inset-0 bg-black/95 backdrop-blur-sm z-50 flex flex-col animate-fade-in">
        <div class="flex justify-between items-center p-6 bg-gradient-to-b from-black/50 to-transparent">
          <h2 class="text-white font-medium">{{ $t('scan_package') }}</h2>
          <button @click="showScanner = false" class="w-10 h-10 rounded-full bg-white/10 flex items-center justify-center text-white hover:bg-white/20">✕</button>
        </div>
        <div class="flex-1 flex items-center justify-center p-4">
          <UniversalScanner 
            mode="DRIVER"             :target-location="currentStop ? { lat: currentStop.lat, lon: currentStop.lng } : null"
             :order-id="currentStop?.id"
             @scan="onScan"
             @anomaly-confirmed="(payload) => console.log('Anomaly override:', payload)"
            />
        </div>
        <div class="p-8 text-center text-surface-400 text-sm">
          {{ $t('align_barcode') }}
        </div>
      </div>

      <!-- Details Modal -->
      <div v-if="showDetails" class="fixed inset-0 bg-black/95 backdrop-blur-sm z-50 flex flex-col animate-fade-in">
        <div class="flex justify-between items-center p-6 bg-gradient-to-b from-black/50 to-transparent">
          <h2 class="text-white font-medium">{{ $t('stop_details') }}</h2>
          <button @click="showDetails = false" class="w-10 h-10 rounded-full bg-white/10 flex items-center justify-center text-white hover:bg-white/20">✕</button>
        </div>
        <div class="flex-1 overflow-y-auto p-6">
          <div class="bg-white rounded-xl p-6">
            <div class="flex justify-between items-center mb-4">
              <h3 class="text-lg font-bold text-gray-900">{{ selectedStop?.customer }}</h3>
              <VoiceRecorder @transcription="onTranscription" />
            </div>
            <DynamicForm 
              :schema="mockSchema" 
              v-model="formData" 
              @submit="handleFormSubmit" 
            />
          </div>
        </div>
      </div>

      <!-- AR Loading Assistant Modal -->
      <ARLoadingAssistant 
        v-if="showARLoading" 
        :manifest="route" 
        @close="showARLoading = false" 
      />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import DriverConfigService from '../services/DriverConfigService'
import RouteService from '../services/RouteService'
import UniversalScanner from '../components/UniversalScanner.vue'
import DynamicForm from '../components/DynamicForm.vue'
import VoiceRecorder from '../components/VoiceRecorder.vue'
import ARLoadingAssistant from '../components/ARLoadingAssistant.vue'
import authStore from '../stores/authStore'
import LocationService from '../services/LocationService'
import LanguageSwitcher from '../components/LanguageSwitcher.vue'

const router = useRouter()
const config = ref({})
const route = ref([])
const loading = ref(true)
const showScanner = ref(false)
const showDetails = ref(false)
const showARLoading = ref(false)
const selectedStop = ref(null)
const formData = ref({})

// Mock Schema - In real app, this would come from AssetDefinitionEntity
const mockSchema = [
  { field_key: "weight_kg", type: "number", label: "Weight (KG)", required: true },
  { field_key: "is_damaged", type: "boolean", label: "Package Damaged?", required: false },
  { field_key: "notes", type: "text", label: "Driver Notes", required: false },
  { field_key: "delivery_type", type: "select", label: "Delivery Type", options: ["Standard", "Express", "Fragile"], required: true }
]

const remainingStops = computed(() => {
  return route.value.filter(s => s.status === 'PENDING').length
})

// Geofence Logic
const currentStop = computed(() => {
  return route.value.find(s => s.status === 'PENDING')
})

const distanceToNextStop = computed(() => {
  if (!currentStop.value || !LocationService.state.latitude) return null
  
  return LocationService.calculateDistance(
    LocationService.state.latitude,
    LocationService.state.longitude,
    currentStop.value.lat,
    currentStop.value.lng
  )
})

const isNearStop = computed(() => {
  return distanceToNextStop.value !== null && distanceToNextStop.value < 300 // 300 meters
})

onMounted(async () => {
  try {
    const [configData, routeData] = await Promise.all([
      DriverConfigService.getConfig(),
      RouteService.getRoute()
    ])
    config.value = configData
    route.value = routeData
    
    // Start Location Tracking
    LocationService.startTracking()
  } finally {
    loading.value = false
  }
})

onUnmounted(() => {
  LocationService.stopTracking()
})

const getStopStatusClass = (status) => {
  switch (status) {
    case 'COMPLETED': return 'bg-brand-500 border-brand-500'
    case 'PENDING': return 'bg-surface-950 border-brand-400 animate-pulse'
    default: return 'bg-surface-800 border-surface-600'
  }
}

const logout = () => {
  authStore.logout()
  router.push('/login')
}

const startRoute = () => {
  alert('Starting route...')
}

const openScanner = () => {
  showScanner.value = true
}

const openARLoading = () => {
  showARLoading.value = true
}

const onScan = async (event) => {
  // event structure: { barcode, gps, anomaly, distance }
  showScanner.value = false
  console.log("Scan Captured:", event)
  
  try {
    await RouteService.sendScanEvent(event);
    
    // Visual feedback for the user
    // In a real app, we might check if this completes a stop/task
    const statusMsg = event.anomaly ? "⚠️ ANOMALY REPORTED" : "✅ SCANNED & VERIFIED";
    alert(`Delivered: ${event.barcode}\n${statusMsg}\nDistance: ${event.distance}m`);
    
    // Refresh route to update status (optional)
    // await fetchRoute();
  } catch (err) {
    console.error("Scan submission failed:", err);
    alert("Failed to submit scan: " + err.message);
  }
}

const openNavigation = () => {
  // Find next pending stop
  const nextStop = route.value.find(s => s.status === 'PENDING')
  if (nextStop) {
    router.push('/navigation')
  } else {
    alert('No pending stops!')
  }
}

const openDetails = (stop) => {
  selectedStop.value = stop
  formData.value = {} // Reset form data or load existing if available
  showDetails.value = true
}

const handleFormSubmit = (data) => {
  console.log('Form Submitted:', data)
  showDetails.value = false
  alert('Details saved successfully!')
}

const onTranscription = (text) => {
  // Auto-fill the notes field
  if (!formData.value.notes) {
    formData.value.notes = '';
  }
  formData.value.notes += (formData.value.notes ? ' ' : '') + text;
}
const isOptimizing = ref(false)

const optimizeRoute = async () => {
  if (isOptimizing.value) return
  
  const pendingStops = route.value.filter(s => s.status === 'PENDING')
  if (pendingStops.length < 2) {
    alert('Not enough stops to optimize!')
    return
  }

  isOptimizing.value = true
  try {
    // Get current location
    const { latitude, longitude } = LocationService.state
    if (!latitude || !longitude) {
      alert('Waiting for GPS location...')
      return
    }

    const response = await fetch('/api/planning/optimization/resequence', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authStore.token}`
      },
      body: JSON.stringify({
        currentLat: latitude,
        currentLng: longitude,
        stops: pendingStops
      })
    })

    if (response.ok) {
      const optimizedStops = await response.json()
      
      // Merge optimized stops back into main route, preserving completed ones
      const completedStops = route.value.filter(s => s.status !== 'PENDING')
      route.value = [...completedStops, ...optimizedStops]
      
      alert('Route optimized successfully!')
    } else {
      throw new Error('Optimization failed')
    }
  } catch (e) {
    console.error('Optimization error:', e)
    alert('Failed to optimize route. Please try again.')
  } finally {
    isOptimizing.value = false
  }
}
</script>
