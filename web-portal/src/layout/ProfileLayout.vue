<template>
  <div class="profile">
    <header class="profile__head">
      <div class="container head">
        <div class="head__left">
          <el-avatar :size="64" :src="userStore.userInfo?.avatar">
            {{ (userStore.userInfo?.realName || userStore.userInfo?.username || 'U').charAt(0) }}
          </el-avatar>
          <div class="head__info">
            <h1 class="head__name">{{ userStore.userInfo?.realName || userStore.userInfo?.username || '访客' }}</h1>
            <p class="head__meta">
              <span class="badge">{{ userStore.roleLabel }}</span>
              <span v-if="userStore.userInfo?.email">· {{ userStore.userInfo.email }}</span>
              <span v-if="userStore.userInfo?.deptName">· {{ userStore.userInfo.deptName }}</span>
            </p>
          </div>
        </div>
        <div class="head__right">
          <el-statistic :value="stat.courses" title="我的课程" />
          <el-statistic :value="stat.assignments" title="待办作业" />
          <el-statistic :value="stat.snippets" title="代码片段" />
        </div>
      </div>
    </header>

    <section class="profile__body">
      <div class="container grid">
        <aside class="grid__side">
          <nav class="sidenav">
            <router-link v-for="item in nav" :key="item.to" :to="item.to" class="sidenav__item" active-class="sidenav__item--active">
              <el-icon class="sidenav__ico"><component :is="item.icon" /></el-icon>
              <span>{{ item.label }}</span>
              <span v-if="item.count" class="sidenav__count">{{ item.count }}</span>
            </router-link>
          </nav>
          <div class="sidenav__cta">
            <p class="sidenav__cta-t">需要帮助？</p>
            <p class="sidenav__cta-d">遇到问题请通过课程页联系任课教师，或在站内消息中心留言。</p>
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
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Bell, Operation, EditPen } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { unreadCount } from '@/api/profile'

const router = useRouter()
const userStore = useUserStore()

// 个人中心统计卡片 —— 当前位挂的是 /portal/my/* 三个端点的 total 数字,
// 后端就绪后会真实呈现；目前为 null 表示"暂未加载"，避免之前写死 6/3/12 的演示数据误导上线用户。
const stat = ref({ courses: null, assignments: null, snippets: null })
const unread = ref(0)

const nav = computed(() => [
  { to: '/profile',         label: '我的资料', icon: User },
  { to: '/profile/security', label: '安全设置', icon: Lock },
  { to: '/profile/message',  label: '我的消息', icon: Bell, count: unread.value || null },
  { to: '/profile/log',      label: '操作日志', icon: Operation }
])

const refreshUnread = async () => {
  if (!userStore.isLogin) return
  try { unread.value = Number((await unreadCount()) || 0) } catch (e) { /* ignore */ }
}

onMounted(async () => {
  if (!userStore.isLogin) { router.push('/login?redirect=/profile'); return }
  try { await userStore.fetchUserInfo() } catch (e) {}
  refreshUnread()
})
</script>

<style scoped>
.profile { padding-bottom: var(--s-9); }
.profile__head { padding: var(--s-8) 0; border-bottom: 1px solid var(--line); background: linear-gradient(180deg, #fff 0%, var(--brand-100) 100%); }
.head { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: var(--s-6); }
.head__left { display: flex; align-items: center; gap: var(--s-5); }
.head__info { display: flex; flex-direction: column; gap: 6px; }
.head__name { font-family: var(--font-display); font-size: 28px; font-weight: 600; color: var(--ink); margin: 0; letter-spacing: -0.012em; }
.head__meta { margin: 0; font-size: 13px; color: var(--ink-soft); display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.badge { font-family: var(--font-mono); font-size: 10.5px; letter-spacing: 0.12em; text-transform: uppercase; padding: 3px 8px; color: var(--accent); background: var(--accent-tint); border: 1px solid var(--accent-tint); }
.head__right { display: flex; gap: clamp(20px, 4vw, 56px); }
.head__right :deep(.el-statistic__number) { font-family: var(--font-display); font-size: 28px; color: var(--ink); }
.head__right :deep(.el-statistic__title) { font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.18em; text-transform: uppercase; color: var(--mute); }

.profile__body { padding: var(--s-8) 0 0; }
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
.sidenav__count { margin-left: auto; font-family: var(--font-mono); font-size: 10px; min-width: 18px; height: 18px; padding: 0 6px; line-height: 18px; text-align: center; background: var(--danger); color: #fff; border-radius: 9px; }
.sidenav__item--active .sidenav__count { background: var(--accent); color: #fff; }

.sidenav__cta { margin-top: var(--s-5); padding: 18px; background: var(--surface); border: 1px solid var(--line); }
.sidenav__cta-t { font-family: var(--font-display); font-size: 14px; color: var(--ink); margin: 0; font-weight: 600; }
.sidenav__cta-d { font-size: 12px; color: var(--mute); margin: 4px 0 var(--s-3); }

.fade-enter-active, .fade-leave-active { transition: opacity var(--t-base) var(--ease); }
.fade-enter-from, .fade-leave-to { opacity: 0; }

@media (max-width: 880px) {
  .head__right { gap: 24px; }
  .grid { grid-template-columns: 1fr; }
}
</style>
