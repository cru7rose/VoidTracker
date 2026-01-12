import api from './axios';

/**
 * Order API Client
 * Handles all order-related API calls
 */

export const orderApi = {
    /**
     * Get orders with optional filters
     */
    async getOrders(filters = {}) {
        const params = new URLSearchParams();

        if (filters.status) params.append('status', filters.status);
        if (filters.customerId) params.append('customerId', filters.customerId);
        if (filters.startDate) params.append('startDate', filters.startDate);
        if (filters.endDate) params.append('endDate', filters.endDate);
        if (filters.priority) params.append('priority', filters.priority);

        const response = await api.get(`/api/orders?${params.toString()}`);
        return response.data;
    },

    /**
     * Get a single order by ID
     */
    async getOrder(id) {
        const response = await api.get(`/api/orders/${id}`);
        return response.data;
    },

    /**
     * Create a new order
     */
    async createOrder(orderData) {
        const response = await api.post('/api/orders', orderData);
        return response.data;
    },

    /**
     * Update order status
     */
    async updateOrderStatus(id, status) {
        const response = await api.put(`/api/orders/${id}/status`, { status });
        return response.data;
    },

    /**
     * Update order details
     */
    async updateOrder(id, orderData) {
        const response = await api.put(`/api/orders/${id}`, orderData);
        return response.data;
    },

    /**
     * Delete an order
     */
    async deleteOrder(id) {
        const response = await api.delete(`/api/orders/${id}`);
        return response.data;
    },

    /**
     * Get ePoD for an order
     */
    async getEpod(id) {
        const response = await api.get(`/api/orders/${id}/epod`);
        return response.data;
    }
};

export default orderApi;
