<template>
  <div class="fixed inset-0 bg-black z-50 flex flex-col">
    <!-- Header -->
    <div class="absolute top-0 left-0 right-0 z-20 p-4 bg-gradient-to-b from-black/80 to-transparent flex justify-between items-center">
      <h2 class="text-white font-bold text-lg">AR Loading Assistant</h2>
      <button @click="$emit('close')" class="w-10 h-10 rounded-full bg-white/20 flex items-center justify-center text-white backdrop-blur-md">
        ✕
      </button>
    </div>

    <!-- Camera View -->
    <div id="ar-reader" class="flex-1 w-full h-full object-cover"></div>

    <!-- Overlay Feedback -->
    <div v-if="scanResult" 
         class="absolute inset-0 z-10 flex flex-col items-center justify-center p-8 transition-colors duration-300 bg-black/60 backdrop-blur-sm"
         :class="scanResult.valid ? 'bg-green-900/40' : 'bg-red-900/40'"
         @click="resetScan">
      
      <div class="bg-white rounded-2xl p-8 w-full max-w-sm shadow-2xl transform transition-all animate-bounce-in text-center">
        <!-- Icon -->
        <div class="w-20 h-20 rounded-full mx-auto mb-6 flex items-center justify-center text-4xl shadow-lg"
             :class="scanResult.valid ? 'bg-green-100 text-green-600' : 'bg-red-100 text-red-600'">
          {{ scanResult.valid ? '✅' : '⛔' }}
        </div>

        <!-- Content -->
        <div v-if="scanResult.valid">
          <div v-if="scanResult.isParent">
             <h3 class="text-2xl font-bold text-purple-600 mb-2">CONTAINER DETECTED</h3>
             <p class="text-gray-600 mb-4">You scanned a master asset.</p>
             <div class="bg-purple-50 p-4 rounded-xl mb-4">
               <p class="text-xs text-purple-500 uppercase tracking-wider font-bold">Contents</p>
               <p class="text-3xl font-bold text-purple-900">{{ scanResult.childCount }} Items</p>
             </div>
             <button class="w-full bg-purple-600 text-white font-bold py-3 rounded-xl shadow-lg hover:bg-purple-700 transition-colors">
               LOAD ALL
             </button>
          </div>
          <div v-else>
            <h3 class="text-2xl font-bold text-gray-900 mb-2">LOAD THIS PACKAGE</h3>
            <div class="space-y-4">
              <div class="bg-gray-50 p-4 rounded-xl">
                <p class="text-xs text-gray-500 uppercase tracking-wider font-bold">Customer</p>
                <p class="text-lg font-bold text-gray-900">{{ scanResult.stop.customer }}</p>
              </div>
              <div class="bg-gray-50 p-4 rounded-xl">
                <p class="text-xs text-gray-500 uppercase tracking-wider font-bold">Address</p>
                <p class="text-sm text-gray-700">{{ scanResult.stop.address }}</p>
              </div>
              <div v-if="scanResult.stop.type === 'PICKUP'" class="inline-block px-3 py-1 bg-orange-100 text-orange-700 rounded-full text-sm font-bold">
                PICKUP
              </div>
            </div>
          </div>
        </div>

        <div v-else>
          <h3 class="text-2xl font-bold text-red-600 mb-2">DO NOT LOAD</h3>
          <p class="text-gray-600">This package is not on your manifest.</p>
          <p class="mt-4 font-mono text-sm bg-gray-100 p-2 rounded">{{ scanResult.code }}</p>
        </div>

        <p class="mt-8 text-sm text-gray-400">Tap anywhere to continue scanning</p>
      </div>
    </div>

    <!-- Scanning Guide -->
    <div v-else class="absolute inset-0 pointer-events-none flex items-center justify-center">
      <div class="w-64 h-64 border-2 border-white/50 rounded-lg relative">
        <div class="absolute top-0 left-0 w-4 h-4 border-t-4 border-l-4 border-white"></div>
        <div class="absolute top-0 right-0 w-4 h-4 border-t-4 border-r-4 border-white"></div>
        <div class="absolute bottom-0 left-0 w-4 h-4 border-b-4 border-l-4 border-white"></div>
        <div class="absolute bottom-0 right-0 w-4 h-4 border-b-4 border-r-4 border-white"></div>
      </div>
      <p class="absolute bottom-20 text-white/80 font-medium bg-black/40 px-4 py-2 rounded-full backdrop-blur-md">
        Point at package barcode
      </p>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue';
import { Html5Qrcode } from 'html5-qrcode';

const props = defineProps({
  manifest: {
    type: Array,
    required: true
  }
});

const emit = defineEmits(['close']);

const scanResult = ref(null);
let html5QrCode = null;

const onScanSuccess = (decodedText, decodedResult) => {
  if (scanResult.value) return; // Already showing a result

  // Check if barcode matches any stop ID or tracking number in manifest
  // For MVP, we assume the barcode contains the stop ID or a tracking number associated with the stop
  // Simplified matching logic: Check if any stop.id or stop.trackingNumber contains the scanned text
  
  // New Logic for Bulk Assets:
  // In a real scenario, we would query the backend /api/assets/{id}/hierarchy to check if this is a parent asset.
  // For this MVP demo, we will simulate a "Parent" scan if the code starts with "PALLET-" or "CAGE-"
  
  if (decodedText.startsWith("PALLET-") || decodedText.startsWith("CAGE-")) {
     // Simulate finding a parent container
     // We assume this container belongs to the current route (simplified)
     const childCount = 50; // Mock count
     scanResult.value = {
       valid: true,
       isParent: true,
       code: decodedText,
       childCount: childCount,
       stop: { customer: "BULK SHIPMENT", address: "Multiple Destinations" }
     };
     new Audio('/sounds/success.mp3').play().catch(() => {});
     return;
  }

  const match = props.manifest.find(stop => 
    stop.id === decodedText || 
    (stop.trackingNumber && stop.trackingNumber === decodedText) ||
    // Loose matching for demo purposes if exact match fails
    JSON.stringify(stop).includes(decodedText)
  );

  if (match) {
    scanResult.value = {
      valid: true,
      stop: match,
      code: decodedText
    };
    // Play success sound
    new Audio('/sounds/success.mp3').play().catch(() => {}); // Ignore if file missing
  } else {
    scanResult.value = {
      valid: false,
      code: decodedText
    };
    // Play error sound
    new Audio('/sounds/error.mp3').play().catch(() => {});
  }
};

const resetScan = () => {
  scanResult.value = null;
};

onMounted(() => {
  html5QrCode = new Html5Qrcode("ar-reader");
  const config = { fps: 10, qrbox: { width: 250, height: 250 } };
  
  html5QrCode.start(
    { facingMode: "environment" }, 
    config, 
    onScanSuccess
  ).catch(err => {
    console.error("Error starting scanner", err);
  });
});

onUnmounted(() => {
  if (html5QrCode) {
    html5QrCode.stop().then(() => {
      html5QrCode.clear();
    }).catch(err => {
      console.error("Failed to stop scanner", err);
    });
  }
});
</script>

<style scoped>
.animate-bounce-in {
  animation: bounce-in 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275) both;
}

@keyframes bounce-in {
  0% {
    opacity: 0;
    transform: scale(0.3);
  }
  50% {
    opacity: 1;
    transform: scale(1.05);
  }
  70% {
    transform: scale(0.9);
  }
  100% {
    transform: scale(1);
  }
}
</style>
