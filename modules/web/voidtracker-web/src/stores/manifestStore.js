import { defineStore } from 'pinia';
import { ref } from 'vue';
import manifestApi from '../api/manifestApi';

export const useManifestStore = defineStore('manifest', () => {
    const manifests = ref([]);
    const currentManifest = ref(null);
    const loading = ref(false);
    const error = ref(null);

    async function fetchManifests(filters = {}) {
        loading.value = true;
        error.value = null;

        try {
            manifests.value = await manifestApi.getManifests(filters);
            return manifests.value;
        } catch (err) {
            error.value = err.response?.data?.message || 'Failed to fetch manifests';
            throw err;
        } finally {
            loading.value = false;
        }
    }

    async function fetchManifest(id) {
        loading.value = true;
        error.value = null;

        try {
            currentManifest.value = await manifestApi.getManifest(id);
            return currentManifest.value;
        } catch (err) {
            error.value = err.response?.data?.message || 'Failed to fetch manifest';
            throw err;
        } finally {
            loading.value = false;
        }
    }

    async function generateManifests() {
        loading.value = true;
        error.value = null;

        try {
            const result = await manifestApi.generateManifests();
            // Refresh manifests list after generation
            await fetchManifests({ date: new Date().toISOString().split('T')[0] });
            return result;
        } catch (err) {
            error.value = err.response?.data?.message || 'Failed to generate manifests';
            throw err;
        } finally {
            loading.value = false;
        }
    }

    async function assignDriver(manifestId, driverId) {
        loading.value = true;
        error.value = null;

        try {
            const updated = await manifestApi.assignDriver(manifestId, driverId);

            // Update in list if exists
            const index = manifests.value.findIndex(m => m.id === manifestId);
            if (index !== -1) {
                manifests.value[index] = updated;
            }

            // Update current manifest if it's the same
            if (currentManifest.value?.id === manifestId) {
                currentManifest.value = updated;
            }

            return updated;
        } catch (err) {
            error.value = err.response?.data?.message || 'Failed to assign driver';
            throw err;
        } finally {
            loading.value = false;
        }
    }

    async function updateStatus(manifestId, status) {
        loading.value = true;
        error.value = null;

        try {
            const updated = await manifestApi.updateStatus(manifestId, status);

            // Update in list if exists
            const index = manifests.value.findIndex(m => m.id === manifestId);
            if (index !== -1) {
                manifests.value[index] = updated;
            }

            // Update current manifest if it's the same
            if (currentManifest.value?.id === manifestId) {
                currentManifest.value = updated;
            }

            return updated;
        } catch (err) {
            error.value = err.response?.data?.message || 'Failed to update manifest status';
            throw err;
        } finally {
            loading.value = false;
        }
    }

    function clearError() {
        error.value = null;
    }

    return {
        manifests,
        currentManifest,
        loading,
        error,
        fetchManifests,
        fetchManifest,
        generateManifests,
        assignDriver,
        updateStatus,
        clearError
    };
});
