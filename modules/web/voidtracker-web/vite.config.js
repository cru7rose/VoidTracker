import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    host: '0.0.0.0', // Nasłuchuj na wszystkich interfejsach (dostęp z zewnątrz)
    port: 5173,
    proxy: {
      '/api/auth': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        secure: false
      },
      '/api/users': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        secure: false
      },
      '/api/orders': {
        target: 'http://localhost:8091',
        changeOrigin: true,
        secure: false
      },
      '/api/planning': {
        target: 'http://localhost:8093',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/api\/planning/, '')
      },
      '/api/dashboard': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      },
      // WebSocket proxy for Planning Service
      '/api/planning/ws-planning': {
        target: 'http://localhost:8093',
        changeOrigin: true,
        ws: true,
        secure: false
      },
      '/ws-planning': {
        target: 'http://localhost:8093',
        changeOrigin: true,
        ws: true,
        secure: false
      },
      // Additional API endpoints
      '/api/organizations': {
        target: 'http://localhost:8091',
        changeOrigin: true,
        secure: false
      },
      '/api/configs': {
        target: 'http://localhost:8091',
        changeOrigin: true,
        secure: false
      },
      '/solutions': {
        target: 'http://localhost:8093',
        changeOrigin: true,
        secure: false
      },
      '/manifests': {
        target: 'http://localhost:8093',
        changeOrigin: true,
        secure: false
      },
      '/api/config': {
        target: 'http://localhost:8093',
        changeOrigin: true,
        secure: false
      },
      '/api/communications': {
        target: 'http://localhost:8093',
        changeOrigin: true,
        secure: false
      },
      // Driver service proxy
      '/api/driver': {
        target: 'http://localhost:8092',
        changeOrigin: true,
        secure: false
      }
    }
  }
})
