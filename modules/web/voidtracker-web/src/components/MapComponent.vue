<script setup>
import { onMounted, onUnmounted, ref, watch } from 'vue';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

const props = defineProps({
  markers: {
    type: Array,
    default: () => []
  },
  center: {
    type: Array,
    default: () => [52.2297, 21.0122] // Warsaw default
  },
  zoom: {
    type: Number,
    default: 13
  },
  routes: {
    type: Array, // Array of { coordinates: [[lat, lng], ...], name: String }
    default: () => []
  }
});

const mapContainer = ref(null);
let map = null;
let markersLayer = null;

onMounted(() => {
  if (mapContainer.value) {
    const bounds = L.latLngBounds(
      L.latLng(47.0, 5.0),
      L.latLng(70.0, 40.0)
    );

    map = L.map(mapContainer.value, {
      center: props.center,
      zoom: props.zoom,
      minZoom: 5,
      maxBounds: bounds,
      maxBoundsViscosity: 1.0
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);

    markersLayer = L.layerGroup().addTo(map);

    updateMarkers();
  }
});

const updateMarkers = () => {
  if (!map || !markersLayer) return;

  markersLayer.clearLayers();

  // Draw Markers
  props.markers.forEach(marker => {
    if (marker.lat && marker.lng) {
      let icon = undefined;
      
      if (marker.type === 'depot') {
          icon = L.divIcon({
              className: 'custom-div-icon',
              html: `<div style="background-color: #1f2937; color: white; border-radius: 50%; width: 24px; height: 24px; display: flex; align-items: center; justify-content: center; border: 2px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);">🏠</div>`,
              iconSize: [24, 24],
              iconAnchor: [12, 12]
          });
      } else if (marker.type === 'numbered') {
          const color = marker.color || 'blue';
          const size = marker.isHighlight ? 30 : 20;
          const fontSize = marker.isHighlight ? 14 : 10;
          
          icon = L.divIcon({
              className: 'custom-div-icon',
              html: `<div style="background-color: ${color}; color: white; border-radius: 50%; width: ${size}px; height: ${size}px; display: flex; align-items: center; justify-content: center; border: 2px solid white; font-size: ${fontSize}px; font-weight: bold; box-shadow: 0 1px 2px rgba(0,0,0,0.3); transition: all 0.2s ease;">${marker.number}</div>`,
              iconSize: [size, size],
              iconAnchor: [size/2, size/2]
          });
      }

      L.marker([marker.lat, marker.lng], { icon, zIndexOffset: marker.isHighlight ? 1000 : 0 })
        .addTo(markersLayer)
        .bindPopup(marker.popup || 'Location');
    }
  });

  // Draw Routes (Polylines)
  if (props.routes && Array.isArray(props.routes)) {
    props.routes.forEach((route, index) => {
      const colors = ['blue', 'red', 'green', 'purple', 'orange', 'teal'];
      const color = colors[index % colors.length];

      if (route.coordinates && route.coordinates.length > 0) {
        L.polyline(route.coordinates, { color: color, weight: 4 })
         .addTo(markersLayer)
         .bindPopup(`Route: ${route.name || 'Route ' + (index + 1)}`);
      }
    });
  }
};

watch(() => [props.markers, props.routes], () => {
  updateMarkers();
}, { deep: true });

onUnmounted(() => {
  if (map) {
    map.remove();
  }
});
</script>

<template>
  <div ref="mapContainer" class="h-full w-full z-0"></div>
</template>

<style>
/* Fix for missing marker icons in Vite/Webpack */
.leaflet-default-icon-path {
  background-image: url(https://unpkg.com/leaflet@1.7.1/dist/images/marker-icon.png);
}
</style>
