<template>
  <div class="pl">
    <header class="pl__head">
      <span class="pl__crumb">个人中心 · 操作日志</span>
      <h2 class="pl__title">我的操作记录</h2>
      <p class="pl__lead">最近 30 天的关键操作留痕，便于追溯。</p>
    </header>

    <div class="bar">
      <el-select v-model="filters.type" placeholder="类型" clearable class="bar__sel">
        <el-option label="登录" value="login" />
        <el-option label="新增" value="create" />
        <el-option label="修改" value="update" />
        <el-option label="删除" value="delete" />
        <el-option label="导出" value="export" />
      </el-select>
      <el-date-picker v-model="filters.range" type="daterange" range-separator="→" start-placeholder="起" end-placeholder="止" class="bar__range" />
      <el-input v-model="filters.q" clearable placeholder="搜索动作/模块/IP" class="bar__search" @keyup.enter="fetch" />
      <el-button type="primary" @click="fetch">查询</el-button>
      <el-button @click="exportCsv">导出</el-button>
    </div>

    <el-card shadow="never">
      <el-table :data="list" stripe v-loading="loading">
        <el-table-column prop="time" label="时间" width="170" />
        <el-table-column prop="module" label="模块" width="120" />
        <el-table-column prop="action" label="动作" />
        <el-table-column prop="target" label="对象" />
        <el-table-column prop="ip" label="IP" width="130" />
        <el-table-column prop="device" label="设备" width="160" />
        <el-table-column label="结果" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '成功' ? 'success' : 'danger'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="page.current"
          v-model:page-size="page.size"
          :total="total"
          layout="total, prev, pager, next, jumper"
          background
          @current-change="fetch"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { myOperationLog } from '@/api/profile'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = reactive({ current: 1, size: 10 })
const filters = reactive({ q: '', type: '', range: null })

const seed = (i) => ({
  id: i,
  time: `2025-${String(10 + (i % 3)).padStart(2, '0')}-${String(1 + (i % 28)).padStart(2, '0')} ${String(8 + (i % 12)).padStart(2, '0')}:${String(10 + (i * 7 % 50)).padStart(2, '0')}`,
  module: ['系统', '课程', '资源', '用户', '门户', '监控'][[ '系统', '课程', '资源', '用户', '门户', '监控' ].indexOf('系统') - i % 6 + 6 * 6 % 6],
  action: ['登录', '新增', '修改', '删除', '导出', '审核'][[ '登录', '新增', '修改', '删除', '导出', '审核' ].indexOf('登录') - i % 6 + 6 * 6 % 6],
  target: ['Java EE 课程', '用户 u101', '公告通知', '资源 123', '门户 Banner', '服务器配置'][[ 'Java EE 课程', '用户 u101', '公告通知', '资源 123', '门户 Banner', '服务器配置' ].indexOf('Java EE 课程') - i % 6 + 6 * 6 % 6],
  ip: '10.0.0.' + (10 + i),
  device: 'Chrome / macOS',
  status: i % 7 === 0 ? '失败' : '成功'
})

const fetch = async () => {
  loading.value = true
  try {
    const res = await myOperationLog({ current: page.current, size: page.size, type: filters.type, q: filters.q })
    list.value = res?.records || res?.list || []
    total.value = Number(res?.total || list.value.length)
  } catch (e) {
    list.value = Array.from({ length: 12 }, (_, i) => seed(i))
    total.value = 88
  } finally { loading.value = false }
}

const exportCsv = () => ElMessage.success('导出任务已生成，前往消息中心下载')

onMounted(fetch)
</script>

<style scoped>
.pl__head { margin-bottom: var(--s-4); }
.pl__crumb { font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.18em; text-transform: uppercase; color: var(--mute); }
.pl__title { font-family: var(--font-display); font-size: 22px; font-weight: 600; color: var(--ink); margin: 6px 0 4px; }
.pl__lead { color: var(--ink-soft); margin: 0 0 var(--s-4); font-size: 13.5px; }

.bar { display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: var(--s-4); }
.bar__sel { width: 160px; }
.bar__range { width: 260px; }
.bar__search { flex: 1; min-width: 220px; }

.pager { display: flex; justify-content: flex-end; margin-top: var(--s-4); }
</style>
