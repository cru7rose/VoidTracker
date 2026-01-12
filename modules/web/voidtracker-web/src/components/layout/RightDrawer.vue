<template>
  <!-- Drawer (when open) -->
  <Transition name="slide-left">
    <div 
      v-if="isOpen" 
      class="fixed right-0 top-14 bottom-0 w-96 glass-panel border-l border-white/10"
    >
      <!-- Header -->
      <div class="px-4 py-3 border-b border-white/10 flex items-center justify-between">
        <h2 class="neon-text font-bold text-sm tracking-wider font-mono">LIVE ORDERS</h2>
        <button 
          @click="close"
          class="text-white/60 hover:text-white transition-colors"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- Content: OrderList -->
      <div class="overflow-y-auto h-[calc(100%-52px)] p-4">
        <OrderList />
      </div>
    </div>
  </Transition>

  <!-- Toggle Button (when closed) -->
  <Transition name="fade">
    <button 
      v-if="!isOpen"
      @click="open"
      class="fixed right-4 top-20 glass-panel glass-panel-hover px-4 py-2 rounded-lg flex items-center space-x-2"
    >
      <span class="text-xl">📋</span>
      <span class="text-white/80 text-sm font-medium">Orders</span>
      <span class="text-void-cyan-400 text-xs font-mono">({{ ordersCount }})</span>
    </button>
  </Transition>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useVoidDb } from '@/composables/useVoidDb';
import OrderList from '@/views/OrderList.vue';

const { orders } = useVoidDb();

const isOpen = ref(false);

const ordersCount = computed(() => orders.value?.length || 0);

function open() {
  isOpen.value = true;
}

function close() {
  isOpen.value = false;
}
</script>

<style scoped>
/* Slide-left transition */
.slide-left-enter-active,
.slide-left-leave-active {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1),
              opacity 0.3s ease;
}

.slide-left-enter-from {
  transform: translateX(100%);
  opacity: 0;
}

.slide-left-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

/* Fade transition */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Custom scrollbar for dark theme */
:deep(.overflow-y-auto) {
  scrollbar-width: thin;
  scrollbar-color: rgba(0, 255, 204, 0.3) rgba(255, 255, 255, 0.05);
}

:deep(.overflow-y-auto::-webkit-scrollbar) {
  width: 6px;
}

:deep(.overflow-y-auto::-webkit-scrollbar-track) {
  background: rgba(255, 255, 255, 0.05);
}

:deep(.overflow-y-auto::-webkit-scrollbar-thumb) {
  background: rgba(0, 255, 204, 0.3);
  border-radius: 3px;
}

:deep(.overflow-y-auto::-webkit-scrollbar-thumb:hover) {
  background: rgba(0, 255, 204, 0.5);
}
</style>
