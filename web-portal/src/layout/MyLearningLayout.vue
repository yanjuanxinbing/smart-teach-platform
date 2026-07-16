<template>
  <div class="mylearn">
    <header class="mylearn__head">
      <div class="container head">
        <div class="head__left">
          <el-avatar :size="64" :src="userStore.userInfo?.avatar">
            {{ (userStore.userInfo?.realName || userStore.userInfo?.username || 'U').charAt(0) }}
          </el-avatar>
          <div class="head__info">
            <h1 class="head__name">{{ userStore.userInfo?.realName || userStore.userInfo?.username || '同学' }}</h1>
            <p class="head__meta">
              <span class="badge">{{ userStore.roleLabel || '学生' }}</span>
              <span v-if="userStore.userInfo?.email">· {{ userStore.userInfo.email }}</span>
              <span v-if="userStore.userInfo?.deptName">· {{ userStore.userInfo.deptName }}</span>
            </p>
          </div>
        </div>
        <div class="head__right">
          <el-statistic :value="stats.courses" title="已选课程" />
          <el-statistic :value="stats.pending" title="待办作业" />
          <el-statistic :value="stats.inProgress" title="进行中实训" />
        </div>
      </div>
    </header>

    <section class="mylearn__body">
      <div class="container grid">
        <aside class="grid__side">
          <nav class="sidenav">
            <router-link
              v-for="item in nav"
              :key="item.to"
              :to="item.to"
              class="sidenav__item"
              active-class="sidenav__item--active"
            >
              <el-icon class="sidenav__ico"><component :is="item.icon" /></el-icon>
              <span>{{ item.label }}</span>
            </router-link>
          </nav>
          <div class="sidenav__cta">
            <p class="sidenav__cta-t">学习中心</p>
            <p class="sidenav__cta-d">数据基于当前登录学生的 userId 隔离展示</p>
          </div>
        </aside>
        <main class="grid__main">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </main>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Notebook, EditPen, Promotion, Files, DataAnalysis } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { myCourses, myAssignments, myTrainings } from '@/api/my'
import { listPortalResources } from '@/api/resource'

const userStore = useUserStore()

const stats = ref({ courses: 0, pending: 0, inProgress: 0 })

const nav = computed(() => [
  { to: '/my/courses',     label: '我的课程', icon: Notebook },
  { to: '/my/assignments', label: '我的作业', icon: EditPen },
  { to: '/my/trainings',   label: '我的实训', icon: Promotion },
  { to: '/my/experiments', label: '我的实验', icon: DataAnalysis },
  { to: '/my/resources',   label: '我的资源', icon: Files }
])

// 用 Promise.allSettled：任一接口未实现或失败也不会阻断其它统计数字；
// 后端未实现时 api/* 的 silentError 抑制 toast,所以页面只是显示 0.
const refreshStats = async () => {
  if (!userStore.isLogin) return
  const results = await Promise.allSettled([
    myCourses(),
    myAssignments({ status: 'pending' }),
    myTrainings({ status: 'in_progress' }),
    listPortalResources({ current: 1, size: 1 })
  ])
  const totalOf = (r) => Number(r.status === 'fulfilled' ? (r.value?.total ?? r.value?.records?.length ?? 0) : 0)
  stats.value = {
    courses:    totalOf(results[0]),
    pending:    totalOf(results[1]),
    inProgress: totalOf(results[2])
  }
}

onMounted(async () => {
  // 安全网:store.userInfo 由 localStorage 同步 hydration,但若被外部清空 /
  // 多端登录不一致导致 userId 缺失,这里再拉一次 /auth/me 把 userInfo 装回 store,
  // 保证后续子页面 fetch 时拿到的 userId 是真实的(而不是默认 -1)。
  if (userStore.isLogin && !userStore.userInfo?.userId) {
    try { await userStore.fetchUserInfo() } catch (e) { /* swallow: 401/403 由响应拦截器统一处理 */ }
  }
  refreshStats()
})
</script>

<style scoped>
.mylearn { padding-bottom: var(--s-9); }
.mylearn__head { padding: var(--s-8) 0; border-bottom: 1px solid var(--line); background: linear-gradient(180deg, #fff 0%, #F4F7FF 100%); }
.head { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: var(--s-6); }
.head__left { display: flex; align-items: center; gap: var(--s-5); }
.head__info { display: flex; flex-direction: column; gap: 6px; }
.head__name { font-family: var(--font-display); font-size: 28px; font-weight: 600; color: var(--ink); margin: 0; letter-spacing: -0.012em; }
.head__meta { margin: 0; font-size: 13px; color: var(--ink-soft); display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.badge { font-family: var(--font-mono); font-size: 10.5px; letter-spacing: 0.12em; text-transform: uppercase; padding: 3px 8px; color: var(--accent); background: var(--surface); border: 1px solid var(--accent); }
.head__right { display: flex; gap: clamp(20px, 4vw, 56px); }
.head__right :deep(.el-statistic__number) { font-family: var(--font-display); font-size: 28px; color: var(--ink); }
.head__right :deep(.el-statistic__title) { font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.18em; text-transform: uppercase; color: var(--mute); }

.mylearn__body { padding: var(--s-8) 0 0; }
.grid { display: grid; grid-template-columns: 240px minmax(0, 1fr); gap: var(--s-7); }

.sidenav { border: 1px solid var(--line); background: var(--surface); }
.sidenav__item {
  display: flex; align-items: center; gap: 12px;
  padding: 16px 20px; color: var(--ink-soft); font-size: 14px;
  border-bottom: 1px solid var(--line-soft);
  transition: background-color var(--t-fast) var(--ease), color var(--t-fast) var(--ease);
}
.sidenav__item:last-child { border-bottom: none; }
.sidenav__item:hover { background: var(--surface-soft); color: var(--ink); }
.sidenav__item--active { background: var(--ink); color: #fff; }
.sidenav__item--active .sidenav__ico { color: var(--accent); }
.sidenav__ico { font-size: 16px; }
.sidenav__item:not(.sidenav__item--active) .sidenav__ico { color: var(--accent); }

.sidenav__cta { margin-top: var(--s-5); padding: 18px; background: var(--surface); border: 1px solid var(--line); }
.sidenav__cta-t { font-family: var(--font-display); font-size: 14px; color: var(--ink); margin: 0; font-weight: 600; }
.sidenav__cta-d { font-size: 12px; color: var(--mute); margin: 4px 0 0; line-height: 1.55; }

.fade-enter-active, .fade-leave-active { transition: opacity var(--t-base) var(--ease); }
.fade-enter-from, .fade-leave-to { opacity: 0; }

@media (max-width: 880px) {
  .head__right { gap: 24px; }
  .grid { grid-template-columns: 1fr; }
}
</style>
