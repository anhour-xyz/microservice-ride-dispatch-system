import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api/v1/rides': { target: 'http://localhost:8083', changeOrigin: true },
      '/api/v1/locations': { target: 'http://localhost:8082', changeOrigin: true },
    },
  },
})
