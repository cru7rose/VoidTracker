<template>
  <div class="p-6">
    <div class="mb-6">
      <button @click="$router.back()" class="text-gray-600 hover:text-gray-900 flex items-center gap-2">
        ← Back to Orders
      </button>
    </div>

    <div v-if="loading" class="text-center py-12">Loading...</div>
    <div v-else-if="error" class="text-center py-12 text-red-500">{{ error }}</div>
    <div v-else-if="!order" class="text-center py-12 text-gray-500">Order not found.</div>
    <div v-else class="space-y-6">
      <!-- Top Stats Bar -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div class="bg-white rounded-lg shadow p-4">
          <div class="text-xs text-gray-500 uppercase">Total Cost</div>
          <div class="text-xl font-bold">{{ order.estimatedCost || 0 }} {{ order.currency }}</div>
        </div>
        <div class="bg-white rounded-lg shadow p-4">
          <div class="text-xs text-gray-500 uppercase">Legacy ID</div>
          <div class="text-lg font-mono">{{ order.legacyId || '-' }}</div>
        </div>
        <div class="bg-white rounded-lg shadow p-4">
            <div class="text-xs text-gray-500 uppercase">Cost Center</div>
            <div class="text-lg font-mono">{{ order.costCenter || '-' }}</div>
        </div>
        <div class="bg-white rounded-lg shadow p-4">
            <div class="text-xs text-gray-500 uppercase">External Ref</div>
            <div class="text-lg font-mono">{{ order.externalReference || '-' }}</div>
        </div>
      </div>

      <!-- Main Layout -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        <!-- Left Column (2 spans) -->
        <div class="lg:col-span-2 space-y-6">
            <!-- Header -->
            <div class="bg-white rounded-lg shadow p-6">
                <!-- ... header content ... -->
                <div class="flex justify-between items-start mb-6">
                <div>
                    <h1 class="text-2xl font-bold text-gray-900">Order Details</h1>
                    <div class="flex gap-2 items-center mt-1 text-gray-500">
                        <span class="font-mono text-sm">{{ order.orderNumber }}</span>
                        <span>•</span>
                        <span class="text-sm">ID: {{ order.orderId }}</span>
                    </div>
                </div>
                <div class="flex gap-2">
                    <span :class="getStatusClass(order.status)" class="px-3 py-1 text-sm font-semibold rounded-full">
                    {{ order.status }}
                    </span>
                    <button v-if="order.status === 'NEW'" @click="assignDriver" class="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700">
                    Assign Driver
                    </button>
                </div>
                </div>

                <!-- Tags / Services -->
                <div v-if="order.requiredServices && order.requiredServices.length > 0" class="mb-6">
                <h3 class="text-sm font-medium text-gray-500 mb-2">Required Services</h3>
                <div class="flex flex-wrap gap-2">
                    <span v-for="service in order.requiredServices" :key="service.serviceCode"
                    class="px-2 py-1 bg-purple-100 text-purple-800 text-xs font-medium rounded-md border border-purple-200">
                    {{ service.name }}
                    </span>
                </div>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <!-- Pickup Info -->
                <div>
                    <h3 class="text-sm font-medium text-gray-500 mb-2">Pickup Information</h3>
                    <div class="bg-gray-50 rounded-lg p-4 h-full relative">
                        <div class="absolute top-4 right-4 text-gray-400">
                             <span class="material-icons text-sm">store</span>
                        </div>
                        <p class="font-medium text-gray-900">{{ order.pickupAddress?.city || 'Unknown City' }}</p>
                        <p class="text-sm text-gray-600">{{ order.pickupAddress?.street || '-' }} {{ order.pickupAddress?.streetNumber }}</p>
                        <p class="text-sm text-gray-600">{{ order.pickupAddress?.postalCode }}</p>
                        
                        <div class="mt-4 pt-4 border-t border-gray-200">
                            <p class="text-xs text-gray-500 uppercase tracking-wide">Pickup Window</p>
                            <p class="text-sm font-medium text-gray-900">
                            {{ formatTimeWindow(order.pickupTimeFrom, order.pickupTimeTo) }}
                            </p>
                        </div>
                         <div v-if="order.pickupInstructions" class="mt-2 text-xs text-gray-600 italic">
                             "{{ order.pickupInstructions }}"
                         </div>
                    </div>
                </div>

                <!-- Delivery Info -->
                <div>
                    <h3 class="text-sm font-medium text-gray-500 mb-2">Delivery Information</h3>
                    <div class="bg-gray-50 rounded-lg p-4 h-full relative">
                        <div class="absolute top-4 right-4 text-gray-400">
                             <span class="material-icons text-sm">local_shipping</span>
                        </div>
                        <p class="font-medium text-gray-900">{{ order.deliveryAddress?.city || 'Unknown City' }}</p>
                        <p class="text-sm text-gray-600">{{ order.deliveryAddress?.street || '-' }} {{ order.deliveryAddress?.streetNumber }}</p>
                        <p class="text-sm text-gray-600">{{ order.deliveryAddress?.postalCode }}</p>
                        
                        <div class="mt-4 pt-4 border-t border-gray-200 grid grid-cols-2 gap-4">
                            <div>
                            <p class="text-xs text-gray-500 uppercase tracking-wide">Delivery Window</p>
                            <p class="text-sm font-medium text-gray-900">
                                {{ formatTimeWindow(order.deliveryTimeFrom, order.deliveryTimeTo) }}
                            </p>
                            </div>
                            <div>
                            <p class="text-xs text-gray-500 uppercase tracking-wide">SLA Deadline</p>
                            <p class="text-sm font-medium" :class="getSlaClass(order.slaDeadline)">
                                {{ formatDate(order.slaDeadline) }}
                            </p>
                            </div>
                        </div>
                         <div v-if="order.deliveryInstructions" class="mt-2 text-xs text-gray-600 italic">
                             "{{ order.deliveryInstructions }}"
                         </div>
                    </div>
                </div>
                </div>

                <!-- Package Info -->
                <div class="mt-6 grid grid-cols-1 md:grid-cols-4 gap-4 bg-gray-50 rounded-lg p-4">
                <div>
                    <p class="text-sm text-gray-500">Total Weight</p>
                    <p class="text-lg font-semibold text-gray-900">{{ order.totalWeightKg || '-' }} kg</p>
                </div>
                <div>
                    <p class="text-sm text-gray-500">Total Volume</p>
                    <p class="text-lg font-semibold text-gray-900">{{ order.totalVolumeM3 || '-' }} m³</p>
                </div>
                <div>
                    <p class="text-sm text-gray-500">Pieces</p>
                    <p class="text-sm font-medium text-gray-900">{{ order.totalPieces || '-' }} pcs</p>
                </div>
                <div>
                    <p class="text-sm text-gray-500">Priority</p>
                    <p class="text-lg font-semibold text-gray-900">{{ order.priority }}</p>
                </div>
                </div>
                
                <div v-if="order.specialInstructions" class="mt-4 bg-yellow-50 border border-yellow-100 rounded-md p-3">
                <p class="text-xs text-yellow-800 font-medium uppercase">Special Instructions</p>
                <p class="text-sm text-yellow-900 mt-1">{{ order.specialInstructions }}</p>
                </div>
            </div>
            
            <!-- Items / Assets List -->
             <div class="bg-white rounded-lg shadow p-6">
                 <h2 class="text-lg font-semibold text-gray-900 mb-4">Cargo Manifest</h2>
                 <table class="w-full text-left text-sm">
                     <thead>
                         <tr class="border-b">
                             <th class="pb-2">Asset Type</th>
                             <th class="pb-2">Barcode</th>
                             <th class="pb-2">Description</th>
                             <th class="pb-2">Attrs</th>
                         </tr>
                     </thead>
                     <tbody>
                         <tr v-if="!order.assets || order.assets.length === 0">
                             <td colspan="4" class="py-4 text-center text-gray-500">No individual assets listed.</td>
                         </tr>
                         <tr v-for="asset in order.assets" :key="asset.id" class="border-b last:border-0 hover:bg-gray-50">
                             <td class="py-3 font-medium">{{ asset.assetDefinition?.name || 'Unknown' }}</td>
                             <td class="py-3 font-mono text-gray-600">{{ asset.barcode || '-' }}</td>
                             <td class="py-3 text-gray-600">{{ asset.description || '-' }}</td>
                             <td class="py-3 text-gray-500">
                                 <span v-for="(v, k) in asset.attributes" :key="k" class="block text-xs">
                                     {{ k }}: {{ v }}
                                 </span>
                             </td>
                         </tr>
                     </tbody>
                 </table>
             </div>
        </div>

        <!-- Right Column (1 span) -->
        <div class="space-y-6">
            <!-- Proof of Delivery -->
            <div class="bg-white rounded-lg shadow p-6 border-l-4 border-green-500" v-if="order.status === 'DELIVERED'">
                <h3 class="text-lg font-semibold text-gray-900 mb-4">Proof of Delivery</h3>
                <div class="space-y-4">
                    <div v-if="order.podSignature">
                        <p class="text-xs text-gray-500 uppercase">Signature</p>
                        <div class="h-24 bg-gray-100 rounded flex items-center justify-center text-gray-400 italic border border-dashed border-gray-300">
                            {{ order.podSignerName }}
                        </div>
                    </div>
                </div>
            </div>

             <!-- Internal Notes -->
            <div class="bg-white rounded-lg shadow p-6">
                <h3 class="text-lg font-semibold text-gray-900 mb-4">Internal Notes</h3>
                <p v-if="order.internalNotes" class="text-sm text-gray-700 whitespace-pre-wrap">{{ order.internalNotes }}</p>
                <p v-else class="text-sm text-gray-400 italic">No notes.</p>
            </div>

            <!-- Timeline -->
            <div class="bg-white rounded-lg shadow p-6">
                <h2 class="text-lg font-semibold text-gray-900 mb-4">Order History</h2>
                <div class="space-y-4 relative pl-4 border-l-2 border-gray-200">
                    <div class="relative">
                        <div class="absolute -left-[21px] top-1 h-3 w-3 rounded-full bg-blue-500 ring-4 ring-white"></div>
                        <p class="text-sm font-medium">Created</p>
                        <p class="text-xs text-gray-500">{{ formatDate(order.createdAt || order.orderPlacedAt) }}</p>
                    </div>
                    <div v-if="order.acceptedAt" class="relative">
                        <div class="absolute -left-[21px] top-1 h-3 w-3 rounded-full bg-indigo-500 ring-4 ring-white"></div>
                        <p class="text-sm font-medium">Warehouse Accepted</p>
                        <p class="text-xs text-gray-500">{{ formatDate(order.acceptedAt) }}</p>
                    </div>
                    <div v-if="order.assignedDriverId" class="relative">
                         <div class="absolute -left-[21px] top-1 h-3 w-3 rounded-full bg-purple-500 ring-4 ring-white"></div>
                         <p class="text-sm font-medium">Driver Assigned</p>
                         <p class="text-xs text-gray-500">{{ formatDate(order.updatedAt) }}</p>
                    </div>
                     <div v-if="order.completedAt" class="relative">
                         <div class="absolute -left-[21px] top-1 h-3 w-3 rounded-full bg-green-500 ring-4 ring-white"></div>
                         <p class="text-sm font-medium">Completed</p>
                         <p class="text-xs text-gray-500">{{ formatDate(order.completedAt) }}</p>
                    </div>
                </div>
            </div>
        </div>

      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import { useOrderStore } from '../../stores/orderStore';
import { format, parseISO, isValid } from 'date-fns';

const route = useRoute();
const orderStore = useOrderStore();

const loading = computed(() => orderStore.loading);
const error = computed(() => orderStore.error);
const order = computed(() => orderStore.currentOrder);

onMounted(async () => {
  if (route.params.id) {
    await orderStore.fetchOrder(route.params.id);
  }
});

function assignDriver() {
  alert('Driver assignment feature coming soon!');
}

function formatDate(dateString) {
  if (!dateString) return '-';
  const date = new Date(dateString);
  return isValid(date) ? format(date, 'MMM dd, yyyy HH:mm') : '-';
}

function formatTimeWindow(from, to) {
  if (!from && !to) return 'Not specified';
  const fromStr = from ? format(new Date(from), 'HH:mm') : '?';
  const toStr = to ? format(new Date(to), 'HH:mm') : '?';
  const dateStr = from ? format(new Date(from), 'MMM dd') : '';
  return `${dateStr} ${fromStr} - ${toStr}`;
}

function getStatusClass(status) {
  const classes = {
    'NEW': 'bg-blue-100 text-blue-800',
    'PICKUP': 'bg-yellow-100 text-yellow-800',
    'DELIVERED': 'bg-green-100 text-green-800',
    'CANCELLED': 'bg-red-100 text-red-800'
  };
  return classes[status] || 'bg-gray-100 text-gray-800';
}

function getSlaClass(slaDate) {
  if (!slaDate) return 'text-gray-900';
  const now = new Date();
  const sla = new Date(slaDate);
  // If SLA is passed or within 1 hour
  if (sla < now) return 'text-red-600 font-bold';
  if (sla - now < 3600000) return 'text-orange-600 font-bold';
  return 'text-green-600';
}
</script>
