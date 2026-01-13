<template>
  <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
    <div 
      v-for="(stat, key) in stats" 
      :key="key"
      class="spotify-card group cursor-pointer"
    >
      <h3 class="text-sm font-semibold text-spotify-gray-400 mb-2 uppercase tracking-wider">
        {{ getLabel(key) }}
      </h3>
      <p class="text-4xl font-bold" :class="getColorClass(key)">
        {{ stat }}
      </p>
    </div>
  </div>
</template>

<script setup>
defineProps({
  stats: {
    type: Object,
    required: true
  }
});

function getLabel(key) {
  const labels = {
    pendingOrders: 'Pending Orders',
    inTransit: 'In Transit',
    deliveredToday: 'Delivered Today',
    activeDrivers: 'Active Drivers'
  };
  return labels[key] || key;
}

function getColorClass(key) {
  const classes = {
    pendingOrders: 'text-yellow-400',
    inTransit: 'text-spotify-green-400',
    deliveredToday: 'text-green-400',
    activeDrivers: 'text-blue-400'
  };
  return classes[key] || 'text-white';
}
</script>
