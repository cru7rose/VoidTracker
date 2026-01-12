import { ref } from 'vue';

export function useGeolocation() {
    const coords = ref(null);
    const error = ref(null);
    const loading = ref(false);

    const getPosition = (options = { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }) => {
        loading.value = true;
        error.value = null;

        return new Promise((resolve, reject) => {
            if (!("geolocation" in navigator)) {
                error.value = "Geolocation is not supported by this browser.";
                loading.value = false;
                reject(new Error(error.value));
                return;
            }

            navigator.geolocation.getCurrentPosition(
                (position) => {
                    coords.value = {
                        lat: position.coords.latitude,
                        lon: position.coords.longitude,
                        accuracy: position.coords.accuracy
                    };
                    loading.value = false;
                    resolve(coords.value);
                },
                (err) => {
                    error.value = `Error getting location: ${err.message}`;
                    loading.value = false;
                    reject(err);
                },
                options
            );
        });
    };

    return {
        coords,
        error,
        loading,
        getPosition
    };
}
