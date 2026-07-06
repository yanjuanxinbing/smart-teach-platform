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
      <h2 class="section-title">新闻资讯</h2>
      <div class="card-list">
        <div v-for="item in list" :key="item.id" class="card-item" @click="goArticle(item)">
          <h3>{{ item.title }}</h3>
          <div class="meta">{{ formatDate(item.publishTime) }}</div>
          <div class="desc">{{ stripHtml(item.content) }}</div>
        </div>
      </div>
      <el-empty v-if="!list.length" description="暂无新闻" />
    </div>

    <footer class="portal-footer">
      <div>Copyright © 2024 智能化在线教学支持服务平台</div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listByType } from '@/api/portal'
import dayjs from 'dayjs'

const router = useRouter()
const list = ref([])
const formatDate = (t) => t ? dayjs(t).format('YYYY-MM-DD') : ''
const stripHtml = (s) => s ? s.replace(/<[^>]+>/g, '').slice(0, 80) + '...' : ''
const goArticle = (item) => router.push(`/article/${item.id}`)

onMounted(async () => { list.value = await listByType(3, 50) })
</script>
