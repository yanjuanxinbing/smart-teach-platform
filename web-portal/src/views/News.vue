<template>
  <div class="page">
    <header class="page__header">
      <div class="container">
        <div class="rubric page__rubric">
          <span>Section · 03 / Journal</span>
          <span class="rubric__dot"></span>
          <span>{{ list.length }} entries</span>
          <span class="rubric__dot"></span>
          <span>Curated &amp; indexed</span>
        </div>
        <h1 class="page__title">
          平台<em>资讯</em>
        </h1>
        <p class="page__lead">
          关于教学法、课程进展与团队动态的记录——一份安静、可信的内部日志。
        </p>
      </div>
    </header>

    <section class="page__body">
      <div class="container">
        <header class="toolbar">
          <span class="toolbar__count">
            <el-icon :size="12"><Reading /></el-icon>
            共 <em>{{ list.length }}</em> 篇资讯
          </span>
          <span class="toolbar__hint">最新优先 · 按发布日期倒序</span>
        </header>

        <div class="news-grid">
          <article
            v-for="(item, idx) in list"
            :key="item.id"
            class="news"
            data-reveal
            :style="{ transitionDelay: `${Math.min(idx, 8) * 60}ms` }"
            @click="goArticle(item)"
          >
            <div class="news__cover">
              <img v-if="item.coverImage" :src="item.coverImage" :alt="item.title" />
              <div v-else class="news__plate">
                <span class="news__plate-num">{{ String(idx + 1).padStart(2, '0') }}</span>
                <el-icon :size="22"><Picture /></el-icon>
              </div>
              <div class="news__tag">
                <el-icon :size="11"><Clock /></el-icon>
                {{ formatDate(item.publishTime) }}
              </div>
            </div>
            <div class="news__body">
              <h3 class="news__title">{{ item.title }}</h3>
              <p class="news__excerpt">{{ stripHtml(item.content) }}</p>
              <div class="news__foot">
                <span class="news__more">
                  阅读全文
                  <el-icon :size="12"><ArrowRight /></el-icon>
                </span>
                <span class="news__meta">By Methōdus</span>
              </div>
            </div>
          </article>
        </div>

        <div v-if="!list.length" class="empty" data-reveal>
          <el-icon :size="28"><Picture /></el-icon>
          <p>暂无资讯</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { ArrowRight, Clock, Picture, Reading } from '@element-plus/icons-vue'
import { listByType } from '@/api/portal'

const router = useRouter()
const list = ref([])

const formatDate = (t) => t ? dayjs(t).format('YYYY.MM.DD') : ''
const stripHtml = (s) => s ? s.replace(/<[^>]+>/g, '').slice(0, 130) + '…' : '暂无摘要'
const goArticle = (item) => router.push(`/article/${item.id}`)

onMounted(async () => {
  try { list.value = (await listByType(3, 50)) || [] } catch (e) { list.value = [] }
})
</script>

<style scoped>
.page__header {
  padding: clamp(64px, 9vw, 112px) 0 clamp(32px, 5vw, 64px);
  border-bottom: 1px solid var(--line);
  background:
    linear-gradient(180deg, #FFFFFF 0%, #F4F7FF 100%);
}
.page__rubric { margin-bottom: var(--s-6); }
.page__rubric .dot { width: 3px; height: 3px; background: var(--mute-soft); border-radius: 50%; }

.page__title {
  font-family: var(--font-display);
  font-size: clamp(40px, 6vw, 72px);
  font-weight: 600;
  line-height: 1.04;
  letter-spacing: -0.025em;
  margin: 0 0 var(--s-4);
}
.page__title em {
  font-family: var(--font-serif);
  font-style: italic;
  font-weight: 500;
  color: var(--accent);
}
.page__lead {
  max-width: 56ch;
  font-size: 15.5px;
  line-height: 1.75;
  color: var(--ink-soft);
  margin: 0;
}

.page__body { padding: clamp(56px, 8vw, 96px) 0; }

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--s-4);
  padding-bottom: var(--s-4);
  margin-bottom: var(--s-6);
  border-bottom: 1px solid var(--line);
}
.toolbar__count {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--mute);
}
.toolbar__count em {
  font-style: normal;
  color: var(--accent);
  font-weight: 500;
  font-family: var(--font-mono);
}
.toolbar__hint {
  font-family: var(--font-mono);
  font-size: 11px;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--mute-light);
}

.news-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: clamp(24px, 3vw, 40px);
}
.news {
  display: flex;
  flex-direction: column;
  background: var(--surface);
  border: 1px solid var(--line);
  cursor: pointer;
  overflow: hidden;
  transition:
    transform    var(--t-base) var(--ease),
    border-color var(--t-base) var(--ease),
    box-shadow   var(--t-base) var(--ease);
}
.news:hover {
  transform: translateY(-3px);
  border-color: var(--accent);
  box-shadow: var(--shadow-blue);
}

.news__cover {
  position: relative;
  aspect-ratio: 4 / 3;
  background: linear-gradient(135deg, #F4F7FF 0%, #DEE6FF 100%);
  overflow: hidden;
}
.news__cover img {
  width: 100%; height: 100%;
  object-fit: cover;
  transition: transform var(--t-slow) var(--ease);
}
.news:hover .news__cover img { transform: scale(1.04); }

.news__plate {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 12px;
  color: var(--mute-soft);
}
.news__plate-num {
  font-family: var(--font-display);
  font-size: 64px;
  font-weight: 600;
  color: var(--accent);
  letter-spacing: -0.025em;
  line-height: 1;
}

.news__tag {
  position: absolute;
  top: 14px; left: 14px;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 6px 10px;
  background: rgba(244, 247, 255, 0.94);
  font-family: var(--font-mono);
  font-size: 11px;
  letter-spacing: 0.06em;
  color: var(--ink-soft);
}

.news__body {
  padding: 24px 24px 22px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
}
.news__title {
  font-family: var(--font-display);
  font-size: 21px;
  font-weight: 600;
  color: var(--ink);
  line-height: 1.35;
  letter-spacing: -0.012em;
  margin: 0;
  transition: color var(--t-fast) var(--ease);
}
.news:hover .news__title { color: var(--accent); }
.news__excerpt {
  margin: 0;
  font-size: 13.5px;
  line-height: 1.7;
  color: var(--ink-soft);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.news__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
  padding-top: 14px;
  border-top: 1px solid var(--line-soft);
}
.news__more {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--accent);
  transition: gap var(--t-base) var(--ease);
}
.news:hover .news__more { gap: 12px; }
.news__meta {
  font-family: var(--font-mono);
  font-size: 11px;
  letter-spacing: 0.06em;
  color: var(--mute-light);
  text-transform: uppercase;
}

.empty {
  padding: 96px 0;
  text-align: center;
  color: var(--mute);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
}
.empty p { margin: 0; font-size: 13px; letter-spacing: 0.06em; }

@media (max-width: 1000px) {
  .news-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
@media (max-width: 640px) {
  .news-grid { grid-template-columns: 1fr; }
}
</style>

