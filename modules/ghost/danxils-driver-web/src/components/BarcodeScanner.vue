<template>
  <div class="relative w-full max-w-sm aspect-square bg-black rounded-3xl overflow-hidden shadow-2xl border border-white/10">
    <div id="reader" class="w-full h-full object-cover"></div>
    
    <!-- Custom Overlay -->
    <div v-if="isScanning" class="absolute inset-0 pointer-events-none flex items-center justify-center z-10">
      <div class="w-64 h-64 border-2 border-brand-500/50 rounded-2xl relative">
        <!-- Corners -->
        <div class="absolute top-0 left-0 w-6 h-6 border-t-4 border-l-4 border-brand-400 -mt-1 -ml-1 rounded-tl-lg"></div>
        <div class="absolute top-0 right-0 w-6 h-6 border-t-4 border-r-4 border-brand-400 -mt-1 -mr-1 rounded-tr-lg"></div>
        <div class="absolute bottom-0 left-0 w-6 h-6 border-b-4 border-l-4 border-brand-400 -mb-1 -ml-1 rounded-bl-lg"></div>
        <div class="absolute bottom-0 right-0 w-6 h-6 border-b-4 border-r-4 border-brand-400 -mb-1 -mr-1 rounded-br-lg"></div>
        
        <!-- Scanning Line -->
        <div class="absolute top-0 left-0 w-full h-1 bg-brand-400/80 shadow-[0_0_15px_rgba(45,212,191,0.8)] animate-scan"></div>
      </div>
    </div>

    <!-- Error Message -->
    <div v-if="error" class="absolute inset-0 flex items-center justify-center bg-black/80 z-20 p-4 text-center">
      <div class="text-red-400">
        <p class="font-bold mb-2">Camera Error</p>
        <p class="text-sm">{{ error }}</p>
        <button @click="startScanning" class="mt-4 px-4 py-2 bg-white/10 rounded-lg text-white text-sm hover:bg-white/20">
          Retry
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
@keyframes scan {
  0% { top: 0; opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { top: 100%; opacity: 0; }
}
.animate-scan {
  animation: scan 2s cubic-bezier(0.4, 0, 0.2, 1) infinite;
}
</style>

<script setup>
import { onMounted, onBeforeUnmount, ref } from 'vue'
import { Html5Qrcode } from 'html5-qrcode'

const emit = defineEmits(['scan'])
const isScanning = ref(false)
const error = ref(null)
let html5QrCode = null

onMounted(() => {
  startScanning()
})

onBeforeUnmount(async () => {
  if (html5QrCode && isScanning.value) {
    try {
      await html5QrCode.stop()
    } catch (err) {
      console.error("Failed to stop scanner:", err)
    }
  }
})

async function startScanning() {
  error.value = null
  
  try {
    html5QrCode = new Html5Qrcode("reader")
    
    const config = { fps: 10, qrbox: { width: 250, height: 250 } }
    
    await html5QrCode.start(
      { facingMode: "environment" },
      config,
      onScanSuccess,
      onScanFailure
    )
    
    isScanning.value = true
  } catch (err) {
    console.error("Error starting scanner:", err)
    error.value = "Could not access camera. Please ensure permissions are granted."
    isScanning.value = false
  }
}

function onScanSuccess(decodedText, decodedResult) {
  emit('scan', decodedText)
  // Optional: Stop scanning after success if desired, or keep scanning
  // stopScanning() 
}

function onScanFailure(error) {
  // handle scan failure, usually better to ignore and keep scanning.
}
</script>
