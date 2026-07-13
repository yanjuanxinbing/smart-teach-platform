<template>
  <div class="page">
    <header class="page__header">
      <div class="container">
        <div class="rubric page__rubric">
          <span>Section · 04 / Insights</span>
          <span class="rubric__dot"></span>
          <span>Data &amp; Analytics</span>
          <span class="rubric__dot"></span>
          <span>Live from backend</span>
        </div>
        <h1 class="page__title">平台<em>数据洞察</em></h1>
        <p class="page__lead">
          课程结构、资源分布与内容发布趋势的一览视图——数据实时来自管理后台。
        </p>
      </div>
    </header>

    <section class="page__body">
      <div class="container">
        <p v-if="loading" class="loading-tip">数据加载中…</p>

        <template v-else>
          <div class="charts-grid">
            <div class="chart-card" data-reveal>
              <div class="chart-card__head">
                <span class="eyebrow eyebrow--accent"><span class="eyebrow__rule"></span>课程性质分布</span>
                <h3 class="chart-card__title">课程<em>性质</em>占比</h3>
              </div>
              <v-chart class="chart" :option="pieOption" autoresize />
            </div>

            <div class="chart-card" data-reveal style="transition-delay:60ms">
              <div class="chart-card__head">
                <span class="eyebrow eyebrow--accent"><span class="eyebrow__rule"></span>教学资源</span>
                <h3 class="chart-card__title">各类型资源<em>数量</em></h3>
              </div>
              <v-chart class="chart" :option="barOption" autoresize />
            </div>

            <div class="chart-card" data-reveal style="transition-delay:120ms">
              <div class="chart-card__head">
                <span class="eyebrow eyebrow--accent"><span class="eyebrow__rule"></span>内容发布</span>
                <h3 class="chart-card__title">近6个月<em>发布趋势</em></h3>
              </div>
              <v-chart class="chart" :option="lineOption" autoresize />
            </div>

            <div class="chart-card" data-reveal style="transition-delay:180ms">
              <div class="chart-card__head">
                <span class="eyebrow eyebrow--accent"><span class="eyebrow__rule"></span>课程学时</span>
                <h3 class="chart-card__title">学时<em>分布</em>直方图</h3>
              </div>
              <v-chart class="chart" :option="histOption" autoresize />
            </div>
          </div>

          <div v-if="loadFailed" class="empty" data-reveal>
            <el-icon :size="28"><Warning /></el-icon>
            <p>数据加载失败，请稍后重试</p>
          </div>
        </template>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, PieChart, LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { Warning } from '@element-plus/icons-vue'
import { siteStats } from '@/api/portal'

use([CanvasRenderer, BarChart, PieChart, LineChart, GridComponent, TooltipComponent, LegendComponent])

const loading = ref(true)
const loadFailed = ref(false)

const stats = ref({
  courseTypeDistribution: [],
  resourceTypeCount: [],
  monthlyArticleTrend: [],
  courseHoursHistogram: []
})

// 与全站蓝色主题一致的图表配色
const PALETTE = ['#5F86FF', '#2F5BFF', '#A9C2FF', '#8A99C2', '#2F3E66']
const axisText = { color: '#98A2BC', fontFamily: 'Inter, "Noto Sans SC", sans-serif', fontSize: 12 }
const axisLine = { lineStyle: { color: '#DDE3F1' } }
const splitLine = { lineStyle: { color: '#E8EDF7' } }

const pieOption = computed(() => ({
  color: PALETTE,
  tooltip: { trigger: 'item' },
  legend: { bottom: 0, textStyle: axisText },
  series: [{
    type: 'pie',
    radius: ['42%', '70%'],
    itemStyle: { borderColor: '#fff', borderWidth: 2 },
    label: { color: '#2F3E66', fontSize: 12 },
    data: (stats.value.courseTypeDistribution || []).map(d => ({ name: d.name, value: d.value }))
  }]
}))

const barOption = computed(() => {
  const list = stats.value.resourceTypeCount || []
  return {
    color: ['#5F86FF'],
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 40, right: 16, top: 24, bottom: 32 },
    xAxis: { type: 'category', data: list.map(d => d.name), axisLine, axisLabel: axisText },
    yAxis: { type: 'value', splitLine, axisLabel: axisText },
    series: [{ type: 'bar', data: list.map(d => d.value), barWidth: '46%', itemStyle: { borderRadius: [3, 3, 0, 0] } }]
  }
})

const lineOption = computed(() => {
  const list = stats.value.monthlyArticleTrend || []
  return {
    color: ['#2F5BFF'],
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 16, top: 24, bottom: 32 },
    xAxis: { type: 'category', data: list.map(d => d.name), axisLine, axisLabel: axisText },
    yAxis: { type: 'value', splitLine, axisLabel: axisText },
    series: [{
      type: 'line',
      data: list.map(d => d.value),
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: { width: 2 },
      areaStyle: { color: 'rgba(95,134,255,0.12)' }
    }]
  }
})

const histOption = computed(() => {
  const list = stats.value.courseHoursHistogram || []
  return {
    color: ['#A9C2FF'],
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 40, right: 16, top: 24, bottom: 40 },
    xAxis: { type: 'category', data: list.map(d => d.name), axisLine, axisLabel: axisText, name: '学时区间', nameTextStyle: axisText },
    yAxis: { type: 'value', splitLine, axisLabel: axisText, name: '课程数' },
    // barCategoryGap: 0 让柱子相邻贴合，呈现直方图的视觉效果
    series: [{ type: 'bar', data: list.map(d => d.value), barCategoryGap: '0%', itemStyle: { color: '#8FB0FF' } }]
  }
})

onMounted(async () => {
  try {
    stats.value = await siteStats()
  } catch (e) {
    loadFailed.value = true
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.page__header {
  padding: clamp(64px, 9vw, 112px) 0 clamp(32px, 5vw, 64px);
  border-bottom: 1px solid var(--line);
  background: linear-gradient(180deg, #FFFFFF 0%, #F4F7FF 100%);
}
.page__rubric { margin-bottom: var(--s-6); }
.page__rubric .dot { width: 3px; height: 3px; background: var(--mute-soft); border-radius: 50%; }
.page__title {
  font-family: var(--font-display);
  font-size: clamp(40px, 6vw, 72px);
  font-weight: 600;
  line-height: 1.04;
  letter-spacing: -0.025em;
  margin: 0 0 var(--s-4);
}
.page__title em { font-family: var(--font-serif); font-style: italic; font-weight: 500; color: var(--accent); }
.page__lead { max-width: 56ch; font-size: 15.5px; line-height: 1.75; color: var(--ink-soft); margin: 0; }

.page__body { padding: clamp(56px, 8vw, 96px) 0; }

.loading-tip { color: var(--mute); font-size: 13px; letter-spacing: 0.06em; text-align: center; padding: 64px 0; }

.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: clamp(20px, 3vw, 32px);
}
.chart-card {
  background: var(--surface);
  border: 1px solid var(--line);
  padding: 24px;
  transition: border-color var(--t-base) var(--ease), box-shadow var(--t-base) var(--ease);
}
.chart-card:hover { border-color: var(--accent); box-shadow: var(--shadow-blue); }
.chart-card__head { margin-bottom: var(--s-4); }
.chart-card__title {
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 600;
  color: var(--ink);
  margin: 6px 0 0;
  letter-spacing: -0.01em;
}
.chart-card__title em { font-family: var(--font-serif); font-style: italic; font-weight: 500; color: var(--accent); }
.chart { height: 320px; }

.empty {
  padding: 56px 0;
  text-align: center;
  color: var(--mute);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}
.empty p { margin: 0; font-size: 13px; letter-spacing: 0.06em; }

@media (max-width: 900px) {
  .charts-grid { grid-template-columns: 1fr; }
}
</style>