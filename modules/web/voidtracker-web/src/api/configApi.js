import api from './axios';

export const configApi = {
    async getSystemConfig(key) {
        try {
            const response = await api.get(`/api/configs/system/${key}`);
            return response.data;
        } catch (error) {
            if (error.response && error.response.status === 404) {
                return null;
            }
            throw error;
        }
    },

    async updateSystemConfig(key, value) {
        await api.put(`/api/configs/system/${key}`, value, {
            headers: {
                'Content-Type': 'text/plain' // Sending raw string/JSON string
            }
        });
    },

    async getUserConfig(userId, key) {
        try {
            const response = await api.get(`/api/configs/user/${userId}/${key}`);
            return response.data;
        } catch (error) {
            if (error.response && error.response.status === 404) {
                return null;
            }
            throw error;
        }
    },

    async updateUserConfig(userId, key, value) {
        await api.put(`/api/configs/user/${userId}/${key}`, value, {
            headers: {
                'Content-Type': 'text/plain'
            }
        });
    }
};

export default configApi;
