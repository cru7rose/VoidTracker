<template>
  <div class="p-6 bg-gray-100 min-h-screen">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-3xl font-bold text-gray-800">{{ $t('dispatch.title') }}</h1>
      <div class="flex space-x-4 border-b mb-4">
        <button 
          v-for="tab in ['Orders', 'Routes', 'Monitoring', 'Automation']" 
          :key="tab"
          @click="currentTab = tab"
          :class="['px-4 py-2', currentTab === tab ? 'border-b-2 border-blue-600 text-blue-600 font-semibold' : 'text-gray-500 hover:text-gray-700']"
        >
          {{ $t(`dispatch.tabs.${tab.toLowerCase()}`) }}
        </button>
      </div>
    </div>

    <!-- Tab Content -->
    <div class="flex-1 overflow-hidden">
      <OrdersBoard v-if="currentTab === 'Orders'" />
      <RoutesMosaic v-if="currentTab === 'Routes'" />
      <MonitoringPanel v-if="currentTab === 'Monitoring'" />
      <AutomationHub v-if="currentTab === 'Automation'" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import OrdersBoard from './OrdersBoard.vue';
import RoutesMosaic from './RoutesMosaic.vue';
import MonitoringPanel from './MonitoringPanel.vue';
import AutomationHub from './AutomationHub.vue';

const currentTab = ref('Orders');

const activeComponent = computed(() => {
  switch (currentTab.value) {
    case 'Orders': return OrdersBoard;
    case 'Routes': return RoutesMosaic;
    case 'Monitoring': return MonitoringPanel;
    case 'Automation': return AutomationHub;
    default: return OrdersBoard;
  }
});
</script>
