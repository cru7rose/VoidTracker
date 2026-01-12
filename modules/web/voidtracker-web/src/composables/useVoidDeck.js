/**
 * Vue Composable for Deck.gl Layer Configuration
 * Provides GPU-accelerated map visualization layers
 * 
 * @example
 * const { profitabilityLayer, voidMeshLayers, viewState } = useVoidDeck(orders);
 */

import { ref, computed, watch } from 'vue';
import { HexagonLayer } from '@deck.gl/aggregation-layers';
import { ArcLayer, IconLayer } from '@deck.gl/layers';

/**
 * Color scale for profitability (Cyberpunk palette)
 * @param {number} margin - Profit margin value
 * @returns {Array<number>} RGB color [r, g, b]
 */
function getProfitabilityColor(margin) {
    if (margin < 50) return [255, 50, 50];        // Crimson (Low)
    if (margin < 100) return [255, 170, 0];       // Amber (Medium)
    if (margin < 200) return [100, 255, 100];     // Green (High)
    return [0, 255, 204];                         // Neon Cyan (Premium)
}

/**
 * Check if order is delayed based on SLA
 * @param {Object} order - Order object
 * @returns {boolean}
 */
function isOrderDelayed(order) {
    if (!order.deliveryAddress?.sla) return false;
    const sla = new Date(order.deliveryAddress.sla);
    return new Date() > sla;
}

/**
 * Main Deck.gl composable
 * @param {import('vue').Ref<Array>} ordersRef - Reactive orders from RxDB
 * @returns {Object} Deck.gl layers and view state
 */
export function useVoidDeck(ordersRef) {
    const viewState = ref({
        longitude: 21.0122,  // Warsaw center
        latitude: 52.2297,
        zoom: 10,
        pitch: 45,           // 3D view angle
        bearing: 0
    });

    const tooltip = ref(null);
    const pulsePhase = ref(0);

    // Animate pulse for delayed orders
    let animationFrame = null;

    function startPulseAnimation() {
        function animate() {
            pulsePhase.value = (pulsePhase.value + 0.05) % (Math.PI * 2);
            animationFrame = requestAnimationFrame(animate);
        }
        animate();
    }

    function stopPulseAnimation() {
        if (animationFrame) {
            cancelAnimationFrame(animationFrame);
            animationFrame = null;
        }
    }

    /**
     * Profitability Heatmap Layer (3D Hexagons)
     */
    const profitabilityLayer = computed(() => {
        const orders = ordersRef.value || [];

        // Filter orders with valid coordinates
        const validOrders = orders.filter(o =>
            o.deliveryAddress?.lat &&
            o.deliveryAddress?.lon
        );

        return new HexagonLayer({
            id: 'profitability-hexagons',
            data: validOrders,
            pickable: true,
            extruded: true,
            radius: 1000,           // 1km hexagons
            elevationScale: 100,
            coverage: 0.9,

            // Position from delivery address
            getPosition: d => [
                d.deliveryAddress.lon,
                d.deliveryAddress.lat
            ],

            // Aggregate profit margin
            getElevationWeight: d => d.properties?.margin || 0,
            getColorWeight: d => d.properties?.margin || 0,

            // Cyberpunk color gradient
            colorRange: [
                [255, 50, 50],       // Crimson
                [255, 170, 0],       // Amber
                [100, 255, 100],     // Green
                [0, 255, 204],       // Neon Cyan
                [0, 200, 255],       // Electric Blue
                [100, 100, 255]      // Purple
            ],

            // Transparency
            opacity: 0.8,

            // Tooltip on hover
            onHover: (info) => {
                if (info.object) {
                    tooltip.value = {
                        x: info.x,
                        y: info.y,
                        content: {
                            region: `Region`,
                            totalProfit: `${Math.round(info.object.elevationValue)} PLN`,
                            orders: info.object.points?.length || 0
                        }
                    };
                } else {
                    tooltip.value = null;
                }
            }
        });
    });

    /**
     * Void-Mesh Arc Layer (Warehouse to Client Routes)
     */
    const voidMeshArcLayer = computed(() => {
        const orders = ordersRef.value || [];

        // Filter orders with warehouse and delivery coordinates
        const routeOrders = orders.filter(o =>
            o.pickupAddress?.lat &&
            o.pickupAddress?.lon &&
            o.deliveryAddress?.lat &&
            o.deliveryAddress?.lon
        );

        // Calculate pulse opacity based on phase
        const pulseOpacity = Math.abs(Math.sin(pulsePhase.value)) * 100 + 100;

        return new ArcLayer({
            id: 'void-mesh-arcs',
            data: routeOrders,
            pickable: true,

            // Warehouse (pickup) as source
            getSourcePosition: d => [
                d.pickupAddress.lon,
                d.pickupAddress.lat
            ],

            // Client (delivery) as target
            getTargetPosition: d => [
                d.deliveryAddress.lon,
                d.deliveryAddress.lat
            ],

            // Dynamic color based on delay status
            getSourceColor: d => {
                const delayed = isOrderDelayed(d);
                if (delayed) {
                    return [255, 0, 0, pulseOpacity];  // Pulsing red
                }
                return [0, 200, 255, 150];  // Neon cyan
            },

            getTargetColor: d => {
                const delayed = isOrderDelayed(d);
                if (delayed) {
                    return [255, 100, 0, pulseOpacity];  // Pulsing orange
                }
                return [100, 255, 100, 150];  // Green
            },

            getWidth: 3,

            // Update trigger for animation
            updateTriggers: {
                getSourceColor: [pulsePhase.value],
                getTargetColor: [pulsePhase.value]
            },

            onHover: (info) => {
                if (info.object) {
                    tooltip.value = {
                        x: info.x,
                        y: info.y,
                        content: {
                            orderId: info.object.orderId?.substring(0, 8),
                            status: info.object.status,
                            delayed: isOrderDelayed(info.object) ? '⚠️ DELAYED' : '✓ ON TIME'
                        }
                    };
                } else {
                    tooltip.value = null;
                }
            }
        });
    });

    /**
     * Icon Layer (Delivery Point Markers)
     */
    const deliveryIconLayer = computed(() => {
        const orders = ordersRef.value || [];

        const validOrders = orders.filter(o =>
            o.deliveryAddress?.lat &&
            o.deliveryAddress?.lon
        );

        return new IconLayer({
            id: 'delivery-markers',
            data: validOrders,
            pickable: true,

            getPosition: d => [
                d.deliveryAddress.lon,
                d.deliveryAddress.lat
            ],

            // Dynamic size based on status
            getSize: d => {
                switch (d.status) {
                    case 'POD': return 45;  // Completed (large)
                    case 'PICKUP': return 35;
                    default: return 30;
                }
            },

            // Color based on status
            getColor: d => {
                switch (d.status) {
                    case 'POD': return [0, 255, 100, 200];     // Green
                    case 'PICKUP': return [255, 200, 0, 200];  // Yellow
                    case 'LOAD': return [100, 100, 255, 200];  // Blue
                    default: return [255, 255, 255, 200];      // White
                }
            },

            // Simple circle icon (no external image needed)
            iconAtlas: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==',
            iconMapping: {},

            // Use billboard mode for better visibility
            billboard: true,

            onHover: (info) => {
                if (info.object) {
                    tooltip.value = {
                        x: info.x,
                        y: info.y,
                        content: {
                            orderId: info.object.orderId?.substring(0, 8),
                            customer: info.object.customerName || 'Unknown',
                            status: info.object.status
                        }
                    };
                } else {
                    tooltip.value = null;
                }
            }
        });
    });

    /**
     * Combined Void-Mesh layers
     */
    const voidMeshLayers = computed(() => [
        voidMeshArcLayer.value,
        deliveryIconLayer.value
    ]);

    /**
     * Update view state (camera position)
     */
    function updateViewState(newState) {
        viewState.value = { ...viewState.value, ...newState };
    }

    // Start pulse animation (for delayed orders)
    startPulseAnimation();

    // Cleanup
    if (typeof window !== 'undefined') {
        window.addEventListener('beforeunload', stopPulseAnimation);
    }

    return {
        // Layers
        profitabilityLayer,
        voidMeshLayers,

        // View state
        viewState,
        updateViewState,

        // Tooltip
        tooltip,

        // Utils
        getProfitabilityColor,
        isOrderDelayed
    };
}
