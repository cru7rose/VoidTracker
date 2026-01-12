<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useOrderStore } from './stores/orderStore'
import BarcodeScanner from './components/BarcodeScanner.vue'

const orderStore = useOrderStore()
const showScanner = ref(false)

onMounted(() => {
  orderStore.loadOrders()
})

function onScan(decodedText: string) {
  // Add scanned order
  const newOrder = {
    id: crypto.randomUUID(),
    customerName: 'Scanned Order',
    status: 'SCANNED',
    address: 'Barcode: ' + decodedText,
    synced: false,
    updatedAt: Date.now()
  }
  orderStore.orders.push(newOrder)
  // In real app, save to store properly
  showScanner.value = false
}
</script>

<template>
  <div class="min-h-screen bg-gray-900 text-white font-sans antialiased p-4">
    <h1 class="text-2xl font-bold mb-4">Driver App (Offline Mode)</h1>
    
    <div class="mb-4 flex gap-2">
      <button 
        @click="orderStore.addMockOrder()"
        class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded transition">
        Add Mock Order
      </button>
      <button 
        @click="showScanner = !showScanner"
        class="bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded transition">
        {{ showScanner ? 'Close Scanner' : 'Scan Barcode' }}
      </button>
    </div>

    <div v-if="showScanner" class="mb-4 p-2 bg-gray-800 rounded">
        <BarcodeScanner @scan="onScan" />
    </div>

    <div v-if="orderStore.loading" class="text-gray-400">Loading...</div>
    
    <div v-else class="space-y-2">
      <div v-if="orderStore.orders.length === 0" class="text-gray-500">
        No orders found. Add one to test persistence.
      </div>
      <div v-for="order in orderStore.orders" :key="order.id" 
           class="bg-gray-800 p-3 rounded shadow border border-gray-700">
        <div class="font-bold">{{ order.customerName }}</div>
        <div class="text-sm text-gray-400">{{ order.address }}</div>
        <div class="text-xs text-gray-500 mt-1">ID: {{ order.id }}</div>
      </div>
    </div>
  </div>
</template>
