<template>
  <div class="page">
    <header class="tlist__head">
      <div class="container head">
        <div class="head__crumbs">
          <router-link to="/my/trainings" class="head__back">
            <el-icon :size="13"><Back /></el-icon>
            <span>返回我的实训</span>
          </router-link>
        </div>
        <h1 class="head__title">可报名的实训计划</h1>
        <p class="head__sub">仅展示进行中的计划；点击卡片查看详情后即可报名，报名后等待管理员审核。</p>
      </div>
    </header>

    <section class="tlist__body">
      <div class="container">
        <div v-if="state === 'loading'" class="loading-tip">实训计划加载中…</div>

        <div v-else-if="state === 'empty'" class="empty">
          <el-icon :size="32"><Promotion /></el-icon>
          <p>暂无可报名的实训计划</p>
        </div>

        <div v-else-if="state === 'error'" class="empty">
          <el-icon :size="32"><Warning /></el-icon>
          <p>实训计划暂时不可用，请稍后再试</p>
          <el-button class="empty__cta" size="small" plain @click="fetch">重新加载</el-button>
        </div>

        <div v-else class="tlist-grid">
          <article
            v-for="(t, idx) in list"
            :key="t.planId"
            class="tlist-card"
            :style="{ transitionDelay: `${Math.min(idx, 8) * 50}ms` }"
            role="button"
            tabindex="0"
            @click="goDetail(t)"
            @keyup.enter="goDetail(t)"
          >
            <header class="tlist-card__head">
              <el-tag :type="regTagType(t)" effect="dark" size="small">
                {{ regTagLabel(t) }}
              </el-tag>
              <span class="tlist-card__id">#{{ t.planId }}</span>
            </header>
            <h3 class="tlist-card__title">{{ t.planTitle || '未命名计划' }}</h3>
            <p class="tlist-card__project">{{ t.projectName || '—' }}</p>
            <p class="tlist-card__meta">
              <span v-if="t.teacherName">指导：{{ t.teacherName }}</span>
              <span v-if="t.className">· {{ t.className }}</span>
              <span v-if="t.semester">· {{ t.semester }}</span>
            </p>
            <footer class="tlist-card__foot">
              <span>起 {{ fmtDate(t.startDate) }}</span>
              <span>止 {{ fmtDate(t.endDate) }}</span>
              <span v-if="t.capacity" class="tlist-card__cap">
                {{ t.registeredCount || 0 }} / {{ t.capacity }}
              </span>
            </footer>
          </article>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Back, Promotion, Warning } from '@element-plus/icons-vue'
import { availableTrainings } from '@/api/training'

const router = useRouter()

// 报名状态 → 卡片右上角 tag
// 0=待审核 1=已通过 2=已驳回 3=已完成 / 未报名=undefined
const REG_MAP = {
  0: { label: '已报名·待审核', type: 'info' },
  1: { label: '已报名·已通过', type: 'success' },
  2: { label: '已报名·已驳回', type: 'danger' },
  3: { label: '已报名·已完成', type: 'primary' }
}
const regTagLabel = (t) => t.registered ? (REG_MAP[t.registrationStatus]?.label || '已报名') : '可报名'
const regTagType  = (t) => t.registered ? (REG_MAP[t.registrationStatus]?.type || 'info') : 'warning'

const fmtDate = (iso) => {
  if (!iso) return '—'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

const state = ref('loading') // loading | ok | empty | error
const list  = ref([])

const fetch = async () => {
  state.value = 'loading'
  try {
    const res = await availableTrainings()
    list.value = Array.isArray(res) ? res : []
    state.value = list.value.length ? 'ok' : 'empty'
  } catch (e) {
    state.value = 'error'
    list.value = []
  }
}

const goDetail = (t) => {
  if (!t?.planId) return
  router.push(`/training/${t.planId}`)
}

onMounted(fetch)
</script>

<style scoped>
.tlist__head { padding: var(--s-7) 0; border-bottom: 1px solid var(--line); background: linear-gradient(180deg, #fff 0%, #F4F7FF 100%); }
.head { display: flex; flex-direction: column; gap: 8px; }
.head__crumbs { display: flex; align-items: center; gap: 8px; }
.head__back { display: inline-flex; align-items: center; gap: 6px; font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.18em; text-transform: uppercase; color: var(--mute); text-decoration: none; }
.head__back:hover { color: var(--accent); }
.head__title { margin: 4px 0 0; font-family: var(--font-display); font-size: 28px; font-weight: 600; color: var(--ink); letter-spacing: -0.012em; }
.head__sub { margin: 0; font-size: 13px; color: var(--ink-soft); line-height: 1.7; }

.tlist__body { padding: var(--s-7) 0 var(--s-9); }

.loading-tip { color: var(--mute); font-size: 13px; padding: 64px 0; text-align: center; }
.empty { padding: 88px 0; text-align: center; color: var(--mute); display: flex; flex-direction: column; align-items: center; gap: 14px; }
.empty__cta { color: var(--accent); }

.tlist-grid {
  display: grid; grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: clamp(20px, 2.6vw, 36px);
}
.tlist-card {
  display: flex; flex-direction: column; gap: 12px;
  padding: 22px; background: var(--surface); border: 1px solid var(--line);
  cursor: pointer;
  transition: transform var(--t-base) var(--ease), border-color var(--t-base) var(--ease), box-shadow var(--t-base) var(--ease);
}
.tlist-card:hover { transform: translateY(-3px); border-color: var(--accent); box-shadow: var(--shadow-blue); }
.tlist-card__head { display: flex; align-items: center; justify-content: space-between; gap: 10px; }
.tlist-card__id { font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.12em; text-transform: uppercase; color: var(--mute); }
.tlist-card__title { margin: 0; font-family: var(--font-display); font-size: 20px; font-weight: 600; color: var(--ink); letter-spacing: -0.012em; }
.tlist-card__project { margin: 0; font-size: 13px; color: var(--ink-soft); line-height: 1.7; }
.tlist-card__meta { margin: 0; font-size: 12px; color: var(--mute); display: flex; gap: 6px; flex-wrap: wrap; }
.tlist-card__foot {
  display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap;
  font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.06em; color: var(--mute);
  padding-top: 12px; border-top: 1px solid var(--line-soft); margin-top: auto;
}
.tlist-card__cap { color: var(--accent); font-weight: 600; }

@media (max-width: 800px) { .tlist-grid { grid-template-columns: 1fr; } }
</style>