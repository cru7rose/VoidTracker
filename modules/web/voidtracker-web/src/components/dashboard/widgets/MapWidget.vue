<template>
  <div id="dashboard-map" class="h-full w-full rounded-lg overflow-hidden"></div>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

let map = null

onMounted(() => {
  map = L.map('dashboard-map').setView([52.2297, 21.0122], 12) // Warsaw

  L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>',
    subdomains: 'abcd',
    maxZoom: 20
  }).addTo(map)

  // Mock Driver Marker
  const driverIcon = L.divIcon({
    className: 'custom-driver-icon',
    html: `<div class="w-4 h-4 bg-blue-600 rounded-full border-2 border-white shadow-lg pulse-ring"></div>`,
    iconSize: [16, 16],
    iconAnchor: [8, 8]
  })

  L.marker([52.235, 21.02], { icon: driverIcon }).addTo(map)
})

onUnmounted(() => {
  if (map) {
    map.remove()
  }
})
</script>

<style>
.pulse-ring::before {
  content: '';
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  border-radius: 50%;
  border: 2px solid #2563eb;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(0.8); opacity: 1; }
  100% { transform: scale(2); opacity: 0; }
}
</style>
