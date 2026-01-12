<script setup>
import { computed } from 'vue';
import { format } from 'date-fns';
import { useVoidDb } from '@/composables/useVoidDb';
import { useAutoAnimateList } from '@/composables/useAutoAnimate';

// RxDB reactive data (replaces manual API calls + mocks)
const { orders, loading, error, syncOrder } = useVoidDb();

// Auto-animate for smooth list transitions
const [tableBodyRef] = useAutoAnimateList();

// Status badge styling
const getStatusColor = (status) => {
  switch (status) {
    case 'NEW': return 'bg-blue-100 text-blue-800';
    case 'PICKUP': return 'bg-yellow-100 text-yellow-800';
    case 'LOAD': return 'bg-purple-100 text-purple-800';
    case 'POD': return 'bg-green-100 text-green-800';
    default: return 'bg-gray-100 text-gray-800';
  }
};

// Computed: Show first 100 orders (performance optimization)
const displayedOrders = computed(() => orders.value.slice(0, 100));
</script>

<template>
  <div>
    <div class="sm:flex sm:items-center">
      <div class="sm:flex-auto">
        <h1 class="text-2xl font-semibold text-gray-900">Orders (Offline-First)</h1>
        <p class="mt-2 text-sm text-gray-700">
          Reactive database powered by RxDB. Changes sync automatically.
          <span v-if="orders.length" class="text-brand-500 font-semibold">{{ orders.length }} orders in local DB</span>
        </p>
      </div>
      <div class="mt-4 sm:mt-0 sm:ml-16 sm:flex-none">
        <button 
          type="button" 
          class="inline-flex items-center justify-center rounded-md border border-transparent bg-indigo-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 sm:w-auto"
          @click="$router.push('/create-order')"
        >
          Create Order
        </button>
      </div>
    </div>

    <!-- Error State -->
    <div v-if="error" class="mt-4 bg-red-50 border border-red-200 rounded-lg p-4">
      <p class="text-sm text-red-800">⚠️ {{ error }}</p>
    </div>
    
    <div class="mt-8 flex flex-col">
      <div class="-my-2 -mx-4 overflow-x-auto sm:-mx-6 lg:-mx-8">
        <div class="inline-block min-w-full py-2 align-middle md:px-6 lg:px-8">
          <div class="overflow-hidden shadow ring-1 ring-black ring-opacity-5 md:rounded-lg">
            <table class="min-w-full divide-y divide-gray-300">
              <thead class="bg-gray-50">
                <tr>
                  <th scope="col" class="py-3.5 pl-4 pr-3 text-left text-sm font-semibold text-gray-900 sm:pl-6">Order ID</th>
                  <th scope="col" class="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">Status</th>
                  <th scope="col" class="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">Pickup</th>
                  <th scope="col" class="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">Delivery</th>
                  <th scope="col" class="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">Driver</th>
                  <th scope="col" class="relative py-3.5 pl-3 pr-4 sm:pr-6">
                    <span class="sr-only">Edit</span>
                  </th>
                </tr>
              </thead>
              <!-- Auto-animated tbody for smooth transitions -->
              <tbody ref="tableBodyRef" class="divide-y divide-gray-200 bg-white">
                <tr v-if="loading && orders.length === 0">
                  <td colspan="6" class="text-center py-8">
                    <div class="flex items-center justify-center">
                      <svg class="animate-spin h-5 w-5 text-indigo-600 mr-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                      </svg>
                      Loading from local database...
                    </div>
                  </td>
                </tr>
                
                <!-- Empty state -->
                <tr v-else-if="orders.length === 0">
                  <td colspan="6" class="text-center py-8 text-gray-500">
                    No orders in local database. Data will sync from backend automatically.
                  </td>
                </tr>

                <!-- Order rows with auto-animate smooth transitions -->
                <tr v-else v-for="order in displayedOrders" :key="order.orderId" class="hover:bg-gray-50 transition-colors">
                  <td class="whitespace-nowrap py-4 pl-4 pr-3 text-sm font-medium text-gray-900 sm:pl-6">
                    {{ order.orderId?.substring(0, 8) }}...
                  </td>
                  <td class="whitespace-nowrap px-3 py-4 text-sm text-gray-500">
                    <span :class="[getStatusColor(order.status), 'inline-flex rounded-full px-2 text-xs font-semibold leading-5']">
                      {{ order.status }}
                    </span>
                  </td>
                  <td class="whitespace-nowrap px-3 py-4 text-sm text-gray-500">
                    {{ order.pickupAddress?.city }}, {{ order.pickupAddress?.street }}
                  </td>
                  <td class="whitespace-nowrap px-3 py-4 text-sm text-gray-500">
                    {{ order.deliveryAddress?.city }}, {{ order.deliveryAddress?.street }}
                    <div v-if="order.deliveryAddress?.sla" class="text-xs text-gray-400">
                      SLA: {{ format(new Date(order.deliveryAddress.sla), 'PP p') }}
                    </div>
                  </td>
                  <td class="whitespace-nowrap px-3 py-4 text-sm text-gray-500">
                    {{ order.assignedDriver || 'Unassigned' }}
                  </td>
                  <td class="relative whitespace-nowrap py-4 pl-3 pr-4 text-right text-sm font-medium sm:pr-6">
                    <a :href="`/orders/${order.orderId}`" class="text-indigo-600 hover:text-indigo-900">View</a>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Smooth transition for row hover */
tr {
  transition: background-color 150ms ease-in-out;
}
</style>
