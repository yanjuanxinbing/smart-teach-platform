<template>
  <BannerList />
</template>

<script setup>
// 通知公告 = 复用轮播图页面组件，只需修改 type 为 2
// 简单起见直接复用结构；如需独立可拷贝 BannerList.vue 改 TYPE = 2
import BannerList from './BannerList.vue'
</script>
