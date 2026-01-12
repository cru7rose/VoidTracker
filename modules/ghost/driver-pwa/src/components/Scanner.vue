<template>
  <div class="scanner-container relative">
    <div id="reader" class="w-full h-80 bg-black rounded-lg overflow-hidden relative">
      <!-- Overlay for branding/focus -->
      <div v-if="isScanning" class="absolute inset-0 pointer-events-none border-2 border-blue-500/30">
        <div class="absolute inset-0 flex items-center justify-center">
            <div class="w-64 h-64 border-2 border-blue-400 rounded-lg relative">
                <div class="absolute top-0 left-0 w-4 h-4 border-t-4 border-l-4 border-blue-500 -mt-1 -ml-1"></div>
                <div class="absolute top-0 right-0 w-4 h-4 border-t-4 border-r-4 border-blue-500 -mt-1 -mr-1"></div>
                <div class="absolute bottom-0 left-0 w-4 h-4 border-b-4 border-l-4 border-blue-500 -mb-1 -ml-1"></div>
                <div class="absolute bottom-0 right-0 w-4 h-4 border-b-4 border-r-4 border-blue-500 -mb-1 -mr-1"></div>
            </div>
        </div>
      </div>
    </div>
    
    <div class="mt-4 flex justify-center">
      <button 
        v-if="!isScanning" 
        @click="startScan" 
        class="bg-blue-600 hover:bg-blue-500 text-white font-bold py-3 px-8 rounded-full shadow-lg transition-transform active:scale-95 flex items-center gap-2"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M15 3h6v6"/><path d="M14 10l6.1-6.1"/><path d="M21 21v-6h-6"/><path d="M3 21h6v-6"/><path d="M3 3v6h6"/><path d="M10 14l-6.1 6.1"/></svg>
        Skanuj
      </button>
      <button 
        v-else 
        @click="stopScan" 
        class="bg-red-600 hover:bg-red-500 text-white font-bold py-3 px-8 rounded-full shadow-lg transition-transform active:scale-95"
      >
        Stop
      </button>
    </div>
    
    <p v-if="error" class="text-red-400 text-sm mt-4 text-center bg-red-900/20 p-2 rounded">{{ error }}</p>
  </div>
</template>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue';
import { Html5Qrcode } from "html5-qrcode";

const emit = defineEmits<{
  (e: 'scan-success', decodedText: string): void
  (e: 'scan-failure', error: any): void
  (e: 'camera-error', error: any): void
}>();

const isScanning = ref(false);
const error = ref('');
let html5QrCode: Html5Qrcode | null = null;

// Reused config from legacy
const QR_CONFIG = {
  fps: 10,
  qrbox: { width: 250, height: 250 }
};

const onScanSuccess = (decodedText: string) => {
  emit('scan-success', decodedText);
  // Ideally play a beep sound here
};

const onScanFailure = (_errorMessage: any) => {
  // console.warn(`Code scan error = ${errorMessage}`);
  // emit('scan-failure', errorMessage); // Too noisy usually
};

const startScan = async () => {
  error.value = '';
  try {
    const devices = await Html5Qrcode.getCameras();
    if (devices && devices.length) {
      // Logic: Prefer back camera (usually last in list or labelled 'back')
      const backCamera = devices.find(d => d.label.toLowerCase().includes('back')) || devices[devices.length - 1];
      if (!backCamera) throw new Error("Camera not found");
      const cameraId = backCamera.id;
      
      html5QrCode = new Html5Qrcode("reader");
      await html5QrCode.start(
        cameraId, 
        QR_CONFIG,
        onScanSuccess,
        onScanFailure
      );
      isScanning.value = true;
    } else {
      error.value = "Nie wykryto kamery.";
      emit('camera-error', "No cameras found");
    }
  } catch (err) {
    console.error(err);
    error.value = "Błąd kamery. Sprawdź uprawnienia.";
    emit('camera-error', err);
  }
};

const stopScan = async () => {
  if (html5QrCode && isScanning.value) {
    try {
      await html5QrCode.stop();
      html5QrCode.clear();
      isScanning.value = false;
    } catch (err) {
      console.error("Failed to stop scanner", err);
    }
  }
};

onUnmounted(() => {
  if (isScanning.value) {
    stopScan();
  }
});

defineExpose({ startScan, stopScan });
</script>

<style scoped>
/* Ensure the reader div has size */
#reader {
  min-height: 300px;
}
</style>
