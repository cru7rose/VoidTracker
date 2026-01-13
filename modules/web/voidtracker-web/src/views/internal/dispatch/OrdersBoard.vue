<template>
  <div class="h-full flex flex-col bg-white dark:bg-spotify-black text-gray-900 dark:text-white transition-colors">
    <div class="flex justify-between items-center mb-4 px-1">
      <h2 class="text-xl font-bold">Orders Management</h2>
      <button class="spotify-button flex items-center gap-2">
        <span>+</span>
        <span>New Order</span>
      </button>
    </div>
    
    <div class="flex-1 overflow-x-auto">
      <div class="flex gap-6 h-full min-w-max pb-4">
        <div 
          v-for="column in columns" 
          :key="column.id" 
          class="w-80 spotify-card rounded-lg p-4 flex flex-col min-h-[400px]"
        >
          <h3 class="font-semibold text-gray-900 dark:text-white mb-3 flex justify-between items-center">
            <span>{{ column.label }}</span>
            <span class="bg-gray-200 dark:bg-spotify-gray-800 text-gray-700 dark:text-spotify-gray-300 px-3 py-1 rounded-full text-sm font-semibold">
              {{ getOrdersByStatus(column.status).length }}
            </span>
          </h3>
          
          <div 
            class="flex-1 overflow-y-auto space-y-3"
            @dragover.prevent
            @drop="onDrop($event, column.status)"
          >
            <div 
              v-for="order in getOrdersByStatus(column.status)" 
              :key="order.id"
              draggable="true"
              @dragstart="onDragStart($event, order)"
              class="bg-white dark:bg-spotify-darker p-4 rounded-lg shadow-sm border border-gray-200 dark:border-spotify-gray-800 cursor-move hover:shadow-md hover:border-spotify-green-400 transition-all"
            >
              <div class="flex justify-between items-start mb-2">
                <span class="font-bold text-gray-900 dark:text-white">#{{ order.id }}</span>
                <span :class="['text-xs px-2 py-1 rounded font-semibold', getPriorityClass(order.priority)]">
                  {{ order.priority }}
                </span>
              </div>
              <p class="text-sm text-gray-700 dark:text-spotify-gray-300 mb-1 font-medium">{{ order.customer }}</p>
              <p class="text-xs text-gray-600 dark:text-spotify-gray-400">{{ order.address }}</p>
              <div class="mt-2 flex gap-1 flex-wrap">
                <span 
                  v-for="tag in order.tags" 
                  :key="tag" 
                  class="text-xs bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-300 px-2 py-0.5 rounded"
                >
                  {{ tag }}
                </span>
              </div>
            </div>
            
            <div 
              v-if="getOrdersByStatus(column.status).length === 0"
              class="text-center py-8 text-gray-400 dark:text-spotify-gray-600 text-sm"
            >
              No orders
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { planningApi } from '../../../api/axios';
import orderApi from '../../../api/orderApi';

const columns = ref([]);
const orders = ref([]);

const fetchConfig = async () => {
  try {
    const res = await planningApi.get('/config/views');
    const config = res.data.find(c => c.viewId === 'ORDERS_BOARD_V1');
    
    if (config && config.configJson) {
      const parsed = JSON.parse(config.configJson);
      columns.value = parsed.columns || [];
    } else {
      columns.value = [
        { id: 'new', label: 'New', status: 'NEW' },
        { id: 'routing', label: 'Ready for Routing', status: 'ROUTING' },
        { id: 'assigned', label: 'Assigned', status: 'ASSIGNED' },
        { id: 'delivered', label: 'Delivered', status: 'DELIVERED' }
      ];
    }
  } catch (e) {
    console.error("Failed to load view config", e);
    columns.value = [
      { id: 'new', label: 'New', status: 'NEW' },
      { id: 'routing', label: 'Ready for Routing', status: 'ROUTING' },
      { id: 'assigned', label: 'Assigned', status: 'ASSIGNED' },
      { id: 'delivered', label: 'Delivered', status: 'DELIVERED' }
    ];
  }
};

const fetchOrders = async () => {
  try {
    // Fetch orders from order-service API
    const response = await orderApi.getOrders();
    const ordersData = response.content || response || [];
    
    // Map API response to component format
    orders.value = ordersData.map(order => ({
      id: order.orderId || order.id,
      status: order.status || 'NEW',
      customer: order.delivery?.customerName || order.client?.name || 'Unknown',
      address: formatAddress(order.delivery) || formatAddress(order.pickup) || 'No address',
      priority: order.priority || 'NORMAL',
      tags: order.requiredServices?.map(s => s.serviceCode || s.name) || []
    }));
  } catch (error) {
    console.error('Failed to fetch orders:', error);
    orders.value = [];
  }
};

const formatAddress = (address) => {
  if (!address) return '';
  const parts = [];
  if (address.street) parts.push(address.street);
  if (address.streetNumber) parts.push(address.streetNumber);
  if (address.city) parts.push(address.city);
  return parts.join(', ') || '';
};

const getOrdersByStatus = (status) => orders.value.filter(o => o.status === status);

const getPriorityClass = (priority) => {
  switch(priority) {
    case 'HIGH': return 'bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-300 border border-red-300 dark:border-red-700';
    case 'NORMAL': return 'bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-300 border border-blue-300 dark:border-blue-700';
    case 'LOW': return 'bg-gray-100 dark:bg-spotify-gray-800 text-gray-800 dark:text-spotify-gray-300 border border-gray-300 dark:border-spotify-gray-700';
    default: return 'bg-gray-100 dark:bg-spotify-gray-800 text-gray-800 dark:text-spotify-gray-300 border border-gray-300 dark:border-spotify-gray-700';
  }
};

const onDragStart = (event, order) => {
  event.dataTransfer.dropEffect = 'move';
  event.dataTransfer.effectAllowed = 'move';
  event.dataTransfer.setData('orderId', order.id);
};

const onDrop = (event, newStatus) => {
  const orderId = event.dataTransfer.getData('orderId');
  const order = orders.value.find(o => o.id === orderId);
  if (order) {
    order.status = newStatus;
  }
};

onMounted(async () => {
    await fetchConfig();
    await fetchOrders();
});
</script>
