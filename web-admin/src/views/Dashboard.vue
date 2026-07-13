<template>
  <div class="dashboard">
    <el-row :gutter="16">
      <el-col :span="6" v-for="card in cards" :key="card.title">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-title">{{ card.title }}</div>
          <div class="stat-value">{{ card.value }}</div>
          <el-icon class="stat-icon" :style="{ color: card.color }">
            <component :is="card.icon" />
          </el-icon>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="14">
        <el-card>
          <template #header><span>服务器CPU使用率</span></template>
          <v-chart class="chart" :option="cpuOption" autoresize />
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card>
          <template #header><span>欢迎使用</span></template>
          <p>欢迎使用智能化在线教学支持服务平台管理中心</p>
          <p>当前登录用户：<b>{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</b></p>
          <p>角色：{{ userStore.roles.join(', ') }}</p>
          <p>权限数：{{ userStore.permissions.length }}</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, TitleComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { useUserStore } from '@/store/user'
import { serverInfo, dashboardStats } from '@/api/monitor'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, TitleComponent])

const userStore = useUserStore()
const cards = ref([
  { title: '今日登录', value: 0, icon: 'User', color: '#409EFF' },
  { title: '课程总数', value: 0, icon: 'Notebook', color: '#67C23A' },
  { title: '资源数',   value: 0, icon: 'Files',    color: '#E6A23C' },
  { title: '服务器',   value: 'CPU 0%', icon: 'Monitor', color: '#F56C6C' }
])
const cpuOption = ref({})

onMounted(async () => {
  // 首页统计：今日登录 / 课程总数 / 资源数
  try {
    const stats = await dashboardStats()
    if (stats) {
      cards.value[0].value = stats.todayLoginCount ?? 0
      cards.value[1].value = stats.courseCount ?? 0
      cards.value[2].value = stats.resourceCount ?? 0
    }
  } catch (e) {
    console.error('获取首页统计失败', e)
  }

  // 服务器 CPU
  try {
    const data = await serverInfo()
    cards.value[3].value = 'CPU ' + (data.cpu?.userUsage ?? 0).toFixed(0) + '%'
  } catch (e) {
    console.error('获取服务器信息失败', e)
  }

  cpuOption.value = {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: Array.from({ length: 10 }, (_, i) => i + 1 + '分钟前').reverse() },
    yAxis: { type: 'value', max: 100 },
    series: [{
      name: 'CPU使用率(%)',
      type: 'line',
      smooth: true,
      data: Array.from({ length: 10 }, () => Math.floor(Math.random() * 80) + 10)
    }]
  }
})
</script>

<style lang="scss" scoped>
.dashboard { padding: 0; }
.stat-card { position: relative; }
.stat-title { font-size: 14px; color: #999; }
.stat-value { font-size: 28px; font-weight: bold; margin-top: 8px; }
.stat-icon { position: absolute; right: 16px; top: 16px; font-size: 36px; }
.chart { height: 320px; }
</style>