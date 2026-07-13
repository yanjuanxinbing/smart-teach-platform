import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { fileURLToPath, URL } from 'node:url'

/**
 * 后端不可达时返回干净的 503 JSON，而不是抛出 ECONNREFUSED。
 */
const fallbackHandler = (req, res) => {
  res.statusCode = 503
  res.setHeader('Content-Type', 'application/json; charset=utf-8')
  res.end(JSON.stringify({
    code: 503,
    message: '后端服务未启动或不可达，请先启动 Spring Boot (http://localhost:8080)',
    path: req.url
  }))
}

const buildProxyOptions = () => ({
  target: 'http://localhost:8080',
  changeOrigin: true,
  timeout: 5000,
  configure(proxy) {
    proxy.on('error', (err, _req, res) => {
      if (res && res.writableEnded === false) return fallbackHandler(_req, res)
    })
  }
})

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({ resolvers: [ElementPlusResolver()] }),
    Components({ resolvers: [ElementPlusResolver()] })
  ],
  resolve: {
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) }
  },
  server: {
    port: 8082,
    proxy: {
      '/api': buildProxyOptions()
    }
  }
})
