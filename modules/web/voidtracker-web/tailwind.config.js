/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Void Theme - Deep Space Aesthetic
        void: {
          black: '#050505',      // Deep space background
          darker: '#0A0A0F',     // Panel backgrounds
          dark: '#1A1A2E',       // Elevated elements
          gray: '#2E2E3E',       // Borders and dividers
          cyan: {
            400: '#00FFCC',      // Primary accent
            500: '#00E5CC',
            600: '#00B3A6',
          },
          pink: {
            400: '#FF006E',      // Alert/danger accent
            500: '#E6005C',
            600: '#CC0052',
          },
          amber: {
            400: '#FFAA00',      // Warning accent
            500: '#E69900',
            600: '#CC8800',
          },
        },
        // Legacy colors (keep for backward compatibility)
        primary: {
          50: '#eff6ff',
          100: '#dbeafe',
          200: '#bfdbfe',
          300: '#93c5fd',
          400: '#60a5fa',
          500: '#3b82f6',
          600: '#2563eb',
          700: '#1d4ed8',
          800: '#1e40af',
          900: '#1e3a8a',
        },
        success: {
          50: '#f0fdf4',
          100: '#dcfce7',
          200: '#bbf7d0',
          300: '#86efac',
          400: '#4ade80',
          500: '#22c55e',
          600: '#16a34a',
          700: '#15803d',
          800: '#166534',
          900: '#14532d',
        },
        warning: {
          50: '#fffbeb',
          100: '#fef3c7',
          200: '#fde68a',
          300: '#fcd34d',
          400: '#fbbf24',
          500: '#f59e0b',
          600: '#d97706',
          700: '#b45309',
          800: '#92400e',
          900: '#78350f',
        },
        danger: {
          50: '#fef2f2',
          100: '#fee2e2',
          200: '#fecaca',
          300: '#fca5a5',
          400: '#f87171',
          500: '#ef4444',
          600: '#dc2626',
          700: '#b91c1c',
          800: '#991b1b',
          900: '#7f1d1d',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'Consolas', 'monospace'],
      },
      backdropBlur: {
        xs: '2px',
        '3xl': '64px',
      },
      animation: {
        'fade-in': 'fadeIn 0.3s ease-in-out',
        'slide-in': 'slideIn 0.3s ease-out',
        'slide-left': 'slideLeft 0.3s ease-out',
        'glow-pulse': 'glowPulse 2s ease-in-out infinite',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideIn: {
          '0%': { transform: 'translateY(-10px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        slideLeft: {
          '0%': { transform: 'translateX(100%)', opacity: '0' },
          '100%': { transform: 'translateX(0)', opacity: '1' },
        },
        glowPulse: {
          '0%, 100%': { opacity: '1' },
          '50%': { opacity: '0.6' },
        },
      },
    },
  },
  plugins: [
    // Glassmorphism & Void utilities
    function ({ addComponents, addUtilities }) {
      addComponents({
        '.glass-panel': {
          '@apply bg-void-darker/60 backdrop-blur-xl border border-white/10 shadow-2xl': {},
        },
        '.glass-panel-hover': {
          '@apply hover:bg-void-darker/80 hover:border-void-cyan-500/30 transition-all duration-300': {},
        },
        '.neon-text': {
          '@apply text-void-cyan-400': {},
          'text-shadow': '0 0 10px rgba(0, 255, 204, 0.5)',
        },
        '.neon-text-pink': {
          '@apply text-void-pink-400': {},
          'text-shadow': '0 0 10px rgba(255, 0, 110, 0.5)',
        },
        '.neon-border': {
          '@apply border border-void-cyan-400': {},
          'box-shadow': '0 0 10px rgba(0, 255, 204, 0.3), inset 0 0 10px rgba(0, 255, 204, 0.1)',
        },
      });

      addUtilities({
        '.text-glow': {
          'text-shadow': '0 0 10px currentColor',
        },
        '.text-glow-strong': {
          'text-shadow': '0 0 20px currentColor, 0 0 40px currentColor',
        },
      });
    },
  ],
}
