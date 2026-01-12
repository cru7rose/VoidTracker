<template>
  <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm" @click.self="closeModal">
    <div class="bg-slate-900 border border-slate-700 rounded-2xl shadow-2xl w-full max-w-2xl mx-4 overflow-hidden">
      <!-- Header -->
      <div class="bg-gradient-to-r from-brand-500 to-brand-600 px-6 py-4">
        <h2 class="text-2xl font-bold text-white flex items-center">
          <span class="mr-3">📥</span>
          Import Order (JSON)
        </h2>
      </div>

      <!-- Body -->
      <div class="p-6 space-y-4">
        <p class="text-slate-400 text-sm">
          Paste a valid JSON payload below to create a new order via the Ingestion API.
        </p>

        <!-- JSON Input -->
        <div>
          <label class="block text-sm font-semibold text-slate-300 mb-2">Order Data (JSON)</label>
          <textarea
            v-model="jsonPayload"
            class="w-full h-64 bg-slate-800 text-white border border-slate-700 rounded-lg p-4 font-mono text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
            placeholder='{
  "customerName": "Acme Corp",
  "pickupAddress": "Warsaw, ul. Marszałkowska 1",
  "deliveryAddress": "Krakow, ul. Floriańska 5",
  "pickupDate": "2026-01-10T08:00:00Z",
  "deliveryDate": "2026-01-10T18:00:00Z"
}'
          ></textarea>
        </div>

        <!-- Error Display -->
        <div v-if="error" class="bg-red-500/10 border border-red-500/30 rounded-lg p-4">
          <p class="text-red-400 text-sm font-semibold">⚠️ {{ error }}</p>
        </div>

        <!-- Success Display -->
        <div v-if="success" class="bg-green-500/10 border border-green-500/30 rounded-lg p-4">
          <p class="text-green-400 text-sm font-semibold">✅ {{ success }}</p>
        </div>
      </div>

      <!-- Footer -->
      <div class="bg-slate-800 px-6 py-4 flex justify-end space-x-3">
        <button
          @click="closeModal"
          class="px-5 py-2 bg-slate-700 hover:bg-slate-600 text-white rounded-lg font-semibold transition-all"
          :disabled="loading"
        >
          Cancel
        </button>
        <button
          @click="submitOrder"
          class="px-5 py-2 bg-brand-500 hover:bg-brand-600 text-white rounded-lg font-semibold transition-all flex items-center"
          :disabled="loading"
        >
          <span v-if="loading" class="mr-2">
            <svg class="animate-spin h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
          </span>
          {{ loading ? 'Importing...' : 'Import Order' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { ingestionApi } from '@/api/ingestionApi';
import { useToast } from '@/composables/useToast';

const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['close', 'success']);

const { success: showSuccess, error: showError } = useToast();

const jsonPayload = ref('');
const loading = ref(false);
const error = ref(null);
const success = ref(null);

async function submitOrder() {
  error.value = null;
  success.value = null;

  // Validate JSON
  let payload;
  try {
    payload = JSON.parse(jsonPayload.value);
  } catch (e) {
    error.value = 'Invalid JSON format. Please check syntax.';
    showError('Invalid JSON format');
    return;
  }

  loading.value = true;

  const result = await ingestionApi.ingestOrder(payload);

  loading.value = false;

  if (result.success) {
    success.value = `Order ${result.orderId} created successfully!`;
    showSuccess(`Order ${result.orderId} imported`);
    
    // Close modal after 2s
    setTimeout(() => {
      emit('success', result.orderId);
      closeModal();
    }, 2000);
  } else {
    error.value = result.message;
    showError(result.message);
  }
}

function closeModal() {
  // Reset state
  jsonPayload.value = '';
  error.value = null;
  success.value = null;
  loading.value = false;
  
  emit('close');
}
</script>

<style scoped>
/* Tailwind handles most styling */
</style>
