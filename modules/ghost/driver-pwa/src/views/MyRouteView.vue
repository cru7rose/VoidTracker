<script setup lang="ts">
import { ref, onMounted } from 'vue'

interface Stop {
  id: string
  address: string
  type: 'PICKUP' | 'DELIVERY'
  status: 'PENDING' | 'COMPLETED'
}

const stops = ref<Stop[]>([])
const loading = ref(true)

onMounted(async () => {
  // Simulate fetching data
  setTimeout(() => {
    stops.value = [
      { id: '1', address: 'Marszałkowska 1, Warszawa', type: 'PICKUP', status: 'PENDING' },
      { id: '2', address: 'Aleje Jerozolimskie 20, Warszawa', type: 'DELIVERY', status: 'PENDING' },
      { id: '3', address: 'Chmielna 5, Warszawa', type: 'DELIVERY', status: 'PENDING' }
    ]
    loading.value = false
  }, 1000)
})
</script>

<template>
  <div class="min-h-screen bg-gray-900 text-white p-4">
    <h1 class="text-2xl font-bold mb-4">My Route</h1>
    
    <div v-if="loading" class="text-center py-8">
      Loading itinerary...
    </div>
    
    <div v-else class="space-y-4">
      <div v-for="(stop, index) in stops" :key="stop.id" 
           class="bg-gray-800 p-4 rounded-lg border border-gray-700 flex justify-between items-center"
      >
        <div>
          <span class="text-xs text-gray-400 font-mono">STOP #{{ index + 1 }}</span>
          <p class="font-medium text-lg">{{ stop.address }}</p>
          <span :class="stop.type === 'PICKUP' ? 'text-green-400' : 'text-blue-400'" class="text-sm font-bold">
            {{ stop.type }}
          </span>
        </div>
        
        <button class="bg-blue-600 hover:bg-blue-700 text-white px-3 py-2 rounded text-sm">
          Navigate
        </button>
      </div>
    </div>
  </div>
</template>
