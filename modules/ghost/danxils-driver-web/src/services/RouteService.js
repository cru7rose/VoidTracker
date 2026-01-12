import axios from 'axios';
import authStore from '../stores/authStore';

export default {
    async getRoute() {
        // If not logged in, return empty (or handle redirect in view)
        const token = localStorage.getItem('driverToken') || authStore.token;
        if (!token) {
            console.warn("No driver token found");
            return [];
        }

        try {
            // Fetch from backend
            const driverId = authStore.user?.username || 'driver-1'; // Use username as ID for now, or true ID from token

            const res = await axios.get(`/api/driver/tasks`, {
                params: { driverId },
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            // Map backend entities to frontend model
            // Backend: DriverTaskEntity (id, address, customerName, status, assignedAt, lat, lon)
            // Frontend: { id, type, status, customer, address, time, lat, lng }
            return res.data.map(task => ({
                id: task.id,
                type: 'DELIVERY', // Defaulting to DELIVERY as backend entity doesn't have type yet, or infer
                status: task.status,
                customer: task.customerName,
                address: task.address,
                time: task.assignedAt ? new Date(task.assignedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '--:--',
                lat: task.lat,
                lng: task.lon // backend has lon, frontend expects lng
            }));
        } catch (e) {
            console.error("Failed to fetch tasks from backend", e);
            return [];
        }
    },

    async sendScanEvent(scanData) {
        const token = localStorage.getItem('driverToken') || authStore.token;
        // payload: { assetId, scanType, lat, lon, metadata: { accuracy, distance } }
        // scanData from UniversalScanner: { barcode, gps, distance, anomaly }

        const payload = {
            assetId: scanData.barcode, // Assuming barcode is the UUID
            scanType: 'DELIVERY_SUCCESS',
            lat: scanData.gps?.lat,
            lon: scanData.gps?.lon,
            distance: scanData.distance,
            anomaly: scanData.anomaly,
            metadata: {
                accuracy: scanData.gps?.accuracy,
                distance: scanData.distance
            },
            timestamp: new Date().toISOString()
        };

        return axios.post(`${API_URL}/scan-events`, payload, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
    },

    async analyzeImage(imageBlob, orderId) {
        const token = localStorage.getItem('token');
        const formData = new FormData();
        formData.append('file', imageBlob, 'capture.jpg');
        if (orderId) {
            formData.append('orderId', orderId);
        }
        return axios.post(`${API_URL}/vision/analyze`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
                'Authorization': `Bearer ${token}`
            }
        });
    }
}
