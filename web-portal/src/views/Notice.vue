<template>
  <div class="page">
    <header class="page__header">
      <div class="container">
        <div class="rubric page__rubric">
          <span>Section · 02 / Notices</span>
          <span class="rubric__dot"></span>
          <span>{{ list.length }} entries</span>
          <span class="rubric__dot"></span>
          <span>Reverse chronological</span>
        </div>
        <h1 class="page__title">
          平台<em>通告</em>
        </h1>
        <p class="page__lead">
          按时间倒序排列的重要公告与日常通知——一切需要被团队成员认真阅读的内容。
        </p>
      </div>
    </header>

    <section class="page__body">
      <div class="container container--narrow">
        <header class="toolbar">
          <span class="toolbar__count">
            <el-icon :size="12"><BellFilled /></el-icon>
            共 <em>{{ list.length }}</em> 条通告
          </span>
          <span class="toolbar__hint">最新优先 · 按发布日期倒序</span>
        </header>

        <ul class="notice-list">
          <li
            v-for="(item, idx) in list"
            :key="item.id"
            class="row"
            data-reveal
            :style="{ transitionDelay: `${Math.min(idx, 6) * 60}ms` }"
            @click="goArticle(item)"
          >
            <div class="row__date">
              <span class="row__day">{{ dayOf(item.publishTime) }}</span>
              <span class="row__ym">{{ ym(item.publishTime) }}</span>
            </div>
            <div class="row__body">
              <span class="row__pin">通告</span>
              <h3 class="row__title">{{ item.title }}</h3>
              <p class="row__excerpt">{{ stripHtml(item.content) }}</p>
            </div>
            <el-icon class="row__arrow" :size="18"><ArrowRight /></el-icon>
          </li>
        </ul>

        <div v-if="!list.length" class="empty" data-reveal>
          <el-icon :size="28"><BellFilled /></el-icon>
          <p>暂无通告</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { ArrowRight, BellFilled } from '@element-plus/icons-vue'
import { listByType } from '@/api/portal'

const router = useRouter()
const list = ref([])

const dayOf = (t) => t ? dayjs(t).format('DD') : '--'
const ym = (t) => t ? dayjs(t).format('YYYY.MM') : '----.--'
const stripHtml = (s) => s ? s.replace(/<[^>]+>/g, '').slice(0, 160) + '…' : '暂无摘要'
const goArticle = (item) => router.push(`/article/${item.id}`)

onMounted(async () => {
  try { list.value = (await listByType(2, 50)) || [] } catch (e) { list.value = [] }
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
  margin-bottom: var(--s-3);
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

.notice-list { list-style: none; margin: 0; padding: 0; }

.row {
  display: grid;
  grid-template-columns: 88px 1fr 24px;
  gap: var(--s-6);
  align-items: flex-start;
  padding: 28px 8px 28px 0;
  border-bottom: 1px solid var(--line-soft);
  cursor: pointer;
  transition:
    padding-left       var(--t-base) var(--ease),
    background-color  var(--t-fast) var(--ease);
}
.row:hover {
  padding-left: 14px;
  background: linear-gradient(to right, #F4F7FF, transparent 60%);
}
.row__date {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding-top: 4px;
}
.row__day {
  font-family: var(--font-display);
  font-size: 36px;
  font-weight: 600;
  color: var(--ink);
  line-height: 1;
  letter-spacing: -0.025em;
}
.row__ym {
  margin-top: 6px;
  font-family: var(--font-mono);
  font-size: 11px;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--mute);
}
.row__body { min-width: 0; }
.row__pin {
  display: inline-block;
  font-family: var(--font-mono);
  font-size: 10.5px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--accent);
  margin-bottom: 6px;
}
.row__title {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 600;
  color: var(--ink);
  margin: 0 0 8px;
  letter-spacing: -0.012em;
  line-height: 1.3;
  transition: color var(--t-fast) var(--ease);
}
.row:hover .row__title { color: var(--accent); }
.row__excerpt {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  color: var(--ink-soft);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.row__arrow {
  margin-top: 10px;
  color: var(--mute-soft);
  transition: transform var(--t-base) var(--ease), color var(--t-fast) var(--ease);
}
.row:hover .row__arrow { color: var(--accent); transform: translateX(4px); }

.empty {
  padding: 88px 0;
  text-align: center;
  color: var(--mute);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
}
.empty p { margin: 0; font-size: 13px; letter-spacing: 0.06em; }

@media (max-width: 720px) {
  .row {
    grid-template-columns: 64px 1fr 18px;
    gap: var(--s-4);
    padding: 22px 4px;
  }
  .row:hover { padding-left: 6px; }
  .row__day { font-size: 28px; }
  .row__title { font-size: 18px; }
}
</style>

