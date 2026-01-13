import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useThemeStore = defineStore('theme', () => {
    // Default Branding
    const branding = ref({
        systemName: 'VoidTracker',
        logoUrl: '', // Empty means use default text/icon
        primaryColor: '#1db954', // Spotify green
        secondaryColor: '#1ed760' // Spotify green hover
    });

    // Dark Mode State with Persistence
    // Default to dark mode if not set
    const isDark = ref(localStorage.getItem('theme') !== 'light');

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
        const html = document.documentElement;

        // Apply Dark Mode Class to both html and body
        if (isDark.value) {
            html.classList.add('dark');
            document.body.classList.add('dark');
        } else {
            html.classList.remove('dark');
            document.body.classList.remove('dark');
        }

        // Apply Branding Colors
        root.style.setProperty('--primary-color', branding.value.primaryColor);
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
