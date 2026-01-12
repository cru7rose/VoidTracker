<template>
  <div class="relative w-full h-full flex flex-col items-center bg-black/95 text-white">
    
    <!-- State: Geofence Check (DRIVER Mode Only) -->
    <div v-if="mode === 'DRIVER' && gpsStatus !== 'OK' && !overrideGeofence" class="absolute inset-0 z-50 flex flex-col items-center justify-center p-6 text-center bg-gray-900">
      
      <!-- Loading State -->
      <div v-if="gpsStatus === 'PENDING'" class="space-y-4">
        <div class="animate-spin w-12 h-12 border-4 border-brand-500 border-t-transparent rounded-full mx-auto"></div>
        <h3 class="text-xl font-bold">Verifying Location...</h3>
        <p class="text-gray-400">Ensuring you are at the correct stop.</p>
      </div>

      <!-- Anomaly State -->
      <div v-else-if="gpsStatus === 'ANOMALY'" class="space-y-6">
        <div class="w-20 h-20 bg-red-500/20 rounded-full flex items-center justify-center mx-auto ring-4 ring-red-500/20">
           <svg xmlns="http://www.w3.org/2000/svg" class="h-10 w-10 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
        </div>
        
        <div>
          <h3 class="text-2xl font-bold text-white mb-2">Distance Alert</h3>
          <p class="text-gray-300">
            You are <span class="text-red-400 font-mono font-bold">{{ formattedDistance }}</span> away from the target.
          </p>
          <p class="text-sm text-gray-500 mt-2">Allowed radius: 300m</p>
        </div>

        <div class="w-full space-y-3">
          <button @click="confirmAnomaly" class="w-full py-4 bg-red-600 hover:bg-red-700 active:bg-red-800 rounded-xl font-bold text-white transition-colors">
            Confirm I am Here
          </button>
          <button @click="retryGps" class="w-full py-4 bg-white/10 hover:bg-white/20 rounded-xl font-medium text-white transition-colors">
            Retry GPS
          </button>
        </div>
      </div>
    </div>

    <!-- Main View -->
    <div v-else class="relative w-full flex-1 flex flex-col">
      <!-- Camera Feed (Hidden when photo captured) -->
      <div v-show="!capturedImage" id="reader" class="w-full flex-1 object-cover overflow-hidden bg-black"></div>
      
      <!-- Captured Photo View -->
      <div v-if="capturedImage" class="w-full flex-1 flex items-center justify-center relative bg-black">
        <img :src="capturedImageUrl" class="max-w-full max-h-full object-contain" />
        
        <!-- Analysis Result Overlay -->
        <div v-if="analysisResult" class="absolute inset-0 bg-black/60 flex flex-col items-center justify-center p-6 text-center animate-fade-in">
           <div class="mb-6">
             <div v-if="analysisResult.condition === 'DAMAGED'" class="text-red-500 font-bold text-4xl mb-2">⚠ DAMAGED</div>
             <div v-else class="text-green-500 font-bold text-4xl mb-2">✓ GOOD</div>
             <p class="text-gray-300">Confidence: {{ (analysisResult.confidence * 100).toFixed(0) }}%</p>
             <div class="flex flex-wrap justify-center gap-2 mt-4">
               <span v-for="tag in analysisResult.tags" :key="tag" class="px-3 py-1 bg-white/10 rounded-full text-sm font-mono">{{ tag }}</span>
             </div>
           </div>
           <button @click="resetPhoto" class="px-8 py-3 bg-white text-black font-bold rounded-xl hover:bg-gray-200">
             Continue
           </button>
        </div>
      </div>

      <!-- UI Overlay (Controls) -->
      <div class="absolute inset-0 pointer-events-none flex flex-col items-center justify-between z-10 p-4">
        
        <!-- Header -->
        <div class="w-full flex justify-between items-center pointer-events-auto">
          <span class="px-3 py-1 rounded-full bg-white/10 backdrop-blur-md text-xs font-mono">
            {{ mode === 'DRIVER' ? 'DELIVERY MODE' : 'HUB MODE' }}
          </span>
          
           <!-- Mode Toggle -->
           <div class="bg-black/40 backdrop-blur-md rounded-full p-1 flex">
             <button 
               @click="setUiMode('SCAN')" 
               class="px-4 py-1.5 rounded-full text-xs font-bold transition-all"
               :class="uiMode === 'SCAN' ? 'bg-white text-black' : 'text-gray-400 hover:text-white'"
             >
               SCAN
             </button>
             <button 
               @click="setUiMode('PHOTO')" 
               class="px-4 py-1.5 rounded-full text-xs font-bold transition-all"
               :class="uiMode === 'PHOTO' ? 'bg-white text-black' : 'text-gray-400 hover:text-white'"
             >
               PHOTO
             </button>
           </div>
        </div>

        <!-- Scanner Overlay (Only in SCAN mode) -->
        <div v-if="uiMode === 'SCAN' && !capturedImage" class="w-72 h-72 border-2 border-brand-500/50 rounded-3xl relative pointer-events-none mt-10">
          <div class="absolute top-0 left-0 w-8 h-8 border-t-4 border-l-4 border-brand-400 -mt-0.5 -ml-0.5 rounded-tl-xl"></div>
          <div class="absolute top-0 right-0 w-8 h-8 border-t-4 border-r-4 border-brand-400 -mt-0.5 -mr-0.5 rounded-tr-xl"></div>
          <div class="absolute bottom-0 left-0 w-8 h-8 border-b-4 border-l-4 border-brand-400 -mb-0.5 -ml-0.5 rounded-bl-xl"></div>
          <div class="absolute bottom-0 right-0 w-8 h-8 border-b-4 border-r-4 border-brand-400 -mb-0.5 -mr-0.5 rounded-br-xl"></div>
          <div class="absolute top-0 left-0 w-full h-0.5 bg-brand-400 shadow-[0_0_20px_rgba(45,212,191,0.8)] animate-scan"></div>
        </div>
        
        <!-- Instructions / Status -->
        <div class="mb-20 pointer-events-auto w-full max-w-sm">
           <!-- Scan Instructions -->
           <div v-if="uiMode === 'SCAN'" class="text-center space-y-1">
             <p class="text-lg font-bold text-white drop-shadow-md">Scan Barcode</p>
             <p class="text-sm text-gray-300 drop-shadow-md">Align code within the frame</p>
           </div>

           <!-- Photo Controls -->
           <div v-else-if="uiMode === 'PHOTO'" class="flex flex-col items-center gap-4">
              <div v-if="!capturedImage" class="animate-pulse text-white font-mono text-sm">Visual Verification Active</div>
              
              <!-- Shutter Button -->
              <button 
                v-if="!capturedImage"
                @click="takePhoto" 
                class="w-20 h-20 rounded-full border-4 border-white flex items-center justify-center active:scale-95 transition-transform bg-white/10 hover:bg-white/20"
              >
                <div class="w-16 h-16 bg-white rounded-full"></div>
              </button>

              <!-- Confirm Actions -->
              <div v-else-if="!analysisResult" class="flex gap-4 w-full">
                <button @click="resetPhoto" class="flex-1 py-3 bg-white/10 backdrop-blur-md rounded-xl font-bold hover:bg-white/20">Retake</button>
                <button @click="analyzePhoto" class="flex-1 py-3 bg-brand-500 rounded-xl font-bold hover:bg-brand-600">
                  <span v-if="isAnalyzing" class="animate-pulse">Analyzing...</span>
                  <span v-else>Analyze</span>
                </button>
              </div>
           </div>
        </div>

      </div>
    </div>

    <!-- Error Toast -->
    <div v-if="scanError" class="absolute top-20 left-4 right-4 bg-red-500 text-white p-4 rounded-xl shadow-lg z-50 animate-bounce-in">
      <div class="flex items-center gap-3">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <span class="font-bold">{{ scanError }}</span>
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
.animate-bounce-in {
  animation: bounce-in 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275) both;
}
@keyframes bounce-in {
  0% { opacity: 0; transform: translateY(-20px); }
  100% { opacity: 1; transform: translateY(0); }
}
.animate-fade-in {
  animation: fade-in 0.3s ease-out both;
}
@keyframes fade-in {
  0% { opacity: 0; } 100% { opacity: 1; }
}
</style>

<script setup>
import { onMounted, onBeforeUnmount, ref, computed, watch, nextTick } from 'vue'
import { Html5Qrcode } from 'html5-qrcode'
import { useGeolocation } from '../composables/useGeolocation'
import { calculateDistance, formatDistance } from '../utils/geoUtils'
import RouteService from '../services/RouteService'

const props = defineProps({
  mode: {
    type: String,
    default: 'DRIVER', // 'DRIVER' or 'HUB'
    validator: (value) => ['DRIVER', 'HUB'].includes(value)
  },
  targetLocation: {
    type: Object, // { lat: number, lon: number }
    default: null
  },
  hubManifest: {
    type: Array, // Array of strings (allowed barcodes)
    default: () => []
  },
  orderId: {
    type: String,
    default: null
  }
})

const emit = defineEmits(['scan', 'anomaly-confirmed', 'photo-analyzed'])

// UI Mode
const uiMode = ref('SCAN') // 'SCAN' | 'PHOTO'
function setUiMode(mode) {
  uiMode.value = mode
  // We can keep scanner running in background, or pause it?
  // Html5Qrcode keeps the camera open. We need the video feed for Photo mode too.
}

// Scanner Logic
let html5QrCode = null
const isScanning = ref(false)
const scanError = ref(null)

// Photo Logic
const capturedImage = ref(null) // Blob
const capturedImageUrl = ref(null)
const isAnalyzing = ref(false)
const analysisResult = ref(null)

function takePhoto() {
  const videoElement = document.querySelector('#reader video');
  if (!videoElement) {
    showError("Camera feed not found.");
    return;
  }
  
  const canvas = document.createElement('canvas');
  canvas.width = videoElement.videoWidth;
  canvas.height = videoElement.videoHeight;
  const ctx = canvas.getContext('2d');
  ctx.drawImage(videoElement, 0, 0, canvas.width, canvas.height);
  
  canvas.toBlob((blob) => {
    capturedImage.value = blob;
    capturedImageUrl.value = URL.createObjectURL(blob);
    // Pause scanner to save resources? 
    // html5QrCode.pause(); // Optional
  }, 'image/jpeg', 0.8);
}

function resetPhoto() {
  capturedImage.value = null
  capturedImageUrl.value = null
  analysisResult.value = null
  isAnalyzing.value = false
  // html5QrCode.resume();
}

async function analyzePhoto() {
  if (!capturedImage.value) return;
  
  isAnalyzing.value = true;
  try {
    // Simulate DAMAGED if user wants (e.g. by shaking device? Nah, just normal mock)
    // The backend mock checks for "damage" in filename.
    // We send blob with name "capture.jpg".
    // To trigger "damage", we might need a hidden trick or just randomness?
    // User requested "Mock Logic: If image filename contains 'damage'".
    // But blob filename is set by us.
    // Let's modify RouteService to append "damage" if a hidden toggle is active?
    // Or simpler: The user (me) during verification will likely not be able to change the filename sent by browser blob easily.
    // FIX: Let's assume the backend mock also randomly detects damage or we send a specific header?
    // Or I change backend logic to check for a query param?
    // I will stick to the plan. I will check logic.
    // Actually, I can control the filename in RouteService via formData.append('file', blob, 'filename.jpg')
    // I will make it random for demo purposes: 50% chance of damage if I click "Analyze"?
    // No, that's confusing.
    // I'll stick to passing the blob. The current backend logic expects "damage" in filename. 
    // I will update the backend logic slightly to be more robust or purely random, 
    // OR I add a debug button "Analyze (Simulate Damage)"
    
    // For now, let's just call the service.
    // I'll trust the plan. 
    // NOTE: I implemented backend to check filename "damage".
    // I will modify client to send "damage.jpg" if I hold Shift? No, touch interface.
    // I will simply send "capture.jpg" which means "GOOD".
    // If I want to test "DAMAGED", I might need to change the code temporarily or add a dev toggle.
    // I'll add a Dev Toggle in UI: Click the "Scanning Laser" to toggle damage simulation?
    // Too complex.
    // I'll just send 'capture.jpg'.
    
    // Wait, let's restart: backend checks "damage" in filename.
    // I will send 'damage.jpg' if text contains "damage" (via manual entry)? No, this is photo.
    // I will simply rename the file in `RouteService` to `damage.jpg` if I want to verify damage logic.
    // But for the main code, `capture.jpg` is fine.
    
    const response = await RouteService.analyzeImage(capturedImage.value, props.orderId);
    analysisResult.value = response.data;
    
    emit('photo-analyzed', response.data);
    
  } catch (err) {
    console.error("Analysis failed", err);
    showError("Analysis failed: " + err.message);
    isAnalyzing.value = false;
  }
}

// Geolocation Logic
const { coords, getPosition } = useGeolocation()
const gpsStatus = ref('PENDING') // 'PENDING', 'OK', 'ANOMALY'
const overrideGeofence = ref(false)
const distance = ref(0)
const formattedDistance = computed(() => formatDistance(distance.value))

onMounted(async () => {
  if (props.mode === 'DRIVER') {
    await checkLocation()
  } else {
    // HUB mode doesn't strictly need Geofence for this MVP, start scanning immediately
    startScanning()
  }
})

onBeforeUnmount(async () => {
  if (html5QrCode) {
    try {
      await html5QrCode.stop()
      await html5QrCode.clear()
    } catch (err) {
      console.error("Scanner cleanup error:", err)
    }
  }
})

// Distance Check Logic
async function checkLocation() {
  gpsStatus.value = 'PENDING'
  try {
    const position = await getPosition()
    if (!props.targetLocation) {
        // No target set, assume OK (e.g. ad-hoc scan)
        gpsStatus.value = 'OK'
        startScanning()
        return
    }

    const dist = calculateDistance(
      position.lat, position.lon,
      props.targetLocation.lat, props.targetLocation.lon
    )
    distance.value = dist

    console.log(`Distance to target: ${dist}m`)

    if (dist > 300) {
      gpsStatus.value = 'ANOMALY'
    } else {
      gpsStatus.value = 'OK'
      startScanning()
    }

  } catch (err) {
    console.warn("GPS Error:", err)
    // If GPS fails, treat as Anomaly to force manual confirmation? 
    // Or maybe a different error state. For now, treating as 'ANOMALY' with safety default
    distance.value = 9999
    gpsStatus.value = 'ANOMALY'
  }
}

function confirmAnomaly() {
  overrideGeofence.value = true
  emit('anomaly-confirmed', { distance: distance.value, coords: coords.value })
  startScanning()
}

function retryGps() {
  checkLocation()
}


// Scanning Logic
async function startScanning() {
  if (html5QrCode) return // Already running

  try {
    const deviceId = await getCameraId()
    html5QrCode = new Html5Qrcode("reader")
    
    const config = { 
      fps: 10, 
      qrbox: { width: 250, height: 250 },
      aspectRatio: 1.0 
    }
    
    await html5QrCode.start(
      deviceId, // Prefer back camera
      config,
      onScanSuccess,
      onScanFailure
    )
    isScanning.value = true
  } catch (err) {
    console.error("Scanner Start Error:", err)
    scanError.value = "Camera access failed."
  }
}

async function getCameraId() {
    // Basic camera selection logic - prefer environment
    try {
        const devices = await Html5Qrcode.getCameras()
        if (devices && devices.length) {
             // Filter for back camera if possible
             const backCam = devices.find(d => d.label.toLowerCase().includes('back') || d.label.toLowerCase().includes('environment'))
             return backCam ? backCam.id : devices[0].id
        }
        return { facingMode: "environment" }
    } catch {
        return { facingMode: "environment" }
    }
}

function onScanSuccess(decodedText, decodedResult) {
  if (uiMode.value === 'PHOTO') return; // Ignore codes in photo mode

  // HUB Mode Verification
  if (props.mode === 'HUB' && props.hubManifest.length > 0) {
    if (!props.hubManifest.includes(decodedText)) {
      showError("WRONG LANE: Item not in manifest!")
      // Play error sound?
      return 
    }
  }

  // Success
  emit('scan', {
    barcode: decodedText,
    gps: coords.value,
    anomaly: overrideGeofence.value,
    distance: distance.value
  })
}

function onScanFailure(error) {
  // Ignore frame read errors
}

function showError(msg) {
  scanError.value = msg
  setTimeout(() => {
    scanError.value = null
  }, 3000)
}

</script>
