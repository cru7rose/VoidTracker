import { defineStore } from 'pinia';
import { ref } from 'vue';
import orderApi from '../api/orderApi';

export const useOrderStore = defineStore('order', () => {
    const orders = ref([]);
    const currentOrder = ref(null);
    const loading = ref(false);
    const error = ref(null);

    async function fetchOrders(filters = {}) {
        loading.value = true;
        error.value = null;

        try {
            orders.value = await orderApi.getOrders(filters);
            return orders.value;
        } catch (err) {
            error.value = err.response?.data?.message || 'Failed to fetch orders';
            throw err;
        } finally {
            loading.value = false;
        }
    }

    async function fetchOrder(id) {
        loading.value = true;
        error.value = null;

        try {
            currentOrder.value = await orderApi.getOrder(id);
            return currentOrder.value;
        } catch (err) {
            error.value = err.response?.data?.message || 'Failed to fetch order';
            throw err;
        } finally {
            loading.value = false;
        }
    }

    async function createOrder(orderData) {
        loading.value = true;
        error.value = null;

        try {
            const newOrder = await orderApi.createOrder(orderData);
            orders.value.unshift(newOrder);
            return newOrder;
        } catch (err) {
            error.value = err.response?.data?.message || 'Failed to create order';
            throw err;
        } finally {
            loading.value = false;
        }
    }

    async function updateOrderStatus(id, status) {
        loading.value = true;
        error.value = null;

        try {
            const updated = await orderApi.updateOrderStatus(id, status);

            // Update in list if exists
            const index = orders.value.findIndex(o => o.id === id);
            if (index !== -1) {
                orders.value[index] = updated;
            }

            // Update current order if it's the same
            if (currentOrder.value?.id === id) {
                currentOrder.value = updated;
            }

            return updated;
        } catch (err) {
            error.value = err.response?.data?.message || 'Failed to update order status';
            throw err;
        } finally {
            loading.value = false;
        }
    }

    function clearError() {
        error.value = null;
    }

    return {
        orders,
        currentOrder,
        loading,
        error,
        fetchOrders,
        fetchOrder,
        createOrder,
        updateOrderStatus,
        clearError
    };
});
