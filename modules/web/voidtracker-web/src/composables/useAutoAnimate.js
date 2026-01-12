/**
 * Vue Composable for @formkit/auto-animate
 * Provides smooth, automatic animations for list/grid elements
 * 
 * @example
 * <script setup>
 * import { useAutoAnimate } from '@/composables/useAutoAnimate';
 * const [listRef] = useAutoAnimate();
 * </script>
 * 
 * <template>
 *   <ul ref="listRef">
 *     <li v-for="item in items" :key="item.id">{{ item.name }}</li>
 *   </ul>
 * </template>
 */

import { ref, onMounted, onBeforeUnmount } from 'vue';
import autoAnimate from '@formkit/auto-animate';

/**
 * Auto-animate composable for Vue 3
 * 
 * @param {Object} options - Animation configuration
 * @param {number} options.duration - Animation duration in ms (default: 200)
 * @param {string} options.easing - CSS easing function (default: 'ease-in-out')
 * @param {boolean} options.disableMobile - Disable on mobile devices (default: false)
 * @returns {[import('vue').Ref, Function]} Tuple of [ref, enableFunction]
 */
export function useAutoAnimate(options = {}) {
    const {
        duration = 200,
        easing = 'ease-in-out',
        disableMobile = false
    } = options;

    const elementRef = ref(null);
    let cleanupFn = null;

    /**
     * Check if animations should be disabled
     */
    function shouldDisableAnimations() {
        // Respect user's motion preferences
        const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
        if (prefersReducedMotion) return true;

        // Optionally disable on mobile for performance
        if (disableMobile) {
            const isMobile = /Android|iPhone|iPad|iPod/i.test(navigator.userAgent);
            if (isMobile) return true;
        }

        return false;
    }

    /**
     * Enable auto-animate on the element
     */
    function enable() {
        if (!elementRef.value) {
            console.warn('[useAutoAnimate] Element ref not set');
            return;
        }

        if (shouldDisableAnimations()) {
            console.log('[useAutoAnimate] Animations disabled (reduced motion or mobile)');
            return;
        }

        // Apply auto-animate
        cleanupFn = autoAnimate(elementRef.value, {
            duration,
            easing,
            // Optional: customize animation behavior
            disrespectUserMotionPreference: false
        });

        console.log('[useAutoAnimate] Animations enabled');
    }

    /**
     * Disable auto-animate
     */
    function disable() {
        if (cleanupFn) {
            cleanupFn();
            cleanupFn = null;
            console.log('[useAutoAnimate] Animations disabled');
        }
    }

    // Auto-enable on mount
    onMounted(() => {
        enable();
    });

    // Cleanup on unmount
    onBeforeUnmount(() => {
        disable();
    });

    return [elementRef, enable, disable];
}

/**
 * Pre-configured auto-animate for lists
 * Faster animations for better UX
 */
export function useAutoAnimateList() {
    return useAutoAnimate({
        duration: 180,
        easing: 'ease-out'
    });
}

/**
 * Pre-configured auto-animate for grids/cards
 * Slightly slower for more graceful movement
 */
export function useAutoAnimateGrid() {
    return useAutoAnimate({
        duration: 250,
        easing: 'cubic-bezier(0.4, 0, 0.2, 1)'
    });
}

/**
 * Custom animation config for specific use cases
 * 
 * @example
 * const [ref] = useAutoAnimateCustom({
 *   duration: 300,
 *   easing: 'spring',
 *   disableMobile: true
 * });
 */
export function useAutoAnimateCustom(config) {
    return useAutoAnimate(config);
}
