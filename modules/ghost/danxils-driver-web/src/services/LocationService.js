import { reactive, readonly } from 'vue';

const state = reactive({
    latitude: null,
    longitude: null,
    error: null,
    timestamp: null
});

let watchId = null;

const calculateDistance = (lat1, lon1, lat2, lon2) => {
    if (!lat1 || !lon1 || !lat2 || !lon2) return null;

    const R = 6371e3; // metres
    const φ1 = lat1 * Math.PI / 180; // φ, λ in radians
    const φ2 = lat2 * Math.PI / 180;
    const Δφ = (lat2 - lat1) * Math.PI / 180;
    const Δλ = (lon2 - lon1) * Math.PI / 180;

    const a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
        Math.cos(φ1) * Math.cos(φ2) *
        Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return R * c; // in metres
};

const startTracking = () => {
    if (!navigator.geolocation) {
        state.error = "Geolocation is not supported by your browser";
        return;
    }

    watchId = navigator.geolocation.watchPosition(
        (position) => {
            state.latitude = position.coords.latitude;
            state.longitude = position.coords.longitude;
            state.timestamp = position.timestamp;
            state.error = null;
        },
        (error) => {
            state.error = `Geolocation error: ${error.message}`;
        },
        {
            enableHighAccuracy: true,
            timeout: 5000,
            maximumAge: 0
        }
    );
};

const stopTracking = () => {
    if (watchId !== null) {
        navigator.geolocation.clearWatch(watchId);
        watchId = null;
    }
};

export default {
    state: readonly(state),
    startTracking,
    stopTracking,
    calculateDistance
};
