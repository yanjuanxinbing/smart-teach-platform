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
        <el-input v-model="filters.q" placeholder="搜索实验 / 课程名" clearable class="search">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>
    </header>

    <div v-if="state === 'loading'" class="loading-tip">实验列表加载中…</div>
    <div v-else-if="state === 'empty'" class="empty">
      <el-icon :size="32"><DataAnalysis /></el-icon>
      <p>还没有分配给你的实验</p>
      <p class="empty__hint">请联系任课教师，确认实验计划分配</p>
    </div>
    <div v-else-if="state === 'error'" class="empty">
      <el-icon :size="32"><Warning /></el-icon>
      <p>实验数据暂时不可用，请稍后再试</p>
      <el-button class="empty__cta" size="small" plain @click="fetch">重新加载</el-button>
    </div>

    <div v-else class="exp-grid">
      <article
        v-for="(e, idx) in list"
        :key="e.experimentId || e.id"
        class="exp"
        :style="{ transitionDelay: `${Math.min(idx, 8) * 50}ms` }"
        role="button"
        tabindex="0"
        @click="goDetail(e)"
        @keyup.enter="goDetail(e)"
      >
        <header class="exp__head">
          <el-tag :type="statusType(e.status)" effect="dark" size="small">{{ statusLabel(e.status) }}</el-tag>
          <span class="exp__no">实验 #{{ e.experimentId || e.id }}</span>
        </header>
        <h3 class="exp__title">{{ e.experimentName || e.title || '未命名实验' }}</h3>
        <p class="exp__course">
          <el-icon><Notebook /></el-icon>
          <span>{{ e.courseName || '—' }}</span>
        </p>
        <p class="exp__meta">
          <span v-if="e.teacherName">指导：{{ e.teacherName }}</span>
          <span v-if="e.labRoom">· {{ e.labRoom }}</span>
        </p>
        <footer class="exp__foot">
          <span>起 {{ fmtDate(e.startDate) }}</span>
          <span>止 {{ fmtDate(e.endDate) }}</span>
          <el-button class="exp__cta" size="small" text @click.stop="goDetail(e)">
            {{ e.status === 'not_started' ? '查看详情' : '进入实验' }} →
          </el-button>
        </footer>
      </article>
    </div>

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
import { useRouter } from 'vue-router'
import { Search, DataAnalysis, Notebook, Warning } from '@element-plus/icons-vue'
import { myExperiments } from '@/api/experiment'

const router = useRouter()

const STATUS_MAP = {
  not_started: { label: '未开始', type: 'info' },
  in_progress: { label: '进行中', type: 'warning' },
  done:        { label: '已结束', type: 'success' }
}
const statusLabel = (s) => STATUS_MAP[s]?.label || '未知'
const statusType  = (s) => STATUS_MAP[s]?.type  || 'info'

const statusOptions = [
  { value: '',            label: '全部' },
  { value: 'not_started', label: '未开始' },
  { value: 'in_progress', label: '进行中' },
  { value: 'done',        label: '已结束' }
]

const fmtDate = (iso) => {
  if (!iso) return '—'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

const state = ref('loading') // loading | empty | error | ok
const list = ref([])
const total = ref(0)
const page = reactive({ current: 1, size: 12 })
const filters = reactive({ q: '', status: '' })

const setStatus = (v) => { filters.status = v; page.current = 1; fetch() }

// 跳详情页：路由 /my/experiments/:id，与 MyLearningLayout 共享 STUDENT 守卫
const goDetail = (e) => {
  const id = e?.experimentId || e?.id
  if (!id) return
  router.push(`/my/experiments/${id}`)
}

const fetch = async () => {
  state.value = 'loading'
  try {
    const res = await myExperiments({ current: page.current, size: page.size, q: filters.q, status: filters.status })
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
.empty__hint { margin: 0; font-size: 12px; color: var(--mute); }

.exp-grid {
  display: grid; grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: clamp(20px, 2.6vw, 36px);
}
.exp {
  display: flex; flex-direction: column; gap: 12px;
  padding: 22px; background: var(--surface); border: 1px solid var(--line);
  cursor: pointer;
  transition: transform var(--t-base) var(--ease), border-color var(--t-base) var(--ease), box-shadow var(--t-base) var(--ease);
}
.exp:hover { transform: translateY(-3px); border-color: var(--accent); box-shadow: var(--shadow-blue); }

.exp__head { display: flex; align-items: center; justify-content: space-between; gap: 10px; }
.exp__no { font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.12em; text-transform: uppercase; color: var(--mute); }
.exp__title { margin: 0; font-family: var(--font-display); font-size: 20px; font-weight: 600; color: var(--ink); letter-spacing: -0.012em; }
.exp__course { margin: 0; font-size: 13px; color: var(--ink-soft); display: flex; align-items: center; gap: 6px; }
.exp__meta { margin: 0; font-size: 12px; color: var(--mute); display: flex; gap: 6px; flex-wrap: wrap; }
.exp__foot { display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap; font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.06em; color: var(--mute); padding-top: 12px; border-top: 1px solid var(--line-soft); margin-top: auto; }
.exp__cta { color: var(--accent); font-weight: 600; }

.pager { display: flex; justify-content: center; margin-top: var(--s-9); }
.pager :deep(.el-pager li), .pager :deep(.btn-prev), .pager :deep(.btn-next) { background: var(--surface) !important; color: var(--ink) !important; font-family: var(--font-mono) !important; }
.pager :deep(.el-pager li.is-active) { background: var(--ink) !important; color: #fff !important; }

@media (max-width: 800px) { .exp-grid { grid-template-columns: 1fr; } }
</style>
