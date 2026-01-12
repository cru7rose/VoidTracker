import { defineStore } from 'pinia';
import axios from 'axios';
import { useOfflineStore } from './offlineStore';

export const useOrderStore = defineStore('order', {
    state: () => ({
        orders: [],
        loading: false,
        error: null
    }),

    actions: {
        async fetchAssignedOrders() {
            this.loading = true;
            this.error = null;
            const offlineStore = useOfflineStore();

            // Strategy: Network First
            try {
                const response = await axios.get('/api/orders/assigned');
                this.orders = response.data;

                // Cache for offline use
                await offlineStore.saveOrders(this.orders);
            } catch (err) {
                console.warn("Network failed, checking offline cache...", err);

                // Fallback: Offline Cache
                const offlineOrders = await offlineStore.getOfflineOrders();
                if (offlineOrders && offlineOrders.length > 0) {
                    this.orders = offlineOrders;
                } else {
                    this.error = "No internet connection and no offline data available.";
                }
            } finally {
                this.loading = false;
            }
        },

        async completeOrder(orderId, proofData) {
            const offlineStore = useOfflineStore();

            // Optimistic UI Update
            const orderIndex = this.orders.findIndex(o => o.id === orderId);
            if (orderIndex !== -1) {
                this.orders[orderIndex].status = 'COMPLETED';
            }

            // Queue Action
            await offlineStore.addToQueue('COMPLETE_ORDER', {
                orderId,
                proofData,
                timestamp: new Date().toISOString()
            });
        }
    }
});
