<template>
  <div class="h-screen bg-surface-950 flex flex-col p-4">
    <div class="text-xl font-bold text-white mb-6">Vehicle Check</div>

    <div v-if="loading" class="text-gray-400">Loading vehicles...</div>

    <div v-else class="flex flex-col gap-4">
      <div class="flex flex-col gap-2">
        <label class="text-sm text-gray-400">Select Vehicle</label>
        <select v-model="selectedVehicleId" class="p-3 rounded bg-surface-800 text-white border border-surface-700">
          <option value="" disabled>Select your van...</option>
          <option v-for="v in vehicles" :key="v.id" :value="v.id">
            {{ v.licensePlate }} ({{ v.model }})
          </option>
        </select>
      </div>

      <div class="flex flex-col gap-2">
        <label class="text-sm text-gray-400">Status</label>
        <div class="flex gap-4">
          <button 
            @click="isOk = true"
            :class="['flex-1 p-4 rounded font-bold transition-all', isOk ? 'bg-green-600 text-white' : 'bg-surface-800 text-gray-400']"
          >
            Vehicle OK
          </button>
          <button 
            @click="isOk = false"
            :class="['flex-1 p-4 rounded font-bold transition-all', !isOk ? 'bg-red-600 text-white' : 'bg-surface-800 text-gray-400']"
          >
            Issue Found
          </button>
        </div>
      </div>

      <transition name="fade">
        <div v-if="!isOk" class="flex flex-col gap-2">
          <label class="text-sm text-gray-400">Describe Issues</label>
          <textarea 
            v-model="issuesText"
            rows="3" 
            class="p-3 rounded bg-surface-800 text-white border border-surface-700"
            placeholder="e.g. Check engine light on, Flat tire..."
          ></textarea>
        </div>
      </transition>

      <button 
        @click="submitCheck" 
        :disabled="!isValid"
        class="mt-4 p-4 rounded bg-primary-600 text-white font-bold hover:bg-primary-500 disabled:opacity-50 disabled:cursor-not-allowed"
      >
        Start Shift
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const vehicles = ref([])
const loading = ref(true)
const selectedVehicleId = ref('')
const isOk = ref(true)
const issuesText = ref('')

const isValid = computed(() => {
  if (!selectedVehicleId.value) return false
  if (!isOk.value && !issuesText.value.trim()) return false
  return true
})

onMounted(async () => {
  try {
    const token = localStorage.getItem('driverToken')
    const res = await axios.get('/api/fleet/checks/vehicles', {
      headers: { Authorization: `Bearer ${token}` }
    })
    vehicles.value = res.data
  } catch (e) {
    console.error("Failed to load vehicles", e)
    // Fallback for demo if API fails/is empty
    vehicles.value = [
      { id: '11111111-1111-1111-1111-111111111111', licensePlate: 'MOCK-001', model: 'Sprinter' }
    ]
  } finally {
    loading.value = false
  }
})

const submitCheck = async () => {
  try {
    const token = localStorage.getItem('driverToken')
    
    // Get user ID from token or storage (assuming store has it, or just send check)
    // For MVP, we might rely on backend extracting user from token, OR send it if required.
    // The controller expects userId in body. We need to decode token or fetch profile.
    // Let's assume we store userId. If not, we fix.
    
    // Quick fix: decode token payload manually
    const payload = JSON.parse(atob(token.split('.')[1]))
    const userId = payload.sub || payload.userId || 'd21e210b-49b5-4d22-8d7d-b5cf5aa7c852' // Fallback to driver seed ID

    await axios.post('/api/fleet/checks', {
      userId: userId, // ideally from auth context
      vehicleId: selectedVehicleId.value,
      isOk: isOk.value,
      issuesJson: JSON.stringify([issuesText.value])
    }, {
      headers: { Authorization: `Bearer ${token}` }
    })

    router.push('/tasks')
  } catch (e) {
    console.error("Failed to submit check", e)
    alert("Failed to submit check. Try again.")
  }
}
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
