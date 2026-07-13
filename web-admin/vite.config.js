import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { fileURLToPath, URL } from 'node:url'
import http from 'node:http'
import https from 'node:https'

/**
 * 后端不可达时返回干净的 503 JSON，
 * 而不是抛出 ECONNREFUSED 把控制台刷红。
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
  // 自定义 agent：用 try-connect 思路；Vite 是基于 http-proxy 的 native 事件
  configure(proxy) {
    proxy.on('error', (err, _req, res) => {
      if (res && res.writableEnded === false) return fallbackHandler(_req, res)
    })
    proxy.on('proxyReq', (_proxyReq, req) => {
      // 标记：当 req 写到底也失败时，fallback
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
    port: 8081,
    proxy: {
      '/api': buildProxyOptions()
    }
  }
})
