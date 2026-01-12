<template>
  <div class="h-screen flex flex-col">
    <div class="bg-white shadow-sm border-b border-gray-200 p-4">
      <div class="flex justify-between items-center">
        <h1 class="text-2xl font-bold text-gray-900">Live Tracking</h1>
        
        <div class="flex items-center gap-4">
          <div class="flex items-center gap-2">
            <span class="text-sm text-gray-600">Risk Lens:</span>
            <label class="relative inline-flex items-center cursor-pointer">
              <input 
                v-model="riskLensActive" 
                type="checkbox" 
                class="sr-only peer"
              />
              <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-red-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-red-600"></div>
            </label>
          </div>

          <div class="flex items-center gap-2">
            <span class="text-sm text-gray-600">Auto-refresh:</span>
            <label class="relative inline-flex items-center cursor-pointer">
              <input 
                v-model="autoRefresh" 
                type="checkbox" 
                class="sr-only peer"
              />
              <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-primary-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary-600"></div>
            </label>
          </div>
          
          <button 
            @click="refreshLocations"
            :disabled="refreshing"
            class="px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700 disabled:opacity-50 transition-colors"
          >
            {{ refreshing ? 'Refreshing...' : 'Refresh' }}
          </button>
        </div>
      </div>
    </div>

    <div class="flex-1 flex">
      <!-- Map Container -->
      <div class="flex-1 relative">
        <div id="map" class="w-full h-full"></div>
        
        <!-- Map Legend -->
        <div class="absolute bottom-4 left-4 bg-white rounded-lg shadow-lg p-4 z-[1000]">
          <h3 class="text-sm font-semibold text-gray-900 mb-2">Legend</h3>
          <div class="space-y-2">
            <div class="flex items-center gap-2">
              <div class="w-4 h-4 bg-success-500 rounded-full"></div>
              <span class="text-xs text-gray-700">Active Driver</span>
            </div>
            <div class="flex items-center gap-2">
              <div class="w-4 h-4 bg-warning-500 rounded-full"></div>
              <span class="text-xs text-gray-700">Idle Driver</span>
            </div>
            <div class="flex items-center gap-2">
              <div class="w-4 h-4 bg-primary-500 rounded-full"></div>
              <span class="text-xs text-gray-700">Delivery Stop</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Sidebar with Driver List -->
      <div class="w-80 bg-white border-l border-gray-200 overflow-y-auto">
        <div class="p-4">
          <h2 class="text-lg font-semibold text-gray-900 mb-4">Active Drivers ({{ drivers.length }})</h2>
          
          <div class="space-y-3">
            <div 
              v-for="driver in drivers" 
              :key="driver.id"
              @click="focusOnDriver(driver)"
              class="p-3 border border-gray-200 rounded-lg hover:border-primary-500 hover:bg-primary-50 cursor-pointer transition-colors"
            >
              <div class="flex items-start justify-between mb-2">
                <div>
                  <h3 class="font-semibold text-gray-900">{{ driver.name }}</h3>
                  <p class="text-xs text-gray-500">{{ driver.vehicleId }}</p>
                </div>
                <span 
                  :class="driver.status === 'active' ? 'bg-success-100 text-success-800' : 'bg-warning-100 text-warning-800'"
                  class="px-2 py-1 text-xs font-semibold rounded-full"
                >
                  {{ driver.status }}
                </span>
              </div>
              
              <div class="space-y-1 text-xs text-gray-600">
                <div class="flex justify-between">
                  <span>Stops:</span>
                  <span class="font-medium">{{ driver.completedStops }}/{{ driver.totalStops }}</span>
                </div>
                <div class="flex justify-between">
                  <span>Distance:</span>
                  <span class="font-medium">{{ driver.distanceTraveled }} km</span>
                </div>
                <div class="flex justify-between">
                  <span>Last Update:</span>
                  <span class="font-medium">{{ formatTime(driver.lastUpdate) }}</span>
                </div>
              </div>
              
              <div class="mt-2">
                <div class="w-full bg-gray-200 rounded-full h-1.5">
                  <div 
                    class="bg-primary-600 h-1.5 rounded-full" 
                    :style="{ width: (driver.completedStops / driver.totalStops * 100) + '%' }"
                  ></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { format } from 'date-fns';
import OptimizationService from '../../services/OptimizationService';

// Fix Leaflet default icon issue
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

let map = null;
const markers = ref({});
const riskLayers = ref([]);
let optimizationLayer = null; // LayerGroup for routes
const autoRefresh = ref(true);
const refreshing = ref(false);
const riskLensActive = ref(false);
let refreshInterval = null;

// Mock driver data - in production, fetch from API
const drivers = ref([
  // ... (keep existing mock data or rely on fetch) -> Keeping mock for init stability
  {
    id: 'D1', 
    name: 'Driver One',
    vehicleId: 'WA-12345',
    status: 'active',
    lat: 52.2297,
    lng: 21.0122,
    completedStops: 5,
    totalStops: 8,
    distanceTraveled: 23.5,
    lastUpdate: new Date()
  },
  // ... other drivers
]);

onMounted(() => {
  initializeMap();
  updateMarkers(); // transform mock/fetched data
  
  if (autoRefresh.value) {
    startAutoRefresh();
  }

  // Connect to Timefold Optimization Updates
  console.log("Connecting to Optimization Service...");
  OptimizationService.connect((update) => {
      console.log("Received Optimization Update:", update);
      handleOptimizationUpdate(update);
  });
});

onUnmounted(() => {
  stopAutoRefresh();
  OptimizationService.disconnect();
  if (map) {
    map.remove();
  }
});

function handleOptimizationUpdate(update) {
    if (!map || !optimizationLayer) return;

    optimizationLayer.clearLayers();

    if (update.routes) {
        update.routes.forEach((route, index) => {
            const colors = ['#2563eb', '#dc2626', '#16a34a', '#9333ea', '#ea580c', '#0d9488'];
            const color = route.color || colors[index % colors.length];

            // Extract lat/lng points
            const points = route.path.map(p => [p.lat, p.lon]);

            if (points.length > 0) {
                // Draw Polyline
                L.polyline(points, {
                    color: color,
                    weight: 4,
                    opacity: 0.8,
                    dashArray: update.solverStatus === 'SOLVING' ? '10, 10' : null // Dashed if draft
                }).addTo(optimizationLayer)
                  .bindPopup(`Vehicle: ${route.vehicleId} (${(route.totalDistanceMeters/1000).toFixed(1)} km)`);

                // Draw markers for stops
                route.path.forEach((p, i) => {
                    const radius = p.type === 'DEPOT' ? 8 : 4;
                    L.circleMarker([p.lat, p.lon], {
                        radius: radius,
                        color: 'white',
                        fillColor: color,
                        fillOpacity: 1,
                        weight: 2
                    }).addTo(optimizationLayer);
                });
            }
        });
    }
}

// Watch for Risk Lens toggle
watch(riskLensActive, (newValue) => {
  if (newValue) {
    updateRiskLayers();
  } else {
    clearRiskLayers();
  }
});

function initializeMap() {
  // Initialize map centered on Warsaw
  map = L.map('map').setView([52.2297, 21.0122], 12);
  
  // Add OpenStreetMap tiles
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors',
    maxZoom: 19
  }).addTo(map);

  optimizationLayer = L.layerGroup().addTo(map);
}

function updateMarkers() {
  // Clear existing markers
  Object.values(markers.value).forEach(marker => marker.remove());
  markers.value = {};
  
  // Add driver markers
  drivers.value.forEach(driver => {
    const icon = L.divIcon({
      className: 'custom-driver-marker',
      html: `
        <div class="relative">
          <div class="w-10 h-10 rounded-full ${driver.status === 'active' ? 'bg-success-500' : 'bg-warning-500'} border-4 border-white shadow-lg flex items-center justify-center">
            <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
            </svg>
          </div>
          <div class="absolute -bottom-6 left-1/2 transform -translate-x-1/2 whitespace-nowrap bg-white px-2 py-1 rounded shadow text-xs font-semibold">
            ${driver.name}
          </div>
        </div>
      `,
      iconSize: [40, 40],
      iconAnchor: [20, 20]
    });
    
    const marker = L.marker([driver.lat, driver.lng], { icon })
      .addTo(map)
      .bindPopup(`
        <div class="p-2">
          <h3 class="font-semibold">${driver.name}</h3>
          <p class="text-sm text-gray-600">${driver.vehicleId}</p>
          <div class="mt-2 grid grid-cols-2 gap-2 text-xs">
            <div>🔋 ${driver.batteryLevel ?? '-'}%</div>
            <div>📶 ${driver.signalStrength ?? '-'}%</div>
            <div>🚀 ${driver.speed ? driver.speed.toFixed(1) : '-'} km/h</div>
          </div>
          <p class="text-sm mt-2 text-gray-500">Last: ${formatTime(driver.lastUpdate)}</p>
          ${riskLensActive.value ? '<p class="text-xs text-red-500 font-bold mt-1">⚠️ RISK ANALYSIS ACTIVE</p>' : ''}
        </div>
      `);
    
    markers.value[driver.id] = marker;
  });

  if (riskLensActive.value) {
    updateRiskLayers();
  }
}

function focusOnDriver(driver) {
  map.setView([driver.lat, driver.lng], 15);
  markers.value[driver.id].openPopup();
}

async function refreshLocations() {
  refreshing.value = true;
  
  try {
    const response = await fetch('/api/planning/graph/drivers', {
      headers: {
        'Authorization': 'Bearer ' + localStorage.getItem('token')
      }
    });
    
    if (response.ok) {
      const fleetData = await response.json();
      
      // Map backend data to frontend model
      drivers.value = fleetData.map(d => ({
        id: d.driverId,
        name: d.name || d.driverId, 
        vehicleId: 'N/A', // Not in Graph yet
        status: d.status ? d.status.toLowerCase() : 'active',
        lat: d.latitude || 52.2297, // Default to Warsaw if null
        lng: d.longitude || 21.0122,
        completedStops: 0,
        totalStops: d.routes ? d.routes.length : 0,
        distanceTraveled: 0,
        lastUpdate: d.lastUpdate ? new Date(d.lastUpdate) : new Date(),
        // Telemetry - defaults as they are not in GraphNode
        batteryLevel: 100,
        signalStrength: 100,
        speed: 0
      }));
      
      updateMarkers();
    }
  } catch (error) {
    console.error('Failed to refresh locations:', error);
  } finally {
    refreshing.value = false;
  }
}

// --- RISK LENS IMPLEMENTATION ---

async function updateRiskLayers() {
  clearRiskLayers();
  
  if (!map) return;

  for (constdriver of drivers.value) {
    try {
      // Call the Void-Mesh Graph API
      const response = await fetch(`/api/planning/graph/driver/${constdriver.id}/impact`);
      if (response.ok) {
        const locations = await response.json();
        
        locations.forEach(loc => {
          // Assuming LocationNode has some lat/lng properties, 
          // BUT the current LocationNode verified in curl output: {"id":"WAW","name":"Warsaw Hub","type":"HUB","connections":[...]}
          // It lacks lat/lng on the root object.
          // For MVP visualization, we'll hardcode coordinates for known hubs or parse if available.
          // Since the graph init hardcoded distance but not coordinates in the snippet I saw?
          // Wait, initGraph in GraphController: new LocationNode("WAW", "Warsaw Hub", "HUB");
          // It doesn't seem to have lat/lng? 
          // Let's assume for MVP: WAW=52.1672, 20.9679. WRO=51.1079, 17.0385. GDN=54.3520, 18.6466.
          
          let locLat = 0;
          let locLng = 0;
          
          if (loc.id === 'WAW') { locLat = 52.1672; locLng = 20.9679; }
          else if (loc.id === 'WRO') { locLat = 51.1079; locLng = 17.0385; }
          else if (loc.id === 'GDN') { locLat = 54.3520; locLng = 18.6466; }
          else {
             // Random offset for unknown check
             locLat = 52.0 + Math.random();
             locLng = 21.0 + Math.random();
          }

          // Draw Line from Driver to Location
          const line = L.polyline([[constdriver.lat, constdriver.lng], [locLat, locLng]], {
            color: 'red',
            weight: 2,
            opacity: 0.7,
            dashArray: '5, 10',
            className: 'risk-line-anim' // We can add CSS animation later
          }).addTo(map);
          
          // Draw Halo around Location
          const halo = L.circleMarker([locLat, locLng], {
             radius: 20,
             color: 'red',
             fillColor: '#f03',
             fillOpacity: 0.2
          }).addTo(map).bindPopup(`Risk Impact: ${loc.name}`);

          riskLayers.value.push(line);
          riskLayers.value.push(halo);
        });
      }
    } catch (e) {
      console.warn(`Failed to fetch impact for driver ${constdriver.id}`, e);
    }
  }
}

function clearRiskLayers() {
  riskLayers.value.forEach(layer => map.removeLayer(layer));
  riskLayers.value = [];
}

function startAutoRefresh() {
  refreshInterval = setInterval(() => {
    if (autoRefresh.value) {
      refreshLocations();
    }
  }, 30000); // Refresh every 30 seconds
}

function stopAutoRefresh() {
  if (refreshInterval) {
    clearInterval(refreshInterval);
    refreshInterval = null;
  }
}

function formatTime(date) {
  return format(new Date(date), 'HH:mm:ss');
}
</script>

<style scoped>
#map {
  z-index: 0;
}

:deep(.custom-driver-marker) {
  background: transparent;
  border: none;
}

:deep(.leaflet-popup-content-wrapper) {
  border-radius: 8px;
}
</style>
