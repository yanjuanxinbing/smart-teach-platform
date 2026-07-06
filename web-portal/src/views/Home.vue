<template>
  <div>
    <header class="portal-header">
      <div class="logo">智能教学平台</div>
      <div class="nav">
        <router-link to="/">首页</router-link>
        <router-link to="/notice">通知公告</router-link>
        <router-link to="/news">新闻资讯</router-link>
        <a href="http://localhost:8081" target="_blank">管理入口</a>
      </div>
    </header>

    <div class="container">
      <el-carousel height="360px" class="banner" v-if="banners.length">
        <el-carousel-item v-for="b in banners" :key="b.id">
          <a :href="b.linkUrl || `/article/${b.id}`" target="_blank">
            <img :src="b.coverImage || defaultBanner" :alt="b.title" style="width:100%;height:360px;object-fit:cover" />
          </a>
        </el-carousel-item>
      </el-carousel>

      <h2 class="section-title">通知公告</h2>
      <el-row :gutter="16">
        <el-col :span="14">
          <el-card>
            <div v-for="item in notices.slice(0, 5)" :key="item.id" class="notice-item" @click="goArticle(item)">
              <span class="title">{{ item.title }}</span>
              <span class="time">{{ formatDate(item.publishTime) }}</span>
            </div>
            <el-empty v-if="!notices.length" description="暂无通知" />
          </el-card>
        </el-col>
        <el-col :span="10">
          <el-card>
            <template #header><span>最新新闻</span></template>
            <div v-for="item in news.slice(0, 4)" :key="item.id" class="news-item" @click="goArticle(item)">
              <div class="title">{{ item.title }}</div>
              <div class="meta">{{ formatDate(item.publishTime) }}</div>
            </div>
            <el-empty v-if="!news.length" description="暂无新闻" />
          </el-card>
        </el-col>
      </el-row>

      <h2 class="section-title">新闻资讯</h2>
      <div class="card-list">
        <div v-for="item in news" :key="item.id" class="card-item" @click="goArticle(item)">
          <h3>{{ item.title }}</h3>
          <div class="meta">{{ formatDate(item.publishTime) }}</div>
          <div class="desc">{{ stripHtml(item.content) }}</div>
        </div>
      </div>
    </div>

    <footer class="portal-footer">
      <div>Copyright © 2024 智能化在线教学支持服务平台</div>
      <div>Powered by Spring Boot + Vue3</div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listByType, detail } from '@/api/portal'
import dayjs from 'dayjs'
import defaultBanner from '@/assets/banner.svg'

const router = useRouter()
const banners = ref([])
const notices = ref([])
const news = ref([])

const formatDate = (t) => t ? dayjs(t).format('YYYY-MM-DD') : ''
const stripHtml = (s) => s ? s.replace(/<[^>]+>/g, '').slice(0, 80) + '...' : ''

const goArticle = (item) => router.push(`/article/${item.id}`)

onMounted(async () => {
  banners.value = await listByType(1, 5)
  notices.value = await listByType(2, 10)
  news.value = await listByType(3, 6)
})
</script>

<style scoped>
.notice-item { padding: 10px 0; border-bottom: 1px dashed #eee; cursor: pointer; display: flex; justify-content: space-between; }
.notice-item:hover .title { color: #1890ff; }
.notice-item .title { color: #333; }
.notice-item .time { color: #999; font-size: 12px; }
.news-item { padding: 8px 0; border-bottom: 1px dashed #eee; cursor: pointer; }
.news-item:hover .title { color: #1890ff; }
.news-item .title { color: #333; font-size: 14px; }
.news-item .meta { color: #999; font-size: 12px; margin-top: 4px; }
</style>
