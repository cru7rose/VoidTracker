<template>
  <div class="spotify-card">
    <h2 class="text-lg font-bold mb-4">
      Active Manifests
    </h2>
    <div class="space-y-2">
      <div 
        v-for="manifest in manifests" 
        :key="manifest.id"
        class="playlist-item"
        @click="$emit('manifest-click', manifest.id)"
      >
        <div class="flex-1 min-w-0">
          <p class="text-sm font-semibold text-white truncate">{{ manifest.driverName }}</p>
          <p class="text-xs text-spotify-gray-400 truncate">{{ manifest.stops }} stops</p>
        </div>
        <div class="flex items-center gap-3">
          <div class="w-24 h-2 bg-spotify-gray-800 rounded-full overflow-hidden">
            <div 
              class="h-full bg-spotify-green-400 transition-all duration-500"
              :style="{ width: `${manifest.progress}%` }"
            ></div>
          </div>
          <span class="text-sm font-semibold text-white min-w-[3rem] text-right">{{ manifest.progress }}%</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  manifests: {
    type: Array,
    required: true
  }
});

defineEmits(['manifest-click']);
</script>
