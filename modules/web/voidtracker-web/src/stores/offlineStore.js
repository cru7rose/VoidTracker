import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useOfflineStore = defineStore('offline', () => {
    const isOnline = ref(navigator.onLine);
    const actionQueue = ref([]);
    const lastSyncTime = ref(null);

    // Listen for online/offline events
    window.addEventListener('online', () => {
        isOnline.value = true;
        syncQueue();
    });
    window.addEventListener('offline', () => {
        isOnline.value = false;
    });

    const queueAction = (action) => {
        actionQueue.value.push({
            id: Date.now(),
            timestamp: new Date().toISOString(),
            ...action
        });
        console.log('Action queued:', action);

        // Try to sync immediately if online
        if (isOnline.value) {
            syncQueue();
        }
    };

    const syncQueue = async () => {
        if (actionQueue.value.length === 0) return;
        if (!isOnline.value) return;

        console.log('Syncing queue...', actionQueue.value.length, 'items');

        // Process queue (FIFO)
        // In a real app, we would loop through and send API requests
        // For now, we simulate success

        const actionsToProcess = [...actionQueue.value];
        for (const action of actionsToProcess) {
            try {
                await processAction(action);
                // Remove from queue on success
                actionQueue.value = actionQueue.value.filter(a => a.id !== action.id);
            } catch (error) {
                console.error('Failed to sync action:', action, error);
                // Keep in queue to retry later
            }
        }

        lastSyncTime.value = new Date().toISOString();
    };

    const processAction = async (action) => {
        // Mock API call
        return new Promise(resolve => setTimeout(resolve, 500));
    };

    const pendingCount = computed(() => actionQueue.value.length);

    return {
        isOnline,
        actionQueue,
        lastSyncTime,
        queueAction,
        syncQueue,
        pendingCount
    };
});
