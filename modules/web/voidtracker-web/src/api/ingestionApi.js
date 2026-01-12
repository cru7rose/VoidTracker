import axios from './axios';

export const ingestionApi = {
    async ingestOrder(payload) {
        try {
            const response = await axios.post('/ingestion/orders', payload);
            return {
                success: true,
                orderId: response.data,
                message: 'Order ingested successfully'
            };
        } catch (err) {
            // Comprehensive error mapping for "5-Star Experience"
            const status = err.response?.status;
            const backendMessage = err.response?.data?.message || err.response?.data;

            let message = 'Failed to ingest order';

            if (status === 400) {
                message = `Validation error: ${backendMessage || 'Invalid order data'}`;
            } else if (status === 409) {
                message = `Conflict: ${backendMessage || 'Order might already exist'}`;
            } else if (status === 500) {
                message = `Server error: ${backendMessage || 'Please contact support'}`;
            } else if (backendMessage) {
                message = String(backendMessage);
            }

            return {
                success: false,
                message: message,
                orderId: null
            };
        }
    }
};
