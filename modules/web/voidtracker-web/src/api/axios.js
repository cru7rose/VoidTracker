import axios from 'axios';
import { useAuthStore } from '../stores/authStore';

// Create axios instances for different services
// Use relative paths to allow Nginx to proxy requests (Gateway Pattern)
const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '/',
    headers: {
        'Content-Type': 'application/json',
    },
});

const planningApi = axios.create({
    baseURL: import.meta.env.VITE_PLANNING_BASE_URL || '/api/planning',
    headers: {
        'Content-Type': 'application/json',
    },
});

// Interceptors
const requestInterceptor = (config) => {
    const authStore = useAuthStore();
    if (authStore.token) {
        config.headers.Authorization = `Bearer ${authStore.token}`;
    }
    return config;
};

const responseInterceptor = (error) => {
    const authStore = useAuthStore();
    if (error.response && error.response.status === 401) {
        authStore.logout();
    }
    return Promise.reject(error);
};

const iamApi = axios.create({
    baseURL: import.meta.env.VITE_IAM_BASE_URL || '/',
    headers: {
        'Content-Type': 'application/json',
    },
});

iamApi.interceptors.request.use(requestInterceptor, (error) => Promise.reject(error));
iamApi.interceptors.response.use((response) => response, responseInterceptor);

// Add interceptors to planningApi
planningApi.interceptors.request.use(requestInterceptor, (error) => Promise.reject(error));
planningApi.interceptors.response.use((response) => response, responseInterceptor);

// Add interceptors to orderApi
api.interceptors.request.use(requestInterceptor, (error) => Promise.reject(error));
api.interceptors.response.use((response) => response, responseInterceptor);

export default api;
export { planningApi, iamApi, api as orderApi };
