<template>
  <div class="h-full flex flex-col space-y-4">
    <!-- KPIs -->
    <div class="grid grid-cols-4 gap-4">
      <div class="bg-white p-4 rounded shadow">
        <div class="text-gray-500 text-sm">Active Routes</div>
        <div class="text-2xl font-bold text-blue-600">12</div>
      </div>
      <div class="bg-white p-4 rounded shadow">
        <div class="text-gray-500 text-sm">Pending Orders</div>
        <div class="text-2xl font-bold text-orange-500">45</div>
      </div>
      <div class="bg-white p-4 rounded shadow">
        <div class="text-gray-500 text-sm">On Time</div>
        <div class="text-2xl font-bold text-green-600">98%</div>
      </div>
      <div class="bg-white p-4 rounded shadow">
        <div class="text-gray-500 text-sm">Alerts</div>
        <div class="text-2xl font-bold text-red-500">2</div>
      </div>
    </div>

    <!-- Map Container -->
    <div class="flex-1 bg-white rounded shadow p-1 relative">
      <div id="map" class="w-full h-full rounded"></div>
      
      <!-- Overlay Legend -->
      <div class="absolute bottom-4 left-4 bg-white p-2 rounded shadow text-xs opacity-90">
        <div class="flex items-center gap-2 mb-1"><span class="w-3 h-3 rounded-full bg-blue-500"></span> Driver</div>
        <div class="flex items-center gap-2"><span class="w-3 h-3 rounded-full bg-red-500"></span> Stop</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

onMounted(() => {
  const map = L.map('map').setView([52.2297, 21.0122], 12); // Warsaw

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap contributors'
  }).addTo(map);

  // Mock Driver
  const driverIcon = L.divIcon({
    className: 'custom-div-icon',
    html: "<div style='background-color:blue; width: 12px; height: 12px; border-radius: 50%; border: 2px solid white;'></div>",
    iconSize: [12, 12],
    iconAnchor: [6, 6]
  });
  
  L.marker([52.235, 21.02], { icon: driverIcon }).addTo(map).bindPopup("Driver: Jan Kowalski");

  // Mock Stop
  const stopIcon = L.divIcon({
    className: 'custom-div-icon',
    html: "<div style='background-color:red; width: 12px; height: 12px; border-radius: 50%; border: 2px solid white;'></div>",
    iconSize: [12, 12],
    iconAnchor: [6, 6]
  });

  L.marker([52.24, 21.03], { icon: stopIcon }).addTo(map).bindPopup("Stop: ACME Corp");
});
</script>

<style>
/* Fix Leaflet z-index issues if any */
.leaflet-pane { z-index: 0; }
</style>
