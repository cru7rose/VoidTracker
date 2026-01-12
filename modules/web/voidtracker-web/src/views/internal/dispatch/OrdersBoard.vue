<template>
  <div class="h-full flex flex-col">
    <div class="flex justify-between mb-4">
      <h2 class="text-xl font-semibold">Orders Management</h2>
      <button class="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700">
        + New Order
      </button>
    </div>
    
    <div class="flex-1 overflow-x-auto">
      <div class="flex gap-6 h-full min-w-max">
        <div v-for="column in columns" :key="column.id" 
             class="w-80 bg-gray-50 rounded-lg p-4 flex flex-col">
          <h3 class="font-medium text-gray-700 mb-3 flex justify-between">
            {{ column.label }}
            <span class="bg-gray-200 text-gray-600 px-2 rounded-full text-sm">
              {{ getOrdersByStatus(column.status).length }}
            </span>
          </h3>
          
          <div class="flex-1 overflow-y-auto space-y-3"
               @dragover.prevent
               @drop="onDrop($event, column.status)">
            <div v-for="order in getOrdersByStatus(column.status)" 
                 :key="order.id"
                 draggable="true"
                 @dragstart="onDragStart($event, order)"
                 class="bg-white p-3 rounded shadow-sm border border-gray-200 cursor-move hover:shadow-md transition-shadow">
              <div class="flex justify-between items-start mb-2">
                <span class="font-bold text-gray-800">#{{ order.id }}</span>
                <span :class="['text-xs px-2 py-1 rounded', getPriorityClass(order.priority)]">
                  {{ order.priority }}
                </span>
              </div>
              <p class="text-sm text-gray-600 mb-1">{{ order.customer }}</p>
              <p class="text-xs text-gray-500">{{ order.address }}</p>
              <div class="mt-2 flex gap-1 flex-wrap">
                <span v-for="tag in order.tags" :key="tag" 
                      class="text-xs bg-blue-100 text-blue-800 px-1.5 py-0.5 rounded">
                  {{ tag }}
                </span>
              </div>
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

const columns = ref([]);
const orders = ref([]);

const fetchConfig = async () => {
  try {
    // Fetch all configs and find the one for Orders Board
    // In a real app, we might query by ID directly
    const res = await planningApi.get('/api/config/views');
    const config = res.data.find(c => c.viewId === 'ORDERS_BOARD_V1');
    
    if (config && config.configJson) {
      const parsed = JSON.parse(config.configJson);
      columns.value = parsed.columns || [];
    } else {
      // Fallback default
      columns.value = [
        { id: 'new', label: 'New', status: 'NEW' },
        { id: 'routing', label: 'Ready for Routing', status: 'ROUTING' },
        { id: 'assigned', label: 'Assigned', status: 'ASSIGNED' },
        { id: 'delivered', label: 'Delivered', status: 'DELIVERED' }
      ];
    }
  } catch (e) {
    console.error("Failed to load view config", e);
    // Fallback default
    columns.value = [
      { id: 'new', label: 'New', status: 'NEW' },
      { id: 'routing', label: 'Ready for Routing', status: 'ROUTING' },
      { id: 'assigned', label: 'Assigned', status: 'ASSIGNED' },
      { id: 'delivered', label: 'Delivered', status: 'DELIVERED' }
    ];
  }
};

const fetchOrders = async () => {
    // Mock orders for now
    orders.value = [
      { id: '101', status: 'NEW', customer: 'Acme Corp', address: 'Warsaw, Center', priority: 'HIGH', tags: ['Frozen'] },
      { id: '102', status: 'NEW', customer: 'Beta Ltd', address: 'Warsaw, South', priority: 'NORMAL', tags: [] },
      { id: '103', status: 'ROUTING', customer: 'Gamma Inc', address: 'Krakow', priority: 'LOW', tags: ['Fragile'] },
      { id: '104', status: 'ASSIGNED', customer: 'Delta Co', address: 'Gdansk', priority: 'HIGH', tags: ['Express'] }
    ];
};

const getOrdersByStatus = (status) => orders.value.filter(o => o.status === status);

const getPriorityClass = (priority) => {
  switch(priority) {
    case 'HIGH': return 'bg-red-100 text-red-800';
    case 'NORMAL': return 'bg-blue-100 text-blue-800';
    case 'LOW': return 'bg-gray-100 text-gray-800';
    default: return 'bg-gray-100 text-gray-800';
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
