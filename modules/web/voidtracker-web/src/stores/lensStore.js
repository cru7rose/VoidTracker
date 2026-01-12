import { defineStore } from 'pinia';
import { ref } from 'vue';
import { lensesApi } from '@/api/lensesApi';
import { useToast } from '@/composables/useToast';

export const useLensStore = defineStore('lens', () => {
    const isLoading = ref(false);
    const error = ref(null);
    const points = ref([]);
    const activeLens = ref('profitability');

    async function loadHeatmap(lensType) {
        if (lensType) activeLens.value = lensType; // Update active lens if provided

        isLoading.value = true;
        error.value = null;
        points.value = []; // Clear old points to avoid confusion

        try {
            let response;
            if (activeLens.value === 'risk') {
                response = await lensesApi.getRiskHeatmap();
            } else {
                response = await lensesApi.getProfitabilityHeatmap();
            }

            // DTO Validation: Ensure backend returns expected structure
            const rawData = response.data;
            if (!Array.isArray(rawData)) {
                throw new Error('Invalid response format: expected array');
            }

            // Map and validate each point
            points.value = rawData
                .filter(p => p && typeof p.lat === 'number' && typeof p.lon === 'number')
                .map(p => ({
                    lat: p.lat,
                    lon: p.lon,
                    intensity: typeof p.intensity === 'number' ? p.intensity : 0.5 // Fallback
                }));

            console.log(`Loaded ${points.value.length} ${activeLens.value} points`);
        } catch (err) {
            console.error('Lens Load Error:', err);

            // "5-Star Experience" Error Handling with specific lens context
            const { error: showError } = useToast();
            const lensName = activeLens.value === 'risk' ? 'Risk Radar' : 'Profitability Lens';
            showError(`${lensName} connection disrupted: ${err.response?.data?.message || err.message || 'Unknown Error'}`);

            error.value = `Failed to load ${lensName} data.`;
        } finally {
            isLoading.value = false;
        }
    }

    return {
        isLoading,
        error,
        points,
        activeLens,
        loadHeatmap
    };
});
