<template>
  <div class="p-6">
    <div class="mb-6 flex justify-between items-center">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Maintenance Scheduler</h1>
        <p class="text-gray-500 dark:text-gray-400">Track fleet health and schedule services.</p>
      </div>
      <button @click="showAddModal = true" class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg shadow-sm flex items-center gap-2">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" /></svg>
        Schedule Service
      </button>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
      <div class="bg-white dark:bg-gray-800 p-4 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700">
        <div class="text-sm text-gray-500 mb-1">Total Vehicles</div>
        <div class="text-2xl font-bold text-gray-900 dark:text-white">24</div>
      </div>
      <div class="bg-white dark:bg-gray-800 p-4 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700">
        <div class="text-sm text-red-500 mb-1">Overdue</div>
        <div class="text-2xl font-bold text-red-600">2</div>
      </div>
      <div class="bg-white dark:bg-gray-800 p-4 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700">
        <div class="text-sm text-orange-500 mb-1">Due Soon</div>
        <div class="text-2xl font-bold text-orange-600">5</div>
      </div>
      <div class="bg-white dark:bg-gray-800 p-4 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700">
        <div class="text-sm text-green-500 mb-1">Healthy</div>
        <div class="text-2xl font-bold text-green-600">17</div>
      </div>
    </div>

    <!-- Vehicle List -->
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
        <thead class="bg-gray-50 dark:bg-gray-900">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Vehicle</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Next Service</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Due Date</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Odometer</th>
            <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200 dark:divide-gray-700">
          <tr v-for="vehicle in vehicles" :key="vehicle.id" class="hover:bg-gray-50 dark:hover:bg-gray-700/50">
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="flex items-center">
                <div class="h-10 w-10 rounded-full bg-gray-200 dark:bg-gray-600 flex items-center justify-center text-gray-500 dark:text-gray-300">
                  <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" /></svg>
                </div>
                <div class="ml-4">
                  <div class="text-sm font-medium text-gray-900 dark:text-white">{{ vehicle.name }}</div>
                  <div class="text-sm text-gray-500">{{ vehicle.plate }}</div>
                </div>
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span :class="['px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full', getStatusColor(vehicle.status)]">
                {{ vehicle.status }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ vehicle.nextService }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ vehicle.dueDate }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ vehicle.odometer }} km</td>
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
              <button class="text-blue-600 hover:text-blue-900 mr-3">Log Service</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Add Modal -->
    <div v-if="showAddModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-xl max-w-md w-full p-6">
        <h2 class="text-xl font-bold mb-4 text-gray-900 dark:text-white">Schedule Service</h2>
        <form @submit.prevent="scheduleService" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Vehicle</label>
            <select v-model="newService.vehicleId" class="block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm">
              <option v-for="v in vehicles" :key="v.id" :value="v.id">{{ v.name }} ({{ v.plate }})</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Service Type</label>
            <select v-model="newService.type" class="block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm">
              <option>Oil Change</option>
              <option>Tire Rotation</option>
              <option>Brake Inspection</option>
              <option>Annual Inspection</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Due Date</label>
            <input v-model="newService.date" type="date" class="block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm" />
          </div>
          
          <div class="flex justify-end gap-3 mt-6">
            <button type="button" @click="showAddModal = false" class="px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg">Cancel</button>
            <button type="submit" class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg">Save</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const showAddModal = ref(false);

const vehicles = ref([
  { id: 1, name: 'Truck 001', plate: 'WX 12345', status: 'Overdue', nextService: 'Oil Change', dueDate: '2023-10-25', odometer: 154000 },
  { id: 2, name: 'Van 005', plate: 'WX 98765', status: 'Due Soon', nextService: 'Tire Rotation', dueDate: '2023-11-05', odometer: 45000 },
  { id: 3, name: 'Truck 002', plate: 'KR 55555', status: 'OK', nextService: 'Annual Inspection', dueDate: '2024-01-15', odometer: 89000 },
]);

const newService = ref({ vehicleId: '', type: 'Oil Change', date: '' });

const getStatusColor = (status) => {
  switch (status) {
    case 'Overdue': return 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-300';
    case 'Due Soon': return 'bg-orange-100 text-orange-800 dark:bg-orange-900/30 dark:text-orange-300';
    case 'OK': return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300';
    default: return 'bg-gray-100 text-gray-800';
  }
};

const scheduleService = () => {
  alert('Service scheduled!');
  showAddModal.value = false;
};
</script>
