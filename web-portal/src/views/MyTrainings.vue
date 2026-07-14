<template>
  <div class="page">
    <header class="toolbar">
      <div class="filters">
        <button
          v-for="t in progressOptions"
          :key="t.value"
          class="tag"
          :class="{ 'tag--active': filters.progress === t.value }"
          @click="setProgress(t.value)"
        >{{ t.label }}</button>
      </div>
      <div class="toolbar__right">
        <el-input v-model="filters.q" placeholder="搜索实训计划" clearable class="search">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>
    </header>

    <div v-if="state === 'loading'" class="loading-tip">实训计划加载中…</div>
    <div v-else-if="state === 'empty'" class="empty">
      <el-icon :size="32"><Promotion /></el-icon>
      <p>还没有分配给你的实训计划</p>
    </div>
    <div v-else-if="state === 'error'" class="empty">
      <el-icon :size="32"><Warning /></el-icon>
      <p>实训数据暂时不可用，请稍后再试</p>
      <el-button class="empty__cta" size="small" plain @click="fetch">重新加载</el-button>
    </div>

    <div v-else class="train-grid">
      <article
        v-for="(t, idx) in list"
        :key="t.id"
        class="train"
        :style="{ transitionDelay: `${Math.min(idx, 8) * 50}ms` }"
      >
        <header class="train__head">
          <el-tag :type="progressType(t.progress)" effect="dark" size="small">{{ progressLabel(t.progress) }}</el-tag>
          <span class="train__plan">{{ t.planName || '—' }}</span>
        </header>
        <h3 class="train__title">{{ t.title || '未命名计划' }}</h3>
        <p class="train__stage">{{ t.stage || '—' }}</p>
        <div class="train__progress">
          <el-progress :percentage="clamp(t.progress)" :stroke-width="6" />
        </div>
        <footer class="train__foot">
          <span>起 {{ fmtDate(t.startAt) }}</span>
          <span>止 {{ fmtDate(t.endAt) }}</span>
          <span class="train__pct">{{ progressText(t) }}</span>
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
import { Search, Promotion, Warning } from '@element-plus/icons-vue'
import { myTrainings } from '@/api/my'

const PROGRESS_MAP = {
  not_started: { label: '未开始', type: 'info' },
  in_progress: { label: '进行中', type: 'warning' },
  done:        { label: '已完成', type: 'success' }
}
const progressLabel = (p) => PROGRESS_MAP[p]?.label || '未知'
const progressType  = (p) => PROGRESS_MAP[p]?.type  || 'info'

const progressOptions = [
  { value: '',          label: '全部' },
  { value: 'not_started', label: '未开始' },
  { value: 'in_progress', label: '进行中' },
  { value: 'done',        label: '已完成' }
]

const clamp = (p) => Math.max(0, Math.min(100, Number(p?.progress ?? p?.progressPercent ?? p) || 0))
const fmtDate = (iso) => {
  if (!iso) return '—'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}
const progressText = (t) => t.progressText || `${Math.round(clamp(t))}%`

const state = ref('loading')
const list = ref([])
const total = ref(0)
const page = reactive({ current: 1, size: 12 })
const filters = reactive({ q: '', progress: '' })

const setProgress = (v) => { filters.progress = v; page.current = 1; fetch() }

const fetch = async () => {
  state.value = 'loading'
  try {
    const res = await myTrainings({ current: page.current, size: page.size, q: filters.q, progress: filters.progress })
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

.train-grid {
  display: grid; grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: clamp(20px, 2.6vw, 36px);
}
.train {
  display: flex; flex-direction: column; gap: 12px;
  padding: 22px; background: var(--surface); border: 1px solid var(--line);
  transition: transform var(--t-base) var(--ease), border-color var(--t-base) var(--ease), box-shadow var(--t-base) var(--ease);
}
.train:hover { transform: translateY(-3px); border-color: var(--accent); box-shadow: var(--shadow-blue); }

.train__head { display: flex; align-items: center; justify-content: space-between; gap: 10px; }
.train__plan { font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.12em; text-transform: uppercase; color: var(--mute); }
.train__title { margin: 0; font-family: var(--font-display); font-size: 20px; font-weight: 600; color: var(--ink); letter-spacing: -0.012em; }
.train__stage { margin: 0; font-size: 13px; color: var(--ink-soft); line-height: 1.7; }
.train__progress { margin-top: 4px; }
.train__foot { display: flex; justify-content: space-between; gap: 12px; flex-wrap: wrap; font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.06em; color: var(--mute); padding-top: 12px; border-top: 1px solid var(--line-soft); }
.train__pct { color: var(--accent); font-weight: 600; }

.pager { display: flex; justify-content: center; margin-top: var(--s-9); }
.pager :deep(.el-pager li), .pager :deep(.btn-prev), .pager :deep(.btn-next) { background: var(--surface) !important; color: var(--ink) !important; font-family: var(--font-mono) !important; }
.pager :deep(.el-pager li.is-active) { background: var(--ink) !important; color: #fff !important; }

@media (max-width: 800px) { .train-grid { grid-template-columns: 1fr; } }
</style>
