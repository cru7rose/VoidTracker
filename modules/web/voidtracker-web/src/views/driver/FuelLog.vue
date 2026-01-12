<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
    <div class="max-w-2xl mx-auto">
      <div class="mb-8 flex justify-between items-center">
        <div>
          <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Fuel Log</h1>
          <p class="text-gray-500 dark:text-gray-400">Record your fill-ups.</p>
        </div>
        <button @click="showAddModal = true" class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg shadow-sm">
          + Log Fuel
        </button>
      </div>

      <!-- Recent Logs -->
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden">
        <div class="p-4 border-b border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-900">
          <h3 class="font-semibold text-gray-900 dark:text-white">Recent Entries</h3>
        </div>
        <div class="divide-y divide-gray-200 dark:divide-gray-700">
          <div v-for="log in logs" :key="log.id" class="p-4 flex justify-between items-center hover:bg-gray-50 dark:hover:bg-gray-700/50">
            <div>
              <div class="font-medium text-gray-900 dark:text-white">{{ log.date }}</div>
              <div class="text-sm text-gray-500">{{ log.liters }}L @ {{ log.price }} PLN/L</div>
            </div>
            <div class="text-right">
              <div class="font-bold text-gray-900 dark:text-white">{{ (log.liters * log.price).toFixed(2) }} PLN</div>
              <div class="text-xs text-gray-500">{{ log.odometer }} km</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Add Modal -->
    <div v-if="showAddModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-xl max-w-md w-full p-6">
        <h2 class="text-xl font-bold mb-4 text-gray-900 dark:text-white">New Fuel Entry</h2>
        <form @submit.prevent="addLog" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Odometer (km)</label>
            <input v-model="newLog.odometer" type="number" required class="mt-1 block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm focus:border-blue-500 focus:ring-blue-500" />
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Liters</label>
              <input v-model="newLog.liters" type="number" step="0.1" required class="mt-1 block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Price/L (PLN)</label>
              <input v-model="newLog.price" type="number" step="0.01" required class="mt-1 block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm" />
            </div>
          </div>
          
          <!-- Receipt Photo -->
          <div class="border-2 border-dashed border-gray-300 dark:border-gray-600 rounded-lg p-6 text-center cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700/50" @click="simulatePhoto">
            <span v-if="!newLog.photo" class="text-sm text-gray-500">Tap to upload receipt</span>
            <span v-else class="text-sm text-green-600 font-medium">Receipt Attached</span>
          </div>

          <div class="flex justify-end gap-3 mt-6">
            <button type="button" @click="showAddModal = false" class="px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg">Cancel</button>
            <button type="submit" class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg">Save Entry</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const showAddModal = ref(false);
const logs = ref([
  { id: 1, date: '2023-10-27', liters: 45.5, price: 6.50, odometer: 154200 },
  { id: 2, date: '2023-10-20', liters: 40.0, price: 6.45, odometer: 153800 },
]);

const newLog = ref({ odometer: '', liters: '', price: '', photo: null });

const simulatePhoto = () => {
  newLog.value.photo = 'mock_url';
};

const addLog = () => {
  logs.value.unshift({
    id: Date.now(),
    date: new Date().toISOString().split('T')[0],
    ...newLog.value
  });
  showAddModal.value = false;
  newLog.value = { odometer: '', liters: '', price: '', photo: null };
};
</script>
