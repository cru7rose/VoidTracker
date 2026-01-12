import axios from './axios';

export const lensesApi = {
    getProfitabilityHeatmap() {
        return axios.get('/dashboard/lenses/profitability');
    },

    getRiskHeatmap() {
        return axios.get('/dashboard/lenses/risk');
    }
};
