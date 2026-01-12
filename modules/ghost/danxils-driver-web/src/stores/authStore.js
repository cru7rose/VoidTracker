import { reactive, readonly } from 'vue';
import axios from 'axios';

const state = reactive({
    user: JSON.parse(localStorage.getItem('driverUser')) || null,
    token: localStorage.getItem('driverToken') || null,
    loading: false,
    error: null
});

// Create axios instance
const api = axios.create({
    baseURL: '/', // Use relative path to leverage Vite proxy
    headers: {
        'Content-Type': 'application/json'
    }
});

// Add auth header interceptor
api.interceptors.request.use(config => {
    if (state.token) {
        config.headers.Authorization = `Bearer ${state.token}`;
    }
    return config;
});

const actions = {
    async requestMagicLink(email) {
        state.loading = true;
        state.error = null;
        try {
            const response = await api.post('/api/auth/magic-link/request', { email });
            return true;
        } catch (err) {
            state.error = err.response?.data?.message || 'Failed to request magic link';
            return false;
        } finally {
            state.loading = false;
        }
    },

    async loginWithMagicLink(token) {
        state.loading = true;
        state.error = null;
        try {
            const response = await api.post('/api/auth/magic-link/login', { token });
            const data = response.data;

            state.token = data.accessToken;
            state.user = {
                username: data.username,
                roles: data.roles || []
            };

            localStorage.setItem('driverToken', state.token);
            localStorage.setItem('driverUser', JSON.stringify(state.user));

            return true;
        } catch (err) {
            state.error = err.response?.data?.message || 'Invalid magic link token';
            return false;
        } finally {
            state.loading = false;
        }
    },

    logout() {
        state.user = null;
        state.token = null;
        localStorage.removeItem('driverToken');
        localStorage.removeItem('driverUser');
    }
};

export default {
    state: readonly(state),
    ...actions,
    api // Export api instance for other services to use
};
