<template>
  <div class="page tdetail">
    <header class="tdetail__head">
      <div class="container head">
        <div class="head__crumbs">
          <router-link to="/my/trainings" class="head__back">
            <el-icon :size="13"><Back /></el-icon>
            <span>返回我的实训</span>
          </router-link>
        </div>
        <div v-if="loading" class="loading-tip">实训详情加载中…</div>
        <div v-else-if="error" class="empty">
          <el-icon :size="32"><Warning /></el-icon>
          <p>实训详情暂时不可用</p>
          <el-button size="small" plain @click="fetchDetail">重新加载</el-button>
        </div>
        <div v-else-if="!plan" class="empty">
          <el-icon :size="32"><Warning /></el-icon>
          <p>未找到该实训计划（ID: {{ route.params.id }}）</p>
          <router-link to="/my/trainings" class="empty__cta">返回列表</router-link>
        </div>
        <template v-else>
          <el-tag :type="progressType(plan.status)" effect="dark" size="small" class="head__tag">
            {{ progressLabel(plan.status) }}
          </el-tag>
          <h1 class="head__title">{{ plan.planName || '未命名实训计划' }}</h1>
          <p class="head__project">{{ plan.projectName || '—' }}</p>
          <div class="head__meta">
            <span class="meta__item">学期：{{ plan.semester || '—' }}</span>
            <span class="meta__item">班级：{{ plan.className || '—' }}</span>
            <span class="meta__item">指导教师：{{ plan.teacherName || '—' }}</span>
            <span class="meta__item">起 {{ fmtDate(plan.startDate) }}</span>
            <span class="meta__item">止 {{ fmtDate(plan.endDate) }}</span>
          </div>
          <div class="head__progress">
            <el-progress :percentage="plan.progress || 0" :stroke-width="8" />
            <span class="head__pct">{{ plan.progress || 0 }}%</span>
          </div>
        </template>
      </div>
    </header>

    <section v-if="plan" class="tdetail__body">
      <div class="container grid">
        <article class="card">
          <h2 class="card__title">实训目标</h2>
          <p class="card__body">{{ plan.objective || '（后端 PortalMyTrainingVO 暂未返回 objective 字段，当前为占位说明）' }}</p>
        </article>
        <article class="card">
          <h2 class="card__title">实训内容</h2>
          <p class="card__body">{{ plan.content || '（后端 PortalMyTrainingVO 暂未返回 content 字段，当前为占位说明）' }}</p>
        </article>
        <article class="card">
          <h2 class="card__title">考核方式</h2>
          <p class="card__body">{{ plan.assessment || '（后端 PortalMyTrainingVO 暂未返回 assessment 字段，当前为占位说明）' }}</p>
        </article>
        <article class="card card--meta">
          <h2 class="card__title">报名信息</h2>
          <dl class="kv">
            <div class="kv__row"><dt>报名记录 ID</dt><dd>{{ plan.registrationId }}</dd></div>
            <div class="kv__row"><dt>计划 ID</dt><dd>{{ plan.trainingId }}</dd></div>
            <div class="kv__row"><dt>学期</dt><dd>{{ plan.semester || '—' }}</dd></div>
            <div class="kv__row"><dt>班级</dt><dd>{{ plan.className || '—' }}</dd></div>
            <div class="kv__row"><dt>指导教师</dt><dd>{{ plan.teacherName || '—' }}</dd></div>
            <div class="kv__row"><dt>开始</dt><dd>{{ fmtDate(plan.startDate) }}</dd></div>
            <div class="kv__row"><dt>结束</dt><dd>{{ fmtDate(plan.endDate) }}</dd></div>
            <div class="kv__row"><dt>状态</dt><dd>{{ progressLabel(plan.status) }}</dd></div>
          </dl>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Back, Warning } from '@element-plus/icons-vue'
import { myTrainings, myTrainingDetail } from '@/api/my'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const PROGRESS_MAP = {
  not_started: { label: '未开始', type: 'info' },
  in_progress: { label: '进行中', type: 'warning' },
  done:        { label: '已完成', type: 'success' }
}
const progressLabel = (s) => PROGRESS_MAP[s]?.label || '未知'
const progressType  = (s) => PROGRESS_MAP[s]?.type  || 'info'
const fmtDate = (iso) => {
  if (!iso) return '—'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

const loading = ref(true)
const error   = ref(false)
const plan    = ref(null)

/**
 * 详情加载策略：
 *   1) 优先调用 myTrainingDetail(id) —— 后续后端补齐 /portal/my/trainings/{id} 后会拿到完整字段；
 *   2) 失败/404 时回退到 myTrainings 列表接口，在前端按 trainingId 过滤。
 *   这样在「后端未就绪」阶段也能让卡片跳转不报错。
 */
const fetchDetail = async () => {
  loading.value = true
  error.value = false
  plan.value = null
  const id = String(route.params.id || '')
  try {
    const detail = await myTrainingDetail(id)
    if (detail && (detail.trainingId || detail.id)) {
      plan.value = detail
      return
    }
  } catch (e) { /* 静默回退 */ }

  try {
    const res = await myTrainings({ current: 1, size: 200 })
    const records = res?.records || res?.list || []
    const hit = records.find(r => String(r.trainingId) === id)
    if (hit) {
      plan.value = hit
    } else {
      error.value = false
    }
  } catch (e) {
    error.value = true
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (!userStore.isLogin) {
    router.replace({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  fetchDetail()
})
</script>

<style scoped>
.tdetail { padding-bottom: var(--s-9); }
.tdetail__head { padding: var(--s-7) 0; border-bottom: 1px solid var(--line); background: linear-gradient(180deg, #fff 0%, #F4F7FF 100%); }
.head { display: flex; flex-direction: column; gap: 10px; }
.head__crumbs { display: flex; align-items: center; gap: 8px; }
.head__back { display: inline-flex; align-items: center; gap: 6px; font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.18em; text-transform: uppercase; color: var(--mute); text-decoration: none; }
.head__back:hover { color: var(--accent); }
.head__tag { align-self: flex-start; }
.head__title { margin: 4px 0 0; font-family: var(--font-display); font-size: 32px; font-weight: 600; color: var(--ink); letter-spacing: -0.012em; }
.head__project { margin: 0; font-size: 14px; color: var(--ink-soft); }
.head__meta { display: flex; gap: 18px; flex-wrap: wrap; font-family: var(--font-mono); font-size: 11.5px; letter-spacing: 0.06em; color: var(--mute); margin-top: 4px; }
.head__progress { display: flex; align-items: center; gap: 14px; margin-top: 10px; max-width: 480px; }
.head__pct { font-family: var(--font-mono); font-size: 13px; color: var(--accent); font-weight: 600; }

.loading-tip { color: var(--mute); font-size: 13px; letter-spacing: 0.06em; padding: 48px 0; text-align: center; }
.empty { padding: 48px 0; text-align: center; color: var(--mute); display: flex; flex-direction: column; align-items: center; gap: 14px; }
.empty__cta { color: var(--accent); text-decoration: none; font-size: 13px; }
.empty__cta:hover { text-decoration: underline; }

.tdetail__body { padding: var(--s-7) 0 0; }
.grid { display: grid; grid-template-columns: 1fr 1fr; gap: clamp(16px, 2vw, 28px); }
.card { background: var(--surface); border: 1px solid var(--line); padding: clamp(20px, 2.4vw, 28px); }
.card__title { margin: 0 0 12px; font-family: var(--font-display); font-size: 16px; font-weight: 600; color: var(--ink); }
.card__body { margin: 0; font-size: 14px; color: var(--ink-soft); line-height: 1.85; white-space: pre-wrap; }
.card--meta { grid-column: 1 / -1; }
.kv { margin: 0; display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px 24px; }
.kv__row { display: flex; gap: 12px; font-size: 13px; padding: 6px 0; border-bottom: 1px dashed var(--line-soft); }
.kv__row dt { color: var(--mute); min-width: 96px; font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.1em; text-transform: uppercase; }
.kv__row dd { margin: 0; color: var(--ink); }

@media (max-width: 800px) { .grid { grid-template-columns: 1fr; } .kv { grid-template-columns: 1fr; } }
</style>
