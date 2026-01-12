<template>
  <div class="min-h-screen bg-gray-900 text-white p-4 pb-24">
    <NetworkStatus />
    
    <header class="mb-6 mt-2">
        <h1 class="text-2xl font-bold">Skaner Przesyłek</h1>
        <p class="text-gray-400 text-sm">Zeskanuj kod kreskowy paczki</p>
    </header>

    <div class="bg-gray-800 rounded-2xl p-4 shadow-xl border border-gray-700">
        <Scanner 
            @scan-success="handleScan" 
            @camera-error="handleError"
        />
    </div>

    <!-- Recent Scans List -->
    <div class="mt-8">
        <h2 class="text-lg font-semibold mb-3 flex items-center justify-between">
            Ostatnie skany
            <span class="text-xs font-normal text-gray-500">Lokalne</span>
        </h2>
        
        <div class="space-y-3">
            <div 
                v-for="action in reversedActions" 
                :key="action.id"
                class="bg-gray-800 p-3 rounded-lg border border-gray-700 flex items-center justify-between"
            >
                <div class="flex items-center gap-3">
                    <div class="bg-blue-900/50 p-2 rounded text-blue-400">
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                    </div>
                    <div>
                        <p class="font-mono font-bold text-lg">{{ action.payload.barcode }}</p>
                        <p class="text-xs text-gray-500">{{ new Date(action.timestamp).toLocaleTimeString() }}</p>
                    </div>
                </div>
                <!-- Status Icon -->
                <div class="text-yellow-500" title="Oczekuje na synchronizację">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                </div>
            </div>
            
            <div v-if="reversedActions.length === 0" class="text-center py-8 text-gray-600">
                Brak zeskanowanych przesyłek.
            </div>
        </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import Scanner from '../components/Scanner.vue';
import NetworkStatus from '../components/NetworkStatus.vue';
import { useOfflineQueueStore } from '../stores/offline-queue';

const queueStore = useOfflineQueueStore();

// Show only scan events
const reversedActions = computed(() => {
    return [...queueStore.pendingActions]
        .filter(a => a.type === 'SCAN_EVENT')
        .reverse();
});

const handleScan = (decodedText: string) => {
    console.log('Scanned:', decodedText);
    
    // Add to offline queue
    queueStore.addAction({
        type: 'SCAN_EVENT',
        payload: { barcode: decodedText },
        timestamp: Date.now()
    });
    
    // Optional: Vibration
    if (navigator.vibrate) navigator.vibrate(200);
};

const handleError = (err: any) => {
    console.error('Scanner error:', err);
};
</script>
