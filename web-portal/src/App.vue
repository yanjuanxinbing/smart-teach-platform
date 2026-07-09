<template>
  <div class="app">
    <div class="scroll-progress" :style="`{ width: ${progress}% }`" aria-hidden="true"></div>
    <router-view />
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'

const progress = ref(0)
let observer = null
let mutation = null

const updateProgress = () => {
  const h = document.documentElement
  const max = h.scrollHeight - h.clientHeight
  progress.value = max > 0 ? Math.min(100, Math.max(0, (h.scrollTop / max) * 100)) : 0
}

const observeReveal = () => {
  if (!('IntersectionObserver' in window)) {
    document.querySelectorAll('[data-reveal]:not(.is-in)').forEach((el) => el.classList.add('is-in'))
    return
  }
  document.querySelectorAll('[data-reveal]:not(.is-in)').forEach((el) => observer.observe(el))
}

const onScroll = () => { updateProgress() }

onMounted(async () => {
  await nextTick()
  updateProgress()

  observer = new IntersectionObserver((entries) => {
    entries.forEach((e) => {
      if (e.isIntersecting) {
        e.target.classList.add('is-in')
        observer.unobserve(e.target)
      }
    })
  }, { threshold: 0.08, rootMargin: '0px 0px -40px 0px' })

  observeReveal()

  // 路由切换时，DOM 会被替换；用 MutationObserver 重新观察新的 [data-reveal]
  mutation = new MutationObserver(() => observeReveal())
  mutation.observe(document.body, { childList: true, subtree: true })

  window.addEventListener('scroll', onScroll, { passive: true })
  window.addEventListener('resize', onScroll)
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', onScroll)
  window.removeEventListener('resize', onScroll)
  if (observer) observer.disconnect()
  if (mutation) mutation.disconnect()
})
</script>

<style>
.app { min-height: 100vh; display: flex; flex-direction: column; background: var(--paper); }
</style>
