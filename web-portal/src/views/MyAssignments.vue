<template>
  <div class="page">
    <header class="toolbar">
      <div class="filters">
        <button
          v-for="t in statusOptions"
          :key="t.value"
          class="tag"
          :class="{ 'tag--active': filters.status === t.value }"
          @click="setStatus(t.value)"
        >{{ t.label }}</button>
      </div>
      <div class="toolbar__right">
        <el-input v-model="filters.q" placeholder="搜索作业标题 / 课程名" clearable class="search">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>
    </header>

    <div v-if="state === 'loading'" class="loading-tip">作业加载中…</div>
    <div v-else-if="state === 'empty'" class="empty">
      <el-icon :size="32"><Document /></el-icon>
      <p>暂无待办作业</p>
      <router-link to="/my/courses" class="empty__cta">去课程里看看吧 →</router-link>
    </div>
    <div v-else-if="state === 'error'" class="empty">
      <el-icon :size="32"><Warning /></el-icon>
      <p>作业数据暂时不可用，请稍后再试</p>
      <el-button class="empty__cta" size="small" plain @click="fetch">重新加载</el-button>
    </div>

    <ul v-else class="alist">
      <li v-for="(a, i) in list" :key="a.id" class="arow" :class="{ 'arow--graded': a.status === 'graded' }">
        <span class="arow__idx">{{ String(i + 1).padStart(2, '0') }}</span>
        <div class="arow__main">
          <div class="arow__top">
            <span class="arow__course">{{ a.courseName || '—' }}</span>
            <el-tag :type="statusType(a.status)" effect="dark" size="small">{{ statusLabel(a.status) }}</el-tag>
          </div>
          <h3 class="arow__title">{{ a.title || '未命名作业' }}</h3>
          <div class="arow__meta">
            <span class="arow__due">截止：{{ fmtDate(a.dueAt) }}</span>
            <span v-if="a.status === 'graded'" class="arow__score">得分：{{ a.score ?? '--' }}</span>
            <span v-else-if="a.submittedAt" class="arow__score">提交于：{{ fmtDate(a.submittedAt) }}</span>
          </div>
        </div>
        <el-button v-if="a.status === 'pending'" type="primary" plain size="small">去提交</el-button>
        <el-button v-else-if="a.status === 'submitted'" plain size="small">查看提交</el-button>
        <el-button v-else plain size="small">查看批改</el-button>
      </li>
    </ul>

    <div v-if="state === 'ok' && list.length" class="pager">
      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        :total="total"
        layout="prev, pager, next, jumper"
        background
        @current-change="fetch"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { Search, Document, Warning } from '@element-plus/icons-vue'
import { myAssignments } from '@/api/my'

const STATUS_MAP = {
  pending:   { label: '待提交', type: 'danger' },
  submitted: { label: '已提交', type: 'info' },
  graded:    { label: '已批改', type: 'success' }
}
const statusLabel = (s) => STATUS_MAP[s]?.label || '未知'
const statusType  = (s) => STATUS_MAP[s]?.type  || 'info'
const fmtDate = (iso) => {
  if (!iso) return '—'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

const statusOptions = [
  { value: '',         label: '全部' },
  { value: 'pending',  label: '待提交' },
  { value: 'submitted',label: '已提交' },
  { value: 'graded',   label: '已批改' }
]

const state = ref('loading') // loading | empty | error | ok
const list = ref([])
const total = ref(0)
const page = reactive({ current: 1, size: 12 })
const filters = reactive({ q: '', status: '' })

const setStatus = (v) => { filters.status = v; page.current = 1; fetch() }

const fetch = async () => {
  state.value = 'loading'
  try {
    const res = await myAssignments({ current: page.current, size: page.size, q: filters.q, status: filters.status })
    const records = res?.records || res?.list || []
    list.value = records
    total.value = Number(res?.total ?? records.length)
    state.value = records.length ? 'ok' : 'empty'
  } catch (e) {
    state.value = 'error'
    list.value = []; total.value = 0
  }
}

let timer
watch(() => filters.q, () => {
  clearTimeout(timer); timer = setTimeout(() => { page.current = 1; fetch() }, 300)
})

onMounted(fetch)
</script>

<style scoped>
.toolbar {
  display: flex; align-items: center; justify-content: space-between;
  gap: var(--s-5); flex-wrap: wrap;
  padding-bottom: var(--s-4); margin-bottom: var(--s-6);
  border-bottom: 1px solid var(--line);
}
.filters { display: flex; gap: 6px; flex-wrap: wrap; }
.tag {
  font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.16em; text-transform: uppercase;
  padding: 9px 14px; background: var(--surface); border: 1px solid var(--line); color: var(--ink-soft);
  cursor: pointer; transition: border-color var(--t-fast) var(--ease), color var(--t-fast) var(--ease), background-color var(--t-fast) var(--ease);
}
.tag:hover { color: var(--ink); border-color: var(--ink); }
.tag--active { background: var(--ink); color: #fff; border-color: var(--ink); }
.toolbar__right { display: flex; gap: var(--s-3); }
.search { width: 240px; }
.search :deep(.el-input__wrapper) { border-radius: 0; }

.loading-tip { color: var(--mute); font-size: 13px; letter-spacing: 0.06em; text-align: center; padding: 64px 0; }
.empty { padding: 88px 0; text-align: center; color: var(--mute); display: flex; flex-direction: column; align-items: center; gap: 14px; }
.empty__cta { color: var(--accent); text-decoration: none; font-size: 13px; }
.empty__cta:hover { text-decoration: underline; }

.alist { list-style: none; margin: 0; padding: 0; border-top: 1px solid var(--line); }
.arow {
  display: grid; grid-template-columns: 48px 1fr auto;
  align-items: center; gap: var(--s-4);
  padding: 18px 4px; border-bottom: 1px solid var(--line-soft);
  transition: background-color var(--t-fast) var(--ease);
}
.arow:hover { background: var(--surface-soft); }
.arow--graded .arow__title { color: var(--ink-soft); }
.arow__idx { font-family: var(--font-mono); font-size: 12px; color: var(--mute); }
.arow__main { display: flex; flex-direction: column; gap: 6px; min-width: 0; }
.arow__top { display: flex; align-items: center; gap: 10px; }
.arow__course { font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.12em; text-transform: uppercase; color: var(--mute); }
.arow__title { margin: 0; font-family: var(--font-display); font-size: 16px; font-weight: 600; color: var(--ink); letter-spacing: -0.01em; }
.arow__meta { display: flex; gap: 18px; flex-wrap: wrap; font-family: var(--font-mono); font-size: 12px; color: var(--ink-soft); }
.arow__due, .arow__score { letter-spacing: 0.04em; }

.pager { display: flex; justify-content: center; margin-top: var(--s-9); }
.pager :deep(.el-pager li), .pager :deep(.btn-prev), .pager :deep(.btn-next) { background: var(--surface) !important; color: var(--ink) !important; font-family: var(--font-mono) !important; }
.pager :deep(.el-pager li.is-active) { background: var(--ink) !important; color: #fff !important; }
</style>
