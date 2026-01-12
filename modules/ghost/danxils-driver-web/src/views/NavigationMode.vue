<template>
  <div class="h-screen flex flex-col bg-surface-950 relative overflow-hidden">
    <!-- Map Container -->
    <div id="nav-map" class="flex-1 bg-surface-900 relative z-0">
      <!-- Map Placeholder / Leaflet Container -->
      <div v-if="!mapInitialized" class="absolute inset-0 flex items-center justify-center text-surface-500">
        <div class="text-center">
          <div class="w-12 h-12 border-2 border-brand-500 border-t-transparent rounded-full animate-spin mx-auto mb-2"></div>
          <p>Initializing Navigation...</p>
        </div>
      </div>
    </div>

    <!-- Top Overlay: Next Stop Info -->
    <div class="absolute top-0 left-0 right-0 p-4 z-10 bg-gradient-to-b from-black/80 to-transparent pointer-events-none">
      <div class="glass-card p-4 pointer-events-auto flex justify-between items-center">
        <div>
          <p class="text-brand-400 text-sm font-bold uppercase tracking-wider">Next Stop</p>
          <h2 class="text-white text-xl font-bold truncate max-w-[250px]">{{ currentStop?.customer || 'Loading...' }}</h2>
          <p class="text-surface-300 text-sm truncate">{{ currentStop?.address || '...' }}</p>
        </div>
        <div class="text-right">
          <p class="text-white text-2xl font-mono font-bold">{{ distanceDisplay }}</p>
          <p class="text-surface-400 text-xs">ETA: {{ etaDisplay }}</p>
        </div>
      </div>
    </div>

    <!-- Bottom Control Panel -->
    <div class="bg-surface-900 border-t border-white/10 p-6 z-20 pb-8 rounded-t-3xl shadow-[0_-10px_40px_rgba(0,0,0,0.5)]">
      <div class="flex gap-4">
        <!-- Navigate Button (External) -->
        <button @click="openExternalMap" 
                class="flex-1 bg-surface-800 hover:bg-surface-700 text-white py-4 rounded-xl font-bold text-lg flex flex-col items-center gap-1 transition-all active:scale-95 border border-white/5">
          <span class="text-2xl">🗺️</span>
          <span>Google Maps</span>
        </button>

        <!-- Arrived Button (Primary Action) -->
        <button @click="handleArrival" 
                class="flex-[2] bg-gradient-to-r from-brand-600 to-brand-500 hover:from-brand-500 hover:to-brand-400 text-white py-4 rounded-xl font-bold text-xl shadow-lg shadow-brand-900/50 flex flex-col items-center justify-center gap-1 transition-all active:scale-95">
          <span class="text-2xl">📍</span>
          <span>I Have Arrived</span>
        </button>
      </div>

      <!-- Secondary Actions -->
      <div class="mt-6 flex justify-between items-center px-2">
        <button @click="skipStop" class="text-surface-400 hover:text-white text-sm font-medium transition-colors">
          Skip Stop
        </button>
        
        <!-- Breakdown Button -->
        <button @click="showBreakdownModal = true" class="text-red-400 hover:text-red-300 text-sm font-medium transition-colors flex items-center gap-1">
          <span class="text-lg">⚠️</span> Report Issue
        </button>

        <button @click="router.push('/tasks')" class="text-brand-400 hover:text-brand-300 text-sm font-medium transition-colors">
          View Full Route
        </button>
      </div>
    </div>

    <!-- Breakdown Modal -->
    <div v-if="showBreakdownModal" class="fixed inset-0 bg-black/95 backdrop-blur-sm z-50 flex flex-col animate-fade-in">
      <div class="flex justify-between items-center p-6 bg-gradient-to-b from-black/50 to-transparent">
        <h2 class="text-white font-bold text-lg text-red-500">Report Issue</h2>
        <button @click="showBreakdownModal = false" class="w-10 h-10 rounded-full bg-white/10 flex items-center justify-center text-white hover:bg-white/20">✕</button>
      </div>
      
      <div class="flex-1 p-6 flex flex-col gap-4">
        <p class="text-surface-300 text-sm mb-2">Select the type of issue to alert Control Tower immediately.</p>
        
        <button @click="reportIssue('BREAKDOWN')" class="bg-surface-800 hover:bg-red-900/30 border border-white/10 hover:border-red-500/50 p-6 rounded-xl flex items-center gap-4 transition-all group">
          <div class="w-12 h-12 rounded-full bg-red-500/20 flex items-center justify-center text-2xl group-hover:scale-110 transition-transform">🔧</div>
          <div class="text-left">
            <h3 class="text-white font-bold">Vehicle Breakdown</h3>
            <p class="text-surface-400 text-xs">Engine failure, flat tire, etc.</p>
          </div>
        </button>

        <button @click="reportIssue('ACCIDENT')" class="bg-surface-800 hover:bg-red-900/30 border border-white/10 hover:border-red-500/50 p-6 rounded-xl flex items-center gap-4 transition-all group">
          <div class="w-12 h-12 rounded-full bg-red-500/20 flex items-center justify-center text-2xl group-hover:scale-110 transition-transform">💥</div>
          <div class="text-left">
            <h3 class="text-white font-bold">Accident</h3>
            <p class="text-surface-400 text-xs">Collision or road incident</p>
          </div>
        </button>

        <button @click="reportIssue('TRAFFIC')" class="bg-surface-800 hover:bg-yellow-900/30 border border-white/10 hover:border-yellow-500/50 p-6 rounded-xl flex items-center gap-4 transition-all group">
          <div class="w-12 h-12 rounded-full bg-yellow-500/20 flex items-center justify-center text-2xl group-hover:scale-110 transition-transform">🚦</div>
          <div class="text-left">
            <h3 class="text-white font-bold">Heavy Traffic</h3>
            <p class="text-surface-400 text-xs">Significant delay expected</p>
          </div>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import RouteService from '../services/RouteService';
import LocationService from '../services/LocationService';

const router = useRouter();
const map = ref(null);
const mapInitialized = ref(false);
const currentStop = ref(null);
const routeLayer = ref(null);

// Mock Data for now if service fails
const mockStop = {
  customer: "TechCorp Industries",
  address: "123 Innovation Blvd, Tech City",
  lat: 52.2297,
  lng: 21.0122
};

const distanceDisplay = computed(() => {
  if (!LocationService.state.latitude || !currentStop.value) return '-- km';
  const dist = LocationService.calculateDistance(
    LocationService.state.latitude,
    LocationService.state.longitude,
    currentStop.value.lat,
    currentStop.value.lng
  );
  return dist < 1000 ? `${Math.round(dist)} m` : `${(dist / 1000).toFixed(1)} km`;
});

const etaDisplay = computed(() => {
  // Simple mock ETA based on 30km/h avg speed
  if (!LocationService.state.latitude || !currentStop.value) return '--:--';
  const dist = LocationService.calculateDistance(
    LocationService.state.latitude,
    LocationService.state.longitude,
    currentStop.value.lat,
    currentStop.value.lng
  );
  const minutes = Math.round((dist / 1000 / 30) * 60);
  const time = new Date(Date.now() + minutes * 60000);
  return time.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
});

onMounted(async () => {
  // Initialize Map
  initMap();
  
  // Load Route Data
  try {
    const route = await RouteService.getRoute();
    currentStop.value = route.find(s => s.status === 'PENDING') || mockStop;
    
    if (currentStop.value && map.value) {
      updateMapFocus();
    }
  } catch (e) {
    console.error("Failed to load route", e);
    currentStop.value = mockStop;
  }

  LocationService.startTracking();
});

onUnmounted(() => {
  LocationService.stopTracking();
  if (map.value) {
    map.value.remove();
  }
});

const initMap = () => {
  // Default to Warsaw center if no location
  const startLat = LocationService.state.latitude || 52.2297;
  const startLng = LocationService.state.longitude || 21.0122;

  map.value = L.map('nav-map', {
    zoomControl: false,
    attributionControl: false
  }).setView([startLat, startLng], 15);

  // Dark Mode Map Tiles (CartoDB Dark Matter)
  L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
    maxZoom: 20
  }).addTo(map.value);

  // User Location Marker
  const userIcon = L.divIcon({
    className: 'bg-brand-500 w-4 h-4 rounded-full border-2 border-white shadow-lg shadow-brand-500/50',
    iconSize: [16, 16]
  });
  
  L.marker([startLat, startLng], { icon: userIcon }).addTo(map.value);
  
  mapInitialized.value = true;
};

const updateMapFocus = () => {
  if (!map.value || !currentStop.value) return;
  
  // Add destination marker
  const destIcon = L.divIcon({
    className: 'text-2xl',
    html: '📍',
    iconSize: [24, 24],
    iconAnchor: [12, 24]
  });
  
  L.marker([currentStop.value.lat, currentStop.value.lng], { icon: destIcon }).addTo(map.value);
  
  // Fit bounds to show both user and dest
  const bounds = L.latLngBounds(
    [LocationService.state.latitude || 52.2297, LocationService.state.longitude || 21.0122],
    [currentStop.value.lat, currentStop.value.lng]
  );
  map.value.fitBounds(bounds, { padding: [50, 50] });
};

const openExternalMap = () => {
  if (currentStop.value) {
    window.open(`https://www.google.com/maps/dir/?api=1&destination=${currentStop.value.lat},${currentStop.value.lng}`, '_blank');
  }
};

const handleArrival = () => {
  // Logic to handle arrival (e.g., open scanner or confirmation)
  // For now, just go back to tasks or show a modal
  alert("You have arrived!");
  router.push('/tasks'); // Or open scanner directly
};

const showBreakdownModal = ref(false);

const reportIssue = async (type) => {
  if (confirm(`Are you sure you want to report: ${type}?`)) {
    try {
      // In real app: await AlertService.sendAlert({ type, lat: ..., lng: ... })
      console.log(`Reporting issue: ${type}`);
      alert("Control Tower has been notified. Stand by for instructions.");
      showBreakdownModal.value = false;
    } catch (e) {
      alert("Failed to send alert. Please call dispatch.");
    }
  }
};

const skipStop = () => {
  if (confirm("Skip this stop?")) {
    // Logic to skip
  }
};
</script>

<style>
/* Custom Map Styles if needed */
.leaflet-container {
  background: #0f172a; /* Match surface-900 */
}
</style>
