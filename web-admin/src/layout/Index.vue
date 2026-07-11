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
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                {{ userStore.userInfo?.realName?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
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
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import MenuItem from '@/layout/MenuItem.vue'
import Breadcrumb from '@/layout/Breadcrumb.vue'
import ChangePasswordDialog from '@/layout/ChangePasswordDialog.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)
const showChangePwd = ref(false)
const menuList = ref([])

const activeMenu = computed(() => route.path)

onMounted(async () => {
  // 总是先拉一次 /auth/me，把 permissions / roles 灌回 store；
  // 不然页面刷新后 hasAuthority() 永远返回 false，依赖权限显隐的按钮会消失
  try { await userStore.fetchUserInfo() } catch (e) {}
  try {
    menuList.value = await userStore.fetchMyMenu()
  } catch (e) {
    menuList.value = []
  }
})

const handleCommand = async (cmd) => {
  if (cmd === 'logout') {
    await ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
    await userStore.logout()
    ElMessage.success('已退出')
    router.push('/login')
  } else if (cmd === 'password') {
    showChangePwd.value = true
  }
}
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
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background: #002140;
  white-space: nowrap;
}
.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,0.08);
  padding: 0 16px;
  z-index: 9;
}
.header-left { display: flex; align-items: center; }
.collapse-btn { font-size: 20px; cursor: pointer; margin-right: 16px; }
.user-info { display: flex; align-items: center; cursor: pointer; }
.username { margin-left: 8px; }
.layout-main { background: #f0f2f5; padding: 16px; }
:deep(.el-menu) { border-right: none; }
.fade-transform-enter-active,
.fade-transform-leave-active { transition: all 0.28s; }
.fade-transform-enter-from { opacity: 0; transform: translateX(-20px); }
.fade-transform-leave-to { opacity: 0; transform: translateX(20px); }
</style>
