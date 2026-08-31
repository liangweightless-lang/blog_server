import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ArcoResolver } from 'unplugin-vue-components/resolvers'
import { fileURLToPath, URL } from 'node:url'

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
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 8080,
    proxy: {
      '/api': {
        target: 'https://test.caibread.com', // 指向服务器 dev
        changeOrigin: true
      },
      '/uploads': {
        target: 'https://test.caibread.com', // 指向服务器 dev
        changeOrigin: true
      }
    }
  },
  esbuild: {
    drop: process.env.NODE_ENV === 'production' ? ['console', 'debugger'] : []
  },
  build: {
    // 关闭压缩大小报告计算，极大减少 rendering chunks 时的 CPU 与内存消耗（防止 2G 机器卡死）
    reportCompressedSize: false,
    // 禁用 sourcemap 减小 50% 内存消耗
    sourcemap: false,
    // 使用极速轻量的 esbuild 压缩
    minify: 'esbuild',
    chunkSizeWarningLimit: 2000,
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('node_modules')) {
            if (id.includes('@arco-design')) return 'arco';
            if (id.includes('vue') || id.includes('pinia')) return 'vendor';
          }
        }
      }
    }
  }
})

