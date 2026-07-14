<template>
  <header class="ph" :class="{ 'ph--scrolled': scrolled, 'ph--open': menuOpen }">
    <div class="container ph__inner">
      <router-link to="/" class="ph__brand" @click="closeMenu">
        <span class="ph__mark" aria-hidden="true">
          <svg viewBox="0 0 32 32" width="22" height="22" focusable="false">
            <path
              d="M4 27 L4 5 L11.5 19 L18 5 L18 27"
              fill="none" stroke="currentColor" stroke-width="1.6"
              stroke-linejoin="miter" stroke-linecap="square"
            />
            <path
              d="M18 5 L25.5 19 L28 5"
              fill="none" stroke="currentColor" stroke-width="1.6"
              stroke-linejoin="miter" stroke-linecap="square"
            />
          </svg>
        </span>
        <span class="ph__wordmark">
          <span class="ph__brand-name">Methōdus</span>
          <span class="ph__brand-sub">智能教学</span>
        </span>
      </router-link>

      <nav class="ph__nav" :aria-expanded="menuOpen ? 'true' : 'false'">
        <router-link
          v-for="item in nav"
          :key="item.to"
          :to="item.to"
          class="ph__link"
          @click="closeMenu"
        >
          <span class="ph__link-index">{{ item.index }}</span>
          <span class="ph__link-label">{{ item.label }}</span>
        </router-link>

        <!-- 个人中心：游客跳登录；已登录打开下拉菜单（含退出登录） -->
        <router-link
          v-if="!userStore.isLogin"
          class="ph__link ph__link--cta"
          :to="profileLink"
          @click="closeMenu"
        >
          <el-icon class="ph__link-ico" :size="12"><User /></el-icon>
          <span class="ph__link-label">个人中心</span>
        </router-link>
        <el-dropdown
          v-else
          trigger="click"
          class="ph__dropdown ph__profile"
          @command="onProfileCommand"
        >
          <span class="ph__link ph__link--cta ph__link--profile">
            <el-icon class="ph__link-ico" :size="12"><User /></el-icon>
            <span class="ph__link-label">{{ displayName }}</span>
            <el-icon class="ph__caret" :size="10"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">我的资料</el-dropdown-item>
              <el-dropdown-item command="security">安全设置</el-dropdown-item>
              <el-dropdown-item command="message">我的消息</el-dropdown-item>
              <el-dropdown-item command="log">我的日志</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <!-- 我的学习：仅学生可见 -->
        <el-dropdown
          v-if="userStore.isLogin && userStore.roleCode === 'STUDENT'"
          trigger="click"
          class="ph__dropdown"
          @command="onMyLearningCommand"
        >
          <span class="ph__link ph__link--cta">
            <el-icon class="ph__link-ico" :size="12"><Notebook /></el-icon>
            <span class="ph__link-label">我的学习</span>
            <el-icon class="ph__caret" :size="10"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item
                v-for="i in myLearningItems"
                :key="i.to"
                :command="i.to"
              >{{ i.label }}</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <!-- 登录注册：所有访问者可见的登录入口（含登录 / 注册下拉） -->
        <el-dropdown v-if="!userStore.isLogin" trigger="click" class="ph__dropdown">
          <span class="ph__link ph__link--cta">
            <el-icon class="ph__link-ico" :size="12"><Key /></el-icon>
            <span class="ph__link-label">登录注册</span>
            <el-icon class="ph__caret" :size="10"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="goTo('/login')">登 录</el-dropdown-item>
              <el-dropdown-item @click="goTo('/register')">注 册</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <!-- 管理中心：按角色控制 —— 教师/管理员 → 后台；学生 → 隐藏改为显示姓名；游客 → 引导登录 -->
        <a
          v-if="!userStore.isLogin"
          href="/login?intent=admin"
          class="ph__link ph__link--cta"
          @click.prevent="goToLoginAsAdmin"
        >
          <el-icon class="ph__link-ico" :size="12"><Position /></el-icon>
          <span class="ph__link-label">管理中心</span>
        </a>
        <a
          v-else-if="isManager"
          href="http://localhost:8081/"
          target="_blank"
          rel="noopener"
          class="ph__link ph__link--cta"
        >
          <el-icon class="ph__link-ico" :size="12"><Position /></el-icon>
          <span class="ph__link-label">管理中心</span>
        </a>
        <span v-else class="ph__link ph__link--name">
          <el-icon class="ph__link-ico" :size="12"><UserFilled /></el-icon>
          <span class="ph__link-label">{{ displayName }}</span>
        </span>
      </nav>

      <button
        class="ph__toggle"
        :aria-label="menuOpen ? '关闭菜单' : '打开菜单'"
        :aria-expanded="menuOpen ? 'true' : 'false'"
        @click="menuOpen = !menuOpen"
      >
        <span class="ph__toggle-bar" :class="{ 'is-open': menuOpen }">
          <span></span>
          <span></span>
        </span>
      </button>
    </div>

    <transition name="fade">
      <div v-if="menuOpen" class="ph__scrim" @click="closeMenu" />
    </transition>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  Position, User, UserFilled, Key, ArrowDown, Notebook
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const menuOpen = ref(false)
const scrolled = ref(false)
const nav = [
  { to: '/',         index: '01', label: '首页' },
  { to: '/course',   index: '02', label: '课程' },
  { to: '/codex',    index: '03', label: '笔记' },
  { to: '/notice',   index: '04', label: '通告' },
  { to: '/news',     index: '05', label: '资讯' },
  { to: '/stats',    index: '06', label: '数据' }
]

// 个人中心：游客跳登录（带 redirect= /profile），已登录走下拉
const profileLink = computed(() =>
  userStore.isLogin ? '/profile' : { path: '/login', query: { redirect: '/profile' } }
)

// 是否具备"老师 / 管理员"身份 —— 在此身份下显示「管理中心」跳转
const isManager = computed(() => {
  const r = userStore.roleCode
  return r === 'ADMIN' || r === 'TEACHER'
})

const displayName = computed(() => {
  const u = userStore.userInfo || {}
  return u.realName || u.username || '我'
})

const closeMenu = () => { menuOpen.value = false }
const onScroll = () => { scrolled.value = window.scrollY > 8 }

const goTo = (path) => { closeMenu(); router.push(path) }
// 「管理中心」对游客的跳转：跳到登录页并标记 intent=admin，登录后做角色校验
const goToLoginAsAdmin = () => {
  closeMenu()
  router.push({ path: '/login', query: { intent: 'admin' } })
}

// 「个人中心」下拉命令：路由跳转 / 退出登录
const PROFILE_COMMANDS = {
  profile:   '/profile',
  security:  '/profile/security',
  message:   '/profile/message',
  log:       '/profile/log'
}
const onProfileCommand = async (cmd) => {
  closeMenu()
  if (cmd === 'logout') {
    try {
      await ElMessageBox.confirm('确认退出当前账号吗？', '退出登录', {
        confirmButtonText: '退出',
        cancelButtonText: '取消',
        type: 'warning'
      })
    } catch (e) {
      return // 用户取消
    }
    // store 内部已 silentError，401 不会触发 403 提示
    await userStore.logout()
    // 用 location.replace 清空历史栈，防止按返回键又回到受保护页面
    window.location.replace('/login')
    return
  }
  const to = PROFILE_COMMANDS[cmd]
  if (to) router.push(to)
}

// 「我的学习」下拉命令（仅学生可见）
const myLearningItems = [
  { label: '我的课程', to: '/my/courses' },
  { label: '我的作业', to: '/my/assignments' },
  { label: '我的实训', to: '/my/trainings' }
]
const onMyLearningCommand = (to) => {
  closeMenu()
  router.push(to)
}

onMounted(() => {
  window.addEventListener('scroll', onScroll, { passive: true })
  onScroll()
  // 已登录但 userInfo 缺失时（如刷新页面）补一次拉取，确保姓名展示
  if (userStore.isLogin && !userStore.userInfo) {
    userStore.fetchUserInfo().catch(() => { /* swallow */ })
  }
})
onBeforeUnmount(() => window.removeEventListener('scroll', onScroll))
</script>

<style scoped>
.ph {
  position: sticky; top: 0; z-index: var(--z-header);
  background: rgba(244, 247, 255, 0.78);
  backdrop-filter: saturate(140%) blur(14px);
  -webkit-backdrop-filter: saturate(140%) blur(14px);
  border-bottom: 1px solid transparent;
  transition: border-color var(--t-base) var(--ease), background-color var(--t-base) var(--ease);
}
.ph--scrolled { border-bottom-color: var(--line); background: rgba(255, 255, 255, 0.94); }

.ph__inner {
  height: var(--header-h);
  display: flex; align-items: center; justify-content: space-between; gap: var(--s-6);
}

.ph__brand { display: inline-flex; align-items: center; gap: var(--s-3); color: var(--ink); flex-shrink: 0; }
.ph__brand:hover { color: var(--ink); }
.ph__mark {
  display: inline-flex; width: 38px; height: 38px;
  align-items: center; justify-content: center;
  color: var(--accent); border: 1px solid var(--line); background: var(--surface);
  transition: color var(--t-fast) var(--ease), border-color var(--t-fast) var(--ease),
              background-color var(--t-fast) var(--ease), box-shadow var(--t-base) var(--ease);
}
.ph__brand:hover .ph__mark { color: #fff; background: var(--accent); border-color: var(--accent); box-shadow: var(--shadow-blue); }
.ph__wordmark { display: flex; flex-direction: column; line-height: 1.05; }
.ph__brand-name { font-family: var(--font-display); font-size: 17px; font-weight: 600; letter-spacing: -0.01em; color: var(--ink); }
.ph__brand-sub { margin-top: 3px; font-size: 10px; letter-spacing: 0.26em; text-transform: uppercase; color: var(--mute); }

.ph__nav { display: flex; align-items: center; gap: clamp(20px, 2.4vw, 36px); }
.ph__link {
  position: relative; display: inline-flex; align-items: baseline; gap: 8px;
  font-size: 13px; letter-spacing: 0.04em; color: var(--ink-soft); padding: 8px 0;
  transition: color var(--t-fast) var(--ease);
  cursor: pointer;
}
.ph__link::after {
  content: ''; position: absolute; left: 0; right: 0; bottom: -2px;
  height: 1px; background: var(--accent);
  transform: scaleX(0); transform-origin: left;
  transition: transform var(--t-base) var(--ease);
}
.ph__link:hover { color: var(--ink); }
.ph__link:hover::after,
.ph__link.router-link-active::after { transform: scaleX(1); }
.ph__link.router-link-active { color: var(--ink); }
.ph__link-index { font-family: var(--font-mono); font-size: 10.5px; color: var(--mute); letter-spacing: 0.06em; transition: color var(--t-fast) var(--ease); }
.ph__link:hover .ph__link-index { color: var(--accent); }
.ph__link.router-link-active .ph__link-index { color: var(--accent); }
.ph__link-ico { color: var(--accent); }
.ph__link--cta {
  color: var(--ink); padding: 8px 0 8px 14px; border-left: 1px solid var(--line);
  align-items: center;
}
.ph__link--cta::after { background: var(--accent); transform: scaleX(0.6); }
.ph__link--cta:hover::after { transform: scaleX(1); }
.ph__link--name { color: var(--ink); padding: 8px 0 8px 14px; border-left: 1px solid var(--line); align-items: center; cursor: default; }
.ph__caret { color: var(--mute); margin-left: 2px; }

.ph__dropdown :deep(.el-dropdown__caret-button)::before,
.ph__dropdown :deep(.el-tooltip__trigger) { display: inline-flex; align-items: center; }

.ph__toggle {
  display: none; width: 44px; height: 44px;
  border: 1px solid var(--line); background: transparent; cursor: pointer; padding: 0;
  align-items: center; justify-content: center;
  transition: border-color var(--t-fast) var(--ease);
}
.ph__toggle:hover { border-color: var(--accent); }
.ph__toggle-bar { position: relative; width: 18px; height: 12px; display: inline-flex; flex-direction: column; justify-content: space-between; }
.ph__toggle-bar span { display: block; height: 1px; width: 100%; background: var(--ink); transition: transform var(--t-base) var(--ease), opacity var(--t-fast) var(--ease); }
.ph__toggle-bar.is-open span:nth-child(1) { transform: translateY(5px) rotate(45deg); }
.ph__toggle-bar.is-open span:nth-child(2) { transform: translateY(-5px) rotate(-45deg); }

.ph__scrim { position: fixed; inset: 0; background: rgba(15, 23, 42, 0.36); z-index: -1; }

@media (max-width: 880px) {
  .ph__brand-sub { display: none; }
  .ph__toggle { display: inline-flex; }
  .ph__nav {
    position: fixed; top: var(--header-h); right: 0; bottom: 0;
    width: min(360px, 86vw);
    background: var(--paper); border-left: 1px solid var(--line);
    flex-direction: column; align-items: stretch; gap: 0;
    padding: var(--s-6) var(--s-5) var(--s-7);
    transform: translateX(100%);
    transition: transform var(--t-base) var(--ease);
    overflow-y: auto;
  }
  .ph--open .ph__nav { transform: translateX(0); }
  .ph__link { padding: 18px 0; border-bottom: 1px solid var(--line-soft); font-size: 15px; align-items: center; }
  .ph__link::after { display: none; }
  .ph__link-index { width: 36px; font-family: var(--font-mono); }
  .ph__link--cta { border-left: none; padding-left: 0; }
  .ph__link--name { border-left: none; padding-left: 0; }
}
@media (max-width: 480px) {
  .ph__brand-name { font-size: 16px; }
  .ph__mark { width: 32px; height: 32px; }
}
</style>
