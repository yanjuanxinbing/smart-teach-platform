<template>
  <div class="page">
    <header class="page__hero">
      <div class="container hero">
        <router-link to="/course" class="hero__back">
          <el-icon :size="13"><Back /></el-icon>
          <span>返回课程中心</span>
        </router-link>
        <div class="hero__meta">
          <span class="chip chip--accent">{{ course?.category || '课程' }}</span>
          <span class="chip">{{ course?.code || '--' }}</span>
          <span class="chip">{{ course?.level || '入门' }}</span>
          <span class="chip">{{ course?.hours || 48 }} 学时</span>
        </div>
        <h1 class="hero__title">{{ course?.title || course?.name || '课程详情' }}</h1>
        <p class="hero__lead">{{ course?.summary || course?.description || '—' }}</p>
        <div class="hero__foot">
          <span class="hero__teacher">主讲：<b>{{ course?.teacherName || '主讲教师' }}</b></span>
          <el-button v-if="userStore.isLogin && !enrolled" type="primary" class="hero__cta" :loading="enrolling" @click="enroll">加入我的学习</el-button>
          <el-button v-if="enrolled" plain class="hero__cta">已加入</el-button>
          <el-button v-if="!userStore.isLogin" type="primary" class="hero__cta" @click="gotoLogin">登录后选课</el-button>
        </div>
      </div>
    </header>

    <section class="page__body">
      <div class="container two">
        <article class="two__main">
          <section class="card">
            <header class="card__head">
              <span class="eyebrow"><span class="eyebrow__rule"></span>课程介绍</span>
              <h3 class="card__title">课程<em>概要</em></h3>
            </header>
            <div class="card__body rich" v-html="course?.description || '本课程暂无简介'"></div>
          </section>

          <section class="card">
            <header class="card__head">
              <span class="eyebrow"><span class="eyebrow__rule"></span>章节与内容</span>
              <h3 class="card__title">章节<em>列表</em></h3>
            </header>
            <ul class="chapters">
              <li v-for="(ch, i) in chapters" :key="ch.id" class="chapter" :class="{ 'chapter--active': activeChapter === ch.id }">
                <button class="chapter__row" @click="toggle(ch)">
                  <span class="chapter__idx">{{ String(i + 1).padStart(2, '0') }}</span>
                  <span class="chapter__name">{{ ch.title }}</span>
                  <span class="chapter__meta">{{ ch.duration || '' }}{{ ch.resourceCount ? ' · ' + ch.resourceCount + ' 资料' : '' }}</span>
                  <el-icon class="chapter__toggle" :size="14"><ArrowDown v-if="activeChapter === ch.id" /><ArrowRight v-else /></el-icon>
                </button>
                <transition name="fade">
                  <div v-if="activeChapter === ch.id && (ch.children?.length || ch.content)" class="chapter__panel">
                    <p v-if="ch.content" class="chapter__desc">{{ ch.content }}</p>
                    <ul v-if="ch.children?.length" class="chapter__sub">
                      <li v-for="sub in ch.children" :key="sub.id">
                        <span class="chapter__sub-idx">·</span>
                        <span>{{ sub.title }}</span>
                      </li>
                    </ul>
                    <p v-else-if="!ch.content" class="chapter__desc">本章暂无详细内容。</p>
                  </div>
                </transition>
              </li>
              <li v-if="!chapters.length" class="chapter chapter--empty">章节待发布</li>
            </ul>
          </section>
        </article>

        <aside class="two__side">
          <section class="card card--floating">
            <header class="card__head">
              <span class="eyebrow"><span class="eyebrow__rule"></span>课程信息</span>
              <h3 class="card__title">课程<em>速览</em></h3>
            </header>
            <ul class="kv">
              <li><span>课程编号</span><b>{{ course?.code || '--' }}</b></li>
              <li><span>课程性质</span><b>{{ course?.category || '课程' }}</b></li>
              <li><span>学时</span><b>{{ course?.hours || '--' }}</b></li>
              <li><span>难度</span><b>{{ course?.level || '入门' }}</b></li>
              <li><span>开课学期</span><b>{{ course?.term || '2025春' }}</b></li>
              <li><span>选课人数</span><b>{{ course?.studentCount || 0 }}</b></li>
            </ul>
          </section>

          <section class="card">
            <header class="card__head">
              <span class="eyebrow"><span class="eyebrow__rule"></span>主讲教师</span>
              <h3 class="card__title">{{ course?.teacherName || '主讲教师' }}</h3>
            </header>
            <p class="card__body">{{ course?.teacherTitle || '教研经验丰富的教师，长期承担本课程教学任务。' }}</p>
          </section>
        </aside>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back, ArrowDown, ArrowRight } from '@element-plus/icons-vue'
import { courseDetail, courseChapters } from '@/api/course'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const course = ref(null)
const chapters = ref([])
const activeChapter = ref(null)
const enrolled = ref(false)
const enrolling = ref(false)

const gotoLogin = () => router.push('/login?redirect=/course/' + route.params.id)

const toggle = (ch) => { activeChapter.value = activeChapter.value === ch.id ? null : ch.id }
const enroll = async () => {
  enrolling.value = true
  try { await new Promise(r => setTimeout(r, 400)); enrolled.value = true; ElMessage.success('已加入我的学习') }
  finally { enrolling.value = false }
}

const load = async (id) => {
  try {
    const [c, chs] = await Promise.all([
      courseDetail(id),
      courseChapters(id).catch(() => [])
    ])
    course.value = c
    chapters.value = chs || []
  } catch (e) {
    course.value = { id, title: '课程详情', code: '—', level: '入门', hours: 48 }
    chapters.value = []
  }
}

onMounted(() => load(route.params.id))
watch(() => route.params.id, (id) => load(id))
</script>

<style scoped>
.page__hero { padding: clamp(48px, 7vw, 96px) 0 clamp(36px, 5vw, 72px); border-bottom: 1px solid var(--line); background: linear-gradient(180deg, #fff 0%, #F4F7FF 100%); }
.hero__back { display: inline-flex; align-items: center; gap: 6px; font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.18em; text-transform: uppercase; color: var(--mute); margin-bottom: var(--s-6); }
.hero__back:hover { color: var(--accent); }
.hero__meta { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: var(--s-4); }
.chip { font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.08em; text-transform: uppercase; padding: 5px 10px; background: var(--surface); border: 1px solid var(--line); color: var(--ink-soft); }
.chip--accent { color: var(--accent); border-color: var(--accent); }
.hero__title { font-family: var(--font-display); font-size: clamp(34px, 4.6vw, 60px); font-weight: 600; color: var(--ink); margin: 0 0 var(--s-4); letter-spacing: -0.02em; line-height: 1.1; }
.hero__lead { font-size: 16px; line-height: 1.75; color: var(--ink-soft); max-width: 64ch; margin: 0 0 var(--s-6); }
.hero__foot { display: flex; flex-wrap: wrap; align-items: center; gap: var(--s-4); }
.hero__teacher { font-size: 14px; color: var(--ink-soft); }
.hero__teacher b { color: var(--ink); font-weight: 600; }
.hero__cta { border-radius: 0; height: 40px; padding: 0 18px; }
.hero__cta, .hero__cta:hover { background: var(--accent); border-color: var(--accent); color: #fff; }

.page__body { padding: clamp(48px, 7vw, 88px) 0; }
.two { display: grid; grid-template-columns: minmax(0, 1.7fr) minmax(0, 1fr); gap: clamp(20px, 3vw, 36px); align-items: start; }

.card { background: var(--surface); border: 1px solid var(--line); padding: clamp(20px, 2.6vw, 32px); margin-bottom: var(--s-5); transition: border-color var(--t-base) var(--ease); }
.card:hover, .card--floating:hover { border-color: var(--accent); }
.card__head { margin-bottom: var(--s-5); }
.card__title { font-family: var(--font-display); font-size: 22px; font-weight: 600; color: var(--ink); margin: 4px 0 0; letter-spacing: -0.012em; }
.card__title em { font-family: var(--font-serif); font-style: italic; font-weight: 500; color: var(--accent); }
.card__body { font-size: 14.5px; line-height: 1.85; color: var(--ink-soft); }
.card__body.rich :deep(p) { margin: 0 0 14px; }
.card__body.rich :deep(ul), .card__body.rich :deep(ol) { padding-left: 22px; }
.eyebrow { display: inline-flex; align-items: center; gap: 8px; font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.18em; text-transform: uppercase; color: var(--mute); }
.eyebrow__rule { display: inline-block; width: 26px; height: 1px; background: var(--accent); }

.chapters { list-style: none; margin: 0; padding: 0; border-top: 1px solid var(--line); }
.chapter { border-bottom: 1px solid var(--line); }
.chapter__row {
  width: 100%; display: grid; grid-template-columns: 48px 1fr auto 16px;
  align-items: center; gap: var(--s-4);
  padding: 16px 4px; background: transparent; border: 0; text-align: left; cursor: pointer;
  font-size: 15px; color: var(--ink);
}
.chapter__idx { font-family: var(--font-mono); font-size: 12px; color: var(--mute); }
.chapter__name { font-weight: 500; }
.chapter__meta { font-family: var(--font-mono); font-size: 11px; color: var(--mute); letter-spacing: 0.08em; text-transform: uppercase; }
.chapter__toggle { color: var(--mute); transition: transform var(--t-base) var(--ease); }
.chapter--active .chapter__name { color: var(--accent); }
.chapter__panel { padding: 0 4px 16px 56px; }
.chapter__desc { margin: 0; font-size: 13.5px; line-height: 1.75; color: var(--ink-soft); }
.chapter__sub { list-style: none; margin: 12px 0 0; padding: 0; display: flex; flex-direction: column; gap: 8px; }
.chapter__sub li { font-size: 13.5px; color: var(--ink-soft); display: flex; gap: 8px; }
.chapter__sub-idx { color: var(--accent); }
.chapter--empty { padding: 24px 4px; color: var(--mute); font-size: 13px; }

.kv { list-style: none; margin: 0; padding: 0; }
.kv li {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 0; border-bottom: 1px solid var(--line-soft);
  font-family: var(--font-mono); font-size: 13px;
}
.kv li:last-child { border-bottom: none; }
.kv span { color: var(--mute); letter-spacing: 0.08em; text-transform: uppercase; font-size: 11px; }
.kv b { color: var(--ink); font-weight: 500; }

.two__side .card--floating { position: sticky; top: calc(var(--header-h) + 16px); }
.fade-enter-active, .fade-leave-active { transition: opacity var(--t-base) var(--ease); }
.fade-enter-from, .fade-leave-to { opacity: 0; }

@media (max-width: 1024px) { .two { grid-template-columns: 1fr; } .two__side .card--floating { position: static; } }
</style>
