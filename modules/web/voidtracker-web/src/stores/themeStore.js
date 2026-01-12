import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useThemeStore = defineStore('theme', () => {
    // Default Branding
    const branding = ref({
        systemName: 'DANXILS Logistics',
        logoUrl: '', // Empty means use default text/icon
        primaryColor: '#2563eb', // Default Blue-600
        secondaryColor: '#1e40af' // Default Blue-800
    });

    // Dark Mode State with Persistence
    const isDark = ref(localStorage.getItem('theme') === 'dark');

    const updateBranding = (newBranding) => {
        branding.value = { ...branding.value, ...newBranding };
        applyTheme();
    };

    const toggleTheme = () => {
        isDark.value = !isDark.value;
        localStorage.setItem('theme', isDark.value ? 'dark' : 'light');
        applyTheme();
    };

    const applyTheme = () => {
        const root = document.documentElement;

        // Apply Dark Mode Class
        if (isDark.value) {
            root.classList.add('dark');
        } else {
            root.classList.remove('dark');
        }

        // Apply Branding Colors
        root.style.setProperty('--primary-color', branding.value.primaryColor);
        // Calculate a darker shade for hover states if not provided
        root.style.setProperty('--primary-color-hover', branding.value.secondaryColor);

        // Update document title
        document.title = branding.value.systemName;
    };

    return {
        branding,
        isDark,
        updateBranding,
        toggleTheme,
        applyTheme
    };
});
