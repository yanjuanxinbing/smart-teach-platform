<template>
  <div class="page">
    <header class="page__header">
      <div class="container">
        <router-link to="/codex" class="back">
          <el-icon :size="13"><Back /></el-icon>
          <span>返回笔记库</span>
        </router-link>
        <div class="rubric page__rubric">
          <span>Section · 03 / Code Library</span>
          <span class="rubric__dot"></span>
          <span>教师评审入口</span>
        </div>
        <h1 class="page__title">学生<em>笔记</em></h1>
        <p class="page__lead">
          学生提交的笔记作品将集中展示在此，供教师评选优秀笔记。
        </p>
      </div>
    </header>

    <section class="page__body">
      <div class="container container--narrow">
        <div class="empty" data-reveal>
          <el-icon :size="48"><DocumentRemove /></el-icon>
          <h3 class="empty__title">当前无人提交笔记</h3>
          <p class="empty__hint">学生暂未向平台提交任何笔记作品，请耐心等待。</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { Back, DocumentRemove } from '@element-plus/icons-vue'
</script>

<style scoped>
.page__header { padding: clamp(64px, 9vw, 112px) 0 clamp(32px, 5vw, 64px); border-bottom: 1px solid var(--line); background: linear-gradient(180deg, #FFFFFF 0%, #F4F7FF 100%); }
.back { display: inline-flex; align-items: center; gap: 6px; font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.18em; text-transform: uppercase; color: var(--mute); margin-bottom: var(--s-6); }
.back:hover { color: var(--accent); }
.page__rubric { margin-bottom: var(--s-6); }
.rubric__dot { display: inline-block; width: 4px; height: 4px; background: var(--mute-soft); border-radius: 50%; margin: 0 10px; vertical-align: middle; }
.page__title { font-family: var(--font-display); font-size: clamp(40px, 6vw, 72px); font-weight: 600; line-height: 1.04; letter-spacing: -0.025em; margin: 0 0 var(--s-4); }
.page__title em { font-family: var(--font-serif); font-style: italic; font-weight: 500; color: var(--accent); }
.page__lead { max-width: 56ch; font-size: 15.5px; line-height: 1.75; color: var(--ink-soft); margin: 0; }

.page__body { padding: clamp(48px, 7vw, 88px) 0; }

.empty {
  padding: 96px 24px;
  border: 1px solid var(--line);
  background: var(--surface);
  text-align: center;
  color: var(--mute);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
}
.empty__title { margin: 0; font-family: var(--font-display); font-size: 22px; font-weight: 600; color: var(--ink); letter-spacing: -0.01em; }
.empty__hint  { margin: 0; font-size: 13.5px; color: var(--mute); letter-spacing: 0.04em; }
</style>