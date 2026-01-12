<template>
  <div class="relative w-full h-screen bg-[#0A0A0F]">
    <!-- Deck.gl Canvas Container -->
    <div ref="deckContainerRef" class="absolute inset-0"></div>

    <!-- God's Eye Controls (Cyberpunk UI) -->
    <div class="absolute top-6 right-6 z-10">
      <div class="bg-slate-900/90 border border-cyan-500/50 rounded-xl p-4 backdrop-blur-sm shadow-2xl shadow-cyan-500/20">
        <h3 class="text-cyan-400 font-bold mb-3 flex items-center">
          <span class="text-2xl mr-2">🔭</span>
          GOD'S EYE
        </h3>
        
        <!-- Lens Switcher -->
        <div class="space-y-2">
          <button 
            @click="activeLens = 'profitability'"
            :class="[
              'w-full px-4 py-3 rounded-lg font-bold transition-all duration-200',
              activeLens === 'profitability' 
                ? 'bg-gradient-to-r from-cyan-500 to-cyan-600 text-white shadow-lg shadow-cyan-500/50' 
                : 'bg-slate-700 text-slate-300 hover:bg-slate-600'
            ]"
          >
            <span class="mr-2">💰</span>
            Profitability
          </button>
          
          <button 
            @click="activeLens = 'void-mesh'"
            :class="[
              'w-full px-4 py-3 rounded-lg font-bold transition-all duration-200',
              activeLens === 'void-mesh' 
                ? 'bg-gradient-to-r from-pink-500 to-pink-600 text-white shadow-lg shadow-pink-500/50' 
                : 'bg-slate-700 text-slate-300 hover:bg-slate-600'
            ]"
          >
            <span class="mr-2">🕸️</span>
            Void-Mesh
          </button>
        </div>

        <!-- Stats Panel -->
        <div class="mt-4 pt-4 border-t border-slate-700">
          <div class="text-xs text-slate-400 space-y-1">
            <div>Orders: <span class="text-cyan-400 font-bold">{{ orders.length }}</span></div>
            <div>FPS: <span :class="fps >= 55 ? 'text-green-400' : 'text-amber-400'" class="font-bold">{{ fps }}</span></div>
            <div v-if="activeLens === 'profitability'">
              Mode: <span class="text-cyan-400">3D Heatmap</span>
            </div>
            <div v-else>
              Mode: <span class="text-pink-400">Route Network</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Tooltip Overlay -->
    <div 
      v-if="tooltip" 
      class="absolute pointer-events-none z-20 bg-slate-900/95 border border-cyan-500/70 rounded-lg p-3 shadow-2xl backdrop-blur-sm"
      :style="{ 
        left: `${tooltip.x + 15}px`, 
        top: `${tooltip.y + 15}px` 
      }"
    >
      <div class="text-xs space-y-1">
        <div v-for="(value, key) in tooltip.content" :key="key" class="flex justify-between gap-4">
          <span class="text-slate-400 capitalize">{{ key }}:</span>
          <span class="text-cyan-300 font-bold">{{ value }}</span>
        </div>
      </div>
    </div>

    <!-- Loading Overlay -->
    <div v-if="loading" class="absolute inset-0 bg-black/80 flex items-center justify-center z-30">
      <div class="text-center">
        <div class="inline-block animate-spin rounded-full h-12 w-12 border-4 border-cyan-500 border-t-transparent mb-4"></div>
        <p class="text-cyan-400 font-bold">Initializing God's Eye...</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, computed } from 'vue';
import { Deck } from '@deck.gl/core';
import { useVoidDb } from '@/composables/useVoidDb';
import { useVoidDeck } from '@/composables/useVoidDeck';

const deckContainerRef = ref(null);
const activeLens = ref('profitability');
const loading = ref(true);
const fps = ref(60);

// Get reactive order data from RxDB
const { orders } = useVoidDb();

// Initialize Deck.gl layers
const { 
  profitabilityLayer, 
  voidMeshLayers, 
  viewState, 
  updateViewState,
  tooltip 
} = useVoidDeck(orders);

let deckInstance = null;
let fpsInterval = null;

/**
 * Initialize Deck.gl instance
 */
function initializeDeck() {
  if (!deckContainerRef.value) {
    console.warn('[VoidMap] Container not ready');
    return;
  }

  try {
    deckInstance = new Deck({
      container: deckContainerRef.value,
      initialViewState: viewState.value,
      controller: true,
      
      // Cyberpunk dark background
      style: {
        backgroundColor: '#0A0A0F'
      },
      
      // Performance optimizations
      parameters: {
        clearColor: [0.04, 0.04, 0.06, 1]
      },
      
      // View state change callback
      onViewStateChange: ({ viewState: newViewState }) => {
        updateViewState(newViewState);
      },
      
      // Initial layers (empty, will update reactively)
      layers: []
    });

    console.log('[VoidMap] Deck.gl initialized');
    loading.value = false;

  } catch (error) {
    console.error('[VoidMap] Initialization failed:', error);
    loading.value = false;
  }
}

/**
 * Update Deck.gl layers based on active lens
 */
function updateLayers() {
  if (!deckInstance) return;

  const layers = activeLens.value === 'profitability' 
    ? [profitabilityLayer.value]
    : voidMeshLayers.value;

  deckInstance.setProps({ layers });
}

/**
 * FPS monitoring
 */
function startFPSMonitoring() {
  let lastTime = performance.now();
  let frameCount = 0;

  fpsInterval = setInterval(() => {
    const now = performance.now();
    const delta = now - lastTime;
    fps.value = Math.round((frameCount / delta) * 1000);
    
    frameCount = 0;
    lastTime = now;
  }, 1000);

  // Count frames
  function countFrame() {
    frameCount++;
    requestAnimationFrame(countFrame);
  }
  countFrame();
}

/**
 * Cleanup
 */
function cleanup() {
  if (deckInstance) {
    deckInstance.finalize();
    deckInstance = null;
  }
  
  if (fpsInterval) {
    clearInterval(fpsInterval);
    fpsInterval = null;
  }
}

// Lifecycle hooks
onMounted(() => {
  initializeDeck();
  startFPSMonitoring();
});

onUnmounted(() => {
  cleanup();
});

// Watch for lens changes
watch(activeLens, () => {
  updateLayers();
});

// Watch for order updates (RxDB reactive)
watch(orders, () => {
  updateLayers();
}, { deep: true });

// Update layers on initial render
watch(() => profitabilityLayer.value, () => {
  if (activeLens.value === 'profitability') {
    updateLayers();
  }
});

watch(() => voidMeshLayers.value, () => {
  if (activeLens.value === 'void-mesh') {
    updateLayers();
  }
}, { deep: true });
</script>

<style scoped>
/* Cyberpunk glow effects */
button {
  position: relative;
  overflow: hidden;
}

button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

button:hover::before {
  left: 100%;
}

/* Smooth transitions */
* {
  transition-property: background-color, border-color, color, fill, stroke;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  transition-duration: 150ms;
}
</style>
