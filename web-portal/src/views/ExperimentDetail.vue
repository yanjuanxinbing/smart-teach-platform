<template>
  <div class="page edetail">
    <header class="edetail__head">
      <div class="container head">
        <div class="head__crumbs">
          <router-link to="/my/experiments" class="head__back">
            <el-icon :size="13"><Back /></el-icon>
            <span>返回我的实验</span>
          </router-link>
        </div>
        <div v-if="loading" class="loading-tip">实验详情加载中…</div>
        <div v-else-if="error" class="empty">
          <el-icon :size="32"><Warning /></el-icon>
          <p>实验详情暂时不可用</p>
          <el-button size="small" plain @click="fetchDetail">重新加载</el-button>
        </div>
        <div v-else-if="!detail" class="empty">
          <el-icon :size="32"><Warning /></el-icon>
          <p>未找到该实验（ID: {{ route.params.id }}）</p>
          <router-link to="/my/experiments" class="empty__cta">返回列表</router-link>
        </div>
        <template v-else>
          <div class="head__top">
            <el-tag :type="statusType(detail.status)" effect="dark" size="small" class="head__tag">
              {{ statusLabel(detail.status) }}
            </el-tag>
            <el-tag v-if="detail.assignmentStatus === 3" type="primary" effect="dark" size="small">
              已完成
            </el-tag>
          </div>
          <h1 class="head__title">{{ detail.expName || '未命名实验' }}</h1>
          <p class="head__plan">{{ detail.planTitle || '—' }}</p>
          <div class="head__meta">
            <span class="meta__item">学期：{{ detail.semester || '—' }}</span>
            <span class="meta__item">班级：{{ detail.className || '—' }}</span>
            <span class="meta__item">课程：{{ detail.courseName || '—' }}</span>
            <span class="meta__item">实验室：{{ detail.labRoom || '—' }}</span>
            <span class="meta__item">实验序号：#{{ detail.expNo ?? '—' }}</span>
            <span class="meta__item">类型：{{ expTypeLabel(detail.expType) }}</span>
            <span class="meta__item">上课日期：{{ fmtDate(detail.classDate) }}</span>
            <span class="meta__item">节次：{{ detail.classPeriod || '—' }}</span>
            <span class="meta__item">学时：{{ detail.hours || '—' }}</span>
          </div>
        </template>
      </div>
    </header>

    <section v-if="detail" class="edetail__body">
      <div class="container grid">
        <article class="card">
          <h2 class="card__title">实验目的</h2>
          <p class="card__body">{{ detail.purpose || '—' }}</p>
        </article>
        <article class="card">
          <h2 class="card__title">实验内容</h2>
          <p class="card__body">{{ detail.content || '—' }}</p>
        </article>
        <article class="card">
          <h2 class="card__title">实验要求</h2>
          <p class="card__body">{{ detail.requirement || '—' }}</p>
        </article>
        <article class="card" v-if="detail.remark">
          <h2 class="card__title">备注</h2>
          <p class="card__body">{{ detail.remark }}</p>
        </article>
        <article class="card card--meta">
          <h2 class="card__title">实验明细</h2>
          <dl class="kv">
            <div class="kv__row"><dt>实验 ID</dt><dd>{{ detail.itemId }}</dd></div>
            <div class="kv__row"><dt>所属计划</dt><dd>{{ detail.planTitle || '—' }}（#{{ detail.planId }}）</dd></div>
            <div class="kv__row"><dt>学期</dt><dd>{{ detail.semester || '—' }}</dd></div>
            <div class="kv__row"><dt>班级</dt><dd>{{ detail.className || '—' }}</dd></div>
            <div class="kv__row"><dt>指导教师</dt><dd>{{ detail.teacherName || '—' }}</dd></div>
            <div class="kv__row"><dt>实验室</dt><dd>{{ detail.labRoom || '—' }}</dd></div>
            <div class="kv__row"><dt>上课日期</dt><dd>{{ fmtDate(detail.classDate) }}</dd></div>
            <div class="kv__row"><dt>节次 / 学时</dt><dd>{{ detail.classPeriod || '—' }} / {{ detail.hours || '—' }} 学时</dd></div>
            <div class="kv__row" v-if="detail.planStartDate">
              <dt>计划起止</dt>
              <dd>{{ fmtDate(detail.planStartDate) }} ~ {{ fmtDate(detail.planEndDate) }}</dd>
            </div>
            <div class="kv__row" v-if="detail.assignmentStatus === 3">
              <dt>成绩</dt>
              <dd class="kv__score">{{ detail.score ?? '—' }}</dd>
            </div>
            <div class="kv__row" v-if="detail.comment">
              <dt>教师评语</dt>
              <dd>{{ detail.comment }}</dd>
            </div>
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
import { myExperimentDetail } from '@/api/experiment'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const STATUS_MAP = {
  not_started: { label: '未开始', type: 'info' },
  in_progress: { label: '进行中', type: 'warning' },
  done:        { label: '已结束', type: 'success' }
}
const statusLabel = (s) => STATUS_MAP[s]?.label || '未知'
const statusType  = (s) => STATUS_MAP[s]?.type  || 'info'

const EXP_TYPE_MAP = { 1: '验证', 2: '综合', 3: '设计', 4: '创新' }
const expTypeLabel = (t) => EXP_TYPE_MAP[t] || '—'

const fmtDate = (iso) => {
  if (!iso) return '—'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

const loading = ref(true)
const error   = ref(false)
const detail  = ref(null)

const fetchDetail = async () => {
  loading.value = true
  error.value = false
  detail.value = null
  const id = route.params.id
  try {
    const data = await myExperimentDetail(id)
    if (data && data.itemId) {
      detail.value = data
    } else {
      error.value = true
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
.edetail { padding-bottom: var(--s-9); }
.edetail__head { padding: var(--s-7) 0; border-bottom: 1px solid var(--line); background: linear-gradient(180deg, #fff 0%, #F4F7FF 100%); }
.head { display: flex; flex-direction: column; gap: 10px; }
.head__crumbs { display: flex; align-items: center; gap: 8px; }
.head__back { display: inline-flex; align-items: center; gap: 6px; font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.18em; text-transform: uppercase; color: var(--mute); text-decoration: none; }
.head__back:hover { color: var(--accent); }
.head__top { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.head__tag { align-self: flex-start; }
.head__title { margin: 4px 0 0; font-family: var(--font-display); font-size: 30px; font-weight: 600; color: var(--ink); letter-spacing: -0.012em; }
.head__plan { margin: 0; font-size: 13px; color: var(--ink-soft); }
.head__meta { display: flex; gap: 18px; flex-wrap: wrap; font-family: var(--font-mono); font-size: 11.5px; letter-spacing: 0.06em; color: var(--mute); margin-top: 4px; }
.meta__item { white-space: nowrap; }

.loading-tip { color: var(--mute); font-size: 13px; letter-spacing: 0.06em; padding: 48px 0; text-align: center; }
.empty { padding: 48px 0; text-align: center; color: var(--mute); display: flex; flex-direction: column; align-items: center; gap: 14px; }
.empty__cta { color: var(--accent); text-decoration: none; font-size: 13px; }
.empty__cta:hover { text-decoration: underline; }

.edetail__body { padding: var(--s-7) 0 0; }
.grid { display: grid; grid-template-columns: 1fr 1fr; gap: clamp(16px, 2vw, 28px); }
.card { background: var(--surface); border: 1px solid var(--line); padding: clamp(20px, 2.4vw, 28px); }
.card__title { margin: 0 0 12px; font-family: var(--font-display); font-size: 16px; font-weight: 600; color: var(--ink); }
.card__body { margin: 0; font-size: 14px; color: var(--ink-soft); line-height: 1.85; white-space: pre-wrap; }
.card--meta { grid-column: 1 / -1; }
.kv { margin: 0; display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px 24px; }
.kv__row { display: flex; gap: 12px; font-size: 13px; padding: 6px 0; border-bottom: 1px dashed var(--line-soft); }
.kv__row dt { color: var(--mute); min-width: 96px; font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.1em; text-transform: uppercase; }
.kv__row dd { margin: 0; color: var(--ink); }
.kv__score { color: var(--accent); font-weight: 600; font-size: 15px; }

@media (max-width: 800px) { .grid { grid-template-columns: 1fr; } .kv { grid-template-columns: 1fr; } }
</style>