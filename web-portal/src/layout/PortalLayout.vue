<template>
  <div class="layout">
    <PortalHeader />
    <main class="layout__main">
      <router-view v-slot="{ Component, route }">
        <transition name="fade" mode="out-in">
          <component :is="Component" :key="route.fullPath" />
        </transition>
      </router-view>
    </main>
    <PortalFooter />
  </div>
</template>

<script setup>
import PortalHeader from '@/layout/PortalHeader.vue'
import PortalFooter from '@/layout/PortalFooter.vue'
</script>

<style scoped>
.layout { display: flex; flex-direction: column; min-height: 100vh; background: var(--paper); }
.layout__main { flex: 1 0 auto; }
</style>
