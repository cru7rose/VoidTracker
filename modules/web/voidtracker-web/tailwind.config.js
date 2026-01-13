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
        // Spotify-inspired color palette
        spotify: {
          black: '#121212',      // Main background (Spotify dark)
          darker: '#181818',      // Card backgrounds
          dark: '#1a1a1a',        // Elevated elements
          gray: {
            50: '#fafafa',
            100: '#f5f5f5',
            200: '#e5e5e5',
            300: '#d4d4d4',
            400: '#a3a3a3',
            500: '#737373',
            600: '#525252',
            700: '#404040',
            800: '#262626',
            900: '#171717',
          },
          green: {
            400: '#1db954',       // Spotify green (primary accent)
            500: '#1ed760',       // Hover green
            600: '#1aa34a',       // Darker green
          },
          white: '#ffffff',
        },
        // Void Theme - Keep for backward compatibility but make it Spotify-like
        void: {
          black: '#121212',      // Spotify black instead of pure black
          darker: '#181818',     // Spotify darker
          dark: '#1a1a1a',       // Spotify dark
          gray: '#2a2a2a',       // Lighter gray
          cyan: {
            300: '#1db954',      // Use Spotify green instead
            400: '#1ed760',     // Spotify green hover
            500: '#1aa34a',
            600: '#158a3d',
            700: '#0f6b2e',
            800: '#0a4d21',
            900: '#052e14',
            950: '#021a0c',
          },
          pink: {
            400: '#ff6b9d',      // Softer pink
            500: '#ff4d7a',
            600: '#e6395c',
          },
          amber: {
            400: '#ffb84d',     // Softer amber
            500: '#ffa726',
            600: '#ff9800',
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
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      },
      animation: {
        'fade-in': 'fadeIn 0.3s ease-in',
        'slide-up': 'slideUp 0.3s ease-out',
        'slide-down': 'slideDown 0.3s ease-out',
        'scale-in': 'scaleIn 0.2s ease-out',
        'spotify-hover': 'spotifyHover 0.2s ease-out',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%': { transform: 'translateY(10px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        slideDown: {
          '0%': { transform: 'translateY(-10px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        scaleIn: {
          '0%': { transform: 'scale(0.95)', opacity: '0' },
          '100%': { transform: 'scale(1)', opacity: '1' },
        },
        spotifyHover: {
          '0%': { transform: 'scale(1)' },
          '100%': { transform: 'scale(1.02)' },
        },
      },
      backdropBlur: {
        xs: '2px',
      },
      boxShadow: {
        'spotify': '0 8px 16px rgba(0, 0, 0, 0.3)',
        'spotify-lg': '0 16px 32px rgba(0, 0, 0, 0.4)',
        'spotify-hover': '0 12px 24px rgba(0, 0, 0, 0.5)',
      },
    },
  },
  plugins: [],
}
