import axios from './axios';

export const graphApi = {
    getShortestPath(start, end) {
        return axios.get('/planning/graph/shortest-path', { params: { start, end } });
    },

    initGraph() {
        return axios.post('/planning/graph/init');
    },

    getAllDrivers() {
        return axios.get('/planning/graph/drivers');
    },

    getDriverImpact(driverId) {
        return axios.get(`/planning/graph/driver/${driverId}/impact`);
    },

    getDriver(driverId) {
        return axios.get(`/planning/graph/driver/${driverId}`);
    }
};
