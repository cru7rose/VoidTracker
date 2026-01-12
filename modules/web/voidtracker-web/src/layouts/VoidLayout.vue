<template>
  <div class="relative w-full h-screen overflow-hidden bg-void-black">
    <!-- LAYER 0: Base Map (Always visible behind everything) -->
    <div class="absolute inset-0 z-0">
      <VoidMap v-if="mapReady" />
      <div v-else class="flex items-center justify-center h-full">
        <div class="text-void-cyan-400 text-lg font-mono">
          <div class="animate-pulse">Initializing Control Tower...</div>
        </div>
      </div>
    </div>

    <!-- LAYER 1: HUD Overlay (Navigation, Status, Panels) -->
    <div class="absolute inset-0 z-10 pointer-events-none">
      <div class="pointer-events-auto">
        <!-- Left Navigation Sidebar -->
        <LeftNavigation />
        
        <!-- Top Status Bar -->
        <TopStatusBar />
        
        <!-- Right Drawer (Collapsible) -->
        <RightDrawer />
      </div>
    </div>

    <!-- LAYER 2: Modal Overlays (Oracle, Modals) -->
    <div class="absolute inset-0 z-50 pointer-events-none">
      <div class="pointer-events-auto">
        <OracleBar />
      </div>
    </div>

    <!-- LAYER 3: Toast Notifications -->
    <div class="absolute bottom-4 right-4 z-[100] pointer-events-none">
      <div class="pointer-events-auto">
        <!-- Toast container placeholder -->
      </div>
    </div>

    <!-- Router View Content (for non-map views) -->
    <div class="absolute inset-0 z-20 pointer-events-none">
      <div class="pointer-events-auto">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import VoidMap from '@/components/VoidMap.vue';
import OracleBar from '@/components/OracleBar.vue';
import LeftNavigation from '@/components/layout/LeftNavigation.vue';
import TopStatusBar from '@/components/layout/TopStatusBar.vue';
import RightDrawer from '@/components/layout/RightDrawer.vue';

const mapReady = ref(false);

onMounted(() => {
  // Delay map initialization to ensure DOM is ready
  setTimeout(() => {
    mapReady.value = true;
  }, 100);
});
</script>

<style scoped>
/* Ensure proper stacking context */
.z-0 { z-index: 0; }
.z-10 { z-index: 10; }
.z-20 { z-index: 20; }
.z-50 { z-index: 50; }
.z-\[100\] { z-index: 100; }
</style>
