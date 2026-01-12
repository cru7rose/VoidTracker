<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <div>
         <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">Fleet Management</h1>
         <p class="text-sm text-gray-500 dark:text-gray-400">Manage vehicle profiles, capacities, and carrier compliance.</p>
      </div>
      <button class="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 flex items-center gap-2">
         <span class="material-icons text-sm">add</span> Add Vehicle
      </button>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
       <div class="bg-white dark:bg-gray-800 p-4 rounded-lg shadow border-l-4 border-blue-500">
          <div class="text-sm text-gray-500">Total Fleet</div>
          <div class="text-2xl font-bold text-gray-800 dark:text-white">124</div>
       </div>
       <div class="bg-white dark:bg-gray-800 p-4 rounded-lg shadow border-l-4 border-green-500">
          <div class="text-sm text-gray-500">Active Duty</div>
          <div class="text-2xl font-bold text-gray-800 dark:text-white">88</div>
       </div>
       <div class="bg-white dark:bg-gray-800 p-4 rounded-lg shadow border-l-4 border-yellow-500">
           <div class="text-sm text-gray-500">Maintenance</div>
           <div class="text-2xl font-bold text-gray-800 dark:text-white">12</div>
       </div>
       <div class="bg-white dark:bg-gray-800 p-4 rounded-lg shadow border-l-4 border-red-500">
           <div class="text-sm text-gray-500">Compliance Issues</div>
           <div class="text-2xl font-bold text-gray-800 dark:text-white">3</div>
       </div>
    </div>

    <!-- Main Table Card -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden">
      <!-- Toolbar -->
      <div class="p-4 border-b border-gray-200 dark:border-gray-700 flex gap-4">
        <input 
           type="text" 
           placeholder="Search vehicles (ID, Driver, Plate)..." 
           class="flex-1 px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-gray-50 dark:bg-gray-900 text-gray-900 dark:text-white"
        />
        <select class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-gray-50 dark:bg-gray-900 text-gray-900 dark:text-white">
           <option>All Types</option>
           <option>Van (3.5t)</option>
           <option>Truck (12t)</option>
           <option>Bike</option>
        </select>
        <select class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-gray-50 dark:bg-gray-900 text-gray-900 dark:text-white">
           <option>All Statuses</option>
           <option>Active</option>
           <option>Inactive</option>
        </select>
      </div>

      <!-- Table -->
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
          <thead class="bg-gray-50 dark:bg-gray-700">
            <tr>
               <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Vehicle</th>
               <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Type / Capacity</th>
               <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Default Driver</th>
               <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Carrier</th>
               <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Status</th>
               <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Actions</th>
            </tr>
          </thead>
          <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
            <tr v-for="vehicle in vehicles" :key="vehicle.id" class="hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors">
              <td class="px-6 py-4 whitespace-nowrap">
                 <div class="flex items-center">
                    <div class="flex-shrink-0 h-10 w-10">
                       <span class="h-10 w-10 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-600 font-bold">
                          {{ vehicle.plate.substring(0, 2) }}
                       </span>
                    </div>
                    <div class="ml-4">
                       <div class="text-sm font-medium text-gray-900 dark:text-white">{{ vehicle.plate }}</div>
                       <div class="text-xs text-gray-500 dark:text-gray-400">ID: {{ vehicle.id }}</div>
                    </div>
                 </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                 <div class="text-sm text-gray-900 dark:text-white font-semibold">{{ vehicle.type }}</div>
                 <div class="text-xs text-gray-500 dark:text-gray-400">Max: {{ vehicle.capacity }}kg | {{ vehicle.volume }}m³</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                 <div class="text-sm text-gray-900 dark:text-white">{{ vehicle.driver || 'Unassigned' }}</div>
                 <div v-if="vehicle.driver" class="text-xs text-green-600">Verified</div>
              </td>
               <td class="px-6 py-4 whitespace-nowrap">
                 <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-blue-100 text-blue-800">
                    {{ vehicle.carrier }}
                 </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                 <span v-if="vehicle.status === 'ACTIVE'" class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">
                    Active
                 </span>
                 <span v-else class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-red-100 text-red-800">
                    Inactive
                 </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                 <button class="text-indigo-600 hover:text-indigo-900 dark:hover:text-indigo-400 mr-3">Edit</button>
                 <button class="text-red-600 hover:text-red-900 dark:hover:text-red-400">Delete</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <!-- Pagination -->
      <div class="px-4 py-3 border-t border-gray-200 dark:border-gray-700 flex items-center justify-between sm:px-6">
         <div class="flex-1 flex justify-between sm:hidden">
            <a href="#" class="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50">Previous</a>
            <a href="#" class="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50">Next</a>
         </div>
         <div class="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
            <div>
               <p class="text-sm text-gray-700 dark:text-gray-400">
                  Showing <span class="font-medium">1</span> to <span class="font-medium">3</span> of <span class="font-medium">124</span> results
               </p>
            </div>
            <div>
               <nav class="relative z-0 inline-flex rounded-md shadow-sm -space-x-px" aria-label="Pagination">
                  <a href="#" class="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50">
                     <span class="sr-only">Previous</span>
                     <svg class="h-5 w-5" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                        <path fill-rule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clip-rule="evenodd" />
                     </svg>
                  </a>
                  <a href="#" class="relative inline-flex items-center px-4 py-2 border border-gray-300 bg-white text-sm font-medium text-gray-700 hover:bg-gray-50">1</a>
                  <a href="#" class="relative inline-flex items-center px-4 py-2 border border-gray-300 bg-white text-sm font-medium text-gray-700 hover:bg-gray-50">2</a>
                  <a href="#" class="relative inline-flex items-center px-4 py-2 border border-gray-300 bg-white text-sm font-medium text-gray-700 hover:bg-gray-50">3</a>
                  <a href="#" class="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50">
                     <span class="sr-only">Next</span>
                     <svg class="h-5 w-5" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                        <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd" />
                     </svg>
                  </a>
               </nav>
            </div>
         </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const vehicles = ref([
    { id: 'V-001', plate: 'WA 12345', type: 'Van (3.5t)', capacity: 1200, volume: 14, driver: 'Jan Kowalski', carrier: 'Danxils Own', status: 'ACTIVE' },
    { id: 'V-002', plate: 'KR 55992', type: 'Truck (12t)', capacity: 8000, volume: 40, driver: 'Marek Nowak', carrier: 'Ext Logistics', status: 'ACTIVE' },
    { id: 'V-003', plate: 'PO 99221', type: 'Van (3.5t)', capacity: 1100, volume: 12, driver: null, carrier: 'Danxils Own', status: 'INACTIVE' }
]);
</script>
