<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-900">{{ t('customer.orders.title') }}</h1>
      <router-link to="/customer/orders/create" class="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700">
        {{ t('customer.orders.create_new') }}
      </router-link>
    </div>

    <div class="bg-white rounded-lg shadow overflow-hidden">
      <div class="p-4 border-b border-gray-200 flex flex-col sm:flex-row gap-4">
        <input
          v-model="searchQuery"
          type="text"
          :placeholder="t('customer.orders.search_placeholder')"
          class="flex-1 px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
        />
        <select
          v-model="statusFilter"
          class="px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
        >
          <option value="">{{ t('customer.orders.all_statuses') }}</option>
          <option value="NEW">{{ t('customer.orders.status.NEW') }}</option>
          <option value="INGESTED">{{ t('customer.orders.status.INGESTED') }}</option>
          <option value="PLANNED">{{ t('customer.orders.status.PLANNED') }}</option>
          <option value="PICKUP">{{ t('customer.orders.status.PICKUP') }}</option>
          <option value="OUT_FOR_DELIVERY">{{ t('customer.orders.status.OUT_FOR_DELIVERY') }}</option>
          <option value="DELIVERED">{{ t('customer.orders.status.DELIVERED') }}</option>
          <option value="EXCEPTION">{{ t('customer.orders.status.EXCEPTION') }}</option>
        </select>
        <select
          v-if="availableDepartments.length > 0"
          v-model="departmentFilter"
          class="px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
        >
          <option value="">{{ t('customer.orders.all_departments') }}</option>
          <option v-for="dept in availableDepartments" :key="dept" :value="dept">{{ dept }}</option>
        </select>
      </div>

      <div v-if="orderStore.loading" class="text-center py-12">Loading...</div>
      <div v-else-if="filteredOrders.length === 0" class="text-center py-12 text-gray-500">
        No orders found.
      </div>
      <div v-else class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ t('customer.dashboard.order_id') }}</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ t('customer.orders.pickup') }}</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ t('customer.orders.delivery') }}</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ t('customer.orders.dept') }}</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ t('customer.orders.type') }}</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ t('customer.orders.time_windows') }}</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ t('customer.orders.services') }}</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ t('common.status') }}</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ t('common.actions') }}</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="order in filteredOrders" :key="order.orderId" class="hover:bg-gray-50">
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900" :title="order.orderId">
                {{ order.orderId.substring(0, 8) }}...
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                <div class="font-medium">{{ order.pickupAddress?.city }}</div>
                <div class="text-xs text-gray-400">{{ order.pickupAddress?.street }}</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                <div class="font-medium">{{ order.deliveryAddress?.city }}</div>
                <div class="text-xs text-gray-400">{{ order.deliveryAddress?.street }}</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {{ order.department || '-' }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {{ order.deliveryType || 'Standard' }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-xs text-gray-500">
                <div v-if="order.pickupTimeWindow">
                  <span class="font-semibold">P:</span> {{ formatTimeWindow(order.pickupTimeWindow) }}
                </div>
                <div v-if="order.deliveryTimeWindow">
                  <span class="font-semibold">D:</span> {{ formatTimeWindow(order.deliveryTimeWindow) }}
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-xs text-gray-500">
                 <div class="flex flex-wrap gap-1">
                    <span v-for="service in order.requiredServices" :key="service" class="px-1.5 py-0.5 bg-gray-100 rounded text-gray-600 border border-gray-200">
                      {{ service }}
                    </span>
                 </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span :class="getStatusClass(order.status)" class="px-2 py-1 text-xs font-semibold rounded-full">
                  {{ order.status }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                <button @click="viewOrder(order.orderId)" class="text-indigo-600 hover:text-indigo-900">{{ t('common.view') }}</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { format } from 'date-fns';
import { useOrderStore } from '../../stores/orderStore';
import { useAuthStore } from '../../stores/authStore';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const router = useRouter();
const orderStore = useOrderStore();
const authStore = useAuthStore();
const searchQuery = ref('');
const statusFilter = ref('');
const departmentFilter = ref('');

const availableDepartments = computed(() => {
  const config = authStore.user?.organizationConfig || {};
  if (config.department && config.department !== '*') {
    return []; // User is restricted to one department, no filter needed
  }
  // Mock available departments for Master Profile
  return ['Sales', 'Logistics', 'Marketing', 'IT']; 
});

const filteredOrders = computed(() => {
  let result = orderStore.orders;

  if (statusFilter.value) {
    result = result.filter(order => order.status === statusFilter.value);
  }

  if (departmentFilter.value) {
    result = result.filter(order => order.department === departmentFilter.value);
  }

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    result = result.filter(order => 
      order.orderId.toLowerCase().includes(query) ||
      order.pickupAddress?.city.toLowerCase().includes(query) ||
      order.deliveryAddress?.city.toLowerCase().includes(query) ||
      order.pickupAddress?.street.toLowerCase().includes(query) ||
      order.deliveryAddress?.street.toLowerCase().includes(query)
    );
  }
  
  return result;
});

onMounted(async () => {
  await orderStore.fetchOrders();
});

function viewOrder(id) {
  router.push(`/customer/orders/${id}`);
}

function formatTimeWindow(window) {
    if (!window) return '-';
    // Assuming window is "HH:mm-HH:mm" or similar string, or an object. 
    // Based on previous edits, it might be an object with start/end or a string.
    // Let's assume it's a string for now as per DTO, or handle object if needed.
    // If it's a string like "09:00-12:00", just return it.
    return window; 
}

function getStatusClass(status) {
  const classes = {
    'NEW': 'bg-gray-100 text-gray-800',
    'INGESTED': 'bg-purple-100 text-purple-800',
    'PLANNED': 'bg-blue-100 text-blue-800',
    'PICKUP': 'bg-indigo-100 text-indigo-800',
    'OUT_FOR_DELIVERY': 'bg-yellow-100 text-yellow-800',
    'DELIVERED': 'bg-green-100 text-green-800',
    'EXCEPTION': 'bg-red-100 text-red-800',
    'CANCELLED': 'bg-red-200 text-red-900'
  };
  return classes[status] || 'bg-gray-100 text-gray-800';
}
</script>
