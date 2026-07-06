<template>
  <div>
    <header class="portal-header">
      <div class="logo">智能教学平台</div>
      <div class="nav">
        <router-link to="/">首页</router-link>
        <router-link to="/notice">通知公告</router-link>
        <router-link to="/news">新闻资讯</router-link>
      </div>
    </header>

    <div class="container">
      <el-card>
        <h1 style="text-align: center">{{ article.title }}</h1>
        <div style="text-align: center; color: #999; margin: 16px 0">
          <span>发布时间：{{ formatDate(article.publishTime) }}</span>
          <span style="margin-left: 16px">浏览：{{ article.viewCount || 0 }}</span>
        </div>
        <el-divider />
        <div class="content" v-html="article.content" />
      </el-card>
    </div>

    <footer class="portal-footer">
      <div>Copyright © 2024 智能化在线教学支持服务平台</div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { detail } from '@/api/portal'
import dayjs from 'dayjs'

const route = useRoute()
const article = ref({})
const formatDate = (t) => t ? dayjs(t).format('YYYY-MM-DD HH:mm') : ''

onMounted(async () => { article.value = await detail(route.params.id) })
</script>

<style scoped>
.content { font-size: 15px; line-height: 1.8; color: #333; }
.content :deep(img) { max-width: 100%; }
</style>
