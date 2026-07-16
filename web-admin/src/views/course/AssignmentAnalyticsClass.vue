<template>
  <div class="app-container">
    <el-card class="filter-bar">
      <div class="filter-row">
        <el-select v-model="query.classId" filterable placeholder="选择班级" style="width: 220px" @change="load">
          <el-option v-for="c in classOptions" :key="c.id" :label="c.className" :value="c.id" />
        </el-select>
        <el-button style="margin-left: 12px" @click="load">刷新</el-button>
        <el-tag v-if="stats && stats.semester" type="info" style="margin-left: 12px">
          学期：{{ stats.semester }}（按当前日期自动推断）
        </el-tag>
      </div>
    </el-card>

    <el-row :gutter="16" class="kpi-row" v-if="stats">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-title">作业总数</div>
          <div class="stat-value">{{ stats.assignmentCount }}</div>
          <el-icon class="stat-icon" style="color: #409EFF"><Notebook /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-title">提交率</div>
          <div class="stat-value">{{ submitRate }}</div>
          <el-icon class="stat-icon" style="color: #67C23A"><Upload /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-title">平均分</div>
          <div class="stat-value">{{ formatNum(stats.avgScore) }}</div>
          <el-icon class="stat-icon" style="color: #E6A23C"><Medal /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-title">滞交人次</div>
          <div class="stat-value">{{ stats.lateCount }}</div>
          <el-icon class="stat-icon" style="color: #F56C6C"><Clock /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row" v-if="stats">
      <el-col :span="12">
        <el-card>
          <template #header><span>分数段分布（已批改）</span></template>
          <v-chart class="chart" :option="bucketOption" autoresize />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>按作业平均分</span></template>
          <v-chart class="chart" :option="avgOption" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row" v-if="stats && stats.monthlyTrend && stats.monthlyTrend.length">
      <el-col :span="24">
        <el-card>
          <template #header><span>按月分数趋势</span></template>
          <v-chart class="chart" :option="trendOption" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <el-card class="ai-card" v-if="stats">
      <template #header>
        <span>AI 智能分析报告（基于本班数据，本地 Ollama 推理）</span>
        <el-button
          type="primary"
          :loading="aiLoading"
          style="float: right"
          @click="generate"
        >一键生成</el-button>
      </template>
      <div v-if="aiReport" class="report" v-html="aiReport"></div>
      <el-empty v-else description="点击右上角「一键生成」，10-30s 内给出 4-6 段教学分析" />
      <div v-if="aiMeta" class="meta">
        模型：<b>{{ aiMeta.model }}</b>
        <span v-if="aiMeta.promptEvalCount"> · prompt tokens {{ aiMeta.promptEvalCount }}</span>
        <span v-if="aiMeta.evalCount"> · eval tokens {{ aiMeta.evalCount }}</span>
        <span v-if="aiMeta.durationMs"> · 耗时 {{ aiMeta.durationMs }}ms</span>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { Notebook, Upload, Medal, Clock } from '@element-plus/icons-vue'
import { classListAll } from '@/api/system'
import { classStats, generateClassReport } from '@/api/analytics'

use([CanvasRenderer, BarChart, LineChart, GridComponent, TooltipComponent, LegendComponent])

const query = reactive({ classId: null, semester: '' })
const classOptions = ref([])
const stats = ref(null)
const aiReport = ref('')
const aiMeta = ref(null)
const aiLoading = ref(false)

const submitRate = computed(() => {
  if (!stats.value) return '-'
  const e = Number(stats.value.expectedSubmissions || 0)
  const s = Number(stats.value.submittedCount || 0)
  if (e <= 0) return '-'
  return (s * 100 / e).toFixed(1) + '%'
})

function formatNum(v) {
  if (v == null) return '-'
  return Number(v).toFixed(1)
}

const bucketOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: ['90+', '80-89', '70-79', '60-69', '60-', '未提交'] },
  yAxis: { type: 'value', minInterval: 1 },
  series: [{
    type: 'bar',
    data: stats.value ? [
      stats.value.bucket90, stats.value.bucket80, stats.value.bucket70,
      stats.value.bucket60, stats.value.bucketFail, stats.value.bucketUnsubmitted
    ] : [],
    itemStyle: { color: '#409EFF' }
  }]
}))

const avgOption = computed(() => {
  const list = stats.value?.assignmentAvgScores || []
  return {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: list.map(a => a.title), axisLabel: { rotate: list.length > 4 ? 15 : 0 } },
    yAxis: { type: 'value', max: 100 },
    series: [{
      type: 'bar',
      data: list.map(a => a.avgScore == null ? 0 : Number(a.avgScore).toFixed(1)),
      itemStyle: { color: '#67C23A' }
    }]
  }
})

const trendOption = computed(() => {
  const list = stats.value?.monthlyTrend || []
  return {
    tooltip: { trigger: 'axis' },
    legend: { data: ['平均分'], top: 0 },
    xAxis: { type: 'category', data: list.map(m => m.month) },
    yAxis: { type: 'value', max: 100 },
    series: [{
      name: '平均分', type: 'line', smooth: true,
      data: list.map(m => m.avgScore == null ? 0 : Number(m.avgScore).toFixed(1))
    }]
  }
})

const load = async () => {
  aiReport.value = ''
  aiMeta.value = null
  if (!query.classId) { stats.value = null; return }
  try {
    // 不传 semester，让后端按当前日期推断学期窗口（与学生端策略一致）
    stats.value = await classStats({ classId: query.classId })
  } catch (e) {
    console.error(e)
    stats.value = null
  }
}

const generate = async () => {
  if (!query.classId) return ElMessage.warning('请先选择班级')
  aiLoading.value = true
  try {
    const res = await generateClassReport({
      classId: query.classId,
      semester: query.semester || undefined,
      question: ''
    })
    aiReport.value = (res.report || '（无内容）').replace(/\n/g, '<br/>')
    aiMeta.value = res
    ElMessage.success('报告已生成')
  } catch (e) {
    console.error(e)
    ElMessage.error(e?.message || 'AI 报告生成失败')
  } finally {
    aiLoading.value = false
  }
}

onMounted(async () => {
  try {
    classOptions.value = await classListAll()
  } catch (e) { console.error(e) }
  if (classOptions.value.length) {
    query.classId = classOptions.value[0].id
    load()
  }
})
</script>

<style lang="scss" scoped>
.filter-bar { margin-bottom: 16px; }
.filter-row { display: flex; align-items: center; flex-wrap: wrap; }
.kpi-row { margin-bottom: 16px; }
.chart-row { margin-bottom: 16px; }
.chart { height: 320px; }
.ai-card { margin-top: 8px; }
.report {
  font-size: 14px;
  line-height: 1.85;
  color: var(--el-text-color-primary);
  white-space: pre-wrap;
  word-break: break-word;
}
.meta {
  margin-top: 12px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.stat-card { position: relative; }
.stat-title { font-size: 14px; color: #999; }
.stat-value { font-size: 26px; font-weight: bold; margin-top: 8px; }
.stat-icon { position: absolute; right: 16px; top: 16px; font-size: 28px; }
</style>
