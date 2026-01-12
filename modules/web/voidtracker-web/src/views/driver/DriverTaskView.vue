<template>
  <div :class="['space-y-4 pb-20', isDarkMode ? 'dark' : '']">
    <div class="fixed top-4 right-4 z-50 flex gap-2">
        <div v-if="!offlineStore.isOnline" class="px-3 py-2 rounded-full bg-red-100 text-red-800 font-bold text-xs shadow-lg flex items-center gap-1">
            <span>📡 Offline</span>
            <span v-if="offlineStore.pendingCount > 0">({{ offlineStore.pendingCount }})</span>
        </div>
        <div v-else-if="offlineStore.pendingCount > 0" class="px-3 py-2 rounded-full bg-yellow-100 text-yellow-800 font-bold text-xs shadow-lg flex items-center gap-1">
            <span>🔄 Syncing ({{ offlineStore.pendingCount }})</span>
        </div>

        <button @click="toggleDarkMode" class="p-2 rounded-full bg-gray-200 dark:bg-gray-700 text-gray-800 dark:text-gray-200 shadow-lg">
            <span v-if="isDarkMode">☀️</span>
            <span v-else>🌙</span>
        </button>
    </div>

    <!-- Current Stop Info -->
    <div v-if="currentStop" class="bg-white dark:bg-gray-800 p-4 rounded-lg shadow transition-colors">
      <h2 class="text-xl font-bold text-gray-800 dark:text-white">Stop #1: {{ currentStop.address }}</h2>
      <p class="text-gray-600 dark:text-gray-400">{{ currentStop.customerName }}</p>
      <div class="mt-2 flex gap-2">
        <a :href="`https://www.google.com/maps/dir/?api=1&destination=${currentStop.lat},${currentStop.lon}`" target="_blank" class="bg-blue-100 text-blue-700 px-3 py-1 rounded text-sm font-semibold">
          Navigate 📍
        </a>
        <span class="bg-yellow-100 text-yellow-700 px-3 py-1 rounded text-sm font-semibold">
          {{ currentStop.status }}
        </span>
      </div>
    </div>
    <div v-else class="bg-white dark:bg-gray-800 p-8 rounded-lg shadow text-center">
        <h2 class="text-xl font-bold text-gray-800 dark:text-white mb-2">No Tasks Assigned</h2>
        <p class="text-gray-600 dark:text-gray-400">You have no pending deliveries. Good job! 🎉</p>
        <button @click="fetchTasks" class="mt-4 px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">Refresh</button>
    </div>

    <!-- Dynamic Actions based on Config -->
    <div v-if="currentStop" class="bg-white dark:bg-gray-800 p-4 rounded-lg shadow space-y-4 transition-colors">
      <h3 class="font-semibold text-gray-700 dark:text-gray-300 border-b dark:border-gray-700 pb-2">Actions</h3>

      <!-- 1. Scan Barcode -->
      <div v-if="configStore.config.workflow.requireScan">
        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Scan Package</label>
        <div v-if="scannedCode" class="flex items-center gap-2 bg-green-50 p-2 rounded border border-green-200 text-green-700">
          <span>✅ Scanned: {{ scannedCode }}</span>
          <button @click="scannedCode = ''" class="text-xs underline">Reset</button>
        </div>
        <ScannerComponent v-else @scan-success="handleScan" />
        
        <div class="mt-2">
           <p class="text-xs text-center text-gray-500 dark:text-gray-400 mb-1">- OR -</p>
           <input v-model="manualCode" placeholder="Type barcode manually" class="w-full border dark:border-gray-600 rounded p-2 text-sm dark:bg-gray-700 dark:text-white" />
        </div>
      </div>

      <!-- 2. Take Photo -->
      <!-- 2. Take Photo -->
      <div v-if="configStore.config.workflow.requirePhoto">
        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Proof of Delivery (Photos)</label>
        
        <div class="grid grid-cols-3 gap-2 mb-2">
          <div v-for="(photo, index) in photos" :key="index" class="relative group">
            <img :src="photo" class="h-24 w-full object-cover rounded border" />
            <button @click="removePhoto(index)" class="absolute top-1 right-1 bg-red-500 text-white rounded-full p-1 shadow-sm opacity-0 group-hover:opacity-100 transition-opacity">
              <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>
            </button>
          </div>
          
          <div v-if="photos.length < (configStore.config.workflow.maxPhotos || 3)" class="h-24 border-2 border-dashed border-gray-300 dark:border-gray-600 rounded flex items-center justify-center bg-gray-50 dark:bg-gray-800 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors relative">
             <input type="file" accept="image/*" capture="environment" @change="handlePhoto" class="absolute inset-0 opacity-0 cursor-pointer" />
             <span class="text-gray-400 dark:text-gray-500 text-2xl">+</span>
          </div>
        </div>
        <p class="text-xs text-gray-500">{{ photos.length }} / {{ configStore.config.workflow.maxPhotos || 3 }} photos</p>
      </div>

      <!-- 3. Signature (Placeholder) -->
      <!-- 3. Signature -->
      <div v-if="configStore.config.workflow.requireSignature">
        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Customer Signature</label>
        <div class="border border-gray-300 dark:border-gray-600 rounded bg-white dark:bg-gray-200 touch-none">
            <canvas ref="signatureCanvas" class="w-full h-40"></canvas>
        </div>
        <div class="flex justify-end mt-1">
            <button @click="clearSignature" class="text-xs text-red-600 hover:text-red-800 dark:text-red-500">Clear Signature</button>
        </div>
      </div>

      <!-- Submit Button -->
      <button 
        @click="completeStop"
        class="w-full bg-green-600 hover:bg-green-700 text-white font-bold py-3 rounded-lg shadow-lg mt-4"
        :disabled="!isReadyToSubmit"
      >
        Complete Delivery
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue';
import ScannerComponent from './ScannerComponent.vue';
import SignaturePad from 'signature_pad';
import axios from 'axios';
import { useRouter } from 'vue-router';

import { useOfflineStore } from '@/stores/offlineStore';
import { notificationService } from '@/services/NotificationService';
import { useConfigStore } from '@/stores/configStore';
import { useAuthStore } from '@/stores/authStore';

const offlineStore = useOfflineStore();
const configStore = useConfigStore();
const authStore = useAuthStore();
const router = useRouter();

const isDarkMode = ref(false);
const toggleDarkMode = () => {
    isDarkMode.value = !isDarkMode.value;
    if (isDarkMode.value) {
        document.documentElement.classList.add('dark');
    } else {
        document.documentElement.classList.remove('dark');
    }
};

// Data
const tasks = ref([]);
const currentStop = computed(() => tasks.value.length > 0 ? tasks.value[0] : null);
const loading = ref(false);

const fetchTasks = async () => {
    loading.value = true;
    try {
        if (!authStore.token) {
            console.warn("No auth token found, redirecting to login");
            router.push('/driver/login');
            return;
        }
        
        // Assuming planning-service is proxied or accessible via /api/planning
        // Adjust URL as needed. If direct: http://localhost:8092/api/driver/tasks
        const response = await axios.get('http://localhost:8092/api/driver/tasks', {
            headers: {
                Authorization: `Bearer ${authStore.token}`
            }
        });
        tasks.value = response.data;
    } catch (error) {
        console.error('Failed to fetch tasks:', error);
        if (error.response && error.response.status === 401) {
             router.push('/driver/login');
        }
    } finally {
        loading.value = false;
    }
};

const scannedCode = ref('');
const manualCode = ref('');
const photos = ref([]);
const signatureCanvas = ref(null);
let signaturePad = null;

onMounted(() => {
    fetchTasks();
    
    if (configStore.config.workflow.requireSignature) {
        nextTick(() => {
            if (signatureCanvas.value) {
                // Adjust canvas resolution for high DPI screens
                const canvas = signatureCanvas.value;
                const ratio = Math.max(window.devicePixelRatio || 1, 1);
                canvas.width = canvas.offsetWidth * ratio;
                canvas.height = canvas.offsetHeight * ratio;
                canvas.getContext("2d").scale(ratio, ratio);

                signaturePad = new SignaturePad(canvas, {
                    backgroundColor: 'rgba(255, 255, 255, 0)' // Transparent
                });
            }
        });
    }
});

const clearSignature = () => {
    if (signaturePad) signaturePad.clear();
};

const handleScan = (code) => {
  scannedCode.value = code;
  // TODO: Validate against manifest
};

const handlePhoto = (event) => {
  const file = event.target.files[0];
  if (file) {
    const reader = new FileReader();
    reader.onload = (e) => {
      photos.value.push(e.target.result);
    };
    reader.readAsDataURL(file);
  }
  // Reset input
  event.target.value = '';
};

const removePhoto = (index) => {
  photos.value.splice(index, 1);
};

const isReadyToSubmit = computed(() => {
  // Simple validation logic based on config
  const hasScan = scannedCode.value || manualCode.value;
  const hasPhoto = photos.value.length > 0;
  
  if (configStore.config.workflow.requireScan && !hasScan) return false;
  if (configStore.config.workflow.requirePhoto && !hasPhoto) return false;
  if (configStore.config.workflow.requireSignature && signaturePad && signaturePad.isEmpty()) return false;
  
  return true;
});

const completeStop = async () => {
    if (!currentStop.value) return;

    const payload = {
        scannedCode: scannedCode.value || manualCode.value,
        photos: photos.value, // In real app, these would be uploaded URLs
        hasSignature: signaturePad && !signaturePad.isEmpty(),
        timestamp: new Date().toISOString()
    };

    if (offlineStore.isOnline) {
        try {
            await axios.post(`http://localhost:8092/api/driver/tasks/${currentStop.value.id}/complete`, payload, {
                headers: {
                    Authorization: `Bearer ${authStore.token}`
                }
            });
            alert('Stop Completed!');
            
            // Remove completed task from list
            tasks.value.shift();
            
            // Send Notification (optional, backend might handle this now)
            notificationService.sendEmail('DELIVERED', 'customer@example.com', {
                name: currentStop.value.customerName,
                orderId: currentStop.value.orderId,
                time: new Date().toLocaleTimeString()
            });
        } catch (error) {
            console.error('Failed to complete task:', error);
            if (error.response && error.response.status === 401) {
                 router.push('/driver/login');
                 return;
            }
            alert('Failed to complete task. Please try again.');
        }

    } else {
        // Offline: Queue action
        offlineStore.queueAction({
            type: 'COMPLETE_STOP',
            payload: { ...payload, taskId: currentStop.value.id }
        });
        alert('Offline: Data saved. Will sync when online.');
        
        // Optimistically remove task
        tasks.value.shift();
    }
    
    // Reset form
    scannedCode.value = '';
    manualCode.value = '';
    photos.value = [];
    if (signaturePad) signaturePad.clear();
};
</script>
