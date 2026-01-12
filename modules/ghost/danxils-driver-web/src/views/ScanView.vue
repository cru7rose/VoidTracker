<template>
  <div class="p-6 bg-slate-900 min-h-screen text-slate-100 flex flex-col items-center">
    <h1 class="text-2xl font-bold mb-6 text-brand-400">Scan Package</h1>
    
    <div class="mb-8">
      <BarcodeScanner @scan="onScan" />
    </div>

    <!-- Scan Result Card -->
    <div v-if="scannedResult" class="w-full max-w-sm bg-slate-800 rounded-xl p-6 shadow-lg border border-slate-700 transition-all">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-semibold text-white">Scan Result</h2>
        <span :class="statusClass" class="px-2 py-1 rounded text-xs font-bold uppercase">{{ statusText }}</span>
      </div>
      
      <div class="space-y-3">
        <div>
          <label class="text-xs text-slate-400 uppercase tracking-wider">Barcode</label>
          <p class="font-mono text-brand-300 text-lg">{{ scannedResult }}</p>
        </div>
        
        <div v-if="matchedOrder">
          <label class="text-xs text-slate-400 uppercase tracking-wider">Matched Order</label>
          <p class="text-white">{{ matchedOrder.externalId }}</p>
          <p class="text-sm text-slate-400">{{ matchedOrder.deliveryAddress?.city }}</p>
        </div>
      </div>

      <div class="mt-6 flex space-x-3">
        <button @click="resetScan" class="flex-1 py-3 rounded-lg border border-slate-600 text-slate-300 hover:bg-slate-700">
          Cancel
        </button>
        <button v-if="matchedOrder" @click="confirmScan" class="flex-1 py-3 rounded-lg bg-brand-500 text-white font-bold hover:bg-brand-400 shadow-lg shadow-brand-500/20">
          Confirm
        </button>
      </div>
    </div>
    
    <div v-else class="text-slate-500 text-center mt-4">
      <p>Point camera at a barcode or QR code.</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import BarcodeScanner from '../components/BarcodeScanner.vue'
import { useOrderStore } from '../stores/orderStore'

const orderStore = useOrderStore()
const scannedResult = ref(null)
const matchedOrder = ref(null)

const statusText = computed(() => matchedOrder.value ? 'Matched' : 'Unknown')
const statusClass = computed(() => matchedOrder.value ? 'bg-green-500/20 text-green-400' : 'bg-yellow-500/20 text-yellow-400')

function onScan(decodedText) {
  if (scannedResult.value === decodedText) return; // Debounce duplicate scans
  
  scannedResult.value = decodedText
  
  // Search in local store (which is synced from offlineStore)
  matchedOrder.value = orderStore.orders.find(o => 
    o.id === decodedText || 
    o.externalId === decodedText ||
    o.scanCode === decodedText
  )
  
  // Audio feedback could be added here
}

function resetScan() {
  scannedResult.value = matchedOrder.value = null
}

function confirmScan() {
  // Logic to update order status or add to proof of delivery
  alert(`Confirmed scan for order: ${matchedOrder.value.externalId}`)
  resetScan()
}
</script>
