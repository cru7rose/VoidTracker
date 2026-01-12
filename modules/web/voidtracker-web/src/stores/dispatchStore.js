import { defineStore } from 'pinia';
import { ref } from 'vue';

import { planningApi, orderApi, iamApi } from '../api/axios';

export const useDispatchStore = defineStore('dispatch', () => {
    // State
    const routes = ref([]);
    const unassignedOrders = ref([]);
    const drivers = ref([]);
    const constraints = ref({
        maxStops: 15,
        maxWeight: 1000,
        timeWindowStrictness: 'HARD'
    });
    const loading = ref(false);
    const lastOptimized = ref(null);
    const currentSolutionId = ref(null);

    function mapSolutionToFrontend(solutionData) {
        if (!solutionData || !solutionData.routes) return [];
        return solutionData.routes.map((route, index) => ({
            id: route.vehicleId || `R${index + 1}`,
            name: `Route ${index + 1}`,
            // Use driver name from backend if available, otherwise 'Unassigned'
            driver: route.driverName || 'Unassigned',
            driverId: route.driverId || null,
            vehicle: route.vehicleId ? `Vehicle ${route.vehicleId.toString().substring(0, 8)}` : `Vehicle ${index + 1}`,
            status: 'PLANNED',
            stops: route.activities ? route.activities.length : 0,
            distance: route.totalDistance ? (route.totalDistance / 1000).toFixed(1) : '0.0',
            time: route.totalTimeSeconds ? (route.totalTimeSeconds / 3600).toFixed(1) : '0.0',
            legs: route.activities ? route.activities.map(act => ({
                orderId: act.orderId,
                address: act.address || `Order ${act.orderId}`, // Use address from backend if available
                eta: act.arrivalTimeMillis ? new Date(act.arrivalTimeMillis).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '--:--'
            })) : []
        }));
    }

    async function fetchLatestPlan() {
        loading.value = true;
        try {
            const res = await planningApi.get('/optimization/latest');
            if (res.data) {
                routes.value = mapSolutionToFrontend(res.data);
                if (res.data.id) currentSolutionId.value = res.data.id;

                // Note: Backend should enrich routes with driver data
                // If driverName is missing, mapSolutionToFrontend will default to 'Unassigned'

                lastOptimized.value = new Date();
            }
        } catch (e) {
            console.error("Failed to fetch latest plan", e);
            throw e; // Re-throw to allow caller to handle
        } finally {
            loading.value = false;
        }
    }

    async function fetchPlan(planId) {
        loading.value = true;
        try {
            const res = await planningApi.get(`/solutions/${planId}`);
            if (res.data) {
                routes.value = mapSolutionToFrontend(res.data);
                if (res.data.id) currentSolutionId.value = res.data.id; // Store ID
                lastOptimized.value = new Date();
            }
        } catch (e) {
            console.error("Failed to fetch plan " + planId, e);
        } finally {
            loading.value = false;
        }
    }

    async function runOptimization(payload) {
        loading.value = true;
        try {
            const res = await planningApi.post('/optimization/optimize', payload);
            if (res.data) {
                routes.value = mapSolutionToFrontend(res.data);
                if (res.data.id) currentSolutionId.value = res.data.id; // Store ID
                lastOptimized.value = new Date();
            }
        } catch (e) {
            console.error("Optimization failed", e);
            throw e;
        } finally {
            loading.value = false;
        }
    }

    async function appendOrder(routeId, orderId) {
        // ... existing
        loading.value = true;
        try {
            const res = await planningApi.post(`/dispatch/routes/${routeId}/orders/append`, null, {
                params: { orderId }
            });
            await fetchLatestPlan();
        } catch (e) {
            console.error("Failed to append order", e);
            throw e;
        } finally {
            loading.value = false;
        }
    }

    async function publishRoutes() {
        if (routes.value.length === 0) return;
        loading.value = true;
        try {
            const payload = {
                solutionId: currentSolutionId.value, // Send ID
                routes: routes.value.map(r => ({
                    vehicleId: r.id,
                    stops: r.legs.map(l => ({
                        orderId: l.orderId,
                        address: l.address
                    }))
                }))
            };

            await planningApi.post('/optimization/publish', payload);
        } catch (e) {
            console.error("Publish failed", e);
            throw e;
        } finally {
            loading.value = false;
        }
    }

    async function fetchDrivers() {
        try {
            const res = await iamApi.get('/api/users');
            // Filter to only users with ROLE_DRIVER
            // Backend returns roles as Set<String>, e.g. ["ROLE_DRIVER"]
            drivers.value = res.data.filter(user =>
                user.roles && user.roles.includes('ROLE_DRIVER')
            );
            console.log('Fetched drivers:', drivers.value);
        } catch (e) {
            console.error("Failed to fetch drivers", e);
            // Mock fallback
            drivers.value = [
                { userId: 'deadbeef-0000-0000-0000-000000000001', username: 'driver1', email: 'driver1@voidtracker.com', roles: ['ROLE_DRIVER'] },
                { userId: 'deadbeef-0000-0000-0000-000000000002', username: 'driver2', email: 'driver2@voidtracker.com', roles: ['ROLE_DRIVER'] }
            ];
        }
    }

    async function assignDriverToRoute(routeId, driverId) {
        loading.value = true;
        try {
            const route = routes.value.find(r => r.id === routeId);
            if (!route) {
                throw new Error("Route not found");
            }

            // Assign driver to ALL orders in the route (order-service)
            const promises = route.legs.map(leg =>
                orderApi.post(`/api/orders/${leg.orderId}/assign-driver`, { driverId })
            );

            await Promise.all(promises);

            // Update local state
            route.driver = driverId;
            route.driverId = driverId; // Store for magic link
            // Fetch latest to reflect changes
            await fetchLatestPlan();
        } catch (e) {
            console.error("Failed to assign driver", e);
            throw e;
        } finally {
            loading.value = false;
        }
    }

    async function fetchUnassignedOrders() {
        loading.value = true;
        try {
            const res = await orderApi.get('/api/orders', {
                params: { statuses: 'NEW' }
            });
            // Backend returns paginated response with .content array
            unassignedOrders.value = res.data.content || res.data || [];
            console.log(`✅ Fetched ${unassignedOrders.value.length} unassigned orders`);
        } catch (e) {
            console.error("Failed to fetch unassigned orders", e);
            throw e;
        } finally {
            loading.value = false;
        }
    }

    async function sendMagicLink(driverId, routeId, email) {
        try {
            const res = await planningApi.post('/driver/auth/generate-link', null, {
                params: {
                    driverId: driverId,
                    routeId: routeId,
                    email: email
                }
            });
            return { token: res.data, link: `http://localhost:5173/driver/login?token=${res.data}` };
        } catch (e) {
            console.error("Failed to send magic link", e);
            throw e;
        }
    }

    return {
        routes,
        unassignedOrders,
        drivers,
        constraints,
        loading,
        lastOptimized,
        fetchLatestPlan,
        fetchUnassignedOrders,
        runOptimization,
        appendOrder,
        publishRoutes,
        fetchPlan,
        fetchDrivers,
        assignDriverToRoute,
        sendMagicLink
    };
});
