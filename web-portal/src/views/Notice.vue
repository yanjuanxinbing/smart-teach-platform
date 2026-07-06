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
      <h2 class="section-title">通知公告</h2>
      <el-card>
        <div v-for="item in list" :key="item.id" class="notice-item" @click="goArticle(item)">
          <span class="title">{{ item.title }}</span>
          <span class="time">{{ formatDate(item.publishTime) }}</span>
        </div>
        <el-empty v-if="!list.length" description="暂无通知" />
      </el-card>
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
const goArticle = (item) => router.push(`/article/${item.id}`)

onMounted(async () => { list.value = await listByType(2, 50) })
</script>

<style scoped>
.notice-item { padding: 14px 0; border-bottom: 1px dashed #eee; cursor: pointer; display: flex; justify-content: space-between; }
.notice-item:hover .title { color: #1890ff; }
.notice-item .title { color: #333; font-size: 15px; }
.notice-item .time { color: #999; font-size: 12px; }
</style>
