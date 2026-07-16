<template>
  <div class="page tdetail">
    <header class="tdetail__head">
      <div class="container head">
        <div class="head__crumbs">
          <router-link to="/training" class="head__back">
            <el-icon :size="13"><Back /></el-icon>
            <span>返回可报名列表</span>
          </router-link>
          <span class="head__sep">/</span>
          <router-link to="/my/trainings" class="head__back">
            <span>我的实训</span>
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
          <router-link to="/training" class="empty__cta">返回列表</router-link>
        </div>
        <template v-else>
          <div class="head__top">
            <el-tag :type="regTagType(plan)" effect="dark" size="small" class="head__tag">
              {{ regTagLabel(plan) }}
            </el-tag>
            <!-- 已通过 / 进行中：跳到我的实训看进度 -->
            <router-link
              v-if="plan.registered && (plan.registrationStatus === 1 || plan.registrationStatus === 3)"
              to="/my/trainings"
              class="head__link"
            >在我的实训中查看 →</router-link>
          </div>
          <h1 class="head__title">{{ plan.planTitle || '未命名实训计划' }}</h1>
          <p class="head__project">{{ plan.projectName || '—' }}</p>
          <div class="head__meta">
            <span class="meta__item">学期：{{ plan.semester || '—' }}</span>
            <span class="meta__item">班级：{{ plan.className || '—' }}</span>
            <span class="meta__item">指导教师：{{ plan.teacherName || '—' }}</span>
            <span class="meta__item">地点：{{ plan.location || '—' }}</span>
            <span class="meta__item">起 {{ fmtDate(plan.startDate) }}</span>
            <span class="meta__item">止 {{ fmtDate(plan.endDate) }}</span>
            <span class="meta__item" v-if="plan.capacity">人数 {{ plan.registeredCount || 0 }} / {{ plan.capacity }}</span>
          </div>

          <div class="head__action">
            <!-- 未报名 + 计划状态正常 + 余位未满：显示报名按钮 -->
            <el-button
              v-if="canRegister"
              type="primary"
              :loading="submitting"
              @click="onRegister"
            >我要报名</el-button>
            <!-- 重复报名/已通过/待审核/已驳回/已完成：按钮禁用并提示 -->
            <el-button
              v-else
              disabled
            >{{ regButtonLabel(plan) }}</el-button>
          </div>
        </template>
      </div>
    </header>

    <section v-if="plan" class="tdetail__body">
      <div class="container grid">
        <article class="card">
          <h2 class="card__title">实训目标</h2>
          <p class="card__body">{{ plan.objective || '—' }}</p>
        </article>
        <article class="card">
          <h2 class="card__title">实训内容</h2>
          <p class="card__body">{{ plan.content || '—' }}</p>
        </article>
        <article class="card">
          <h2 class="card__title">考核方式</h2>
          <p class="card__body">{{ plan.assessment || '—' }}</p>
        </article>
        <article class="card card--meta">
          <h2 class="card__title">报名信息</h2>
          <dl class="kv">
            <div class="kv__row"><dt>计划 ID</dt><dd>{{ plan.planId }}</dd></div>
            <div class="kv__row"><dt>学期</dt><dd>{{ plan.semester || '—' }}</dd></div>
            <div class="kv__row"><dt>班级</dt><dd>{{ plan.className || '—' }}</dd></div>
            <div class="kv__row"><dt>指导教师</dt><dd>{{ plan.teacherName || '—' }}</dd></div>
            <div class="kv__row"><dt>地点</dt><dd>{{ plan.location || '—' }}</dd></div>
            <div class="kv__row"><dt>开始</dt><dd>{{ fmtDate(plan.startDate) }}</dd></div>
            <div class="kv__row"><dt>结束</dt><dd>{{ fmtDate(plan.endDate) }}</dd></div>
            <div class="kv__row"><dt>名额</dt><dd>{{ plan.registeredCount || 0 }} / {{ plan.capacity || '不限' }}</dd></div>
            <div class="kv__row" v-if="plan.registered">
              <dt>我的报名状态</dt>
              <dd>{{ regStatusLabel(plan.registrationStatus) }}</dd>
            </div>
          </dl>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Back, Warning } from '@element-plus/icons-vue'
import { trainingDetail as fetchPortalTraining, registerTraining } from '@/api/training'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 报名状态枚举 (0=待审核 1=已通过 2=已驳回 3=已完成 / 未报名 = undefined)
const REG_MAP = {
  0: { label: '待审核', type: 'info', btn: '已提交报名（待审核）' },
  1: { label: '已通过', type: 'success', btn: '已通过' },
  2: { label: '已驳回', type: 'danger', btn: '已被驳回' },
  3: { label: '已完成', type: 'primary', btn: '已完成' }
}
const regTagLabel     = (p) => p.registered ? (REG_MAP[p.registrationStatus]?.label || '已报名') : '可报名'
const regTagType      = (p) => p.registered ? (REG_MAP[p.registrationStatus]?.type  || 'info')   : 'warning'
const regStatusLabel  = (s) => REG_MAP[s]?.label || '—'
const regButtonLabel  = (p) => p.registered ? (REG_MAP[p.registrationStatus]?.btn || '已报名') : '不可报名'

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
const submitting = ref(false)

// 报名按钮可点击的条件：未报名 + 计划存在 + 余位未满（capacity 为 null/0 时不限制）
const canRegister = computed(() => {
  const p = plan.value
  if (!p) return false
  if (p.registered) return false
  if (p.status !== undefined && p.status !== 3) return false
  if (p.capacity && (p.registeredCount || 0) >= p.capacity) return false
  return true
})

const fetchDetail = async () => {
  loading.value = true
  error.value = false
  plan.value = null
  const id = route.params.id
  try {
    const detail = await fetchPortalTraining(id)
    if (detail && detail.planId) {
      plan.value = detail
    } else {
      error.value = true
    }
  } catch (e) {
    error.value = true
  } finally {
    loading.value = false
  }
}

const onRegister = async () => {
  if (!plan.value?.planId) return
  try {
    await ElMessageBox.confirm(
      `确定报名「${plan.value.planTitle || '该实训计划'}」？提交后需等待管理员审核。`,
      '确认报名',
      { type: 'info' }
    )
  } catch (e) { return }  // 用户取消

  submitting.value = true
  try {
    const vo = await registerTraining(plan.value.planId)
    plan.value = vo  // 刷新当前视图：会进入「已报名·待审核」分支
    ElMessage.success('报名已提交，请等待管理员审核')
  } catch (e) {
    // toast 由全局响应拦截器处理
  } finally {
    submitting.value = false
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
.head__crumbs { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.head__back { display: inline-flex; align-items: center; gap: 6px; font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.18em; text-transform: uppercase; color: var(--mute); text-decoration: none; }
.head__back:hover { color: var(--accent); }
.head__sep { color: var(--mute); font-size: 12px; }
.head__top { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.head__tag { align-self: flex-start; }
.head__link { font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.12em; text-transform: uppercase; color: var(--accent); text-decoration: none; }
.head__link:hover { text-decoration: underline; }
.head__title { margin: 4px 0 0; font-family: var(--font-display); font-size: 32px; font-weight: 600; color: var(--ink); letter-spacing: -0.012em; }
.head__project { margin: 0; font-size: 14px; color: var(--ink-soft); }
.head__meta { display: flex; gap: 18px; flex-wrap: wrap; font-family: var(--font-mono); font-size: 11.5px; letter-spacing: 0.06em; color: var(--mute); margin-top: 4px; }
.head__action { margin-top: 14px; }
.head__action :deep(.el-button) { border-radius: 0; padding: 11px 24px; font-family: var(--font-mono); font-size: 12px; letter-spacing: 0.14em; text-transform: uppercase; }

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