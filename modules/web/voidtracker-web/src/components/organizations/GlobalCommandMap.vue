<template>
  <div id="map" class="w-full h-full z-0"></div>
</template>

<script setup>
import { onMounted, onUnmounted, watch, ref } from 'vue';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { telemetryService } from '@/services/TelemetryService';

const props = defineProps({
  sites: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['select-site']);

let map = null;
let markers = [];
let driverMarkers = new Map();
let telemetryUnsubscribe = null;
const followedDriverId = ref(null);

// Fix Leaflet icon issues in Webpack/Vite
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

const initMap = () => {
  // Default to Copenhagen
  map = L.map('map').setView([55.6761, 12.5683], 7);

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
  }).addTo(map);

  updateMarkers();
  
  // Connect Telemetry
  telemetryService.connect();
  telemetryUnsubscribe = telemetryService.subscribe(updateDriverPositions);
};

const updateDriverPositions = (drivers) => {
    if (!map) return;

    drivers.forEach(driver => {
        let marker = driverMarkers.get(driver.id);

        if (!marker) {
            // Create new marker
            const icon = L.divIcon({
                className: 'custom-driver-icon',
                html: `<div style="transform: rotate(${driver.heading}deg); font-size: 24px;">🚚</div>`,
                iconSize: [30, 30],
                iconAnchor: [15, 15]
            });

            marker = L.marker([driver.lat, driver.lng], { icon })
                .addTo(map)
                .bindPopup(`
                    <div class="font-sans">
                        <h3 class="font-bold text-sm">${driver.name}</h3>
                        <p class="text-xs text-gray-600">Speed: ${driver.speed.toFixed(1)} km/h</p>
                        <p class="text-xs text-gray-600">Status: ${driver.status}</p>
                        <button class="mt-2 text-xs text-blue-600 hover:underline" onclick="window.dispatchEvent(new CustomEvent('follow-driver', { detail: '${driver.id}' }))">
                            ${followedDriverId.value === driver.id ? 'Unfollow' : 'Follow'}
                        </button>
                    </div>
                `);
            driverMarkers.set(driver.id, marker);
        } else {
            // Update position and rotation
            marker.setLatLng([driver.lat, driver.lng]);
            const icon = marker.getIcon();
            // Update rotation in HTML (a bit hacky for Leaflet DivIcon but works)
            icon.options.html = `<div style="transform: rotate(${driver.heading}deg); font-size: 24px;">🚚</div>`;
            marker.setIcon(icon);
            
            // Update popup content if open
            if (marker.isPopupOpen()) {
                 // In a real app, we'd update the DOM inside the popup more gracefully
            }
        }

        // Follow logic
        if (followedDriverId.value === driver.id) {
            map.panTo([driver.lat, driver.lng]);
        }
    });
};

const updateMarkers = () => {
  if (!map) return;

  // Clear existing markers
  markers.forEach(marker => map.removeLayer(marker));
  markers = [];

  props.sites.forEach(site => {
    // Mock coordinates if not present (in real app, address should have lat/lon)
    // For demo, we'll randomize slightly around Copenhagen if no coords
    let lat = site.address?.latitude;
    let lon = site.address?.longitude;

    if (!lat || !lon) {
        // Random spread for demo
        lat = 55.6 + (Math.random() - 0.5) * 2;
        lon = 12.5 + (Math.random() - 0.5) * 3;
    }

    const marker = L.marker([lat, lon])
      .addTo(map)
      .bindPopup(`
        <div class="font-sans">
            <h3 class="font-bold text-sm">${site.siteType}</h3>
            <p class="text-xs text-gray-600">${site.address?.city || 'Unknown Location'}</p>
            <button class="mt-2 text-xs text-blue-600 hover:underline" onclick="window.dispatchEvent(new CustomEvent('site-select', { detail: '${site.siteId}' }))">
                View Details
            </button>
        </div>
      `);
    
    markers.push(marker);
  });
};

// Listen for custom event from popup
// Listen for custom event from popup
window.addEventListener('site-select', (e) => {
    emit('select-site', e.detail);
});

window.addEventListener('follow-driver', (e) => {
    if (followedDriverId.value === e.detail) {
        followedDriverId.value = null;
    } else {
        followedDriverId.value = e.detail;
    }
});

watch(() => props.sites, () => {
    updateMarkers();
}, { deep: true });

onMounted(() => {
  initMap();
});

onUnmounted(() => {
  if (telemetryUnsubscribe) {
      telemetryUnsubscribe();
  }
  telemetryService.disconnect();
  
  if (map) {
    map.remove();
    map = null;
  }
});
</script>

<style scoped>
/* Ensure map container has height */
#map {
    min-height: 400px;
}
</style>
