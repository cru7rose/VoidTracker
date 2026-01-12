<template>
  <div class="fixed top-4 right-4 z-50 flex items-center gap-2 bg-gray-900/80 backdrop-blur px-3 py-1.5 rounded-full border border-gray-700 shadow-xl">
    <div 
      class="w-3 h-3 rounded-full transition-colors duration-300"
      :class="isOnline ? 'bg-green-500 shadow-[0_0_10px_rgba(34,197,94,0.5)]' : 'bg-red-500 animate-pulse'"
    ></div>
    <span class="text-xs font-medium text-gray-300">
      {{ isOnline ? 'Online' : 'Offline' }}
    </span>
    <span v-if="pendingCount > 0" class="ml-1 text-xs bg-blue-600 text-white px-1.5 py-0.5 rounded-full">
      {{ pendingCount }}
    </span>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useOfflineQueueStore } from '../stores/offline-queue'
import { storeToRefs } from 'pinia'

const isOnline = ref(navigator.onLine)
const queueStore = useOfflineQueueStore()
const { pendingCount } = storeToRefs(queueStore)

const updateStatus = () => {
  isOnline.value = navigator.onLine
  if (isOnline.value) {
      queueStore.processQueue();
  }
}

onMounted(() => {
  window.addEventListener('online', updateStatus)
  window.addEventListener('offline', updateStatus)
  // Initial sync attempt
  if (navigator.onLine) {
      queueStore.processQueue()
  }
})

onUnmounted(() => {
  window.removeEventListener('online', updateStatus)
  window.removeEventListener('offline', updateStatus)
})
</script>
