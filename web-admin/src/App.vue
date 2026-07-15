<template>
  <router-view />
</template>

<script setup>
import { useUserStore } from '@/store/user'

// 启动时检查 URL query,接管 portal 端带过来的 access_token
// 必须在 router 初始化之前调用,否则 beforeEach 会因为没 token 跳到 /login
const userStore = useUserStore()
userStore.bootFromQuery()
</script>

<style lang="scss">
html, body, #app {
  height: 100%;
  margin: 0;
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}
</style>
