<template>
  <div class="scanner-container">
    <div id="reader" class="w-full h-64 bg-black rounded-lg overflow-hidden"></div>
    <div class="mt-4 flex justify-center">
      <button 
        v-if="!isScanning" 
        @click="startScan" 
        class="bg-green-600 text-white px-4 py-2 rounded shadow"
      >
        Start Camera
      </button>
      <button 
        v-else 
        @click="stopScan" 
        class="bg-red-600 text-white px-4 py-2 rounded shadow"
      >
        Stop Camera
      </button>
    </div>
    <p v-if="error" class="text-red-500 text-sm mt-2 text-center">{{ error }}</p>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { Html5Qrcode } from "html5-qrcode";

const emit = defineEmits(['scan-success']);
const isScanning = ref(false);
const error = ref('');
let html5QrCode = null;

const onScanSuccess = (decodedText, decodedResult) => {
  // Handle the scanned code
  console.log(`Code matched = ${decodedText}`, decodedResult);
  emit('scan-success', decodedText);
  stopScan(); // Optional: stop after one scan
};

const onScanFailure = (errorMessage) => {
  // handle scan failure, usually better to ignore and keep scanning.
  // console.warn(`Code scan error = ${errorMessage}`);
};

const startScan = async () => {
  error.value = '';
  try {
    const devices = await Html5Qrcode.getCameras();
    if (devices && devices.length) {
      const cameraId = devices[devices.length - 1].id; // Use back camera usually at end
      
      html5QrCode = new Html5Qrcode("reader");
      await html5QrCode.start(
        cameraId, 
        {
          fps: 10,    // Optional, frame per seconds for qr code scanning
          qrbox: { width: 250, height: 250 }  // Optional, if you want bounded box UI
        },
        onScanSuccess,
        onScanFailure
      );
      isScanning.value = true;
    } else {
      error.value = "No cameras found.";
    }
  } catch (err) {
    error.value = "Error starting camera: " + err;
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
  stopScan();
});
</script>

<style scoped>
#reader {
  width: 100%;
}
</style>
