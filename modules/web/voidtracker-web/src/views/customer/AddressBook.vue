<template>
  <div class="p-6">
    <div class="mb-6 flex justify-between items-center">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Address Book</h1>
        <p class="text-gray-500 dark:text-gray-400">Manage your frequent pickup and delivery locations.</p>
      </div>
      <button @click="showModal = true" class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg shadow-sm">
        + Add New Address
      </button>
    </div>

    <!-- Address Groups -->
    <div class="space-y-8">
      <div v-for="(group, customerName) in groupedAddresses" :key="customerName">
        <h2 class="text-xl font-semibold text-gray-800 mb-4 border-b pb-2">{{ customerName }}</h2>
        
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div v-for="addr in group" :key="addr.id" class="bg-white dark:bg-gray-800 p-6 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 relative group">
            <div class="absolute top-4 right-4 opacity-0 group-hover:opacity-100 transition-opacity">
              <button @click="deleteAddress(addr.id)" class="text-red-500 hover:text-red-700">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" /></svg>
              </button>
            </div>
            
            <div class="flex items-center gap-3 mb-3">
              <span class="p-2 bg-blue-50 dark:bg-blue-900/20 rounded-lg text-blue-600 dark:text-blue-400">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" /><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" /></svg>
              </span>
              <h3 class="font-semibold text-gray-900 dark:text-white">{{ addr.alias }}</h3>
            </div>
            
            <p class="text-gray-600 dark:text-gray-300 text-sm mb-1">{{ addr.street }}</p>
            <p class="text-gray-600 dark:text-gray-300 text-sm">{{ addr.city }}, {{ addr.zip }}</p>
            <p class="text-gray-500 text-xs mt-3">{{ addr.country }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Add Modal -->
    <div v-if="showModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div class="bg-white dark:bg-gray-800 rounded-xl shadow-xl max-w-md w-full p-6">
        <h2 class="text-xl font-bold mb-4 text-gray-900 dark:text-white">Add New Address</h2>
        <form @submit.prevent="addAddress" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Customer Name (Group)</label>
            <input v-model="newAddress.customerName" type="text" required class="mt-1 block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm focus:border-blue-500 focus:ring-blue-500" placeholder="e.g. Acme Corp" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Alias (Short Name)</label>
            <input v-model="newAddress.alias" type="text" required class="mt-1 block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm focus:border-blue-500 focus:ring-blue-500" placeholder="e.g. Warehouse A" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Street Address</label>
            <input v-model="newAddress.street" type="text" required class="mt-1 block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm" />
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">City</label>
              <input v-model="newAddress.city" type="text" required class="mt-1 block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">ZIP Code</label>
              <input v-model="newAddress.zip" type="text" required class="mt-1 block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-700 shadow-sm" />
            </div>
          </div>
          
          <div class="flex justify-end gap-3 mt-6">
            <button type="button" @click="showModal = false" class="px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg">Cancel</button>
            <button type="submit" class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg">Save Address</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const showModal = ref(false);
const addresses = ref([
  { id: 1, customerName: 'Acme Corp', alias: 'Main Warehouse', street: '123 Logistics Way', city: 'Warsaw', zip: '00-001', country: 'Poland' },
  { id: 2, customerName: 'Acme Corp', alias: 'Downtown Store', street: '45 Market St', city: 'Krakow', zip: '30-001', country: 'Poland' },
  { id: 3, customerName: 'Globex Inc', alias: 'Distribution Center', street: '789 Industrial Blvd', city: 'Gdansk', zip: '80-001', country: 'Poland' }
]);

const groupedAddresses = computed(() => {
  return addresses.value.reduce((groups, addr) => {
    const key = addr.customerName || 'Other';
    if (!groups[key]) {
      groups[key] = [];
    }
    groups[key].push(addr);
    return groups;
  }, {});
});

const newAddress = ref({ customerName: '', alias: '', street: '', city: '', zip: '', country: 'Poland' });

const addAddress = () => {
  addresses.value.push({ ...newAddress.value, id: Date.now() });
  showModal.value = false;
  newAddress.value = { customerName: '', alias: '', street: '', city: '', zip: '', country: 'Poland' };
};

const deleteAddress = (id) => {
  if (confirm('Are you sure?')) {
    addresses.value = addresses.value.filter(a => a.id !== id);
  }
};
</script>
