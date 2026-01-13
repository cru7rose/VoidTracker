import { defineStore } from 'pinia';
import { ref } from 'vue';
import axios from 'axios';
import { useAuthStore } from './authStore';

export const useGatekeeperStore = defineStore('gatekeeper', () => {
    const authStore = useAuthStore();
    
    // State
    const pendingApprovals = ref([]);
    const loading = ref(false);
    const error = ref(null);

    /**
     * Fetch pending approval requests
     */
    async function fetchPendingApprovals() {
        loading.value = true;
        error.value = null;
        try {
            const headers = { Authorization: `Bearer ${authStore.token}` };
            const res = await axios.get('/api/planning/gatekeeper/pending', { headers });
            pendingApprovals.value = res.data.pendingApprovals || [];
        } catch (e) {
            error.value = e.response?.data?.error || e.message;
            console.error('Failed to fetch pending approvals:', e);
        } finally {
            loading.value = false;
        }
    }

    /**
     * Approve optimization solution
     */
    async function approveSolution(solutionId, notes = '') {
        loading.value = true;
        error.value = null;
        try {
            const headers = { Authorization: `Bearer ${authStore.token}` };
            const res = await axios.post(
                `/api/planning/gatekeeper/${solutionId}/approve`,
                {
                    approvedBy: authStore.user?.username || 'system',
                    notes
                },
                { headers }
            );
            // Remove from pending list
            pendingApprovals.value = pendingApprovals.value.filter(a => a.solutionId !== solutionId);
            return res.data;
        } catch (e) {
            error.value = e.response?.data?.error || e.message;
            throw e;
        } finally {
            loading.value = false;
        }
    }

    /**
     * Reject optimization solution
     */
    async function rejectSolution(solutionId, reason) {
        loading.value = true;
        error.value = null;
        try {
            const headers = { Authorization: `Bearer ${authStore.token}` };
            const res = await axios.post(
                `/api/planning/gatekeeper/${solutionId}/reject`,
                {
                    rejectedBy: authStore.user?.username || 'system',
                    reason
                },
                { headers }
            );
            // Remove from pending list
            pendingApprovals.value = pendingApprovals.value.filter(a => a.solutionId !== solutionId);
            return res.data;
        } catch (e) {
            error.value = e.response?.data?.error || e.message;
            throw e;
        } finally {
            loading.value = false;
        }
    }

    /**
     * Check approval status for solution
     */
    async function getApprovalStatus(solutionId) {
        try {
            const headers = { Authorization: `Bearer ${authStore.token}` };
            const res = await axios.get(`/api/planning/gatekeeper/${solutionId}/status`, { headers });
            return res.data;
        } catch (e) {
            console.error('Failed to get approval status:', e);
            return null;
        }
    }

    return {
        pendingApprovals,
        loading,
        error,
        fetchPendingApprovals,
        approveSolution,
        rejectSolution,
        getApprovalStatus
    };
});
