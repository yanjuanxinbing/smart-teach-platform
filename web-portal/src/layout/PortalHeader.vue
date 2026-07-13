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
          <span class="ph__brand-sub">智能教学 · 教研支持</span>
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
        <a
          class="ph__link ph__link--cta"
          href="http://localhost:8081"
          target="_blank" rel="noopener"
        >
          <el-icon class="ph__link-ico" :size="12"><Position /></el-icon>
          <span class="ph__link-label">管理入口</span>
        </a>
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
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { Position } from '@element-plus/icons-vue'

const menuOpen = ref(false)
const scrolled = ref(false)
const nav = [
  { to: '/',       index: '01', label: '首页' },
  { to: '/notice', index: '02', label: '通告' },
  { to: '/news',   index: '03', label: '资讯' },
  { to: '/stats',  index: '04', label: '数据' }
]

const closeMenu = () => { menuOpen.value = false }
const onScroll = () => { scrolled.value = window.scrollY > 8 }

onMounted(() => {
  window.addEventListener('scroll', onScroll, { passive: true })
  onScroll()
})
onBeforeUnmount(() => window.removeEventListener('scroll', onScroll))
</script>

<style scoped>
.ph {
  position: sticky;
  top: 0;
  z-index: var(--z-header);
  background: rgba(244, 247, 255, 0.78);
  backdrop-filter: saturate(140%) blur(14px);
  -webkit-backdrop-filter: saturate(140%) blur(14px);
  border-bottom: 1px solid transparent;
  transition:
    border-color var(--t-base) var(--ease),
    background-color var(--t-base) var(--ease);
}
.ph--scrolled {
  border-bottom-color: var(--line);
  background: rgba(255, 255, 255, 0.94);
}

.ph__inner {
  height: var(--header-h);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--s-6);
}

/* Brand */
.ph__brand {
  display: inline-flex;
  align-items: center;
  gap: var(--s-3);
  color: var(--ink);
  flex-shrink: 0;
}
.ph__brand:hover { color: var(--ink); }
.ph__mark {
  display: inline-flex;
  width: 38px; height: 38px;
  align-items: center; justify-content: center;
  color: var(--accent);
  border: 1px solid var(--line);
  background: var(--surface);
  transition:
    color            var(--t-fast) var(--ease),
    border-color     var(--t-fast) var(--ease),
    background-color var(--t-fast) var(--ease),
    box-shadow       var(--t-base) var(--ease);
}
.ph__brand:hover .ph__mark {
  color: #fff;
  background: var(--accent);
  border-color: var(--accent);
  box-shadow: var(--shadow-blue);
}
.ph__wordmark { display: flex; flex-direction: column; line-height: 1.05; }
.ph__brand-name {
  font-family: var(--font-display);
  font-size: 17px;
  font-weight: 600;
  letter-spacing: -0.01em;
  color: var(--ink);
}
.ph__brand-sub {
  margin-top: 3px;
  font-size: 10px;
  letter-spacing: 0.26em;
  text-transform: uppercase;
  color: var(--mute);
}

/* Nav */
.ph__nav {
  display: flex;
  align-items: center;
  gap: clamp(24px, 3vw, 44px);
}
.ph__link {
  position: relative;
  display: inline-flex;
  align-items: baseline;
  gap: 8px;
  font-size: 13px;
  letter-spacing: 0.04em;
  color: var(--ink-soft);
  padding: 8px 0;
  transition: color var(--t-fast) var(--ease);
}
.ph__link::after {
  content: '';
  position: absolute;
  left: 0; right: 0; bottom: -2px;
  height: 1px;
  background: var(--accent);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform var(--t-base) var(--ease);
}
.ph__link:hover { color: var(--ink); }
.ph__link:hover::after,
.ph__link.router-link-active::after { transform: scaleX(1); }
.ph__link.router-link-active { color: var(--ink); }
.ph__link-index {
  font-family: var(--font-mono);
  font-size: 10.5px;
  color: var(--mute);
  letter-spacing: 0.06em;
  transition: color var(--t-fast) var(--ease);
}
.ph__link:hover .ph__link-index { color: var(--accent); }
.ph__link.router-link-active .ph__link-index { color: var(--accent); }
.ph__link-ico { color: var(--accent); }
.ph__link--cta {
  color: var(--ink);
  padding-left: 14px;
  border-left: 1px solid var(--line);
  align-items: center;
}
.ph__link--cta::after {
  background: var(--accent);
  transform: scaleX(0.6);
}

/* Mobile toggle */
.ph__toggle {
  display: none;
  width: 44px; height: 44px;
  border: 1px solid var(--line);
  background: transparent;
  cursor: pointer;
  padding: 0;
  align-items: center; justify-content: center;
  transition: border-color var(--t-fast) var(--ease);
}
.ph__toggle:hover { border-color: var(--accent); }
.ph__toggle-bar {
  position: relative;
  width: 18px;
  height: 12px;
  display: inline-flex;
  flex-direction: column;
  justify-content: space-between;
}
.ph__toggle-bar span {
  display: block;
  height: 1px;
  width: 100%;
  background: var(--ink);
  transition: transform var(--t-base) var(--ease), opacity var(--t-fast) var(--ease);
}
.ph__toggle-bar.is-open span:nth-child(1) { transform: translateY(5px) rotate(45deg); }
.ph__toggle-bar.is-open span:nth-child(2) { transform: translateY(-5px) rotate(-45deg); }

.ph__scrim {
  position: fixed; inset: 0;
  background: rgba(15, 23, 42, 0.36);
  z-index: -1;
}

@media (max-width: 880px) {
  .ph__brand-sub { display: none; }
  .ph__toggle { display: inline-flex; }
  .ph__nav {
    position: fixed;
    top: var(--header-h);
    right: 0; bottom: 0;
    width: min(360px, 86vw);
    background: var(--paper);
    border-left: 1px solid var(--line);
    flex-direction: column;
    align-items: stretch;
    gap: 0;
    padding: var(--s-6) var(--s-5) var(--s-7);
    transform: translateX(100%);
    transition: transform var(--t-base) var(--ease);
    overflow-y: auto;
  }
  .ph--open .ph__nav { transform: translateX(0); }
  .ph__link {
    padding: 22px 0;
    border-bottom: 1px solid var(--line-soft);
    font-size: 15px;
    align-items: center;
  }
  .ph__link::after { display: none; }
  .ph__link-index { width: 36px; font-family: var(--font-mono); }
  .ph__link--cta {
    border-left: none;
    padding-left: 0;
    margin-top: var(--s-3);
  }
}

@media (max-width: 480px) {
  .ph__brand-name { font-size: 16px; }
  .ph__mark { width: 32px; height: 32px; }
}
</style>
