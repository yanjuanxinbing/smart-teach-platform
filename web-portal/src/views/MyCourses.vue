<template>
  <div class="page">
    <header class="toolbar">
      <div class="filters">
        <button
          v-for="t in tagOptions"
          :key="t.value"
          class="tag"
          :class="{ 'tag--active': filters.tag === t.value }"
          @click="setTag(t.value)"
        >{{ t.label }}</button>
      </div>
      <div class="toolbar__right">
        <el-input v-model="filters.q" placeholder="搜索课程名 / 教师名" clearable class="search">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>
    </header>

    <div v-if="state === 'loading'" class="loading-tip">课程加载中…</div>
    <div v-else-if="state === 'empty'" class="empty">
      <el-icon :size="32"><Notebook /></el-icon>
      <p>你还没有选修任何课程</p>
      <router-link to="/course" class="empty__cta">去课程中心看看 →</router-link>
    </div>
    <div v-else-if="state === 'error'" class="empty">
      <el-icon :size="32"><Warning /></el-icon>
      <p>学习中心暂时不可用，请稍后再试</p>
      <el-button class="empty__cta" size="small" plain @click="fetch">重新加载</el-button>
    </div>

    <div v-else class="mycourse-grid">
      <article
        v-for="(c, idx) in list"
        :key="c.id"
        class="mycourse"
        :style="{ transitionDelay: `${Math.min(idx, 8) * 50}ms` }"
      >
        <div class="mycourse__cover" :style="coverOf(c)">
          <span class="mycourse__code">{{ c.courseCode }}</span>
          <div class="mycourse__plate">
            <span class="mycourse__plate-num">{{ String(idx + 1).padStart(2, '0') }}</span>
            <el-icon :size="28"><Reading /></el-icon>
          </div>
        </div>
        <div class="mycourse__body">
          <div class="mycourse__meta">
            <span class="mycourse__tag">{{ tagOf(c.courseType) || '课程' }}</span>
            <span class="mycourse__hours">{{ c.totalHours || 48 }}h</span>
          </div>
          <h3 class="mycourse__title">{{ c.courseName }}</h3>
          <p class="mycourse__teacher">主讲教师：{{ c.teacherName || '主讲教师' }}</p>
          <div class="mycourse__progress">
            <el-progress :percentage="clampProgress(c.progress)" :stroke-width="6" />
          </div>
        </div>
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
import { Search, Notebook, Reading, Warning } from '@element-plus/icons-vue'
import { myCourses } from '@/api/my'

const TAG_TYPE_MAP = { 1: 'required', 2: 'elective', 3: 'elective' }
const TYPE_LABEL = { required: '必修', elective: '选修' }

const tagOptions = [
  { value: '', label: '全部' },
  { value: 'required', label: '必修' },
  { value: 'elective', label: '选修' }
]

const loading = ref(true)
const state = ref('loading') // loading | empty | error | ok
const list = ref([])
const total = ref(0)
const page = reactive({ current: 1, size: 12 })
const filters = reactive({ q: '', tag: '' })

const setTag = (v) => { filters.tag = v; page.current = 1; fetch() }
const tagOf = (courseType) => TYPE_LABEL[TAG_TYPE_MAP[courseType] || ''] || ''
const clampProgress = (p) => Math.max(0, Math.min(100, Number(p) || 0))

const coverOf = (c) => {
  const palette = ['#F4F7FF', '#DEE6FF', '#A9C2FF', '#E8EDF7', '#ECF0FA']
  const i = (c.id || 0) % palette.length
  return { background: `linear-gradient(135deg, ${palette[i]} 0%, #A9C2FF 100%)` }
}

const fetch = async () => {
  state.value = 'loading'
  loading.value = true
  try {
    const res = await myCourses({ current: page.current, size: page.size, q: filters.q, tag: filters.tag })
    const records = res?.records || res?.list || []
    list.value = records
    total.value = Number(res?.total ?? records.length)
    state.value = records.length ? 'ok' : 'empty'
  } catch (e) {
    state.value = 'error'
    list.value = []
    total.value = 0
  } finally { loading.value = false }
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

.mycourse-grid {
  display: grid; grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: clamp(20px, 2.6vw, 36px);
}
.mycourse {
  display: flex; flex-direction: column; background: var(--surface);
  border: 1px solid var(--line); overflow: hidden;
  transition: transform var(--t-base) var(--ease), border-color var(--t-base) var(--ease), box-shadow var(--t-base) var(--ease);
}
.mycourse:hover { transform: translateY(-3px); border-color: var(--accent); box-shadow: var(--shadow-blue); }

.mycourse__cover {
  position: relative; aspect-ratio: 4 / 3;
  display: flex; align-items: center; justify-content: center;
}
.mycourse__code {
  position: absolute; top: 14px; left: 14px;
  font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.06em;
  padding: 5px 9px; background: rgba(255,255,255,0.94); color: var(--ink);
}
.mycourse__plate { color: rgba(255,255,255,0.92); display: flex; flex-direction: column; align-items: center; gap: 6px; }
.mycourse__plate-num { font-family: var(--font-display); font-size: 64px; font-weight: 600; color: #fff; letter-spacing: -0.04em; line-height: 1; opacity: 0.86; }

.mycourse__body { padding: 22px; display: flex; flex-direction: column; gap: 10px; flex: 1; }
.mycourse__meta { display: flex; justify-content: space-between; align-items: center; font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.08em; text-transform: uppercase; color: var(--mute); }
.mycourse__tag { color: var(--accent); }
.mycourse__title { font-family: var(--font-display); font-size: 21px; font-weight: 600; color: var(--ink); margin: 4px 0 6px; letter-spacing: -0.012em; }
.mycourse__teacher { margin: 0; font-size: 13px; color: var(--ink-soft); }
.mycourse__progress { margin-top: auto; padding-top: 12px; }

.pager { display: flex; justify-content: center; margin-top: var(--s-9); }
.pager :deep(.el-pager li), .pager :deep(.btn-prev), .pager :deep(.btn-next) { background: var(--surface) !important; color: var(--ink) !important; font-family: var(--font-mono) !important; }
.pager :deep(.el-pager li.is-active) { background: var(--ink) !important; color: #fff !important; }

@media (max-width: 1000px) { .mycourse-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } .search { width: 200px; } }
@media (max-width: 640px)  { .mycourse-grid { grid-template-columns: 1fr; } }
</style>
