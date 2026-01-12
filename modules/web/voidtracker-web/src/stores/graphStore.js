import { defineStore } from 'pinia';
import { ref } from 'vue';
import { graphApi } from '@/api/graphApi';

/**
 * Graph Store - manages Neo4j driver and location data
 * Used by Risk Lens and other map-based views for real-time driver tracking
 */
export const useGraphStore = defineStore('graph', () => {
    // State
    const currentDriver = ref(null);
    const locations = ref([]);
    const loading = ref(false);
    const error = ref(null);

    /**
     * Fetch driver data with impact zones (served locations)
     * @param {string} driverId - Driver UUID
     */
    async function fetchDriverWithImpact(driverId) {
        loading.value = true;
        error.value = null;

        try {
            // Fetch driver details
            const driverRes = await graphApi.getDriver(driverId);
            currentDriver.value = driverRes.data;

            // Fetch impact zones (locations served by this driver)
            const impactRes = await graphApi.getDriverImpact(driverId);
            locations.value = impactRes.data;

        } catch (err) {
            console.error('Graph Store Error:', err);
            error.value = err.response?.data?.message || 'Failed to load driver data';

            // Reset state on error
            currentDriver.value = null;
            locations.value = [];
        } finally {
            loading.value = false;
        }
    }

    /**
     * Fetch all drivers from Neo4j
     */
    async function fetchAllDrivers() {
        loading.value = true;
        error.value = null;

        try {
            const res = await graphApi.getAllDrivers();
            return res.data;
        } catch (err) {
            console.error('Graph Store Error:', err);
            error.value = 'Failed to load drivers list';
            return [];
        } finally {
            loading.value = false;
        }
    }

    /**
     * Clear current driver state
     */
    function clearDriver() {
        currentDriver.value = null;
        locations.value = [];
        error.value = null;
    }

    return {
        // State
        currentDriver,
        locations,
        loading,
        error,

        // Actions
        fetchDriverWithImpact,
        fetchAllDrivers,
        clearDriver
    };
});
