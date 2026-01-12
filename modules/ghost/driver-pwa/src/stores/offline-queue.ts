import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { openDB, type DBSchema } from 'idb';

interface OfflineAction {
    id?: number;
    type: 'SCAN_EVENT' | 'PHOTO_UPLOAD' | 'STATUS_UPDATE';
    payload: any;
    timestamp: number;
}

interface GhostDB extends DBSchema {
    actions: {
        key: number;
        value: OfflineAction;
    };
}

const dbPromise = openDB<GhostDB>('ghost-pwa-db', 1, {
    upgrade(db) {
        db.createObjectStore('actions', { keyPath: 'id', autoIncrement: true });
    },
});

export const useOfflineQueueStore = defineStore('offline-queue', () => {
    const pendingActions = ref<OfflineAction[]>([]);

    const pendingCount = computed(() => pendingActions.value.length);

    // Load initial state
    const init = async () => {
        const db = await dbPromise;
        pendingActions.value = await db.getAll('actions');
    };

    const addAction = async (action: Omit<OfflineAction, 'id'>) => {
        const db = await dbPromise;
        const newAction = { ...action, timestamp: Date.now() };
        const id = await db.add('actions', newAction as OfflineAction);
        pendingActions.value.push({ ...newAction, id });

        // Try to process immediately if online
        if (navigator.onLine) {
            processQueue();
        }
    };

    const processQueue = async () => {
        if (!navigator.onLine || pendingActions.value.length === 0) return;

        const db = await dbPromise;
        const actions = [...pendingActions.value];

        console.log(`Processing ${actions.length} offline actions...`);

        for (const action of actions) {
            try {
                await sendActionToBackend(action);
                // If success, remove from DB and local state
                if (action.id) {
                    await db.delete('actions', action.id);
                    pendingActions.value = pendingActions.value.filter(a => a.id !== action.id);
                }
            } catch (e) {
                console.error('Failed to process action:', action, e);
                // Keep in queue, break or continue depending on strategy. 
                // For now, break to preserve order.
                break;
            }
        }
    };

    const sendActionToBackend = async (action: OfflineAction) => {
        // Mock API call - replace with real fetch
        console.log('Sending to API:', action);

        // Simulate network delay
        await new Promise(resolve => setTimeout(resolve, 500));

        // Simulate Success
        return true;
    };

    // Initialize on store creation
    init();

    return { pendingActions, pendingCount, addAction, processQueue };
});
