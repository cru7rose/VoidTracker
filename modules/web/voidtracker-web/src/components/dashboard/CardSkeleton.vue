<template>
  <div class="hologram-panel rounded-xl p-6 border border-void-cyan-800 animate-pulse">
    <div class="h-6 bg-void-cyan-900 rounded w-32 mb-4"></div>
    <div class="space-y-3">
      <div 
        v-for="n in 3" 
        :key="n"
        class="h-16 bg-void-cyan-900 rounded-lg"
      ></div>
    </div>
  </div>
</template>

<script setup>
// Skeleton loader component
</script>
