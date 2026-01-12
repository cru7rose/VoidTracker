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
        secure: false
      },
      '/api/dashboard': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      }
    }
  }
})
