<template>
  <div class="page">
    <header class="page__header">
      <div class="container">
        <div class="rubric page__rubric">
          <span>Section · 02 / Courses</span>
          <span class="rubric__dot"></span>
          <span>{{ total }} courses</span>
          <span class="rubric__dot"></span>
          <span>{{ activeTagLabel }}</span>
        </div>
        <h1 class="page__title">课程<em>中心</em></h1>
        <p class="page__lead">
          按课程性质、开课学院、难度等级筛选——教务课程库。
        </p>
      </div>
    </header>

    <section class="page__body">
      <div class="container">

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
            <el-select v-model="filters.level" placeholder="难度" clearable class="level">
              <el-option label="初级" value="easy" />
              <el-option label="中级" value="medium" />
              <el-option label="高级" value="hard" />
            </el-select>
          </div>
        </header>

        <div v-if="loading" class="loading-tip">课程加载中…</div>
        <div v-else-if="!list.length" class="empty"><el-icon :size="32"><Notebook /></el-icon><p>暂无符合条件的课程</p></div>

        <div v-else class="course-grid">
          <article
            v-for="(c, idx) in list"
            :key="c.id"
            class="course"
            data-reveal
            :style="{ transitionDelay: `${Math.min(idx, 8) * 50}ms` }"
            @click="goDetail(c)"
          >
            <div class="course__cover" :style="coverOf(c)">
              <span class="course__code">{{ c.code }}</span>
              <span class="course__level">{{ levelOf(c.level) }}</span>
              <div class="course__plate">
                <span class="course__plate-num">{{ String(idx + 1).padStart(2, '0') }}</span>
                <el-icon :size="28"><Reading /></el-icon>
              </div>
            </div>
            <div class="course__body">
              <div class="course__meta">
                <span class="course__tag">{{ tagOf(c.category) }}</span>
                <span class="course__hours">{{ c.hours || 48 }}h</span>
              </div>
              <h3 class="course__title">{{ c.title || c.name }}</h3>
              <p class="course__excerpt">{{ c.summary || c.description || '本课程从基础原理到综合应用，循序渐进带您掌握核心知识。' }}</p>
              <div class="course__foot">
                <span class="course__teacher">
                  <span class="dot"></span>
                  {{ c.teacherName || '主讲教师' }}
                </span>
                <span class="course__more">
                  查看详情
                  <el-icon :size="12"><ArrowRight /></el-icon>
                </span>
              </div>
            </div>
          </article>
        </div>

        <div class="pager">
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
    </section>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Notebook, Reading, ArrowRight } from '@element-plus/icons-vue'
import { listCourses } from '@/api/course'

const router = useRouter()
const loading = ref(true)
const list = ref([])
const total = ref(0)
const page = reactive({ current: 1, size: 12 })

const tagOptions = [
  { value: '', label: '全部' },
  { value: 'required', label: '必修' },
  { value: 'elective', label: '选修' },
  { value: 'lab', label: '实验' },
  { value: 'training', label: '实训' }
]
const filters = reactive({ q: '', tag: '', level: '' })
const activeTagLabel = computed(() => tagOptions.find(t => t.value === filters.tag)?.label || '全部')

const setTag = (v) => { filters.tag = v; page.current = 1; fetch() }
const tagOf = (v) => tagOptions.find(t => t.value === v)?.label || '课程'
const levelOf = (v) => ({ easy: '初级', medium: '中级', hard: '高级' }[v] || v || '入门')
const coverOf = (c) => {
  const palette = ['#F4F7FF', '#DEE6FF', '#A9C2FF', '#E8EDF7', '#ECF0FA']
  const i = (c.id || 0) % palette.length
  return { background: `linear-gradient(135deg, ${palette[i]} 0%, #A9C2FF 100%)` }
}
const goDetail = (c) => router.push(`/course/${c.id}`)

const fetch = async () => {
  loading.value = true
  try {
    const res = await listCourses({ current: page.current, size: page.size, q: filters.q, tag: filters.tag, level: filters.level })
    list.value = res?.records || res?.list || []
    total.value = Number(res?.total || list.value.length)
  } catch (e) {
    list.value = []; total.value = 0
  } finally { loading.value = false }
}

let timer
watch(() => filters.q, () => {
  clearTimeout(timer); timer = setTimeout(() => { page.current = 1; fetch() }, 300)
})
watch(() => filters.level, () => { page.current = 1; fetch() })

onMounted(fetch)
</script>

<style scoped>
.page__header {
  padding: clamp(64px, 9vw, 112px) 0 clamp(32px, 5vw, 64px);
  border-bottom: 1px solid var(--line);
  background: linear-gradient(180deg, #FFFFFF 0%, #F4F7FF 100%);
}
.page__rubric { margin-bottom: var(--s-6); }
.rubric__dot { display: inline-block; width: 4px; height: 4px; background: var(--mute-soft); border-radius: 50%; margin: 0 10px; vertical-align: middle; }
.page__title { font-family: var(--font-display); font-size: clamp(40px, 6vw, 72px); font-weight: 600; line-height: 1.04; letter-spacing: -0.025em; margin: 0 0 var(--s-4); }
.page__title em { font-family: var(--font-serif); font-style: italic; font-weight: 500; color: var(--accent); }
.page__lead { max-width: 56ch; font-size: 15.5px; line-height: 1.75; color: var(--ink-soft); margin: 0; }
.page__body { padding: clamp(48px, 7vw, 88px) 0; }

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
.search :deep(.el-input__wrapper), .level :deep(.el-input__wrapper), .level :deep(.el-select__wrapper) { border-radius: 0; }

.loading-tip { color: var(--mute); font-size: 13px; letter-spacing: 0.06em; text-align: center; padding: 64px 0; }
.empty { padding: 88px 0; text-align: center; color: var(--mute); display: flex; flex-direction: column; align-items: center; gap: 14px; }

.course-grid {
  display: grid; grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: clamp(20px, 2.6vw, 36px);
}
.course {
  display: flex; flex-direction: column; background: var(--surface);
  border: 1px solid var(--line); cursor: pointer; overflow: hidden;
  transition: transform var(--t-base) var(--ease), border-color var(--t-base) var(--ease), box-shadow var(--t-base) var(--ease);
}
.course:hover { transform: translateY(-3px); border-color: var(--accent); box-shadow: var(--shadow-blue); }

.course__cover {
  position: relative; aspect-ratio: 4 / 3;
  display: flex; align-items: center; justify-content: center;
  overflow: hidden;
}
.course__code {
  position: absolute; top: 14px; left: 14px;
  font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.06em;
  padding: 5px 9px; background: rgba(255,255,255,0.94); color: var(--ink);
}
.course__level {
  position: absolute; top: 14px; right: 14px;
  font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.08em;
  padding: 5px 9px; background: var(--accent); color: #fff;
}
.course__plate { color: rgba(255,255,255,0.92); display: flex; flex-direction: column; align-items: center; gap: 6px; }
.course__plate-num { font-family: var(--font-display); font-size: 84px; font-weight: 600; color: #fff; letter-spacing: -0.04em; line-height: 1; opacity: 0.86; }

.course__body { padding: 22px; display: flex; flex-direction: column; gap: 10px; flex: 1; }
.course__meta { display: flex; justify-content: space-between; align-items: center; font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.08em; text-transform: uppercase; color: var(--mute); }
.course__tag { color: var(--accent); }
.course__title { font-family: var(--font-display); font-size: 21px; font-weight: 600; color: var(--ink); line-height: 1.35; margin: 4px 0 6px; letter-spacing: -0.012em; transition: color var(--t-fast) var(--ease); }
.course:hover .course__title { color: var(--accent); }
.course__excerpt {
  margin: 0; font-size: 13.5px; line-height: 1.7; color: var(--ink-soft);
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.course__foot { display: flex; align-items: center; justify-content: space-between; margin-top: auto; padding-top: 14px; border-top: 1px solid var(--line-soft); }
.course__teacher { display: inline-flex; align-items: center; gap: 6px; font-size: 12px; color: var(--ink-soft); }
.course__teacher .dot { width: 5px; height: 5px; background: var(--accent); border-radius: 50%; }
.course__more { display: inline-flex; align-items: center; gap: 6px; font-size: 11px; letter-spacing: 0.18em; text-transform: uppercase; color: var(--accent); transition: gap var(--t-base) var(--ease); }
.course:hover .course__more { gap: 12px; }

.pager { display: flex; justify-content: center; margin-top: var(--s-9); }
.pager :deep(.el-pager li), .pager :deep(.btn-prev), .pager :deep(.btn-next) { background: var(--surface) !important; color: var(--ink) !important; font-family: var(--font-mono) !important; }
.pager :deep(.el-pager li.is-active) { background: var(--ink) !important; color: #fff !important; }

@media (max-width: 1000px) { .course-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } .search { width: 200px; } }
@media (max-width: 640px)  { .course-grid { grid-template-columns: 1fr; } }
</style>
