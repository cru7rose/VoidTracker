import { useOfflineStore } from '../stores/offlineStore';
import axios from 'axios';

class SyncService {
    constructor() {
        this.store = null;
    }

    init() {
        this.store = useOfflineStore();

        // Watch for queue changes or online status
        this.store.$subscribe((mutation, state) => {
            if (state.isOnline && state.queue.length > 0 && !state.isSyncing) {
                this.processQueue();
            }
        });
    }

    async processQueue() {
        if (this.store.isSyncing) return;
        this.store.isSyncing = true;

        const queue = [...this.store.queue];

        for (const item of queue) {
            try {
                await this.dispatchAction(item);
                await this.store.removeFromQueue(item.id);
            } catch (error) {
                console.error('Sync failed for item:', item, error);
                // Implement retry logic or move to dead-letter queue
                // For now, we leave it in the queue but maybe backoff?
                break; // Stop syncing if one fails to preserve order
            }
        }

        this.store.isSyncing = false;
    }

    async dispatchAction(item) {
        // Map actionTypes to API calls
        switch (item.actionType) {
            case 'COMPLETE_TASK':
                await axios.post('/api/tasks/complete', item.payload);
                break;
            case 'UPDATE_STATUS':
                await axios.post('/api/driver/status', item.payload);
                break;
            case 'COMPLETE_ORDER':
                await axios.post(`/api/orders/${item.payload.orderId}/complete`, item.payload);
                break;
            default:
                console.warn('Unknown action type:', item.actionType);
        }
    }
}

export const syncService = new SyncService();
