<template>
  <div class="page agrade">
    <header class="agrade__head">
      <div class="head">
        <router-link to="/my/assignments" class="head__back">
          <el-icon :size="13"><Back /></el-icon>
          <span>返回我的作业</span>
        </router-link>
        <h1 class="head__title">{{ assignment?.title || '查看批改' }}</h1>
        <p v-if="assignment" class="head__meta">
          <span class="meta__item">课程：{{ assignment.courseName || '—' }}</span>
          <span class="meta__item">截止：{{ fmtDate(assignment.deadline) }}</span>
          <span class="meta__item">提交时间：{{ fmtDate(assignment.submittedAt) }}</span>
        </p>
      </div>
    </header>

    <section class="agrade__body">
      <div v-if="loading" class="loading-tip">批改信息加载中…</div>
      <div v-else-if="loadError" class="empty">
        <el-icon :size="32"><Warning /></el-icon>
        <p>批改信息暂时不可用</p>
        <el-button size="small" plain @click="load">重新加载</el-button>
      </div>
      <div v-else class="grid">
        <article class="scorecard">
          <div class="scorecard__col">
            <p class="scorecard__label">本次得分</p>
            <p class="scorecard__value">
              <span class="num">{{ grade?.score ?? assignment?.score ?? '—' }}</span>
              <span class="total">/ {{ assignment?.totalScore || 100 }}</span>
            </p>
          </div>
          <div class="scorecard__col">
            <p class="scorecard__label">批改人</p>
            <p class="scorecard__name">{{ grade?.graderName || '—' }}</p>
            <p class="scorecard__time">{{ fmtDate(grade?.gradeTime || assignment?.gradedAt) }}</p>
          </div>
        </article>

        <article class="card">
          <h2 class="card__title">教师评语</h2>
          <p class="card__body">{{ grade?.comment || '（暂无评语）' }}</p>
        </article>

        <article class="card">
          <h2 class="card__title">我提交的版本</h2>
          <pre class="card__body card__body--pre">{{ submission?.submitText || '（无文字内容）' }}</pre>
        </article>

        <article v-if="submission?.fileUrl" class="card">
          <h2 class="card__title">附件</h2>
          <div class="file">
            <el-icon :size="20"><Paperclip /></el-icon>
            <a class="file__name" :href="submission.fileUrl" target="_blank" rel="noopener">
              {{ submission.originalName || '下载附件' }}
            </a>
            <span v-if="submission.fileSize" class="file__size">{{ humanSize(submission.fileSize) }}</span>
            <span v-if="submission.fileSuffix" class="file__suffix">.{{ submission.fileSuffix }}</span>
          </div>
        </article>

        <div class="actions">
          <router-link to="/my/assignments">
            <el-button>返回作业列表</el-button>
          </router-link>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Back, Paperclip, Warning } from '@element-plus/icons-vue'
import { myAssignmentDetail, myAssignmentGrade, myLatestSubmission } from '@/api/my'
import { myAssignments } from '@/api/my'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const loadError = ref(false)
const assignment = ref(null)
const grade = ref(null)
const submission = ref(null)

const fmtDate = (iso) => {
  if (!iso) return '—'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}
const humanSize = (n) => {
  if (!n || n <= 0) return ''
  if (n < 1024) return `${n} B`
  if (n < 1024 * 1024) return `${(n / 1024).toFixed(1)} KB`
  return `${(n / 1024 / 1024).toFixed(2)} MB`
}

const load = async () => {
  loading.value = true
  loadError.value = false
  const id = String(route.params.id || '')
  try {
    const detail = await myAssignmentDetail(id)
    if (detail && detail.assignmentId) {
      assignment.value = detail
    } else {
      const res = await myAssignments({ current: 1, size: 200 })
      assignment.value = (res?.records || res?.list || []).find(a => String(a.assignmentId) === id) || null
    }
    // 并行拉批改详情 + 提交快照（即使后端 grade 接口未就绪也不影响提交展示）
    const [g, s] = await Promise.allSettled([
      myAssignmentGrade(id),
      myLatestSubmission(id)
    ])
    grade.value      = g.status === 'fulfilled' ? g.value : null
    submission.value = s.status === 'fulfilled' ? s.value : null
  } catch (e) {
    loadError.value = true
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (!userStore.isLogin) {
    router.replace({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  load()
})
</script>

<style scoped>
.agrade { padding-bottom: var(--s-9); }
.agrade__head { padding: var(--s-7) 0; border-bottom: 1px solid var(--line); background: linear-gradient(180deg, #fff 0%, #F4F7FF 100%); }
.head { display: flex; flex-direction: column; gap: 8px; }
.head__back { display: inline-flex; align-items: center; gap: 6px; font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.18em; text-transform: uppercase; color: var(--mute); text-decoration: none; align-self: flex-start; }
.head__back:hover { color: var(--accent); }
.head__title { margin: 0; font-family: var(--font-display); font-size: 28px; font-weight: 600; color: var(--ink); letter-spacing: -0.012em; }
.head__meta { margin: 0; display: flex; gap: 18px; flex-wrap: wrap; font-family: var(--font-mono); font-size: 12px; color: var(--ink-soft); }

.agrade__body { padding: var(--s-7) 0 0; }
.grid { display: flex; flex-direction: column; gap: clamp(16px, 2vw, 24px); max-width: 960px; }
.scorecard { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; padding: clamp(22px, 2.6vw, 30px); background: var(--ink); color: #fff; }
.scorecard__col { display: flex; flex-direction: column; gap: 6px; }
.scorecard__label { margin: 0; font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.18em; text-transform: uppercase; color: rgba(255,255,255,0.6); }
.scorecard__value { margin: 0; font-family: var(--font-display); display: flex; align-items: baseline; gap: 6px; }
.scorecard__value .num { font-size: 44px; font-weight: 600; color: var(--accent); }
.scorecard__value .total { font-size: 16px; color: rgba(255,255,255,0.7); }
.scorecard__name { margin: 0; font-family: var(--font-display); font-size: 18px; font-weight: 600; }
.scorecard__time { margin: 0; font-family: var(--font-mono); font-size: 12px; color: rgba(255,255,255,0.6); }

.card { background: var(--surface); border: 1px solid var(--line); padding: clamp(20px, 2.4vw, 28px); }
.card__title { margin: 0 0 12px; font-family: var(--font-display); font-size: 15px; font-weight: 600; color: var(--ink); }
.card__body { margin: 0; font-size: 14px; color: var(--ink-soft); line-height: 1.85; }
.card__body--pre { font-family: var(--font-mono); font-size: 13px; white-space: pre-wrap; word-break: break-word; background: var(--surface-soft); border: 1px solid var(--line-soft); padding: 14px 16px; }
.file { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; font-size: 13px; }
.file__name { color: var(--accent); text-decoration: none; }
.file__name:hover { text-decoration: underline; }
.file__size, .file__suffix { color: var(--mute); font-family: var(--font-mono); font-size: 11.5px; }

.actions { display: flex; gap: 12px; justify-content: flex-end; padding-top: 8px; border-top: 1px solid var(--line-soft); }
.actions a { text-decoration: none; }

.loading-tip { color: var(--mute); font-size: 13px; letter-spacing: 0.06em; padding: 48px 0; text-align: center; }
.empty { padding: 48px 0; text-align: center; color: var(--mute); display: flex; flex-direction: column; align-items: center; gap: 14px; }

@media (max-width: 700px) { .scorecard { grid-template-columns: 1fr; } }
</style>
