<template>
  <div class="h-screen flex flex-col bg-slate-900">
    <!-- Header/Control Bar -->
    <div class="h-16 bg-slate-800 border-b border-slate-700 flex items-center justify-between px-6 z-20">
      <h1 class="text-xl font-bold text-white flex items-center">
        <span class="text-brand-400 mr-2">◉</span> LENSES: Control Tower
      </h1>
      
      <div class="flex space-x-4">
        <button 
          @click="switchLens('profitability')"
          :class="['px-4 py-2 rounded-lg text-sm font-bold transition-all', activeLens === 'profitability' ? 'bg-green-500/20 text-green-400 border border-green-500/50' : 'text-slate-400 hover:text-white']"
        >
          $$ Profitability
        </button>
        <button 
          @click="switchLens('risk')"
          :class="['px-4 py-2 rounded-lg text-sm font-bold transition-all', activeLens === 'risk' ? 'bg-red-500/20 text-red-400 border border-red-500/50' : 'text-slate-400 hover:text-white']"
        >
          ⚠ Risk Radar
        </button>
      </div>
    </div>

    <!-- Map Container -->
    <div class="flex-1 relative z-10">
      <div id="lenses-map" class="w-full h-full bg-slate-900"></div>

      <!-- Legend Overlay -->
      <div class="absolute bottom-8 right-8 bg-slate-900/90 border border-slate-700 p-4 rounded-xl shadow-2xl backdrop-blur-sm z-[1000]">
        <h3 class="text-xs font-bold text-slate-400 uppercase mb-2">Heatmap Intensity</h3>
        <div class="flex items-center space-x-2">
          <span class="text-xs text-slate-500">Low</span>
          <div class="w-32 h-2 bg-gradient-to-r from-blue-500 via-green-500 to-red-500 rounded-full"></div>
          <span class="text-xs text-slate-500">High</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, watch, computed, ref } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import 'leaflet.heat'
import { useLensStore } from '@/stores/lensStore'
import { useGraphStore } from '@/stores/graphStore'
import { useToast } from '@/composables/useToast'

const map = ref(null)
const heatLayer = ref(null)
const driverMarkers = ref([])
const pollingInterval = ref(null)

const lensStore = useLensStore()
const graphStore = useGraphStore()
const { error: showError } = useToast()

// Computeds for template
const activeLens = computed(() => lensStore.activeLens)

onMounted(async () => {
  initMap()
  await lensStore.loadHeatmap()
  
  // Start driver polling if Risk Lens is active
  if (activeLens.value === 'risk') {
    startDriverPolling()
  }
})

onUnmounted(() => {
  stopDriverPolling()
})

// Watch for lens switch to start/stop driver polling
watch(activeLens, (newLens) => {
  if (newLens === 'risk') {
    startDriverPolling()
  } else {
    stopDriverPolling()
    clearDriverMarkers()
  }
})

// Watch for data changes to redraw heatmap
watch(() => lensStore.points, (newPoints) => {
  drawHeatmap(newPoints)
}, { deep: true })

// Watch for driver data to update markers
watch(() => graphStore.locations, (newLocations) => {
  if (activeLens.value === 'risk') {
    updateDriverMarkers(newLocations)
  }
}, { deep: true })

function initMap() {
  map.value = L.map('lenses-map').setView([52.2297, 21.0122], 7)
  
  // Dark Mode Tiles
  L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>',
    subdomains: 'abcd',
    maxZoom: 19
  }).addTo(map.value)
}

function drawHeatmap(pointsData) {
  if (!map.value) return;

  if (heatLayer.value) {
      map.value.removeLayer(heatLayer.value)
  }

  // Transform DTO to [lat, lon, intensity]
  const points = pointsData.map(p => [p.lat, p.lon, p.intensity])

  if (points.length > 0) {
      heatLayer.value = L.heatLayer(points, {
          radius: 25,
          blur: 15,
          maxZoom: 10,
          // Dynamic gradient based on lens type
          gradient: activeLens.value === 'risk' 
            ? {0.2: 'yellow', 0.65: 'orange', 1: 'red'} 
            : {0.4: 'blue', 0.65: 'lime', 1: 'red'}
      }).addTo(map.value)
  }
}

function startDriverPolling() {
  // Fetch all drivers first
  fetchDriversData()
  
  // Poll every 10 seconds
  pollingInterval.value = setInterval(() => {
    fetchDriversData()
  }, 10000)
}

function stopDriverPolling() {
  if (pollingInterval.value) {
    clearInterval(pollingInterval.value)
    pollingInterval.value = null
  }
}

async function fetchDriversData() {
  try {
    const drivers = await graphStore.fetchAllDrivers()
    
    // For demo: fetch impact for first driver
    // In production, you'd fetch all active drivers or selected driver
    if (drivers && drivers.length > 0) {
      await graphStore.fetchDriverWithImpact(drivers[0].id)
    }
  } catch (err) {
    console.error('Driver polling error:', err)
    // Don't show error toast on every poll failure, just log it
  }
}

function updateDriverMarkers(locations) {
  if (!map.value) return;
  
  // Clear existing driver markers
  clearDriverMarkers()
  
  // Add new markers for served locations
  locations.forEach(loc => {
    if (loc.lat && loc.lon) {
      const marker = L.marker([loc.lat, loc.lon], {
        icon: L.divIcon({
          className: 'driver-marker',
          html: '<div style="background: #10b981; width: 12px; height: 12px; border-radius: 50%; border: 2px solid white;"></div>',
          iconSize: [12, 12]
        })
      }).addTo(map.value)
      
      // Tooltip with driver info
      if (graphStore.currentDriver) {
        marker.bindTooltip(`
          <strong>${graphStore.currentDriver.name || 'Driver'}</strong><br/>
          Location: ${loc.name || 'Served Zone'}
        `, { permanent: false, direction: 'top' })
      }
      
      driverMarkers.value.push(marker)
    }
  })
}

function clearDriverMarkers() {
  driverMarkers.value.forEach(marker => {
    if (map.value) {
      map.value.removeLayer(marker)
    }
  })
  driverMarkers.value = []
}

function switchLens(lens) {
    lensStore.loadHeatmap(lens)
}
</script>

<style>
/* Fix Leaflet z-index issues if any */
.leaflet-pane { z-index: 10; }

/* Driver marker custom style */
.driver-marker {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>
