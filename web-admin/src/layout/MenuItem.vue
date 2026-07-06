<template>
  <template v-for="item in menuList" :key="item.id">
    <!-- 目录 -->
    <el-sub-menu v-if="item.children && item.children.length" :index="String(item.id)">
      <template #title>
        <el-icon><component :is="item.icon || 'Menu'" /></el-icon>
        <span>{{ item.menuName }}</span>
      </template>
      <MenuItem :menu-list="item.children" />
    </el-sub-menu>
    <!-- 菜单 -->
    <el-menu-item v-else-if="item.menuType === 2" :index="item.path">
      <el-icon><component :is="item.icon || 'Menu'" /></el-icon>
      <template #title>{{ item.menuName }}</template>
    </el-menu-item>
  </template>
</template>

<script setup>
defineProps({
  menuList: { type: Array, default: () => [] }
})
</script>
