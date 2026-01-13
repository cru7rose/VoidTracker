<template>
  <div class="stop-action-sheet bg-gray-900 text-white">
    <div class="p-4 border-b border-gray-700">
      <h3 class="text-lg font-semibold">{{ stop.address }}</h3>
      <p class="text-sm text-gray-400">{{ stop.type }} • Stop #{{ stopIndex + 1 }}</p>
    </div>

    <div class="p-4 space-y-4 max-h-[calc(100vh-200px)] overflow-y-auto">
      <!-- Status Update -->
      <div>
        <label class="block text-sm font-medium mb-2">Status</label>
        <div class="grid grid-cols-2 gap-2">
          <button
            v-for="status in availableStatuses"
            :key="status"
            @click="updateStatus(status)"
            :class="[
              'py-2 px-3 rounded-lg text-sm font-medium transition-colors',
              currentStatus === status
                ? 'bg-blue-600 text-white'
                : 'bg-gray-800 text-gray-300 hover:bg-gray-700'
            ]"
          >
            {{ statusLabels[status] }}
          </button>
        </div>
      </div>

      <!-- Barcode Scanner (if required) -->
      <div v-if="requiresBarcode">
        <label class="block text-sm font-medium mb-2">Skanuj kod kreskowy</label>
        <Scanner
          @scan-success="handleBarcodeScan"
          @camera-error="handleScannerError"
        />
        <input
          v-if="allowManualEntry"
          v-model="manualBarcode"
          type="text"
          placeholder="Lub wpisz ręcznie"
          class="mt-2 w-full bg-gray-800 border border-gray-600 rounded-lg p-2 text-white"
          @keyup.enter="handleManualBarcode"
        />
      </div>

      <!-- Delivery Code Scanner (conditional) -->
      <div v-if="requiresDeliveryCode">
        <label class="block text-sm font-medium mb-2">Kod dostawy</label>
        <Scanner
          @scan-success="handleDeliveryCodeScan"
          @camera-error="handleScannerError"
        />
      </div>

      <!-- Camera - DMG (optional) -->
      <div v-if="showDmgCamera">
        <label class="block text-sm font-medium mb-2">Zdjęcie uszkodzenia (opcjonalne)</label>
        <CameraCapture
          @photo-captured="handleDmgPhoto"
          @error="handleCameraError"
        />
      </div>

      <!-- Camera - POD (required) -->
      <div v-if="showPodCamera">
        <label class="block text-sm font-medium mb-2">
          Zdjęcie dostawy <span class="text-red-400">*</span>
        </label>
        <CameraCapture
          @photo-captured="handlePodPhoto"
          @error="handleCameraError"
        />
        <p v-if="podPhotos.length > 0" class="mt-2 text-sm text-gray-400">
          Zrobiono: {{ podPhotos.length }} zdjęć
        </p>
      </div>

      <!-- Signature -->
      <div v-if="requiresSignature">
        <SignaturePad
          label="Podpis odbiorcy"
          :capture-recipient-name="true"
          @signature-saved="handleSignature"
        />
      </div>

      <!-- Actions -->
      <div class="pt-4 border-t border-gray-700">
        <button
          @click="completeStop"
          :disabled="!canComplete"
          class="w-full bg-green-600 hover:bg-green-700 disabled:bg-gray-700 disabled:cursor-not-allowed text-white font-semibold py-3 rounded-lg"
        >
          Zakończ dostawę
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import Scanner from './Scanner.vue'
import CameraCapture from './CameraCapture.vue'
import SignaturePad from './SignaturePad.vue'
import { updateStopStatus } from '../services/routeService'

interface Stop {
  id: string
  address: string
  type: 'PICKUP' | 'DELIVERY'
  status: string
  orderId?: string
  lat?: number
  lon?: number
}

interface Props {
  stop: Stop
  stopIndex: number
  workflowConfig?: any
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'stop-completed', stopId: string): void
}>()

const currentStatus = ref(props.stop.status)
const scannedBarcode = ref<string | null>(null)
const manualBarcode = ref('')
const deliveryCode = ref<string | null>(null)
const dmgPhotos = ref<File[]>([])
const podPhotos = ref<File[]>([])
const signature = ref<string | null>(null)
const recipientName = ref<string | null>(null)

// Workflow configuration checks
const requiresBarcode = computed(() => {
  return props.workflowConfig?.scan?.barcode?.enabled !== false
})

const allowManualEntry = computed(() => {
  return props.workflowConfig?.scan?.barcode?.allowManual !== false
})

const requiresDeliveryCode = computed(() => {
  // Conditional logic based on address/client policy
  // Schema: address.requiresDeliveryCode, client.scanDeliveryCodePolicy
  if (!props.stop || !props.workflowConfig) {
    return false
  }

  // Check if workflow config has delivery code enabled
  const config = props.workflowConfig.scan?.deliveryCode
  if (config?.enabled === false) {
    return false
  }
  if (config?.enabled === true) {
    return true
  }

  // Conditional mode: check address and client policy
  if (config?.enabled === 'conditional' || config?.enabled === 'IF_RAMP') {
    // Check client policy first
    const client = (props.stop as any).order?.client || (props.stop as any).client
    if (client?.scanDeliveryCodePolicy) {
      if (client.scanDeliveryCodePolicy === 'ALWAYS') {
        return true
      }
      if (client.scanDeliveryCodePolicy === 'NEVER') {
        return false
      }
      // IF_RAMP - check address
      if (client.scanDeliveryCodePolicy === 'IF_RAMP') {
        const address = (props.stop as any).deliveryAddress || (props.stop as any).address
        return address?.requiresDeliveryCode === true
      }
    }

    // Fallback: check address directly
    const address = (props.stop as any).deliveryAddress || (props.stop as any).address
    return address?.requiresDeliveryCode === true
  }

  return false
})

const showDmgCamera = computed(() => {
  return props.workflowConfig?.photo?.dmg?.enabled !== false
})

const showPodCamera = computed(() => {
  return props.workflowConfig?.photo?.pod?.enabled !== false
})

const requiresSignature = computed(() => {
  return props.workflowConfig?.signature?.required !== false
})

const availableStatuses = computed(() => {
  return props.workflowConfig?.statuses || [
    'IN_TRANSIT',
    'ARRIVED',
    'LOADING',
    'UNLOADING',
    'POD',
    'ISSUE',
    'COMPLETED'
  ]
})

const statusLabels: Record<string, string> = {
  'IN_TRANSIT': 'W drodze',
  'ARRIVED': 'Przybył',
  'LOADING': 'Załadunek',
  'UNLOADING': 'Rozładunek',
  'POD': 'Dostarczono',
  'ISSUE': 'Problem',
  'COMPLETED': 'Zakończone'
}

const canComplete = computed(() => {
  // Check if all required steps are completed
  if (requiresBarcode.value && !scannedBarcode.value && !manualBarcode.value) {
    return false
  }
  if (showPodCamera.value && podPhotos.value.length === 0) {
    return false
  }
  if (requiresSignature.value && !signature.value) {
    return false
  }
  return currentStatus.value === 'POD' || currentStatus.value === 'COMPLETED'
})

const handleBarcodeScan = (barcode: string) => {
  scannedBarcode.value = barcode
  // TODO: Validate barcode matches order
}

const handleManualBarcode = () => {
  if (manualBarcode.value) {
    scannedBarcode.value = manualBarcode.value
    manualBarcode.value = ''
  }
}

const handleDeliveryCodeScan = (code: string) => {
  deliveryCode.value = code
}

const handleDmgPhoto = (file: File, dataUrl: string) => {
  dmgPhotos.value.push(file)
  // TODO: Upload to backend
}

const handlePodPhoto = (file: File, dataUrl: string) => {
  podPhotos.value.push(file)
  // TODO: Upload to backend
}

const handleSignature = (dataUrl: string, name?: string) => {
  signature.value = dataUrl
  recipientName.value = name || null
  // TODO: Upload to backend
}

const updateStatus = async (status: string) => {
  currentStatus.value = status
  
  try {
    await updateStopStatus({
      stopId: props.stop.id,
      status: status,
      location: props.stop.lat && props.stop.lon
        ? { lat: props.stop.lat, lon: props.stop.lon }
        : undefined,
      timestamp: new Date().toISOString()
    })
  } catch (error) {
    console.error('Failed to update status:', error)
  }
}

const completeStop = async () => {
  if (!canComplete.value) return

  await updateStatus('COMPLETED')
  emit('stop-completed', props.stop.id)
}

const handleScannerError = (error: any) => {
  console.error('Scanner error:', error)
}

const handleCameraError = (error: string) => {
  console.error('Camera error:', error)
}
</script>

<style scoped>
.stop-action-sheet {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  max-height: 90vh;
  border-top-left-radius: 1rem;
  border-top-right-radius: 1rem;
  box-shadow: 0 -4px 6px rgba(0, 0, 0, 0.1);
  z-index: 50;
}
</style>
