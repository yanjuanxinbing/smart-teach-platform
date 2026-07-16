<template>
  <div class="app-container">
    <el-card class="header-card">
      <div class="header-row">
        <div>
          <div class="title">本学期成绩分析</div>
          <div class="sub">
            <el-icon style="vertical-align: -2px"><User /></el-icon>
            <span v-if="stats">{{ stats.studentName }}（{{ stats.className || '未分班' }}）</span>
            <span v-else>加载中…</span>
            <el-tag v-if="stats && stats.semester" type="info" style="margin-left: 12px">
              学期：{{ stats.semester }}
            </el-tag>
          </div>
        </div>
        <el-button type="primary" :loading="aiLoading" @click="generate">
          一键生成 AI 反思
        </el-button>
      </div>
    </el-card>

    <el-row :gutter="16" class="kpi-row" v-if="stats">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-title">作业总数</div>
          <div class="stat-value">{{ stats.assignmentCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-title">已批改</div>
          <div class="stat-value">{{ stats.gradedCount }} / {{ stats.assignmentCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-title">平均分</div>
          <div class="stat-value">{{ formatNum(stats.avgScore) }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-title">最高 / 最低</div>
          <div class="stat-value">{{ formatNum(stats.maxScore) }} / {{ formatNum(stats.minScore) }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="kpi-row" v-if="stats">
      <el-col :span="24">
        <el-card>
          <template #header><span>分数随时间序列（按作业 deadline 排序）</span></template>
          <v-chart class="chart" :option="trendOption" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" v-if="stats">
      <el-col :span="12">
        <el-card>
          <template #header><span>未完成 / 草稿（{{ stats.unsubmittedCount }} 条）</span></template>
          <el-table :data="stats.unsubmittedList || []" size="small" empty-text="全部完成 🎉">
            <el-table-column prop="title" label="作业" />
            <el-table-column label="截止" width="120">
              <template #default="{ row }">
                <span>{{ row.deadline ? row.deadline.substring(0, 10) : '?' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'warning' : 'info'" size="small">
                  {{ row.status === 0 ? '草稿' : '未提交' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>分数序列明细</span></template>
          <el-table :data="stats.scoreTrend || []" size="small" empty-text="暂无已批改作业">
            <el-table-column prop="title" label="作业" />
            <el-table-column label="时间" width="120">
              <template #default="{ row }">
                <span>{{ row.deadline ? row.deadline.substring(0, 10) : '?' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="分数" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.score >= 85 ? 'success' : (row.score >= 60 ? 'primary' : 'danger')" size="small">
                  {{ formatNum(row.score) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="ai-card" v-if="stats">
      <template #header>
        <span>AI 学习反思（基于本学期数据）</span>
        <el-button
          v-if="!aiReport"
          type="primary"
          :loading="aiLoading"
          style="float: right"
          @click="generate"
        >一键生成</el-button>
      </template>
      <div v-if="aiReport" class="report" v-html="aiReport"></div>
      <el-empty v-else description="点「一键生成」让 AI 教练用第二人称给 3-5 段反思（10-30s）" />
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, MarkPointComponent, MarkLineComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { User } from '@element-plus/icons-vue'
import { myStats, generateStudentReport } from '@/api/analytics'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, LegendComponent, MarkPointComponent, MarkLineComponent])

const stats = ref(null)
const aiReport = ref('')
const aiMeta = ref(null)
const aiLoading = ref(false)

function formatNum(v) {
  if (v == null) return '-'
  return Number(v).toFixed(1)
}

const trendOption = computed(() => {
  const list = stats.value?.scoreTrend || []
  return {
    tooltip: { trigger: 'axis' },
    legend: { data: ['分数'], top: 0 },
    xAxis: { type: 'category', data: list.map(p => p.deadline ? p.deadline.substring(0, 10) : '?'), axisLabel: { rotate: 30 } },
    yAxis: { type: 'value', max: 100, min: 0 },
    series: [{
      name: '分数', type: 'line', smooth: true,
      data: list.map(p => p.score == null ? null : Number(p.score).toFixed(1)),
      markLine: { data: [{ type: 'average', name: '均值' }] }
    }]
  }
})

const load = async () => {
  try {
    stats.value = await myStats()
  } catch (e) {
    console.error(e)
    stats.value = null
  }
}

const generate = async () => {
  aiLoading.value = true
  try {
    const res = await generateStudentReport({ classId: stats.value?.studentId || 0, question: '' })
    aiReport.value = (res.report || '（无内容）').replace(/\n/g, '<br/>')
    aiMeta.value = res
    ElMessage.success('反思已生成')
  } catch (e) {
    console.error(e)
    ElMessage.error(e?.message || '生成失败')
  } finally {
    aiLoading.value = false
  }
}

onMounted(load)
</script>

<style lang="scss" scoped>
.header-card { margin-bottom: 16px; }
.header-row { display: flex; align-items: center; justify-content: space-between; }
.title { font-size: 20px; font-weight: 600; }
.sub { margin-top: 6px; color: var(--el-text-color-secondary); font-size: 13px; }
.kpi-row { margin-bottom: 16px; }
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
</style>
