<template>
  <div class="min-h-screen bg-white dark:bg-spotify-black text-gray-900 dark:text-white p-6 transition-colors">
    <!-- Header -->
    <div class="mb-6 flex justify-between items-center">
      <div>
        <h1 class="text-3xl font-bold mb-2">
          Dispatcher Command Center
        </h1>
        <p class="text-gray-500 dark:text-spotify-gray-400 text-sm">Order Management System</p>
      </div>
      <div class="flex gap-4">
        <button 
          v-if="selectedOrders.size > 0"
          @click="sendToDispatch" 
          class="spotify-button flex items-center gap-2"
        >
          <span>📤</span>
          <span>Send to Dispatch ({{ selectedOrders.size }})</span>
        </button>
      </div>
    </div>

    <!-- Main Panel -->
    <div class="spotify-card rounded-xl overflow-hidden">
      <!-- Filters -->
      <div class="p-4 border-b border-gray-200 dark:border-spotify-gray-800 flex gap-4 bg-gray-50 dark:bg-spotify-darker">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Search orders (ID, Customer, Address, Tags)..."
          class="spotify-input flex-1"
        />
        <select 
          v-model="statusFilter" 
          class="spotify-input"
        >
          <option value="">All Statuses</option>
          <option value="NEW">New / Pending</option>
          <option value="INGESTED">Ingested</option>
          <option value="PLANNED">Planned</option>
          <option value="OUT_FOR_DELIVERY">Out for Delivery</option>
          <option value="DELIVERED">Delivered</option>
          <option value="EXCEPTION">Exception</option>
        </select>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="text-center py-12">
        <div class="inline-flex items-center gap-3 text-spotify-gray-500">
          <div class="w-6 h-6 border-2 border-spotify-green-400 border-t-transparent rounded-full animate-spin"></div>
          <span>Loading orders...</span>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else-if="filteredOrders.length === 0" class="text-center py-12 text-spotify-gray-500">
        <div class="text-6xl mb-4">📦</div>
        <p class="text-lg">No orders found.</p>
      </div>

      <!-- Orders Table -->
      <div v-else class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200 dark:divide-spotify-gray-800">
          <thead class="bg-gray-50 dark:bg-spotify-darker">
            <tr>
              <th class="px-6 py-3 w-10">
                <input 
                  type="checkbox" 
                  :checked="isAllSelected" 
                  @change="toggleAll" 
                  class="rounded border-gray-300 dark:border-spotify-gray-700 bg-white dark:bg-spotify-darker text-spotify-green-400 focus:ring-spotify-green-500"
                >
              </th>
              <th class="w-10 px-6 py-3"></th>
              <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-spotify-gray-400 uppercase tracking-wider">Order ID</th>
              <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-spotify-gray-400 uppercase tracking-wider">Date</th>
              <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-spotify-gray-400 uppercase tracking-wider">Priority</th>
              <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-spotify-gray-400 uppercase tracking-wider">Customer / Org</th>
              <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-spotify-gray-400 uppercase tracking-wider">Pickup</th>
              <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-spotify-gray-400 uppercase tracking-wider">Delivery</th>
              <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-spotify-gray-400 uppercase tracking-wider">SLA</th>
              <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-spotify-gray-400 uppercase tracking-wider">Status</th>
              <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-spotify-gray-400 uppercase tracking-wider">Actions</th>
            </tr>
          </thead>
          <tbody class="bg-white dark:bg-spotify-black divide-y divide-gray-200 dark:divide-spotify-gray-800">
            <template v-for="order in filteredOrders" :key="order.orderId">
              <tr 
                class="hover:bg-gray-50 dark:hover:bg-spotify-darker cursor-pointer transition-colors border-l-2 border-transparent hover:border-spotify-green-400" 
                @click="toggleRowSelection(order.orderId)"
              >
                <td class="px-6 py-4" @click.stop>
                  <input 
                    type="checkbox" 
                    :checked="selectedOrders.has(order.orderId)" 
                    @change="toggleSelection(order.orderId)" 
                    class="rounded border-gray-300 dark:border-spotify-gray-700 bg-white dark:bg-spotify-darker text-spotify-green-400 focus:ring-spotify-green-500"
                  >
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-spotify-gray-500" @click.stop="toggleExpand(order.orderId)">
                  <span v-if="expandedOrders.has(order.orderId)">▼</span>
                  <span v-else>▶</span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-semibold text-gray-900 dark:text-white">
                  <div class="text-spotify-green-400">{{ order.orderId.substring(0, 8) }}...</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-700 dark:text-spotify-gray-300">
                  <div>{{ formatDate(order.pickupTimeFrom) }}</div>
                  <div class="text-xs text-gray-500 dark:text-spotify-gray-500">Creation: {{ formatDate(order.timestamps?.created) }}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm">
                  <span :class="getPriorityClass(order.priority)" class="px-2 py-0.5 rounded text-xs font-semibold">
                    {{ order.priority || 'NORMAL' }}
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                  <div class="font-medium">{{ order.delivery?.customerName || 'Unknown' }}</div>
                  <div class="text-xs text-gray-500 dark:text-spotify-gray-500">Org Profile: Standard</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-700 dark:text-spotify-gray-300">
                  <div class="font-medium text-gray-900 dark:text-white">{{ order.pickup?.city || '-' }}</div>
                  <div class="text-xs">{{ order.pickup?.street }} {{ order.pickup?.streetNumber }}</div>
                  <div class="text-xs text-gray-500 dark:text-spotify-gray-500 mt-1">{{ formatShortTime(order.pickupTimeFrom) }} - {{ formatShortTime(order.pickupTimeTo) }}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-700 dark:text-spotify-gray-300">
                  <div class="font-medium text-gray-900 dark:text-white">{{ order.delivery?.city || '-' }}</div>
                  <div class="text-xs">{{ order.delivery?.street }} {{ order.delivery?.streetNumber }}</div>
                  <div class="text-xs text-gray-500 dark:text-spotify-gray-500 mt-1">{{ formatShortTime(order.deliveryTimeFrom) }} - {{ formatShortTime(order.deliveryTimeTo) }}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-700 dark:text-spotify-gray-300">
                  <div v-if="order.delivery?.sla" class="text-red-500 dark:text-red-400 font-semibold">
                    {{ formatDateTime(order.delivery.sla) }}
                  </div>
                  <div v-else class="text-gray-400 dark:text-spotify-gray-600">-</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span :class="getStatusClass(order.status)" class="px-2 py-1 text-xs font-semibold rounded-full">
                    {{ order.status }}
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium" @click.stop>
                  <button 
                    @click="viewOrder(order.orderId)" 
                    class="text-spotify-green-400 hover:text-spotify-green-500 transition-colors"
                  >
                    View
                  </button>
                </td>
              </tr>
              <!-- Expanded Details -->
              <tr v-if="expandedOrders.has(order.orderId)" class="bg-gray-50 dark:bg-spotify-darker">
                <td colspan="11" class="px-6 py-4">
                  <div class="grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <h4 class="font-semibold text-gray-900 dark:text-white mb-2 uppercase tracking-wider">Full Addresses</h4>
                      <p class="text-gray-700 dark:text-spotify-gray-300 mb-1"><span class="font-semibold text-gray-900 dark:text-white">Pickup:</span> {{ formatAddress(order.pickup) }} ({{ order.pickup?.postalCode }})</p>
                      <p class="text-gray-700 dark:text-spotify-gray-300"><span class="font-semibold text-gray-900 dark:text-white">Delivery:</span> {{ formatAddress(order.delivery) }} ({{ order.delivery?.postalCode }})</p>
                    </div>
                    <div>
                      <h4 class="font-semibold text-gray-900 dark:text-white mb-2 uppercase tracking-wider">Instructions</h4>
                      <p class="text-gray-700 dark:text-spotify-gray-300 mb-1"><span class="font-semibold text-gray-900 dark:text-white">Remarks:</span> {{ order.remark || '-' }}</p>
                      <p class="text-gray-700 dark:text-spotify-gray-300"><span class="font-semibold text-gray-900 dark:text-white">Delivery Note:</span> {{ order.delivery?.note || '-' }}</p>
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
import { useThemeStore } from '../../stores/themeStore';
import orderApi from '../../api/orderApi';

const router = useRouter();
const themeStore = useThemeStore();
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
    sessionStorage.setItem('pendingOrders', JSON.stringify(Array.from(selectedOrders.value)));
    console.log(`📦 Sending ${selectedOrders.value.size} orders to Dispatch view`);
    router.push('/internal/dispatch');
}

function viewOrder(id) {
  router.push(`/internal/orders/${id}`);
}

function getStatusClass(status) {
  const classes = {
    'NEW': 'bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-300 border border-blue-300 dark:border-blue-700',
    'INGESTED': 'bg-gray-100 dark:bg-spotify-gray-800 text-gray-800 dark:text-spotify-gray-300 border border-gray-300 dark:border-spotify-gray-700',
    'PLANNED': 'bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300 border border-green-300 dark:border-green-700',
    'OUT_FOR_DELIVERY': 'bg-yellow-100 dark:bg-yellow-900/30 text-yellow-800 dark:text-yellow-300 border border-yellow-300 dark:border-yellow-700',
    'DELIVERED': 'bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300 border border-green-300 dark:border-green-700',
    'EXCEPTION': 'bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-300 border border-red-300 dark:border-red-700'
  };
  return classes[status] || 'bg-gray-100 dark:bg-spotify-gray-800 text-gray-800 dark:text-spotify-gray-300 border border-gray-300 dark:border-spotify-gray-700';
}

function getPriorityClass(prio) {
    switch(prio) {
        case 'HIGH': return 'bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-300 border border-red-300 dark:border-red-700';
        case 'LOW': return 'bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300 border border-green-300 dark:border-green-700';
        default: return 'bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-300 border border-blue-300 dark:border-blue-700';
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
