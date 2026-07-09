<template>
  <div class="page">
    <header class="page__header">
      <div class="container container--read">
        <router-link to="/" class="back">
          <el-icon :size="14"><ArrowLeft /></el-icon>
          <span>返回</span>
        </router-link>

        <div class="rubric page__rubric">
          <span>Reading · {{ readingTime }} min</span>
          <span class="rubric__dot"></span>
          <span>Methōdus · Journal</span>
        </div>

        <h1 class="page__title">{{ article.title || '正在载入…' }}</h1>

        <div class="meta">
          <span class="meta__item">
            <el-icon :size="13"><Clock /></el-icon>
            {{ formatDate(article.publishTime) }}
          </span>
          <span class="meta__sep" aria-hidden="true">·</span>
          <span class="meta__item">
            <el-icon :size="13"><View /></el-icon>
            {{ article.viewCount || 0 }} 次浏览
          </span>
          <span class="meta__sep" aria-hidden="true">·</span>
          <span class="meta__item meta__item--accent">
            <el-icon :size="13"><EditPen /></el-icon>
            By Methōdus Studio
          </span>
        </div>
      </div>
    </header>

    <section class="page__body">
      <div class="container container--read">
        <div class="rule" />
        <article class="content" v-html="article.content || '<p class=&quot;placeholder&quot;>正在载入内容…</p>'" />

        <div class="rule rule--double" />

        <div class="afterword">
          <div class="afterword__mark">M.</div>
          <p class="afterword__text">
            本文由 Methōdus 编辑团队整理发布。如有反馈或建议，欢迎通过平台内部通道与编辑部联系。
          </p>
        </div>

        <div class="nav-row">
          <button class="nav-btn" @click="goBack">
            <el-icon :size="14"><ArrowLeft /></el-icon>
            返回上一页
          </button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { ArrowLeft, Clock, View, EditPen } from '@element-plus/icons-vue'
import { detail } from '@/api/portal'

const route = useRoute()
const router = useRouter()
const article = ref({})

const formatDate = (t) => t ? dayjs(t).format('YYYY年M月D日 HH:mm') : ''
const readingTime = computed(() => {
  const len = (article.value.content || '').replace(/<[^>]+>/g, '').length
  return Math.max(1, Math.ceil(len / 400))
})
const goBack = () => router.back()

onMounted(async () => {
  try { article.value = (await detail(route.params.id)) || {} } catch (e) { article.value = {} }
})
</script>

<style scoped>
.page__header {
  padding: clamp(56px, 8vw, 96px) 0 clamp(32px, 5vw, 56px);
  background: linear-gradient(180deg, #FFFFFF 0%, #F4F7FF 100%);
  border-bottom: 1px solid var(--line);
}

.back {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-family: var(--font-mono);
  font-size: 11px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--mute);
  margin-bottom: var(--s-5);
  transition:
    color     var(--t-fast) var(--ease),
    transform var(--t-base) var(--ease);
}
.back:hover { color: var(--accent); transform: translateX(-3px); }

.page__rubric { margin-bottom: var(--s-4); }
.page__rubric .dot { width: 3px; height: 3px; background: var(--mute-soft); border-radius: 50%; }

.page__title {
  font-family: var(--font-display);
  font-size: clamp(32px, 5vw, 56px);
  font-weight: 600;
  line-height: 1.1;
  letter-spacing: -0.022em;
  margin: var(--s-3) 0 var(--s-5);
  color: var(--ink);
}

.meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  font-family: var(--font-mono);
  font-size: 11.5px;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--mute);
}
.meta__item { display: inline-flex; align-items: center; gap: 6px; }
.meta__item--accent { color: var(--accent); }
.meta__sep { color: var(--mute-soft); }

.page__body { padding: clamp(48px, 7vw, 80px) 0 clamp(96px, 14vw, 160px); }
.rule { height: 1px; background: var(--line); margin: var(--s-7) 0; border: 0; }
.rule--double {
  height: 4px;
  background:
    linear-gradient(to bottom, var(--line) 0 1px, transparent 1px 3px, var(--line) 3px 4px);
}

.content {
  font-family: var(--font-sans);
  font-size: 16px;
  line-height: 1.95;
  color: var(--ink-soft);
  letter-spacing: 0.005em;
}
.content :deep(h1), .content :deep(h2), .content :deep(h3), .content :deep(h4) {
  font-family: var(--font-display);
  color: var(--ink);
  margin: 2em 0 0.8em;
  letter-spacing: -0.018em;
  line-height: 1.3;
  font-weight: 600;
}
.content :deep(h2) { font-size: 28px; }
.content :deep(h3) { font-size: 22px; }
.content :deep(h4) { font-size: 18px; }
.content :deep(p)  { margin: 0 0 1.4em; }
.content :deep(blockquote) {
  margin: 2em 0;
  padding: 4px 0 4px 24px;
  border-left: 2px solid var(--accent);
  font-family: var(--font-serif);
  font-style: italic;
  font-size: 19px;
  color: var(--ink);
  line-height: 1.6;
}
.content :deep(ul), .content :deep(ol) { padding-left: 1.4em; margin: 0 0 1.4em; }
.content :deep(li) { margin: 0.4em 0; }
.content :deep(img) {
  margin: 2em auto;
  border: 1px solid var(--line);
}
.content :deep(a) {
  color: var(--accent);
  border-bottom: 1px solid var(--accent-tint);
  transition: border-color var(--t-fast) var(--ease);
}
.content :deep(a:hover) { border-bottom-color: var(--accent); }
.content :deep(code) {
  font-family: var(--font-mono);
  font-size: 0.9em;
  padding: 2px 6px;
  background: var(--accent-tint);
  color: var(--accent-deep);
}
.content :deep(pre) {
  background: #13192C;
  color: #D7DEEC;
  padding: 24px 28px;
  overflow-x: auto;
  font-family: var(--font-mono);
  font-size: 13.5px;
  line-height: 1.7;
}
.content :deep(.placeholder) { color: var(--mute); font-style: italic; }

.afterword {
  display: flex;
  align-items: flex-start;
  gap: var(--s-4);
  padding: var(--s-5) 0;
}
.afterword__mark {
  font-family: var(--font-serif);
  font-style: italic;
  font-size: 36px;
  color: var(--accent);
  line-height: 1;
  flex-shrink: 0;
}
.afterword__text {
  margin: 0;
  font-size: 13.5px;
  line-height: 1.75;
  color: var(--mute);
  font-style: italic;
  letter-spacing: 0.005em;
}

.nav-row { display: flex; justify-content: flex-start; margin-top: var(--s-7); }
.nav-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 13px 22px;
  background: transparent;
  border: 1px solid var(--line);
  color: var(--ink-soft);
  font-family: var(--font-sans);
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  cursor: pointer;
  transition:
    border-color var(--t-fast) var(--ease),
    color        var(--t-fast) var(--ease),
    background   var(--t-fast) var(--ease);
}
.nav-btn:hover {
  border-color: var(--accent);
  background: var(--accent);
  color: #fff;
}

@media (max-width: 640px) {
  .meta { font-size: 11px; }
}
</style>
