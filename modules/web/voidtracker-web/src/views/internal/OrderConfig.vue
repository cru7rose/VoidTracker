<template>
  <div class="min-h-screen bg-gray-50 py-8">
    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="bg-white rounded-lg shadow">
        <!-- Header -->
        <div class="border-b border-gray-200 px-6 py-4">
          <h1 class="text-2xl font-bold text-gray-900">Order Configuration</h1>
          <p class="mt-1 text-sm text-gray-600">Configure which fields are mandatory for order creation</p>
        </div>

        <!-- Configuration Form -->
        <div class="p-6">
          <div class="space-y-6">
            <!-- Pickup Address Fields -->
            <div class="border-b border-gray-200 pb-6">
              <h2 class="text-lg font-semibold text-gray-900 mb-4">Pickup Address</h2>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div v-for="field in pickupFields" :key="field.key" 
                  class="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:bg-gray-50">
                  <div>
                    <label class="text-sm font-medium text-gray-900">{{ field.label }}</label>
                    <p class="text-xs text-gray-500">{{ field.description }}</p>
                  </div>
                  <label class="relative inline-flex items-center cursor-pointer">
                    <input type="checkbox" v-model="config.pickup[field.key].mandatory" 
                      :disabled="field.alwaysMandatory"
                      class="sr-only peer" />
                    <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-indigo-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-indigo-600"></div>
                    <span class="ml-3 text-sm font-medium text-gray-900">
                      {{ config.pickup[field.key].mandatory ? 'Required' : 'Optional' }}
                    </span>
                  </label>
                </div>
              </div>
            </div>

            <!-- Delivery Address Fields -->
            <div class="border-b border-gray-200 pb-6">
              <h2 class="text-lg font-semibold text-gray-900 mb-4">Delivery Address</h2>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div v-for="field in deliveryFields" :key="field.key" 
                  class="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:bg-gray-50">
                  <div>
                    <label class="text-sm font-medium text-gray-900">{{ field.label }}</label>
                    <p class="text-xs text-gray-500">{{ field.description }}</p>
                  </div>
                  <label class="relative inline-flex items-center cursor-pointer">
                    <input type="checkbox" v-model="config.delivery[field.key].mandatory" 
                      :disabled="field.alwaysMandatory"
                      class="sr-only peer" />
                    <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-indigo-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-indigo-600"></div>
                    <span class="ml-3 text-sm font-medium text-gray-900">
                      {{ config.delivery[field.key].mandatory ? 'Required' : 'Optional' }}
                    </span>
                  </label>
                </div>
              </div>
            </div>

            <!-- Package Details Fields -->
            <div class="border-b border-gray-200 pb-6">
              <h2 class="text-lg font-semibold text-gray-900 mb-4">Package Details</h2>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div v-for="field in packageFields" :key="field.key" 
                  class="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:bg-gray-50">
                  <div>
                    <label class="text-sm font-medium text-gray-900">{{ field.label }}</label>
                    <p class="text-xs text-gray-500">{{ field.description }}</p>
                  </div>
                  <label class="relative inline-flex items-center cursor-pointer">
                    <input type="checkbox" v-model="config.package[field.key].mandatory" 
                      :disabled="field.alwaysMandatory"
                      class="sr-only peer" />
                    <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-indigo-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-indigo-600"></div>
                    <span class="ml-3 text-sm font-medium text-gray-900">
                      {{ config.package[field.key].mandatory ? 'Required' : 'Optional' }}
                    </span>
                  </label>
                </div>
              </div>
            </div>

            <!-- Validation Rules -->
            <div class="pb-6">
              <h2 class="text-lg font-semibold text-gray-900 mb-4">Validation Rules</h2>
              <div class="space-y-3">
                <div class="flex items-center justify-between p-4 border border-gray-200 rounded-lg bg-blue-50">
                  <div>
                    <label class="text-sm font-medium text-gray-900">Prevent Same Pickup & Delivery Address</label>
                    <p class="text-xs text-gray-500">InPost standard - addresses must be different</p>
                  </div>
                  <div class="flex items-center">
                    <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                    </svg>
                    <span class="ml-2 text-sm font-medium text-blue-900">Enabled</span>
                  </div>
                </div>

                <div class="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:bg-gray-50">
                  <div>
                    <label class="text-sm font-medium text-gray-900">Auto-calculate Volume from Dimensions</label>
                    <p class="text-xs text-gray-500">Calculate volume (m³) from L×W×H</p>
                  </div>
                  <label class="relative inline-flex items-center cursor-pointer">
                    <input type="checkbox" v-model="config.validation.autoCalculateVolume" class="sr-only peer" />
                    <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-indigo-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-indigo-600"></div>
                    <span class="ml-3 text-sm font-medium text-gray-900">
                      {{ config.validation.autoCalculateVolume ? 'Enabled' : 'Disabled' }}
                    </span>
                  </label>
                </div>

                <div class="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:bg-gray-50">
                  <div>
                    <label class="text-sm font-medium text-gray-900">Allow Manual Volume Override</label>
                    <p class="text-xs text-gray-500">Let users enter volume manually instead of dimensions</p>
                  </div>
                  <label class="relative inline-flex items-center cursor-pointer">
                    <input type="checkbox" v-model="config.validation.allowManualVolume" class="sr-only peer" />
                    <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-indigo-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-indigo-600"></div>
                    <span class="ml-3 text-sm font-medium text-gray-900">
                      {{ config.validation.allowManualVolume ? 'Enabled' : 'Disabled' }}
                    </span>
                  </label>
                </div>
              </div>
            </div>
          </div>

          <!-- Save Button -->
          <div class="flex justify-end space-x-4 mt-8 pt-6 border-t border-gray-200">
            <button type="button" @click="resetToDefaults"
              class="px-6 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 transition">
              Reset to Defaults
            </button>
            <button type="button" @click="saveConfiguration" :disabled="saving"
              class="px-6 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 disabled:opacity-50 transition">
              {{ saving ? 'Saving...' : 'Save Configuration' }}
            </button>
          </div>

          <!-- Success Message -->
          <div v-if="showSuccess" class="mt-4 p-4 bg-green-50 border border-green-200 rounded-md">
            <p class="text-sm text-green-800">✓ Configuration saved successfully</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { configApi } from '../../api/configApi';

const pickupFields = [
  { key: 'street', label: 'Street Address', description: 'Full street address', alwaysMandatory: true },
  { key: 'city', label: 'City', description: 'City name', alwaysMandatory: true },
  { key: 'postalCode', label: 'Postal Code', description: 'ZIP/Postal code', alwaysMandatory: true },
  { key: 'contactName', label: 'Contact Name', description: 'Contact person name', alwaysMandatory: true },
  { key: 'contactPhone', label: 'Contact Phone', description: 'Phone number', alwaysMandatory: true },
  { key: 'contactEmail', label: 'Contact Email', description: 'Email address', alwaysMandatory: false },
];

const deliveryFields = [
  { key: 'street', label: 'Street Address', description: 'Full street address', alwaysMandatory: true },
  { key: 'city', label: 'City', description: 'City name', alwaysMandatory: true },
  { key: 'postalCode', label: 'Postal Code', description: 'ZIP/Postal code', alwaysMandatory: true },
  { key: 'contactName', label: 'Contact Name', description: 'Contact person name', alwaysMandatory: true },
  { key: 'contactPhone', label: 'Contact Phone', description: 'Phone number', alwaysMandatory: true },
  { key: 'contactEmail', label: 'Contact Email', description: 'Email address', alwaysMandatory: false },
];

const packageFields = [
  { key: 'weight', label: 'Weight (kg)', description: 'Package weight', alwaysMandatory: false },
  { key: 'length', label: 'Length (cm)', description: 'Package length', alwaysMandatory: false },
  { key: 'width', label: 'Width (cm)', description: 'Package width', alwaysMandatory: false },
  { key: 'height', label: 'Height (cm)', description: 'Package height', alwaysMandatory: false },
  { key: 'volume', label: 'Volume (m³)', description: 'Package volume', alwaysMandatory: false },
  { key: 'specialInstructions', label: 'Special Instructions', description: 'Handling notes', alwaysMandatory: false },
];

const config = reactive({
  pickup: {
    street: { mandatory: true },
    city: { mandatory: true },
    postalCode: { mandatory: true },
    contactName: { mandatory: true },
    contactPhone: { mandatory: true },
    contactEmail: { mandatory: false },
  },
  delivery: {
    street: { mandatory: true },
    city: { mandatory: true },
    postalCode: { mandatory: true },
    contactName: { mandatory: true },
    contactPhone: { mandatory: true },
    contactEmail: { mandatory: false },
  },
  package: {
    weight: { mandatory: true },
    length: { mandatory: false },
    width: { mandatory: false },
    height: { mandatory: false },
    volume: { mandatory: false },
    specialInstructions: { mandatory: false },
  },
  validation: {
    preventSameAddress: true, // Always enabled (InPost standard)
    autoCalculateVolume: true,
    allowManualVolume: true,
  },
});

const saving = ref(false);
const showSuccess = ref(false);

async function saveConfiguration() {
  saving.value = true;
  
  try {
    await configApi.updateSystemConfig('order-field-rules', JSON.stringify(config));
    // localStorage.setItem('orderFieldConfig', JSON.stringify(config));
    saving.value = false;
    showSuccess.value = true;
    
    setTimeout(() => {
      showSuccess.value = false;
    }, 3000);
  } catch (e) {
    console.error('Failed to save configuration', e);
    saving.value = false;
  }
}

function resetToDefaults() {
  config.package.weight.mandatory = true;
  config.package.volume.mandatory = false;
  config.package.specialInstructions.mandatory = false;
  config.validation.autoCalculateVolume = true;
  config.validation.allowManualVolume = true;
}

// Load saved configuration on mount
onMounted(async () => {
    try {
        const savedConfig = await configApi.getSystemConfig('order-field-rules');
        if (savedConfig) {
            Object.assign(config, JSON.parse(savedConfig));
        }
    } catch (e) {
        console.error('Failed to load configuration', e);
    }
});
</script>
