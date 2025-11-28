import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import electron from 'vite-plugin-electron'

export default defineConfig({
  plugins: [
    vue(),
    electron({
      entry: 'src/main/index.js', // 主进程入口
      onstart: (options) => {
        options.startup() // 开发模式自动启动Electron
      }
    })
  ],
  resolve: {
    alias: {
      '@': '/src' // 路径别名（方便引入文件）
    }
  },
  server: {
    port: 3344, // Vue开发服务器端口（避免与后端3000端口冲突）
    proxy: {
      // 代理API请求到后端（解决跨域）
      '/api': {
        target: 'http://localhost:3000', // 后端启动地址（参考Nodejs README）
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})