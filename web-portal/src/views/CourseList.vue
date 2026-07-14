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
              <span class="course__code">{{ c.courseCode }}</span>
              <span class="course__level">{{ levelOf(c.level) }}</span>
              <div class="course__plate">
                <span class="course__plate-num">{{ String(idx + 1).padStart(2, '0') }}</span>
                <el-icon :size="28"><Reading /></el-icon>
              </div>
            </div>
            <div class="course__body">
              <div class="course__meta">
                <span class="course__tag">{{ tagOf(c.courseType) || c.categoryName || '课程' }}</span>
                <span class="course__hours">{{ c.totalHours || 48 }}h</span>
              </div>
              <h3 class="course__title">{{ c.courseName }}</h3>
              <p class="course__excerpt">{{ c.description || '本课程从基础原理到综合应用，循序渐进带您掌握核心知识。' }}</p>
              <div class="course__foot">
                <span class="course__teacher">
                  <span class="dot"></span>
                  {{ c.teacherName || '主讲教师' }}
                </span>
                <div class="course__actions">
                  <button
                    v-if="!userStore.isLogin && Number(c.courseType) === 2"
                    class="course__enroll"
                    type="button"
                    @click.stop="goEnroll(c)"
                  >登录后选课</button>
                  <span class="course__more">
                    查看详情
                    <el-icon :size="12"><ArrowRight /></el-icon>
                  </span>
                </div>
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
import { ElMessage } from 'element-plus'
import { Search, Notebook, Reading, ArrowRight } from '@element-plus/icons-vue'
import { listCourses } from '@/api/course'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)
const list = ref([])
const total = ref(0)
const page = reactive({ current: 1, size: 12 })

// TODO: [constants 模块] [P2] TAG_TYPE_MAP / TYPE_LABEL 与 MyCourses.vue 完全重复;
//       应提取为 web-portal/src/constants/course.js,导入后保证前后台、公开/学生两套页面同源.
// 后端 course_type: 1必修 2选修 3通识 —— 与字典 sys_dict_type.course_type 对应
const TAG_TYPE_MAP = { 1: 'required', 2: 'elective', 3: 'elective' }
const TYPE_LABEL = { required: '必修', elective: '选修', lab: '实验', training: '实训' }

const tagOptions = [
  { value: '', label: '全部' },
  { value: 'required', label: '必修' },
  { value: 'elective', label: '选修' }
]
const filters = reactive({ q: '', tag: '', level: '' })
const activeTagLabel = computed(() => tagOptions.find(t => t.value === filters.tag)?.label || '全部')

const setTag = (v) => { filters.tag = v; page.current = 1; fetch() }
// 将后端 courseType 渲染为中文角标
const tagOf = (courseType) => TYPE_LABEL[TAG_TYPE_MAP[courseType] || ''] || ''
// 将前端 tag 值映射到后端约定的过滤值
const tagForApi = (v) => (v === 'required' || v === 'elective') ? v : ''
const levelOf = (v) => ({ easy: '初级', medium: '中级', hard: '高级' }[v] || v || '入门')
const coverOf = (c) => {
  const palette = ['#F4F7FF', '#DEE6FF', '#A9C2FF', '#E8EDF7', '#ECF0FA']
  const i = (c.id || 0) % palette.length
  return { background: `linear-gradient(135deg, ${palette[i]} 0%, #A9C2FF 100%)` }
}
const goDetail = (c) => router.push(`/course/${c.id}`)
// 选课入口：目前选课界面尚未明确，先跳到详情页并以提示告知
const goEnroll = (c) => {
  ElMessage.info(`《${c.courseName}》为选修课程，请登录后前往选课界面`)
  router.push({ path: '/login', query: { redirect: `/course/${c.id}` } })
}

// 关键词命中打分：值越高相关性越强 —— 用于搜索结果重排
const scoreOf = (c, kw) => {
  if (!kw) return 0
  const needle = kw.toLowerCase()
  // 字段权重：名称 > 编号 > 教师 > 分类 > 简介
  const fields = [
    { text: String(c.courseName || ''), weight: 8 },
    { text: String(c.courseCode || ''), weight: 5 },
    { text: String(c.teacherName || ''), weight: 3 },
    { text: String(c.categoryName || ''), weight: 2 },
    { text: String(c.description || ''), weight: 1 }
  ]
  let score = 0
  for (const f of fields) {
    const t = f.text.toLowerCase()
    if (!t) continue
    // 命中 + 加成（命中长度越接近总长，加成越高，体现高"占比"）
    const idx = t.indexOf(needle)
    if (idx >= 0) {
      const occ = t.split(needle).length - 1
      const density = needle.length / Math.max(t.length, 1) // 关键词占比
      score += occ * f.weight + density * f.weight
    }
  }
  return score
}

const fetch = async () => {
  loading.value = true
  try {
    const res = await listCourses({
      current: page.current,
      size: page.size,
      q: filters.q,
      tag: tagForApi(filters.tag),
      level: filters.level
    })
    let records = res?.records || res?.list || []
    // 搜索时按关键词命中占比重排：占比高的排在前面
    if (filters.q && filters.q.trim()) {
      const kw = filters.q.trim()
      records = [...records]
        .map((c) => ({ c, s: scoreOf(c, kw) }))
        .filter((x) => x.s > 0)
        .sort((a, b) => b.s - a.s)
        .map((x) => x.c)
    }
    list.value = records
    total.value = Number(res?.total || records.length)
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
.course__actions { display: inline-flex; align-items: center; gap: 12px; }
.course__enroll {
  font-family: var(--font-mono); font-size: 10.5px; letter-spacing: 0.18em; text-transform: uppercase;
  padding: 6px 10px; background: var(--accent); color: #fff; border: 1px solid var(--accent);
  cursor: pointer; transition: background-color var(--t-fast) var(--ease), border-color var(--t-fast) var(--ease);
}
.course__enroll:hover { background: #2a4ee0; border-color: #2a4ee0; }

.pager { display: flex; justify-content: center; margin-top: var(--s-9); }
.pager :deep(.el-pager li), .pager :deep(.btn-prev), .pager :deep(.btn-next) { background: var(--surface) !important; color: var(--ink) !important; font-family: var(--font-mono) !important; }
.pager :deep(.el-pager li.is-active) { background: var(--ink) !important; color: #fff !important; }

@media (max-width: 1000px) { .course-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } .search { width: 200px; } }
@media (max-width: 640px)  { .course-grid { grid-template-columns: 1fr; } }
</style>
