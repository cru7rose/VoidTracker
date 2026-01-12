<template>
  <div class="p-6">
    <div class="mb-6 flex justify-between items-center">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white">{{ $t('customer.bulk_upload') || 'Bulk Order Upload' }}</h1>
        <p class="text-gray-500 dark:text-gray-400">Import multiple orders at once using CSV.</p>
      </div>
      <button @click="downloadTemplate" class="text-blue-600 hover:text-blue-800 text-sm font-medium">
        Download Template
      </button>
    </div>

    <!-- Upload Zone -->
    <div 
      @dragover.prevent="isDragging = true"
      @dragleave.prevent="isDragging = false"
      @drop.prevent="handleDrop"
      :class="[
        'border-2 border-dashed rounded-xl p-12 text-center transition-colors cursor-pointer',
        isDragging ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20' : 'border-gray-300 dark:border-gray-700 hover:border-gray-400'
      ]"
    >
      <input type="file" ref="fileInput" @change="handleFileSelect" accept=".csv" class="hidden" />
      <div class="flex flex-col items-center gap-4">
        <div class="p-4 bg-gray-100 dark:bg-gray-800 rounded-full">
          <svg class="w-8 h-8 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
          </svg>
        </div>
        <div>
          <p class="text-lg font-medium text-gray-900 dark:text-white">
            Drop your CSV file here, or <button @click="$refs.fileInput.click()" class="text-blue-600 hover:text-blue-500">browse</button>
          </p>
          <p class="text-sm text-gray-500 mt-1">Supports CSV files up to 10MB</p>
        </div>
      </div>
    </div>

    <!-- Preview Table -->
    <div v-if="parsedData.length > 0" class="mt-8">
      <div class="flex justify-between items-center mb-4">
        <h3 class="text-lg font-semibold text-gray-900 dark:text-white">Preview ({{ parsedData.length }} orders)</h3>
        <div class="flex gap-3">
          <button @click="parsedData = []" class="px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg">Cancel</button>
          <button @click="importOrders" class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg shadow-sm">
            Import {{ parsedData.length }} Orders
          </button>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-800 shadow-sm rounded-lg overflow-hidden border border-gray-200 dark:border-gray-700">
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
            <thead class="bg-gray-50 dark:bg-gray-900">
              <tr>
                <th v-for="header in headers" :key="header" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  {{ header }}
                </th>
              </tr>
            </thead>
            <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
              <tr v-for="(row, idx) in parsedData.slice(0, 5)" :key="idx">
                <td v-for="header in headers" :key="header" class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-300">
                  {{ row[header] }}
                </td>
              </tr>
              <tr v-if="parsedData.length > 5">
                <td :colspan="headers.length" class="px-6 py-4 text-center text-sm text-gray-500 italic">
                  ...and {{ parsedData.length - 5 }} more rows
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const isDragging = ref(false);
const fileInput = ref(null);
const parsedData = ref([]);
const headers = ref([]);

const handleFileSelect = (e) => processFile(e.target.files[0]);
const handleDrop = (e) => {
  isDragging.value = false;
  processFile(e.dataTransfer.files[0]);
};

const processFile = (file) => {
  if (!file || file.type !== 'text/csv') {
    alert('Please upload a valid CSV file.');
    return;
  }

  const reader = new FileReader();
  reader.onload = (e) => {
    const text = e.target.result;
    const lines = text.split('\n').map(l => l.trim()).filter(l => l);
    if (lines.length < 2) return;

    headers.value = lines[0].split(',').map(h => h.trim());
    parsedData.value = lines.slice(1).map(line => {
      const values = line.split(',');
      const row = {};
      headers.value.forEach((h, i) => row[h] = values[i]?.trim());
      return row;
    });
  };
  reader.readAsText(file);
};

const downloadTemplate = () => {
  const csvContent = "Customer Name,Pickup Address,Delivery Address,Weight (kg),Description\nAcme Corp,123 Main St,456 Elm St,10,Box of parts";
  const blob = new Blob([csvContent], { type: 'text/csv' });
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'orders_template.csv';
  a.click();
};

const importOrders = () => {
  // Mock API call
  setTimeout(() => {
    alert(`Successfully imported ${parsedData.value.length} orders!`);
    parsedData.value = [];
  }, 1000);
};
</script>
