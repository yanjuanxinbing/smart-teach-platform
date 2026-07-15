<template>
  <div class="page asview">
    <header class="asview__head">
      <div class="head">
        <router-link to="/my/assignments" class="head__back">
          <el-icon :size="13"><Back /></el-icon>
          <span>返回我的作业</span>
        </router-link>
        <h1 class="head__title">{{ assignment?.title || '查看提交' }}</h1>
        <p v-if="assignment" class="head__meta">
          <span class="meta__item">课程：{{ assignment.courseName || '—' }}</span>
          <span class="meta__item">截止：{{ fmtDate(assignment.deadline) }}</span>
          <span class="meta__item">状态：
            <el-tag :type="statusType(assignment.status)" size="small" effect="plain">{{ statusLabel(assignment.status) }}</el-tag>
          </span>
        </p>
      </div>
    </header>

    <section class="asview__body">
      <div v-if="loading" class="loading-tip">提交记录加载中…</div>
      <div v-else-if="loadError" class="empty">
        <el-icon :size="32"><Warning /></el-icon>
        <p>提交记录暂时不可用</p>
        <el-button size="small" plain @click="load">重新加载</el-button>
      </div>
      <div v-else-if="!submission" class="empty">
        <el-icon :size="32"><DocumentRemove /></el-icon>
        <p>尚未提交（status: {{ assignment?.status || '—' }}）</p>
        <router-link :to="`/my/assignment/${assignment?.assignmentId || route.params.id}/submit`" class="empty__cta">去提交 →</router-link>
      </div>
      <div v-else class="grid">
        <article class="card">
          <h2 class="card__title">提交正文</h2>
          <pre class="card__body card__body--pre">{{ submission.submitText || '（无文字内容）' }}</pre>
        </article>

        <article class="card">
          <h2 class="card__title">附件</h2>
          <div v-if="submission.fileUrl" class="file">
            <el-icon :size="20"><Paperclip /></el-icon>
            <a class="file__name" :href="submission.fileUrl" target="_blank" rel="noopener">
              {{ submission.originalName || '下载附件' }}
            </a>
            <span v-if="submission.fileSize" class="file__size">{{ humanSize(submission.fileSize) }}</span>
            <span v-if="submission.fileSuffix" class="file__suffix">.{{ submission.fileSuffix }}</span>
          </div>
          <p v-else class="card__body card__body--mute">未上传附件</p>
        </article>

        <article class="card card--meta">
          <h2 class="card__title">提交元信息</h2>
          <dl class="kv">
            <div class="kv__row"><dt>提交时间</dt><dd>{{ fmtDate(submission.submitTime) }}</dd></div>
            <div class="kv__row"><dt>是否逾期</dt><dd>{{ submission.isLate ? '是' : '否' }}</dd></div>
            <div class="kv__row"><dt>状态</dt><dd>{{ statusLabel(assignment?.status) }}</dd></div>
            <div class="kv__row"><dt>附件名</dt><dd>{{ submission.originalName || '—' }}</dd></div>
          </dl>
        </article>

        <div class="actions">
          <el-button v-if="canResubmit" type="primary" plain @click="goResubmit">重新提交</el-button>
          <el-button @click="router.back()">返回</el-button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back, Paperclip, DocumentRemove, Warning } from '@element-plus/icons-vue'
import { myAssignmentDetail, myLatestSubmission } from '@/api/my'
import { myAssignments } from '@/api/my'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const STATUS_MAP = {
  pending:   { label: '待提交', type: 'danger' },
  submitted: { label: '已提交', type: 'info' },
  graded:    { label: '已批改', type: 'success' }
}
const statusLabel = (s) => STATUS_MAP[s]?.label || '未知'
const statusType  = (s) => STATUS_MAP[s]?.type  || 'info'

const loading = ref(true)
const loadError = ref(false)
const assignment = ref(null)
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

const canResubmit = computed(() => assignment.value?.status !== 'graded')

const goResubmit = () => {
  router.push(`/my/assignment/${assignment.value?.assignmentId || route.params.id}/submit`)
}

const load = async () => {
  loading.value = true
  loadError.value = false
  submission.value = null
  const id = String(route.params.id || '')
  try {
    const detail = await myAssignmentDetail(id)
    if (detail && detail.assignmentId) {
      assignment.value = detail
    } else {
      const res = await myAssignments({ current: 1, size: 200 })
      assignment.value = (res?.records || res?.list || []).find(a => String(a.assignmentId) === id) || null
    }
    if (assignment.value) {
      try {
        submission.value = await myLatestSubmission(id)
      } catch (e) { submission.value = null }
    }
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
.asview { padding-bottom: var(--s-9); }
.asview__head { padding: var(--s-7) 0; border-bottom: 1px solid var(--line); background: linear-gradient(180deg, #fff 0%, #F4F7FF 100%); }
.head { display: flex; flex-direction: column; gap: 8px; }
.head__back { display: inline-flex; align-items: center; gap: 6px; font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.18em; text-transform: uppercase; color: var(--mute); text-decoration: none; align-self: flex-start; }
.head__back:hover { color: var(--accent); }
.head__title { margin: 0; font-family: var(--font-display); font-size: 28px; font-weight: 600; color: var(--ink); letter-spacing: -0.012em; }
.head__meta { margin: 0; display: flex; gap: 18px; flex-wrap: wrap; font-family: var(--font-mono); font-size: 12px; color: var(--ink-soft); align-items: center; }

.asview__body { padding: var(--s-7) 0 0; }
.grid { display: flex; flex-direction: column; gap: clamp(16px, 2vw, 24px); max-width: 960px; }
.card { background: var(--surface); border: 1px solid var(--line); padding: clamp(20px, 2.4vw, 28px); }
.card__title { margin: 0 0 12px; font-family: var(--font-display); font-size: 15px; font-weight: 600; color: var(--ink); }
.card__body { margin: 0; font-size: 14px; color: var(--ink-soft); line-height: 1.85; }
.card__body--pre { font-family: var(--font-mono); font-size: 13px; white-space: pre-wrap; word-break: break-word; background: var(--surface-soft); border: 1px solid var(--line-soft); padding: 14px 16px; }
.card__body--mute { color: var(--mute); font-style: italic; }
.file { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; font-size: 13px; }
.file__name { color: var(--accent); text-decoration: none; }
.file__name:hover { text-decoration: underline; }
.file__size, .file__suffix { color: var(--mute); font-family: var(--font-mono); font-size: 11.5px; }

.kv { margin: 0; display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px 24px; }
.kv__row { display: flex; gap: 12px; font-size: 13px; padding: 6px 0; border-bottom: 1px dashed var(--line-soft); }
.kv__row dt { color: var(--mute); min-width: 96px; font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.1em; text-transform: uppercase; }
.kv__row dd { margin: 0; color: var(--ink); }

.actions { display: flex; gap: 12px; justify-content: flex-end; padding-top: 8px; border-top: 1px solid var(--line-soft); }

.loading-tip { color: var(--mute); font-size: 13px; letter-spacing: 0.06em; padding: 48px 0; text-align: center; }
.empty { padding: 48px 0; text-align: center; color: var(--mute); display: flex; flex-direction: column; align-items: center; gap: 14px; }
.empty__cta { color: var(--accent); text-decoration: none; font-size: 13px; }
.empty__cta:hover { text-decoration: underline; }
</style>
