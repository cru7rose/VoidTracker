<template>
  <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
    <div 
      v-for="n in 4" 
      :key="n"
      class="spotify-card animate-pulse"
    >
      <div class="h-4 bg-spotify-gray-700 rounded w-24 mb-4"></div>
      <div class="h-10 bg-spotify-gray-700 rounded w-16"></div>
    </div>
  </div>
</template>

<script setup>
// Skeleton loader component
</script>
