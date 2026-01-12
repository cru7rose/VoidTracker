<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import api from '../api/axios';
import { format } from 'date-fns';
import { ArrowLeft, MapPin, Box, Clock, User } from 'lucide-vue-next';
import MapComponent from '../components/MapComponent.vue';

const route = useRoute();
const order = ref(null);
const loading = ref(true);
const error = ref(null);

const fetchOrder = async () => {
  loading.value = true;
  try {
    const orderId = route.params.id;
    // Call real API
    // const response = await api.get(`/orders/${orderId}`);
    // order.value = response.data;
    
    // Mock data for now
    await new Promise(r => setTimeout(r, 500));
    order.value = {
      orderId: orderId,
      status: 'PICKUP',
      priority: 'HIGH',
      pickup: {
        customerName: 'Warehouse A',
        street: 'Logistics Way 1',
        city: 'Warsaw',
        postalCode: '00-001',
        country: 'PL',
        contactPerson: 'John Doe',
        phone: '+48 123 456 789'
      },
      delivery: {
        customerName: 'Client B',
        street: 'Market Square 5',
        city: 'Kraków',
        postalCode: '30-001',
        country: 'PL',
        contactPerson: 'Jane Smith',
        phone: '+48 987 654 321',
        sla: '2023-12-01T16:00:00Z',
        route: 'R-123',
        routePart: '1/5'
      },
      packageDetails: {
        barcode1: 'DANX-123456',
        weight: 15.5,
        colli: 2,
        serviceType: 'NIGHT_DELIVERY',
        driverNote: 'Gate code: 1234'
      },
      assignedDriver: 'driver-01',
      timestamps: {
        created: '2023-12-01T08:00:00Z',
        lastStatusChange: '2023-12-01T10:30:00Z'
      }
    };
  } catch (e) {
    error.value = 'Failed to load order details';
    console.error(e);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchOrder();
});

const getStatusColor = (status) => {
  switch (status) {
    case 'NEW': return 'bg-blue-100 text-blue-800';
    case 'PICKUP': return 'bg-yellow-100 text-yellow-800';
    case 'LOAD': return 'bg-purple-100 text-purple-800';
    case 'POD': return 'bg-green-100 text-green-800';
    default: return 'bg-gray-100 text-gray-800';
  }
};
</script>

<template>
  <div>
    <div class="mb-6">
      <router-link to="/orders" class="flex items-center text-gray-500 hover:text-gray-700">
        <ArrowLeft class="h-4 w-4 mr-1" />
        Back to Orders
      </router-link>
    </div>

    <div v-if="loading" class="text-center py-12">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900"></div>
      <p class="mt-2 text-gray-500">Loading order details...</p>
    </div>

    <div v-else-if="error" class="bg-red-50 border-l-4 border-red-400 p-4">
      <div class="flex">
        <div class="ml-3">
          <p class="text-sm text-red-700">{{ error }}</p>
        </div>
      </div>
    </div>

    <div v-else>
      <!-- Header -->
      <div class="md:flex md:items-center md:justify-between mb-6">
        <div class="flex-1 min-w-0">
          <h2 class="text-2xl font-bold leading-7 text-gray-900 sm:text-3xl sm:truncate">
            Order #{{ order.orderId.substring(0, 8) }}
          </h2>
          <div class="mt-1 flex flex-col sm:flex-row sm:flex-wrap sm:mt-0 sm:space-x-6">
            <div class="mt-2 flex items-center text-sm text-gray-500">
              <span :class="[getStatusColor(order.status), 'px-2 inline-flex text-xs leading-5 font-semibold rounded-full']">
                {{ order.status }}
              </span>
            </div>
            <div class="mt-2 flex items-center text-sm text-gray-500">
              <Clock class="flex-shrink-0 mr-1.5 h-5 w-5 text-gray-400" />
              Created {{ format(new Date(order.timestamps.created), 'PP p') }}
            </div>
            <div class="mt-2 flex items-center text-sm text-gray-500">
              <User class="flex-shrink-0 mr-1.5 h-5 w-5 text-gray-400" />
              {{ order.assignedDriver || 'Unassigned' }}
            </div>
          </div>
        </div>
        <div class="mt-4 flex md:mt-0 md:ml-4">
          <button type="button" class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500">
            Edit
          </button>
          <button type="button" class="ml-3 inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500">
            Track
          </button>
        </div>
      </div>

      <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <!-- Enterprise Details -->
        <div class="lg:col-span-2 space-y-6">
           <div class="bg-white shadow overflow-hidden sm:rounded-lg">
             <div class="px-4 py-5 sm:px-6">
               <h3 class="text-lg leading-6 font-medium text-gray-900">Order Context</h3>
             </div>
             <div class="border-t border-gray-200 px-4 py-5 sm:p-0">
               <dl class="sm:divide-y sm:divide-gray-200">
                 <div class="py-4 sm:py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
                   <dt class="text-sm font-medium text-gray-500">Identifiers</dt>
                   <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
                     <div v-if="order.legacyId">Legacy ID: {{ order.legacyId }}</div>
                     <div v-if="order.externalReference">Ref: {{ order.externalReference }}</div>
                     <div v-if="order.orderNumber">Number: {{ order.orderNumber }}</div>
                   </dd>
                 </div>
                 <div class="py-4 sm:py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
                   <dt class="text-sm font-medium text-gray-500">Financials</dt>
                   <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
                     <span v-if="order.costCenter" class="mr-4">CC: {{ order.costCenter }}</span>
                     <span v-if="order.department">Dept: {{ order.department }}</span>
                   </dd>
                 </div>
                 <div class="py-4 sm:py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
                   <dt class="text-sm font-medium text-gray-500">Required Services</dt>
                   <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
                     <div v-if="order.requiredServices && order.requiredServices.length">
                        <span v-for="svc in order.requiredServices" :key="svc" class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-indigo-100 text-indigo-800 mr-2">
                          {{ svc }}
                        </span>
                     </div>
                     <span v-else class="text-gray-400">None</span>
                   </dd>
                 </div>
                 <div class="py-4 sm:py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
                    <dt class="text-sm font-medium text-gray-500">Remarks</dt>
                    <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">{{ order.remark || '-' }}</dd>
                 </div>
               </dl>
             </div>
           </div>

          <!-- Addresses -->
          <div class="bg-white shadow overflow-hidden sm:rounded-lg">
            <div class="px-4 py-5 sm:px-6">
              <h3 class="text-lg leading-6 font-medium text-gray-900">Route Information</h3>
            </div>
            <div class="border-t border-gray-200 px-4 py-5 sm:p-0">
              <dl class="sm:divide-y sm:divide-gray-200">
                <div class="py-4 sm:py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
                  <dt class="text-sm font-medium text-gray-500 flex items-center">
                    <MapPin class="h-5 w-5 mr-2 text-blue-500" /> Pickup
                  </dt>
                  <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
                    <div class="font-medium">{{ order.pickup.customerName }}</div>
                    <div>{{ order.pickup.street }}</div>
                    <div>{{ order.pickup.postalCode }} {{ order.pickup.city }}, {{ order.pickup.country }}</div>
                    <div class="text-gray-500 text-xs mt-1">Contact: {{ order.pickup.contactPerson }} ({{ order.pickup.phone }})</div>
                  </dd>
                </div>
                <div class="py-4 sm:py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
                  <dt class="text-sm font-medium text-gray-500 flex items-center">
                    <MapPin class="h-5 w-5 mr-2 text-green-500" /> Delivery
                  </dt>
                  <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
                    <div class="font-medium">{{ order.delivery.customerName }}</div>
                    <div>{{ order.delivery.street }}</div>
                    <div>{{ order.delivery.postalCode }} {{ order.delivery.city }}, {{ order.delivery.country }}</div>
                    <div class="text-gray-500 text-xs mt-1">Contact: {{ order.delivery.contactPerson }} ({{ order.delivery.phone }})</div>
                    <div v-if="order.delivery.sla" class="mt-2 inline-flex items-center px-2.5 py-0.5 rounded-md text-sm font-medium bg-red-100 text-red-800">
                      SLA: {{ format(new Date(order.delivery.sla), 'PP p') }}
                    </div>
                  </dd>
                </div>
                <div class="py-4 sm:py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
                  <dt class="text-sm font-medium text-gray-500">Route Details</dt>
                  <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
                    Route: {{ order.delivery.route || 'N/A' }} | Part: {{ order.delivery.routePart || 'N/A' }}
                  </dd>
                </div>
              </dl>
            </div>
          </div>

          <!-- Package Details -->
          <div class="bg-white shadow overflow-hidden sm:rounded-lg">
            <div class="px-4 py-5 sm:px-6">
              <h3 class="text-lg leading-6 font-medium text-gray-900">Package Details</h3>
            </div>
            <div class="border-t border-gray-200 px-4 py-5 sm:p-0">
              <dl class="sm:divide-y sm:divide-gray-200">
                <div class="py-4 sm:py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
                  <dt class="text-sm font-medium text-gray-500">Barcode</dt>
                  <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">{{ order.packageDetails.barcode1 }}</dd>
                </div>
                <div class="py-4 sm:py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
                  <dt class="text-sm font-medium text-gray-500">Dimensions & Weight</dt>
                  <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
                    {{ order.packageDetails.weight }} kg | {{ order.packageDetails.colli }} colli
                  </dd>
                </div>
                <div class="py-4 sm:py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
                  <dt class="text-sm font-medium text-gray-500">Service Type</dt>
                  <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">{{ order.packageDetails.serviceType }}</dd>
                </div>
                <div class="py-4 sm:py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
                  <dt class="text-sm font-medium text-gray-500">Driver Note</dt>
                  <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">{{ order.packageDetails.driverNote || '-' }}</dd>
                </div>
              </dl>
            </div>
          </div>
        </div>

        <!-- Sidebar Info (Map) -->
        <div class="space-y-6">
          <div class="bg-white shadow overflow-hidden sm:rounded-lg">
            <div class="px-4 py-5 sm:px-6">
              <h3 class="text-lg leading-6 font-medium text-gray-900">Location</h3>
            </div>
            <div class="h-64 bg-gray-100 overflow-hidden">
              <MapComponent 
                :center="[52.2297, 21.0122]" 
                :zoom="11"
                :markers="[
                  { lat: 52.2297, lng: 21.0122, popup: 'Pickup' },
                  { lat: 50.0647, lng: 19.9450, popup: 'Delivery' }
                ]" 
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
