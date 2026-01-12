<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import api from '../api/axios';
import { ArrowLeft } from 'lucide-vue-next';

const router = useRouter();
const loading = ref(false);
const error = ref(null);

const form = ref({
  customerId: 'cust-001', // Default for now
  priority: 'NORMAL',
  pickupAddress: {
    customerName: '',
    street: '',
    streetNumber: '',
    postalCode: '',
    city: '',
    country: 'PL',
    contactPerson: '',
    phone: ''
  },
  deliveryAddress: {
    customerName: '',
    street: '',
    streetNumber: '',
    postalCode: '',
    city: '',
    country: 'PL',
    contactPerson: '',
    phone: '',
    sla: '',
    route: '',
    serviceType: ''
  },
  packageDetails: {
    barcode1: '',
    weight: 0,
    colli: 1,
    serviceType: 'STANDARD',
    driverNote: ''
  }
});

const submitOrder = async () => {
  loading.value = true;
  error.value = null;
  
  try {
    // Transform data if needed to match API DTO
    const payload = {
      ...form.value,
      // Ensure SLA is in ISO format if provided
      deliveryAddress: {
        ...form.value.deliveryAddress,
        sla: form.value.deliveryAddress.sla ? new Date(form.value.deliveryAddress.sla).toISOString() : null
      }
    };

    await api.post('/orders', payload);
    router.push('/orders');
  } catch (e) {
    console.error(e);
    error.value = 'Failed to create order. Please check the inputs.';
  } finally {
    loading.value = false;
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

    <div class="md:grid md:grid-cols-3 md:gap-6">
      <div class="md:col-span-1">
        <div class="px-4 sm:px-0">
          <h3 class="text-lg font-medium leading-6 text-gray-900">Create New Order</h3>
          <p class="mt-1 text-sm text-gray-600">
            Fill in the details to create a new transport order.
          </p>
        </div>
      </div>
      
      <div class="mt-5 md:mt-0 md:col-span-2">
        <form @submit.prevent="submitOrder">
          <div class="shadow sm:rounded-md sm:overflow-hidden">
            <div class="px-4 py-5 bg-white space-y-6 sm:p-6">
              
              <!-- Error Message -->
              <div v-if="error" class="bg-red-50 border-l-4 border-red-400 p-4">
                <p class="text-sm text-red-700">{{ error }}</p>
              </div>

              <!-- Pickup Section -->
              <div>
                <h4 class="text-md font-medium text-gray-900 border-b pb-2 mb-4">Pickup Address</h4>
                <div class="grid grid-cols-6 gap-6">
                  <div class="col-span-6 sm:col-span-3">
                    <label class="block text-sm font-medium text-gray-700">Customer Name</label>
                    <input type="text" v-model="form.pickupAddress.customerName" required class="mt-1 focus:ring-indigo-500 focus:border-indigo-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                  </div>
                  <div class="col-span-6 sm:col-span-3">
                    <label class="block text-sm font-medium text-gray-700">Phone</label>
                    <input type="text" v-model="form.pickupAddress.phone" class="mt-1 focus:ring-indigo-500 focus:border-indigo-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                  </div>
                  <div class="col-span-6 sm:col-span-4">
                    <label class="block text-sm font-medium text-gray-700">Street</label>
                    <input type="text" v-model="form.pickupAddress.street" required class="mt-1 focus:ring-indigo-500 focus:border-indigo-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                  </div>
                  <div class="col-span-6 sm:col-span-2">
                    <label class="block text-sm font-medium text-gray-700">Number</label>
                    <input type="text" v-model="form.pickupAddress.streetNumber" required class="mt-1 focus:ring-indigo-500 focus:border-indigo-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                  </div>
                  <div class="col-span-6 sm:col-span-2">
                    <label class="block text-sm font-medium text-gray-700">City</label>
                    <input type="text" v-model="form.pickupAddress.city" required class="mt-1 focus:ring-indigo-500 focus:border-indigo-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                  </div>
                  <div class="col-span-6 sm:col-span-2">
                    <label class="block text-sm font-medium text-gray-700">ZIP / Postal Code</label>
                    <input type="text" v-model="form.pickupAddress.postalCode" required class="mt-1 focus:ring-indigo-500 focus:border-indigo-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                  </div>
                </div>
              </div>

              <!-- Delivery Section -->
              <div>
                <h4 class="text-md font-medium text-gray-900 border-b pb-2 mb-4">Delivery Address</h4>
                <div class="grid grid-cols-6 gap-6">
                  <div class="col-span-6 sm:col-span-3">
                    <label class="block text-sm font-medium text-gray-700">Customer Name</label>
                    <input type="text" v-model="form.deliveryAddress.customerName" required class="mt-1 focus:ring-indigo-500 focus:border-indigo-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                  </div>
                  <div class="col-span-6 sm:col-span-3">
                    <label class="block text-sm font-medium text-gray-700">SLA (Deadline)</label>
                    <input type="datetime-local" v-model="form.deliveryAddress.sla" class="mt-1 focus:ring-indigo-500 focus:border-indigo-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                  </div>
                  <div class="col-span-6 sm:col-span-4">
                    <label class="block text-sm font-medium text-gray-700">Street</label>
                    <input type="text" v-model="form.deliveryAddress.street" required class="mt-1 focus:ring-indigo-500 focus:border-indigo-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                  </div>
                  <div class="col-span-6 sm:col-span-2">
                    <label class="block text-sm font-medium text-gray-700">Number</label>
                    <input type="text" v-model="form.deliveryAddress.streetNumber" required class="mt-1 focus:ring-indigo-500 focus:border-indigo-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                  </div>
                  <div class="col-span-6 sm:col-span-2">
                    <label class="block text-sm font-medium text-gray-700">City</label>
                    <input type="text" v-model="form.deliveryAddress.city" required class="mt-1 focus:ring-indigo-500 focus:border-indigo-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                  </div>
                  <div class="col-span-6 sm:col-span-2">
                    <label class="block text-sm font-medium text-gray-700">ZIP / Postal Code</label>
                    <input type="text" v-model="form.deliveryAddress.postalCode" required class="mt-1 focus:ring-indigo-500 focus:border-indigo-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                  </div>
                </div>
              </div>

              <!-- Package Section -->
              <div>
                <h4 class="text-md font-medium text-gray-900 border-b pb-2 mb-4">Package Details</h4>
                <div class="grid grid-cols-6 gap-6">
                  <div class="col-span-6 sm:col-span-3">
                    <label class="block text-sm font-medium text-gray-700">Barcode</label>
                    <input type="text" v-model="form.packageDetails.barcode1" required class="mt-1 focus:ring-indigo-500 focus:border-indigo-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                  </div>
                  <div class="col-span-6 sm:col-span-3">
                    <label class="block text-sm font-medium text-gray-700">Service Type</label>
                    <select v-model="form.packageDetails.serviceType" class="mt-1 block w-full py-2 px-3 border border-gray-300 bg-white rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm">
                      <option>STANDARD</option>
                      <option>EXPRESS</option>
                      <option>NIGHT_DELIVERY</option>
                    </select>
                  </div>
                  <div class="col-span-6 sm:col-span-2">
                    <label class="block text-sm font-medium text-gray-700">Weight (kg)</label>
                    <input type="number" step="0.1" v-model="form.packageDetails.weight" class="mt-1 focus:ring-indigo-500 focus:border-indigo-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                  </div>
                  <div class="col-span-6 sm:col-span-2">
                    <label class="block text-sm font-medium text-gray-700">Colli</label>
                    <input type="number" v-model="form.packageDetails.colli" class="mt-1 focus:ring-indigo-500 focus:border-indigo-500 block w-full shadow-sm sm:text-sm border-gray-300 rounded-md">
                  </div>
                  <div class="col-span-6">
                    <label class="block text-sm font-medium text-gray-700">Driver Note</label>
                    <textarea v-model="form.packageDetails.driverNote" rows="3" class="shadow-sm focus:ring-indigo-500 focus:border-indigo-500 mt-1 block w-full sm:text-sm border border-gray-300 rounded-md"></textarea>
                  </div>
                </div>
              </div>

            </div>
            <div class="px-4 py-3 bg-gray-50 text-right sm:px-6">
              <button type="submit" :disabled="loading" class="inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50">
                {{ loading ? 'Creating...' : 'Create Order' }}
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
