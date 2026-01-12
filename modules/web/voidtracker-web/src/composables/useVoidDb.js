/**
 * Vue Composable for VoidTracker RxDB Integration
 * Provides reactive access to offline-first database
 * 
 * @example
 * const { orders, loading, error, syncOrder, queryByStatus } = useVoidDb();
 * watch(orders, (newOrders) => console.log('Orders updated:', newOrders));
 */

import { ref, onUnmounted, computed } from 'vue';
import { getVoidDb } from '../database/void-db.js';

/**
 * Main composable for VoidDB access
 * @returns {Object} Reactive database interface
 */
export function useVoidDb() {
    const orders = ref([]);
    const drivers = ref([]);
    const loading = ref(false);
    const error = ref(null);

    let ordersSubscription = null;
    let driversSubscription = null;
    let dbInstance = null;

    /**
     * Initialize database and start listening to changes
     */
    async function initialize() {
        loading.value = true;
        error.value = null;

        try {
            dbInstance = await getVoidDb();

            // Subscribe to orders collection (reactive)
            ordersSubscription = dbInstance.orders
                .find()
                .sort({ createdAt: 'desc' })
                .$.subscribe(docs => {
                    orders.value = docs.map(doc => doc.toJSON());
                });

            // Subscribe to drivers collection
            driversSubscription = dbInstance.drivers
                .find()
                .$.subscribe(docs => {
                    drivers.value = docs.map(doc => doc.toJSON());
                });

            console.log('[useVoidDb] Subscriptions active');
        } catch (err) {
            console.error('[useVoidDb] Initialization failed:', err);
            error.value = `Database initialization failed: ${err.message}`;
        } finally {
            loading.value = false;
        }
    }

    /**
     * Sync order to local database (upsert)
     * @param {Object} orderData - Order data to sync
     * @returns {Promise<void>}
     */
    async function syncOrder(orderData) {
        if (!dbInstance) await initialize();

        try {
            // Ensure timestamps
            const now = new Date().toISOString();
            const fullOrderData = {
                ...orderData,
                updatedAt: now,
                createdAt: orderData.createdAt || now,
                _syncStatus: 'pending'
            };

            await dbInstance.orders.upsert(fullOrderData);
            console.log(`[useVoidDb] Order ${orderData.orderId} synced`);
        } catch (err) {
            console.error('[useVoidDb] Sync failed:', err);
            throw new Error(`Failed to sync order: ${err.message}`);
        }
    }

    /**
     * Sync driver to local database
     * @param {Object} driverData - Driver data to sync
     * @returns {Promise<void>}
     */
    async function syncDriver(driverData) {
        if (!dbInstance) await initialize();

        try {
            const fullDriverData = {
                ...driverData,
                createdAt: driverData.createdAt || new Date().toISOString()
            };

            await dbInstance.drivers.upsert(fullDriverData);
            console.log(`[useVoidDb] Driver ${driverData.driverId} synced`);
        } catch (err) {
            console.error('[useVoidDb] Driver sync failed:', err);
            throw err;
        }
    }

    /**
     * Query orders by status (reactive)
     * @param {string} status - Order status to filter
     * @returns {import('vue').Ref<Array>}
     */
    function queryByStatus(status) {
        const filteredOrders = computed(() =>
            orders.value.filter(order => order.status === status)
        );
        return filteredOrders;
    }

    /**
     * Query orders by customer (reactive)
     * @param {string} customerId - Customer ID to filter
     * @returns {import('vue').Ref<Array>}
     */
    function queryByCustomer(customerId) {
        const filteredOrders = computed(() =>
            orders.value.filter(order => order.customerId === customerId)
        );
        return filteredOrders;
    }

    /**
     * Delete order from local database
     * @param {string} orderId - Order ID to delete
     * @returns {Promise<void>}
     */
    async function deleteOrder(orderId) {
        if (!dbInstance) await initialize();

        try {
            const order = await dbInstance.orders
                .findOne({ selector: { orderId } })
                .exec();

            if (order) {
                await order.remove();
                console.log(`[useVoidDb] Order ${orderId} deleted`);
            }
        } catch (err) {
            console.error('[useVoidDb] Delete failed:', err);
            throw err;
        }
    }

    /**
     * Get order by ID
     * @param {string} orderId - Order ID
     * @returns {Promise<Object|null>}
     */
    async function getOrderById(orderId) {
        if (!dbInstance) await initialize();

        try {
            const order = await dbInstance.orders
                .findOne({ selector: { orderId } })
                .exec();

            return order ? order.toJSON() : null;
        } catch (err) {
            console.error('[useVoidDb] Get order failed:', err);
            return null;
        }
    }

    /**
     * Cleanup subscriptions on component unmount
     */
    onUnmounted(() => {
        if (ordersSubscription) {
            ordersSubscription.unsubscribe();
        }
        if (driversSubscription) {
            driversSubscription.unsubscribe();
        }
        console.log('[useVoidDb] Subscriptions cleaned up');
    });

    // Auto-initialize on composable creation
    initialize();

    return {
        // Reactive state
        orders,
        drivers,
        loading,
        error,

        // Actions
        syncOrder,
        syncDriver,
        deleteOrder,
        getOrderById,

        // Query helpers
        queryByStatus,
        queryByCustomer,

        // Manual control
        initialize
    };
}

/**
 * Lightweight composable for single order queries (no subscriptions)
 * Use when you don't need reactive updates
 * 
 * @returns {Object}
 */
export function useVoidDbQuery() {
    const loading = ref(false);
    const error = ref(null);

    async function findOrders(selector = {}, limit = 100) {
        loading.value = true;
        error.value = null;

        try {
            const db = await getVoidDb();
            const docs = await db.orders
                .find({ selector, limit })
                .exec();

            return docs.map(doc => doc.toJSON());
        } catch (err) {
            error.value = err.message;
            return [];
        } finally {
            loading.value = false;
        }
    }

    return {
        loading,
        error,
        findOrders
    };
}
