import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ArcoResolver } from 'unplugin-vue-components/resolvers'
import path from 'path'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      imports: ['vue', 'vue-router']
    }),
    Components({
      resolvers: [
        ArcoResolver({
          sideEffect: true,
          resolveIcons: true
        })
      ]
    })
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    port: 8080,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8081',
        changeOrigin: true
      },
      '/uploads': {
        target: 'http://127.0.0.1:8081',
        changeOrigin: true
      }
    }
  }
})
