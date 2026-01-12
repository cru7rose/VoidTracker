/**
 * Planning Store - Manages map filters and Oracle command execution
 */

import { defineStore } from 'pinia';

export const usePlanningStore = defineStore('planning', {
    state: () => ({
        // Filters for map data
        filters: {
            status: null,
            city: null,
            risk: null
        },

        // Active lens (profitability or void-mesh)
        activeLens: 'profitability',

        // Command history (for analytics)
        commandHistory: []
    }),

    getters: {
        /**
         * Check if any filters are active
         */
        hasActiveFilters: (state) => {
            return state.filters.status !== null ||
                state.filters.city !== null ||
                state.filters.risk !== null;
        },

        /**
         * Get filter summary for UI display
         */
        filterSummary: (state) => {
            const parts = [];
            if (state.filters.status) parts.push(`Status: ${state.filters.status}`);
            if (state.filters.city) parts.push(`City: ${state.filters.city}`);
            if (state.filters.risk) parts.push(`Risk: ${state.filters.risk}`);
            return parts.join(' | ') || 'No filters';
        }
    },

    actions: {
        /**
         * Execute command from Oracle NLP system
         * @param {Object} commandResponse - Response from /api/nlp/command/parse
         */
        async executeOracleCommand(commandResponse) {
            const { action, payload } = commandResponse;

            // Log command for analytics
            this.commandHistory.push({
                action,
                payload,
                timestamp: new Date().toISOString()
            });

            // Execute action based on type
            switch (action) {
                case 'FILTER_STATUS':
                    this.filters.status = payload.status;
                    if (payload.city) {
                        this.filters.city = payload.city;
                    }
                    console.log(`[Oracle] Applied status filter: ${payload.status}`);
                    break;

                case 'FILTER_LOCATION':
                    this.filters.city = payload.city;
                    console.log(`[Oracle] Applied location filter: ${payload.city}`);
                    break;

                case 'FILTER_RISK':
                    this.filters.risk = payload.risk;
                    console.log(`[Oracle] Applied risk filter: ${payload.risk}`);
                    break;

                case 'SWITCH_LENS':
                    this.activeLens = payload.lens;
                    console.log(`[Oracle] Switched to lens: ${payload.lens}`);
                    break;

                case 'CLEAR_FILTERS':
                    this.clearAllFilters();
                    console.log('[Oracle] Cleared all filters');
                    break;

                case 'SHOW_STATS':
                    // Trigger stats modal (implement separately)
                    console.log('[Oracle] Show statistics requested');
                    break;

                default:
                    console.warn(`[Oracle] Unknown action: ${action}`);
            }
        },

        /**
         * Clear all active filters
         */
        clearAllFilters() {
            this.filters = {
                status: null,
                city: null,
                risk: null
            };
        },

        /**
         * Set specific filter
         */
        setFilter(type, value) {
            if (this.filters.hasOwnProperty(type)) {
                this.filters[type] = value;
            }
        },

        /**
         * Switch active lens
         */
        switchLens(lens) {
            if (['profitability', 'void-mesh'].includes(lens)) {
                this.activeLens = lens;
            }
        }
    }
});
