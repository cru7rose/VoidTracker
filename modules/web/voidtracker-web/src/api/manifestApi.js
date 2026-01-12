import { planningApi } from './axios';

/**
 * Manifest API Client
 * Handles all manifest-related API calls to the planning-service
 */

export const manifestApi = {
    /**
     * Generate new manifests
     */
    async generateManifests() {
        const response = await planningApi.post('/manifests/generate');
        return response.data;
    },

    /**
     * Get manifests with optional filters
     */
    async getManifests(filters = {}) {
        const params = new URLSearchParams();

        if (filters.date) params.append('date', filters.date);
        if (filters.status) params.append('status', filters.status);
        if (filters.driverId) params.append('driverId', filters.driverId);
        if (filters.startDate) params.append('startDate', filters.startDate);
        if (filters.endDate) params.append('endDate', filters.endDate);

        const response = await planningApi.get(`/manifests?${params.toString()}`);
        return response.data;
    },

    /**
     * Get a single manifest by ID
     */
    async getManifest(id) {
        const response = await planningApi.get(`/manifests/${id}`);
        return response.data;
    },

    /**
     * Get manifests for a specific driver
     */
    async getDriverManifests(driverId, date = null) {
        const params = date ? `?date=${date}` : '';
        const response = await planningApi.get(`/manifests/driver/${driverId}${params}`);
        return response.data;
    },

    /**
     * Assign a driver to a manifest
     */
    async assignDriver(manifestId, driverId) {
        const response = await planningApi.put(`/manifests/${manifestId}/assign`, {
            driverId
        });
        return response.data;
    },

    /**
     * Update manifest status
     */
    async updateStatus(manifestId, status) {
        const response = await planningApi.put(`/manifests/${manifestId}/status`, null, {
            params: { status }
        });
        return response.data;
    }
};

export default manifestApi;
