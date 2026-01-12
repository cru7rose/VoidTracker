<template>
  <div class="p-6">
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Custom Report Builder</h1>
      <p class="text-gray-500 dark:text-gray-400">Design and generate custom analytics reports.</p>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Configuration Panel -->
      <div class="bg-white dark:bg-gray-800 p-6 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 space-y-6">
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">1. Data Source</label>
          <select v-model="config.source" class="block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm">
            <option value="orders">Orders</option>
            <option value="drivers">Drivers</option>
            <option value="fuel">Fuel Costs</option>
          </select>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">2. Metric</label>
          <select v-model="config.metric" class="block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm">
            <option value="count">Count / Total</option>
            <option value="sum">Sum (Revenue/Cost)</option>
            <option value="avg">Average</option>
          </select>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">3. Group By</label>
          <select v-model="config.groupBy" class="block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm">
            <option value="date">Date</option>
            <option value="status">Status</option>
            <option value="driver">Driver</option>
          </select>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">4. Visualization</label>
          <div class="grid grid-cols-3 gap-2">
            <button 
              v-for="type in ['bar', 'line', 'pie']" 
              :key="type"
              @click="config.type = type"
              :class="['p-2 border rounded-lg text-center text-sm capitalize', config.type === type ? 'bg-blue-50 border-blue-500 text-blue-700' : 'border-gray-200 dark:border-gray-700']"
            >
              {{ type }}
            </button>
          </div>
        </div>

        <button @click="generateReport" class="w-full py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg shadow-sm">
          Generate Report
        </button>
      </div>

      <!-- Preview Panel -->
      <div class="lg:col-span-2 bg-white dark:bg-gray-800 p-6 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 flex flex-col items-center justify-center min-h-[400px]">
        <div v-if="!generated" class="text-center text-gray-400">
          <svg class="w-16 h-16 mx-auto mb-4 opacity-50" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" /></svg>
          <p>Configure report settings and click Generate</p>
        </div>
        <div v-else class="w-full h-full flex flex-col">
          <div class="flex justify-between items-center mb-6">
            <h3 class="text-lg font-bold text-gray-900 dark:text-white">Report Preview</h3>
            <button class="text-sm text-blue-600 hover:text-blue-800">Save Report</button>
          </div>
          
          <!-- Mock Chart Visualization -->
          <div class="flex-1 flex items-end justify-around gap-4 px-4 pb-4 border-b border-l border-gray-300 dark:border-gray-600 h-64">
            <div v-for="(val, idx) in mockData" :key="idx" class="w-16 bg-blue-500 rounded-t-lg relative group transition-all hover:bg-blue-600" :style="{ height: val.height + '%' }">
              <span class="absolute -top-6 left-1/2 -translate-x-1/2 text-xs font-bold text-gray-700 dark:text-gray-300 opacity-0 group-hover:opacity-100">{{ val.value }}</span>
            </div>
          </div>
          <div class="flex justify-around px-4 mt-2 text-xs text-gray-500">
            <span v-for="(val, idx) in mockData" :key="idx">{{ val.label }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const config = ref({
  source: 'orders',
  metric: 'count',
  groupBy: 'date',
  type: 'bar'
});

const generated = ref(false);
const mockData = ref([]);

const generateReport = () => {
  generated.value = true;
  // Generate random mock data for visualization
  mockData.value = [
    { label: 'Mon', value: 120, height: 40 },
    { label: 'Tue', value: 150, height: 50 },
    { label: 'Wed', value: 180, height: 60 },
    { label: 'Thu', value: 220, height: 75 },
    { label: 'Fri', value: 280, height: 90 },
  ];
};
</script>
