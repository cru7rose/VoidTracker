<template>
  <div class="fixed top-0 left-16 right-0 h-14 glass-panel border-b border-white/10 flex items-center justify-between px-6">
    <!-- Left: System Status -->
    <div class="flex items-center space-x-6">
      <!-- Status Indicator -->
      <div class="flex items-center space-x-2">
        <div class="relative">
          <div class="w-2 h-2 rounded-full bg-green-500 animate-glow-pulse"></div>
          <div class="absolute inset-0 w-2 h-2 rounded-full bg-green-500 animate-ping opacity-75"></div>
        </div>
        <span class="text-white/80 text-xs font-mono font-semibold tracking-wider">OPERATIONAL</span>
      </div>
      
      <!-- Metrics -->
      <div class="flex items-center space-x-4 text-white/60 text-xs font-mono">
        <div class="flex items-center space-x-1">
          <span>📦</span>
          <span>{{ ordersCount }}</span>
        </div>
        <div class="flex items-center space-x-1">
          <span>🚛</span>
          <span>{{ vehiclesCount }}</span>
        </div>
        <div class="flex items-center space-x-1">
          <span>⚡</span>
          <span>{{ fps }} FPS</span>
        </div>
      </div>
    </div>

    <!-- Center: Title -->
    <div class="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2">
      <h1 class="neon-text font-bold text-base tracking-widest font-mono">
        VOID-FLOW CONTROL
      </h1>
    </div>

    <!-- Right: Oracle Hint + User -->
    <div class="flex items-center space-x-4">
      <!-- Oracle Keyboard Shortcut -->
      <div class="flex items-center space-x-2 text-white/40 text-xs">
        <span>Command Palette:</span>
        <kbd class="px-2 py-0.5 bg-white/10 border border-white/20 rounded text-xs font-mono">
          ⌘K
        </kbd>
      </div>

      <!-- User Avatar -->
      <div class="w-8 h-8 rounded-full bg-void-cyan-500/20 border border-void-cyan-500 flex items-center justify-center">
        <span class="text-void-cyan-400 text-sm font-bold">U</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useVoidDb } from '@/composables/useVoidDb';

const { orders } = useVoidDb();

const ordersCount = computed(() => orders.value?.length || 0);
const vehiclesCount = ref(5);  // TODO: From vehicle store
const fps = ref(60);

// FPS monitoring
let fpsInterval = null;

onMounted(() => {
  // Simple FPS counter
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
