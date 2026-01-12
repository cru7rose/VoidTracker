<template>
  <div class="min-h-screen bg-gray-50 py-8">
    <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="bg-white rounded-lg shadow p-8">
        <div class="flex justify-between items-center mb-6">
          <h1 class="text-2xl font-bold text-gray-900">
            {{ orderType === 'TRANSFER' ? 'Request Transfer Quote' : 'Create New Order' }}
          </h1>
          
          <!-- Order Type Toggle -->
          <div class="bg-gray-100 p-1 rounded-lg flex">
            <button 
              @click="orderType = 'STANDARD'"
              :class="{'bg-white shadow text-indigo-600': orderType === 'STANDARD', 'text-gray-500 hover:text-gray-700': orderType !== 'STANDARD'}"
              class="px-4 py-2 rounded-md text-sm font-medium transition-all duration-200">
              Standard Order
            </button>
            <button 
              @click="orderType = 'TRANSFER'"
              :class="{'bg-white shadow text-indigo-600': orderType === 'TRANSFER', 'text-gray-500 hover:text-gray-700': orderType !== 'TRANSFER'}"
              class="px-4 py-2 rounded-md text-sm font-medium transition-all duration-200">
              Transfer (Quote)
            </button>
          </div>
        </div>
        
        <form @submit.prevent="handleSubmit" class="space-y-8">
          <!-- Pickup Details Section -->
          <div class="border-b border-gray-200 pb-8">
            <h2 class="text-lg font-semibold text-gray-900 mb-4">Pickup Details</h2>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div class="relative">
                <label class="block text-sm font-medium text-gray-700 mb-1">
                  Street Address <span class="text-red-500">*</span>
                </label>
                <input v-model="form.pickup.street" type="text" required
                  @input="searchAddress($event.target.value, 'pickup')"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500" 
                  placeholder="Start typing to search..." />
                
                <!-- Autocomplete Suggestions -->
                <div v-if="addressSuggestions.pickup.length > 0" class="absolute z-10 w-full bg-white border border-gray-300 rounded-md shadow-lg mt-1 max-h-60 overflow-y-auto">
                  <div v-for="(suggestion, index) in addressSuggestions.pickup" :key="index"
                    @click="selectAddress(suggestion, 'pickup')"
                    class="px-4 py-2 hover:bg-gray-100 cursor-pointer text-sm text-gray-700">
                    {{ suggestion.display_name }}
                  </div>
                </div>
              </div>

              <!-- Building and Apartment -->
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">Building Number <span class="text-red-500">*</span></label>
                  <input v-model="form.pickup.streetNumber" type="text" required
                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">Apartment</label>
                  <input v-model="form.pickup.apartment" type="text"
                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500" />
                </div>
              </div>
              
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">City <span class="text-red-500">*</span></label>
                  <input v-model="form.pickup.city" type="text" required
                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">Postal Code <span class="text-red-500">*</span></label>
                  <input v-model="form.pickup.postalCode" type="text" required
                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500" />
                </div>
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Contact Name <span class="text-red-500">*</span></label>
                <input v-model="form.pickup.contactName" type="text" required
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500" />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Contact Phone <span class="text-red-500">*</span></label>
                <input v-model="form.pickup.contactPhone" type="tel" required
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500" />
              </div>
            </div>
            
            <!-- Pickup Time Window -->
            <div class="mt-4 grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Pickup Time From</label>
                <input v-model="form.pickup.timeFrom" type="datetime-local"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Pickup Time To</label>
                <input v-model="form.pickup.timeTo" type="datetime-local"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500" />
              </div>
            </div>
          </div>

          <!-- Delivery Details Section -->
          <div class="border-b border-gray-200 pb-8">
            <h2 class="text-lg font-semibold text-gray-900 mb-4">Delivery Details</h2>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div class="relative">
                <label class="block text-sm font-medium text-gray-700 mb-1">
                  Street Address <span class="text-red-500">*</span>
                </label>
                <input v-model="form.delivery.street" type="text" required
                  @input="searchAddress($event.target.value, 'delivery')"
                  @blur="validateAddresses"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500"
                  :class="{ 'border-red-500': validationErrors.sameAddress }" 
                  placeholder="Start typing to search..." />
                
                <!-- Autocomplete Suggestions -->
                <div v-if="addressSuggestions.delivery.length > 0" class="absolute z-10 w-full bg-white border border-gray-300 rounded-md shadow-lg mt-1 max-h-60 overflow-y-auto">
                  <div v-for="(suggestion, index) in addressSuggestions.delivery" :key="index"
                    @click="selectAddress(suggestion, 'delivery')"
                    class="px-4 py-2 hover:bg-gray-100 cursor-pointer text-sm text-gray-700">
                    {{ suggestion.display_name }}
                  </div>
                </div>

                <p v-if="validationErrors.sameAddress" class="mt-1 text-sm text-red-600">
                  {{ validationErrors.sameAddress }}
                </p>
              </div>

              <!-- Building and Apartment -->
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">Building Number <span class="text-red-500">*</span></label>
                  <input v-model="form.delivery.streetNumber" type="text" required
                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">Apartment</label>
                  <input v-model="form.delivery.apartment" type="text"
                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500" />
                </div>
              </div>
              
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">City <span class="text-red-500">*</span></label>
                  <input v-model="form.delivery.city" type="text" required @blur="validateAddresses"
                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">Postal Code <span class="text-red-500">*</span></label>
                  <input v-model="form.delivery.postalCode" type="text" required @blur="validateAddresses"
                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500" />
                </div>
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Contact Name <span class="text-red-500">*</span></label>
                <input v-model="form.delivery.contactName" type="text" required
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500" />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Contact Phone <span class="text-red-500">*</span></label>
                <input v-model="form.delivery.contactPhone" type="tel" required
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500" />
              </div>
            </div>

            <!-- Delivery Time Window -->
            <div class="mt-4 grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Delivery Time From</label>
                <input v-model="form.delivery.timeFrom" type="datetime-local"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Delivery Time To</label>
                <input v-model="form.delivery.timeTo" type="datetime-local"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500" />
              </div>
            </div>
          </div>

          <!-- Package Details Section -->
          <div class="border-b border-gray-200 pb-8">
            <h2 class="text-lg font-semibold text-gray-900 mb-4">Package Details</h2>
            
            <!-- Dimensions for Auto-calculation -->
            <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Length (cm)</label>
                <input v-model.number="form.package.length" type="number" step="0.1" min="0" @input="calculateVolume"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Width (cm)</label>
                <input v-model.number="form.package.width" type="number" step="0.1" min="0" @input="calculateVolume"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Height (cm)</label>
                <input v-model.number="form.package.height" type="number" step="0.1" min="0" @input="calculateVolume"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500" />
              </div>
            </div>

            <!-- Volume (Auto or Manual) -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">
                  Volume (m³) <span v-if="fieldConfig.volume?.mandatory" class="text-red-500">*</span>
                </label>
                <input v-model.number="form.package.volume" type="number" step="0.001" min="0"
                  :required="fieldConfig.volume?.mandatory"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500 bg-gray-50" 
                  placeholder="Auto-calculated or enter manually" />
                <p class="mt-1 text-xs text-gray-500">Auto-calculated from dimensions or enter manually</p>
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">
                  Weight (kg) <span v-if="fieldConfig.weight?.mandatory" class="text-red-500">*</span>
                </label>
                <input v-model.number="form.package.weight" type="number" step="0.1" min="0"
                  :required="fieldConfig.weight?.mandatory"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500" />
              </div>
            </div>

            <!-- Special Instructions -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">
                Special Instructions <span v-if="fieldConfig.specialInstructions?.mandatory" class="text-red-500">*</span>
              </label>
              <textarea v-model="form.package.specialInstructions" rows="3"
                :required="fieldConfig.specialInstructions?.mandatory"
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500"
                placeholder="Any special handling requirements..."></textarea>
            </div>
            </div>


          <!-- Additional Services (Tags) -->
          <div class="border-b border-gray-200 pb-8">
            <h2 class="text-lg font-semibold text-gray-900 mb-4">Additional Services</h2>
            <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
              <label class="flex items-center space-x-2 cursor-pointer">
                <input type="checkbox" v-model="form.services" value="LIFT" class="rounded text-indigo-600 focus:ring-indigo-500">
                <span class="text-sm text-gray-700">Lift Required</span>
              </label>
              <label class="flex items-center space-x-2 cursor-pointer">
                <input type="checkbox" v-model="form.services" value="ADR" class="rounded text-indigo-600 focus:ring-indigo-500">
                <span class="text-sm text-gray-700">ADR (Hazmat)</span>
              </label>
              <label class="flex items-center space-x-2 cursor-pointer">
                <input type="checkbox" v-model="form.services" value="FRAGILE" class="rounded text-indigo-600 focus:ring-indigo-500">
                <span class="text-sm text-gray-700">Fragile</span>
              </label>
              <label class="flex items-center space-x-2 cursor-pointer">
                <input type="checkbox" v-model="form.services" value="COD" class="rounded text-indigo-600 focus:ring-indigo-500">
                <span class="text-sm text-gray-700">Cash on Delivery</span>
              </label>
              <label class="flex items-center space-x-2 cursor-pointer">
                <input type="checkbox" v-model="form.services" value="DOC_RETURN" class="rounded text-indigo-600 focus:ring-indigo-500">
                <span class="text-sm text-gray-700">Document Return</span>
              </label>
            </div>
          </div>



          <!-- Error Message -->
          <div v-if="error" class="bg-red-50 border border-red-200 rounded-md p-4">
            <p class="text-sm text-red-800">{{ error }}</p>
          </div>

          <!-- Submit Buttons -->
          <div class="flex justify-end space-x-4">
            <button type="button" @click="$router.go(-1)"
              class="px-6 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 transition">
              Cancel
            </button>
            <button type="submit" :disabled="loading || !!validationErrors.sameAddress"
              class="px-6 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition">
              {{ loading ? 'Processing...' : (orderType === 'TRANSFER' ? 'Request Quote' : 'Create Order') }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useOrderStore } from '../../stores/orderStore';
import axios from 'axios'; // Import axios for Nominatim calls

const router = useRouter();
const orderStore = useOrderStore();

const orderType = ref('STANDARD');
const addressSuggestions = reactive({
  pickup: [],
  delivery: []
});

const form = reactive({
  pickup: {
    street: '',
    streetNumber: '',
    apartment: '',
    city: '',
    postalCode: '',
    contactName: '',
    contactPhone: '',
  },
  delivery: {
    street: '',
    streetNumber: '',
    apartment: '',
    city: '',
    postalCode: '',
    contactName: '',
    contactPhone: '',
  },
  package: {
    length: null,
    width: null,
    height: null,
    volume: null,
    weight: null,
    specialInstructions: '',
  },
  services: [],

});

// Field configuration - fetched from admin panel (mock for now)
const fieldConfig = ref({
  volume: { mandatory: false },
  weight: { mandatory: true },
  specialInstructions: { mandatory: false },
});

const validationErrors = reactive({
  sameAddress: null,
});

const loading = ref(false);
const error = ref('');

// Address Search with Nominatim
let debounceTimer = null;
async function searchAddress(query, type) {
  if (debounceTimer) clearTimeout(debounceTimer);
  
  if (!query || query.length < 3) {
    addressSuggestions[type] = [];
    return;
  }

  debounceTimer = setTimeout(async () => {
    try {
      const response = await axios.get(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&addressdetails=1&limit=5`);
      addressSuggestions[type] = response.data;
    } catch (e) {
      console.error('Address search failed', e);
    }
  }, 300);
}

function selectAddress(suggestion, type) {
  const address = suggestion.address;
  form[type].street = suggestion.display_name.split(',')[0]; // Simple street extraction
  form[type].streetNumber = address.house_number || '';
  form[type].city = address.city || address.town || address.village || '';
  form[type].postalCode = address.postcode || '';
  addressSuggestions[type] = [];
}

// Validate that pickup and delivery addresses are different
function validateAddresses() {
  const pickup = `${form.pickup.street},${form.pickup.city},${form.pickup.postalCode}`.toLowerCase().trim();
  const delivery = `${form.delivery.street},${form.delivery.city},${form.delivery.postalCode}`.toLowerCase().trim();
  
  if (pickup && delivery && pickup === delivery) {
    validationErrors.sameAddress = 'Pickup and delivery addresses cannot be the same';
  } else {
    validationErrors.sameAddress = null;
  }
}

// Auto-calculate volume from dimensions
function calculateVolume() {
  const { length, width, height } = form.package;
  
  if (length && width && height && length > 0 && width > 0 && height > 0) {
    // Convert cm³ to m³ (divide by 1,000,000)
    form.package.volume = (length * width * height) / 1000000;
  }
}

async function handleSubmit() {
  if (validationErrors.sameAddress) {
    error.value = 'Please fix validation errors before submitting';
    return;
  }

  loading.value = true;
  error.value = '';

  try {
    const orderData = {
      pickupAddress: {
        street: form.pickup.street,
        streetNumber: form.pickup.streetNumber,
        apartment: form.pickup.apartment,
        city: form.pickup.city,
        postalCode: form.pickup.postalCode,
        contactName: form.pickup.contactName,
        contactPhone: form.pickup.contactPhone,
      },
      deliveryAddress: {
        street: form.delivery.street,
        streetNumber: form.delivery.streetNumber,
        apartment: form.delivery.apartment,
        city: form.delivery.city,
        postalCode: form.delivery.postalCode,
        contactName: form.delivery.contactName,
        contactPhone: form.delivery.contactPhone,
      },
      packageDetails: {
        length: form.package.length,
        width: form.package.width,
        height: form.package.height,
        volume: form.package.volume,
        weight: form.package.weight,
        specialInstructions: form.package.specialInstructions,
      },
      pickupTimeFrom: form.pickup.timeFrom ? new Date(form.pickup.timeFrom).toISOString() : null,
      pickupTimeTo: form.pickup.timeTo ? new Date(form.pickup.timeTo).toISOString() : null,
      deliveryTimeFrom: form.delivery.timeFrom ? new Date(form.delivery.timeFrom).toISOString() : null,
      deliveryTimeTo: form.delivery.timeTo ? new Date(form.delivery.timeTo).toISOString() : null,
      requiredServiceCodes: form.services,
      isTransfer: orderType.value === 'TRANSFER',
    };

    await orderStore.createOrder(orderData);
    router.push('/customer/orders');
  } catch (err) {
    error.value = err.message || 'Failed to create order. Please try again.';
  } finally {
    loading.value = false;
  }
}
</script>
