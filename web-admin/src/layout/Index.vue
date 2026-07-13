<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <div class="layout-logo">
        <img src="@/assets/logo.svg" alt="logo" v-if="false" />
        <span v-if="!isCollapse">智能教学平台</span>
        <span v-else>智</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        background-color="#001529"
        text-color="#ffffffcc"
        active-text-color="#ffffff"
        router
        unique-opened
      >
        <MenuItem :menu-list="menuList" />
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Expand v-if="isCollapse" />
            <Fold v-else />
          </el-icon>
          <Breadcrumb />
        </div>

        <div class="header-right">
          <!-- 顶栏铃铛 -->
          <el-popover
            placement="bottom-end"
            :width="320"
            trigger="click"
            popper-class="la-bell-pop"
          >
            <template #reference>
              <el-badge v-if="unread" :value="unread" :max="99" class="bell-badge">
                <el-icon class="bell" :size="18" @click="bellOpen = true"><BellFilled /></el-icon>
              </el-badge>
              <el-icon v-else class="bell" :size="18" @click="bellOpen = true"><Bell /></el-icon>
            </template>
            <div class="bell">
              <header class="bell__head">
                <span class="bell__title">消息通知</span>
                <el-link type="primary" :underline="false" @click="go('/message/notice'); bellOpen = false">查看全部</el-link>
              </header>
              <ul v-if="latest.length" class="bell__list">
                <li v-for="m in latest" :key="m.id" class="bell__item" @click="go('/message/notice'); bellOpen = false">
                  <span class="bell__dot" :class="{ 'is-read': m.read }"></span>
                  <div class="bell__body">
                    <div class="bell__row">
                      <span class="bell__name">{{ m.title }}</span>
                      <span class="bell__time">{{ shortTime(m.createTime) }}</span>
                    </div>
                    <p class="bell__brief">{{ m.brief }}</p>
                  </div>
                </li>
              </ul>
              <div v-else class="bell__empty">暂无新消息</div>
              <footer class="bell__foot">
                <el-button size="small" plain @click="markAllRead">全部标为已读</el-button>
              </footer>
            </div>
          </el-popover>

          <!-- 用户菜单 -->
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                {{ userStore.userInfo?.realName?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
              <span class="user-role">{{ userStore.roleLabel }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="message">
                  我的消息
                  <el-badge v-if="unread" :value="unread" :max="99" class="menu-badge" />
                </el-dropdown-item>
                <el-dropdown-item command="security">安全设置</el-dropdown-item>
                <el-dropdown-item command="password" divided>修改密码</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>

  <ChangePasswordDialog v-model="showChangePwd" />
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Bell, BellFilled, Expand, Fold } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import MenuItem from '@/layout/MenuItem.vue'
import Breadcrumb from '@/layout/Breadcrumb.vue'
import ChangePasswordDialog from '@/layout/ChangePasswordDialog.vue'
import { unreadCount, myMessages, markAllRead as markAllReadApi } from '@/api/profile'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)
const showChangePwd = ref(false)
const bellOpen = ref(false)
const menuList = ref([])
const unread = ref(0)
const latest = ref([])

const activeMenu = computed(() => route.path)
const shortTime = (t) => t ? String(t).slice(5, 16) : ''

const refreshBell = async () => {
  if (!userStore.token) return
  try {
    unread.value = Number((await unreadCount()) || 0)
    const res = await myMessages({ size: 5 }).catch(() => null)
    latest.value = res?.records || res?.list || []
  } catch (e) { /* ignore */ }
}

const markAllRead = async () => {
  try { await markAllReadApi() } catch (e) {}
  latest.value.forEach(i => i.read = true)
  unread.value = 0
  ElMessage.success('已全部标为已读')
}

const go = (path) => router.push(path)

const handleCommand = async (cmd) => {
  if (cmd === 'logout') {
    await ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
    await userStore.logout()
    ElMessage.success('已退出')
    router.push('/login')
  } else if (cmd === 'password') {
    showChangePwd.value = true
  } else if (cmd === 'profile') {
    router.push('/profile/index')
  } else if (cmd === 'security') {
    router.push('/profile/security')
  } else if (cmd === 'message') {
    router.push('/message/notice')
  }
}

let timer = null
onMounted(async () => {
  try { await userStore.fetchUserInfo() } catch (e) {}
  try { menuList.value = await userStore.fetchMyMenu() } catch (e) { menuList.value = [] }
  refreshBell()
  timer = setInterval(refreshBell, 60_000)
})
onBeforeUnmount(() => { if (timer) clearInterval(timer) })
</script>

<style lang="scss" scoped>
.layout-container { height: 100vh; }
.layout-aside {
  background: #001529;
  transition: width 0.28s;
  overflow: hidden;
}
.layout-logo {
  height: 60px;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 18px; font-weight: bold;
  background: #002140; white-space: nowrap;
}
.layout-header {
  display: flex; align-items: center; justify-content: space-between;
  background: #fff; box-shadow: 0 1px 4px rgba(0,21,41,0.08);
  padding: 0 16px; z-index: 9;
}
.header-left { display: flex; align-items: center; }
.collapse-btn { font-size: 20px; cursor: pointer; margin-right: 16px; }
.header-right { display: flex; align-items: center; gap: 16px; }
.bell, .bell-badge { cursor: pointer; padding: 6px; transition: color var(--t-fast, 180ms) cubic-bezier(0.22, 0.61, 0.36, 1); }
.bell:hover { color: #409EFF; }

.user-info { display: flex; align-items: center; cursor: pointer; padding: 4px 8px; border: 1px solid #e6ebf2; }
.user-info:hover { border-color: #409EFF; }
.username { margin-left: 8px; }
.user-role {
  margin-left: 6px; padding: 2px 6px; font-size: 11px; letter-spacing: 0.06em;
  background: #ecf5ff; color: #409EFF; border: 1px solid #d9ecff; border-radius: 2px;
}
.menu-badge { margin-left: 8px; }

.layout-main { background: #f0f2f5; padding: 16px; }
:deep(.el-menu) { border-right: none; }

.fade-transform-enter-active, .fade-transform-leave-active { transition: all 0.28s; }
.fade-transform-enter-from { opacity: 0; transform: translateX(-20px); }
.fade-transform-leave-to { opacity: 0; transform: translateX(20px); }
</style>

<style>
/* 全局 — 后台消息弹层 */
.la-bell-pop { padding: 0 !important; border-radius: 0 !important; }
.la-bell-pop .bell { padding: 12px 12px 8px; }
.la-bell-pop .bell__head { display: flex; align-items: center; justify-content: space-between; padding-bottom: 8px; border-bottom: 1px solid #e6ebf2; }
.la-bell-pop .bell__title { font-weight: 600; color: #2F3E66; }
.la-bell-pop .bell__list { list-style: none; padding: 0; margin: 0; max-height: 320px; overflow-y: auto; }
.la-bell-pop .bell__item { display: flex; gap: 8px; padding: 10px 4px; border-bottom: 1px solid #f0f2f5; cursor: pointer; }
.la-bell-pop .bell__item:hover { background: #f8fafc; }
.la-bell-pop .bell__dot { width: 6px; height: 6px; background: #f56c6c; margin-top: 6px; border-radius: 50%; flex-shrink: 0; }
.la-bell-pop .bell__dot.is-read { background: #dcdfe6; }
.la-bell-pop .bell__body { flex: 1; min-width: 0; }
.la-bell-pop .bell__row { display: flex; justify-content: space-between; gap: 8px; }
.la-bell-pop .bell__name { font-size: 13px; color: #2F3E66; font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.la-bell-pop .bell__time { font-size: 11px; color: #909399; font-family: ui-monospace, monospace; }
.la-bell-pop .bell__brief { margin: 4px 0 0; font-size: 12px; line-height: 1.5; color: #606266; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.la-bell-pop .bell__empty { padding: 28px 0; text-align: center; color: #909399; font-size: 12px; }
.la-bell-pop .bell__foot { display: flex; justify-content: flex-end; padding-top: 8px; border-top: 1px solid #e6ebf2; margin-top: 8px; }
</style>
