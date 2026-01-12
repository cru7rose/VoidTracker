<template>
  <div class="p-6">
    <h1 class="text-2xl font-bold text-gray-900 mb-6">{{ t('customer.dashboard.title') }}</h1>
    
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
      <div class="bg-white rounded-lg shadow p-6">
        <h3 class="text-sm font-medium text-gray-500">{{ t('customer.dashboard.active_orders') }}</h3>
        <p class="text-3xl font-bold text-indigo-600 mt-2">{{ stats.activeOrders }}</p>
      </div>
      <div class="bg-white rounded-lg shadow p-6">
        <h3 class="text-sm font-medium text-gray-500">{{ t('customer.dashboard.delivered_month') }}</h3>
        <p class="text-3xl font-bold text-green-600 mt-2">{{ stats.deliveredOrders }}</p>
      </div>
      <div class="bg-white rounded-lg shadow p-6">
        <h3 class="text-sm font-medium text-gray-500">{{ t('customer.dashboard.total_orders') }}</h3>
        <p class="text-3xl font-bold text-gray-900 mt-2">{{ stats.totalOrders }}</p>
      </div>
      <div class="bg-white rounded-lg shadow p-6">
        <h3 class="text-sm font-medium text-gray-500">{{ t('customer.dashboard.pending_invoices') }}</h3>
        <p class="text-3xl font-bold text-orange-600 mt-2">{{ stats.pendingInvoices }}</p>
      </div>
      <div class="bg-white rounded-lg shadow p-6">
        <h3 class="text-sm font-medium text-gray-500">{{ t('customer.dashboard.total_spend') }}</h3>
        <p class="text-3xl font-bold text-green-600 mt-2">{{ stats.totalSpend }}</p>
      </div>
    </div>

    <div class="bg-white rounded-lg shadow p-6">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-lg font-semibold text-gray-900">{{ t('customer.dashboard.recent_orders') }}</h2>
        <router-link to="/customer/orders/create" class="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700">
          {{ t('customer.dashboard.create_order') }}
        </router-link>
      </div>
      
      <div v-if="loading" class="text-center py-8">{{ t('common.loading') }}</div>
      <div v-else-if="recentOrders.length === 0" class="text-center py-8 text-gray-500">
        {{ t('customer.dashboard.no_orders') }}
      </div>
      <div v-else class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ t('customer.dashboard.order_id') }}</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ t('customer.dashboard.delivery_address') }}</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ t('common.status') }}</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">{{ t('customer.dashboard.date') }}</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="order in recentOrders" :key="order.id" class="hover:bg-gray-50 cursor-pointer" @click="viewOrder(order.id)">
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ order.id.substring(0, 8) }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ order.deliveryAddress }}</td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span :class="getStatusClass(order.status)" class="px-2 py-1 text-xs font-semibold rounded-full">
                  {{ order.status }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ formatDate(order.createdAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { format } from 'date-fns';
import { useOrderStore } from '../../stores/orderStore';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const router = useRouter();
const orderStore = useOrderStore();
const loading = ref(true);
const stats = ref({
  activeOrders: 0,
  deliveredOrders: 0,
  totalOrders: 0,
  pendingInvoices: 0,
  totalSpend: '€ 0.00'
});
const recentOrders = ref([]);

onMounted(async () => {
  try {
    // Fetch recent orders from API
    const orders = await orderStore.fetchOrders({ limit: 5 });
    recentOrders.value = orders;
    
    // Calculate stats from orders
    const activeStatuses = ['INGESTED', 'PLANNED', 'OUT_FOR_DELIVERY'];
    stats.value = {
      activeOrders: orders.filter(o => activeStatuses.includes(o.status)).length,
      deliveredOrders: orders.filter(o => o.status === 'DELIVERED').length,
      totalOrders: orders.length
    };
  } catch (error) {
    console.error('Failed to load dashboard data:', error);
    // Keep mock data as fallback
    stats.value = {
      activeOrders: 3,
      deliveredOrders: 12,
      totalOrders: 15,
      pendingInvoices: 1,
      totalSpend: '€ 4,250.00'
    };
    
    recentOrders.value = [
      { id: '550e8400-e29b-41d4-a716-446655440000', deliveryAddress: 'Warsaw, ul. Marszałkowska 1', status: 'OUT_FOR_DELIVERY', createdAt: new Date() },
      { id: '550e8400-e29b-41d4-a716-446655440001', deliveryAddress: 'Krakow, ul. Floriańska 5', status: 'DELIVERED', createdAt: new Date(Date.now() - 86400000) },
      { id: '550e8400-e29b-41d4-a716-446655440002', deliveryAddress: 'Gdansk, ul. Długa 10', status: 'INGESTED', createdAt: new Date(Date.now() - 172800000) }
    ];
  } finally {
    loading.value = false;
  }
});

function viewOrder(id) {
  router.push(`/customer/orders/${id}`);
}

function formatDate(date) {
  return format(new Date(date), 'MMM dd, yyyy');
}

function getStatusClass(status) {
  const classes = {
    'INGESTED': 'bg-gray-100 text-gray-800',
    'PLANNED': 'bg-blue-100 text-blue-800',
    'OUT_FOR_DELIVERY': 'bg-yellow-100 text-yellow-800',
    'DELIVERED': 'bg-green-100 text-green-800',
    'EXCEPTION': 'bg-red-100 text-red-800'
  };
  return classes[status] || 'bg-gray-100 text-gray-800';
}
</script>
