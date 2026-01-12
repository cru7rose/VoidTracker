import { defineStore } from 'pinia';
import { openDB } from 'idb';

const DB_NAME = 'danxils-driver-db';
const STORE_NAME = 'sync-queue';

export const useOfflineStore = defineStore('offline', {
    state: () => ({
        isOnline: navigator.onLine,
        queue: [],
        isSyncing: false
    }),

    actions: {
        async initDB() {
            this.db = await openDB(DB_NAME, 1, {
                upgrade(db) {
                    if (!db.objectStoreNames.contains(STORE_NAME)) {
                        db.createObjectStore(STORE_NAME, { keyPath: 'id', autoIncrement: true });
                    }
                    if (!db.objectStoreNames.contains('orders')) {
                        db.createObjectStore('orders', { keyPath: 'id' });
                    }
                },
            });
            await this.loadQueue();

            window.addEventListener('online', () => {
                this.isOnline = true;
                this.sync();
            });
            window.addEventListener('offline', () => {
                this.isOnline = false;
            });
        },

        async loadQueue() {
            if (!this.db) await this.initDB();
            this.queue = await this.db.getAll(STORE_NAME);
        },

        async addToQueue(actionType, payload) {
            if (!this.db) await this.initDB();
            const item = {
                actionType,
                payload,
                timestamp: Date.now(),
                retryCount: 0
            };
            await this.db.add(STORE_NAME, item);
            await this.loadQueue();

            if (this.isOnline) {
                this.sync();
            }
        },

        async removeFromQueue(id) {
            if (!this.db) await this.initDB();
            await this.db.delete(STORE_NAME, id);
            await this.loadQueue();
        },

        async sync() {
            if (this.isSyncing || this.queue.length === 0 || !this.isOnline) return;

            this.isSyncing = true;
            // Sync logic will be triggered by SyncService observing this state
            // or we could dispatch from here if we imported the service (circular dependency risk)
        },

        // --- Data Persistence for Offline Read ---

        async saveOrders(orders) {
            if (!this.db) await this.initDB();
            const tx = this.db.transaction('orders', 'readwrite');
            await Promise.all([
                ...orders.map(order => tx.store.put(order)),
                tx.done
            ]);
        },

        async getOfflineOrders() {
            if (!this.db) await this.initDB();
            return await this.db.getAll('orders');
        }
    }
});
