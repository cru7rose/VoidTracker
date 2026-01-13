<template>
  <div class="fixed top-0 left-20 right-0 h-16 glass-panel border-b border-void-cyan-900/50 flex items-center justify-between px-6 backdrop-blur-2xl z-40">
    <!-- Left: System Status -->
    <div class="flex items-center space-x-6">
      <!-- Status Indicator -->
      <div class="flex items-center space-x-3">
        <div class="relative">
          <div class="w-3 h-3 rounded-full bg-green-500 animate-glow-pulse shadow-lg shadow-green-500/50"></div>
          <div class="absolute inset-0 w-3 h-3 rounded-full bg-green-500 animate-ping opacity-75"></div>
        </div>
        <span class="text-void-cyan-300 text-xs font-mono font-bold tracking-widest neon-glow">OPERATIONAL</span>
      </div>
      
      <!-- Metrics -->
      <div class="flex items-center space-x-6 text-void-cyan-500 text-xs font-mono">
        <div class="flex items-center space-x-2">
          <span class="text-lg">📦</span>
          <span class="text-void-cyan-400 font-semibold">{{ ordersCount }}</span>
        </div>
        <div class="flex items-center space-x-2">
          <span class="text-lg">🚛</span>
          <span class="text-void-cyan-400 font-semibold">{{ vehiclesCount }}</span>
        </div>
        <div class="flex items-center space-x-2">
          <span class="text-lg">⚡</span>
          <span class="text-void-cyan-400 font-semibold">{{ fps }} FPS</span>
        </div>
      </div>
    </div>

    <!-- Center: Title -->
    <div class="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2">
      <h1 class="neon-text-strong font-bold text-lg tracking-[0.2em] font-mono">
        VOID-FLOW CONTROL
      </h1>
    </div>

    <!-- Right: Oracle Hint + User -->
    <div class="flex items-center space-x-4">
      <!-- Oracle Keyboard Shortcut -->
      <div class="flex items-center space-x-2 text-void-cyan-600 text-xs font-mono">
        <span>Command:</span>
        <kbd class="px-2 py-1 bg-void-cyan-950/50 border border-void-cyan-800 rounded text-void-cyan-400 font-mono text-xs">
          ⌘K
        </kbd>
      </div>

      <!-- User Avatar -->
      <div class="w-10 h-10 rounded-full bg-void-cyan-900/50 border-2 border-void-cyan-600 flex items-center justify-center hover:border-void-cyan-400 transition-colors cursor-pointer">
        <span class="text-void-cyan-400 text-sm font-bold font-mono">U</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useVoidDb } from '@/composables/useVoidDb';

const { orders } = useVoidDb();

const ordersCount = computed(() => orders.value?.length || 0);
const vehiclesCount = ref(5);
const fps = ref(60);

let fpsInterval = null;

onMounted(() => {
  let lastTime = performance.now();
  let frameCount = 0;

  function countFrames() {
    frameCount++;
    requestAnimationFrame(countFrames);
  }
  countFrames();

  fpsInterval = setInterval(() => {
    const now = performance.now();
    const elapsed = now - lastTime;
    fps.value = Math.round((frameCount / elapsed) * 1000);
    frameCount = 0;
    lastTime = now;
  }, 1000);
});

onUnmounted(() => {
  if (fpsInterval) {
    clearInterval(fpsInterval);
  }
});
</script>

<style scoped>
.neon-text-strong {
  text-shadow: 
    0 0 10px rgba(0, 255, 204, 0.8),
    0 0 20px rgba(0, 255, 204, 0.6),
    0 0 30px rgba(0, 255, 204, 0.4);
}
</style>
