<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
        <h1 class="text-2xl font-bold text-gray-900">Dispatcher Command Center</h1>
        <div class="flex gap-4">
            <button 
                v-if="selectedOrders.size > 0"
                @click="sendToDispatch" 
                class="px-4 py-2 bg-cyan-600 text-white rounded-md hover:bg-cyan-700 flex items-center gap-2">
                <span class="material-icons text-sm">send</span>
                Send to Dispatch ({{ selectedOrders.size }})
            </button>
        </div>
    </div>

    <div class="bg-white rounded-lg shadow overflow-hidden">
      <!-- Filters -->
      <div class="p-4 border-b border-gray-200 flex gap-4">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Search orders (ID, Customer, Address, Tags)..."
          class="flex-1 px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-500"
        />
        <select v-model="statusFilter" class="px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-500">
          <option value="">All Statuses</option>
          <option value="NEW">New / Pending</option>
          <option value="INGESTED">Ingested</option>
          <option value="PLANNED">Planned</option>
          <option value="OUT_FOR_DELIVERY">Out for Delivery</option>
          <option value="DELIVERED">Delivered</option>
          <option value="EXCEPTION">Exception</option>
        </select>
      </div>

      <div v-if="loading" class="text-center py-12">Loading...</div>
      <div v-else-if="filteredOrders.length === 0" class="text-center py-12 text-gray-500">
        No orders found.
      </div>
      <div v-else class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 w-10">
                  <input type="checkbox" :checked="isAllSelected" @change="toggleAll" class="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500">
              </th>
              <th class="w-10 px-6 py-3"></th> <!-- Expand toggle -->
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Order ID</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Date</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Priority</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Customer / Org</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Pickup</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Delivery</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">SLA</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <template v-for="order in filteredOrders" :key="order.orderId">
              <tr class="hover:bg-gray-50 cursor-pointer" @click="toggleRowSelection(order.orderId)">
                <td class="px-6 py-4" @click.stop>
                    <input type="checkbox" :checked="selectedOrders.has(order.orderId)" @change="toggleSelection(order.orderId)" class="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500">
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500" @click.stop="toggleExpand(order.orderId)">
                  <span v-if="expandedOrders.has(order.orderId)">▼</span>
                  <span v-else>▶</span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    <div>{{ order.orderId.substring(0, 8) }}...</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    <div>{{ formatDate(order.pickupTimeFrom) }}</div>
                    <div class="text-xs text-gray-400">Creation: {{ formatDate(order.timestamps?.created) }}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm">
                     <span :class="getPriorityClass(order.priority)" class="px-2 py-0.5 rounded text-xs font-bold">{{ order.priority || 'NORMAL' }}</span>
                </td>
                 <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    <div class="font-medium">{{ order.delivery?.customerName || 'Unknown' }}</div>
                    <div class="text-xs text-gray-500">Org Profile: Standard</div> <!-- Placeholder for now -->
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  <div class="font-medium text-gray-900">{{ order.pickup?.city || '-' }}</div>
                  <div class="text-xs">{{ order.pickup?.street }} {{ order.pickup?.streetNumber }}</div>
                  <div class="text-xs text-blue-600 mt-1">{{ formatShortTime(order.pickupTimeFrom) }} - {{ formatShortTime(order.pickupTimeTo) }}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  <div class="font-medium text-gray-900">{{ order.delivery?.city || '-' }}</div>
                  <div class="text-xs">{{ order.delivery?.street }} {{ order.delivery?.streetNumber }}</div>
                  <div class="text-xs text-blue-600 mt-1">{{ formatShortTime(order.deliveryTimeFrom) }} - {{ formatShortTime(order.deliveryTimeTo) }}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                     <div v-if="order.delivery?.sla" class="text-red-600 font-bold">
                        {{ formatDateTime(order.delivery.sla) }}
                     </div>
                     <div v-else class="text-gray-400">-</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span :class="getStatusClass(order.status)" class="px-2 py-1 text-xs font-semibold rounded-full">
                    {{ order.status }}
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium" @click.stop>
                  <button @click="viewOrder(order.orderId)" class="text-gray-600 hover:text-gray-900 mr-3">View</button>
                </td>
              </tr>
              <!-- Expanded Details -->
              <tr v-if="expandedOrders.has(order.orderId)" class="bg-gray-50">
                <td colspan="10" class="px-6 py-4">
                  <div class="grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <h4 class="font-bold text-gray-700 mb-1">Full Addresses</h4>
                      <p><span class="font-semibold">Pickup:</span> {{ formatAddress(order.pickup) }} ({{ order.pickup?.postalCode }})</p>
                      <p><span class="font-semibold">Delivery:</span> {{ formatAddress(order.delivery) }} ({{ order.delivery?.postalCode }})</p>
                    </div>
                    <div>
                      <h4 class="font-bold text-gray-700 mb-1">Instructions</h4>
                      <p><span class="font-semibold">Remarks:</span> {{ order.remark || '-' }}</p>
                      <p><span class="font-semibold">Delivery Note:</span> {{ order.delivery?.note || '-' }}</p>
                    </div>
                  </div>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
    </div>


  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import orderApi from '../../api/orderApi';
import { planningApi } from '../../api/axios'; // Import customized axios

const router = useRouter();
const loading = ref(true);
const searchQuery = ref('');
const statusFilter = ref('');
const orders = ref([]);
const expandedOrders = ref(new Set());
const selectedOrders = ref(new Set());

const filteredOrders = computed(() => {
  let filtered = orders.value;
  
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    filtered = filtered.filter(order => 
      order.orderId.toLowerCase().includes(query) ||
      (order.delivery?.customerName || '').toLowerCase().includes(query) ||
      (order.pickup?.street || '').toLowerCase().includes(query) ||
      (order.delivery?.street || '').toLowerCase().includes(query) ||
      order.requiredServices?.some(s => s.serviceCode.toLowerCase().includes(query) || s.name.toLowerCase().includes(query))
    );
  }
  
  if (statusFilter.value) {
    filtered = filtered.filter(order => order.status === statusFilter.value);
  }
  
  return filtered;
});

const isAllSelected = computed(() => {
    return filteredOrders.value.length > 0 && selectedOrders.value.size === filteredOrders.value.length;
});

onMounted(async () => {
  await fetchOrders();
});

async function fetchOrders() {
  loading.value = true;
  try {
    const response = await orderApi.getOrders();
    // Use fallback if API returns Page object
    orders.value = response.content || response || []; 
  } catch (error) {
    console.error('Failed to fetch orders:', error);
    orders.value = [];
  } finally {
    loading.value = false;
  }
}



function toggleExpand(id) {
  const newSet = new Set(expandedOrders.value);
  if (newSet.has(id)) {
    newSet.delete(id);
  } else {
    newSet.add(id);
  }
  expandedOrders.value = newSet;
}

function toggleSelection(id) {
    const newSet = new Set(selectedOrders.value);
    if (newSet.has(id)) {
        newSet.delete(id);
    } else {
        newSet.add(id);
    }
    selectedOrders.value = newSet;
}

function toggleRowSelection(id) {
    // Optional: make clicking the row toggle selection (UX choice)
    // toggleSelection(id);
}

function toggleAll() {
    if (isAllSelected.value) {
        selectedOrders.value = new Set();
    } else {
        const newSet = new Set();
        filteredOrders.value.forEach(o => newSet.add(o.orderId));
        selectedOrders.value = newSet;
    }
}

function sendToDispatch() {
    // Store selected order IDs in sessionStorage for DispatchView
    sessionStorage.setItem('pendingOrders', JSON.stringify(Array.from(selectedOrders.value)));
    
    console.log(`📦 Sending ${selectedOrders.value.size} orders to Dispatch view`);
    
    // Navigate to dispatch view
    router.push('/internal/dispatch');
}

function viewOrder(id) {
  router.push(`/internal/orders/${id}`);
}

function getStatusClass(status) {
  const classes = {
    'NEW': 'bg-blue-50 text-blue-700 border border-blue-100',
    'INGESTED': 'bg-gray-100 text-gray-800',
    'PLANNED': 'bg-indigo-100 text-indigo-800',
    'OUT_FOR_DELIVERY': 'bg-yellow-100 text-yellow-800',
    'DELIVERED': 'bg-green-100 text-green-800',
    'EXCEPTION': 'bg-red-100 text-red-800'
  };
  return classes[status] || 'bg-gray-100 text-gray-800';
}

function getPriorityClass(prio) {
    switch(prio) {
        case 'HIGH': return 'bg-red-100 text-red-800';
        case 'LOW': return 'bg-green-100 text-green-800';
        default: return 'bg-gray-100 text-gray-600';
    }
}

function formatShortTime(dateString) {
    if (!dateString) return '--:--';
    return new Date(dateString).toLocaleTimeString('pl-PL', { hour: '2-digit', minute: '2-digit' });
}

function formatAddress(addr) {
    if (!addr) return '-';
    return `${addr.street} ${addr.streetNumber}, ${addr.city}`;
}

function formatDate(isoString) {
    if (!isoString) return '-';
    return new Date(isoString).toLocaleDateString('pl-PL');
}

function formatDateTime(isoString) {
    if (!isoString) return '-';
    return new Date(isoString).toLocaleString('pl-PL', { 
        month: 'short', day: 'numeric', 
        hour: '2-digit', minute: '2-digit' 
    });
}
</script>
