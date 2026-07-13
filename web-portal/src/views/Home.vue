<template>
  <div class="home">

    <!-- ============================== HERO ============================== -->
    <section class="hero">
      <div class="hero__aurora" aria-hidden="true">
        <div class="hero__aurora-blob hero__aurora-blob--1"></div>
        <div class="hero__aurora-blob hero__aurora-blob--2"></div>
      </div>
      <div class="container">
        <div class="hero__grid">

          <div class="hero__copy">
            <div class="rubric hero__rubric">
              <span>Issue · {{ issue }}</span>
              <span class="rubric__dot"></span>
              <span>CN&nbsp;/&nbsp;ZH</span>
              <span class="rubric__dot"></span>
              <span>Studio No.&nbsp;{{ issueNo }}</span>
            </div>

            <h1 class="hero__title">
              智能化在线教学<br />
              <em>支持服务平台</em>
            </h1>

            <p class="hero__lead">
              面向高校与培训机构的教研协作中枢，将课程组织、实验实训、资源归档与师生协同凝练为克制、可信、可被监管的工作环境。
            </p>

            <div class="hero__actions">
              <router-link to="/news" class="btn btn--primary">
                进入资讯中心
                <el-icon class="icon" :size="14"><ArrowRight /></el-icon>
              </router-link>
              <a href="http://localhost:8081" target="_blank" rel="noopener" class="btn">
                <el-icon class="icon" :size="14"><Position /></el-icon>
                管理中心入口
              </a>
            </div>

            <div class="hero__index">
              <a href="#focus" class="hero__index-item">
                <span class="hero__index-key">01</span>
                <span class="hero__index-label">本期焦点</span>
              </a>
              <a href="#bullets" class="hero__index-item">
                <span class="hero__index-key">02</span>
                <span class="hero__index-label">数据面板</span>
              </a>
              <a href="#latest" class="hero__index-item">
                <span class="hero__index-key">03</span>
                <span class="hero__index-label">最新动态</span>
              </a>
              <a href="#method" class="hero__index-item">
                <span class="hero__index-key">04</span>
                <span class="hero__index-label">工作方式</span>
              </a>
              <a href="http://localhost:8081" target="_blank" rel="noopener" class="hero__index-item">
                <span class="hero__index-key">05</span>
                <span class="hero__index-label">管理入口</span>
              </a>
            </div>
          </div>

          <aside class="hero__panel" aria-label="管理中心预览">
            <div class="panel__head">
              <div class="panel__head-left">
                <span class="panel__dot"></span>
                <span class="panel__dot"></span>
                <span class="panel__dot"></span>
                <span class="panel__title">Methōdus · Control&nbsp;Plane</span>
              </div>
              <span class="panel__pill">
                <span class="panel__pulse" aria-hidden="true"></span>
                Live
              </span>
            </div>

            <div class="panel__body">
              <div class="panel__row panel__row--kpi">
                <div class="kpi">
                  <div class="kpi__label">活跃课程</div>
                  <div class="kpi__value"><span ref="kpiCourses">0</span><span class="kpi__suffix">+</span></div>
                </div>
                <div class="kpi">
                  <div class="kpi__label">协同教研组</div>
                  <div class="kpi__value"><span ref="kpiGroups">0</span></div>
                </div>
                <div class="kpi">
                  <div class="kpi__label">资源归档</div>
                  <div class="kpi__value"><span ref="kpiResources">0</span><span class="kpi__suffix">+</span></div>
                </div>
                <div class="kpi">
                  <div class="kpi__label">服务可用</div>
                  <div class="kpi__value"><span ref="kpiUptime">0</span><span class="kpi__unit">%</span></div>
                </div>
              </div>

              <div class="panel__sub">今日 · 教研安排</div>
              <ul class="panel__list">
                <li v-for="(item, i) in schedule" :key="i" class="panel__item">
                  <span class="panel__time">{{ item.time }}</span>
                  <span class="panel__title-row">{{ item.title }}</span>
                  <span class="panel__tag" :class="`is-${item.kind}`">{{ item.kindLabel }}</span>
                </li>
              </ul>

              <div class="panel__sub">最近 · 操作动态</div>
              <ul class="panel__feed">
                <li v-for="(f, i) in feed" :key="i">
                  <span class="panel__feed-time">{{ f.time }}</span>
                  <span class="panel__feed-text">{{ f.text }}</span>
                </li>
              </ul>
            </div>
          </aside>

        </div>
      </div>
    </section>

    <!-- ============================== MARQUEE ============================== -->
    <div class="ticker">
      <div class="ticker__track">
        <span v-for="(t, i) in tickerDoubled" :key="i" class="ticker__item">
          <span class="ticker__dot" aria-hidden="true"></span>
          {{ t }}
        </span>
      </div>
    </div>

    <!-- ============================== STATS ============================== -->
    <section class="stats" id="bullets">
      <div class="container">
        <div class="stats__grid">
          <div v-for="(s, i) in stats" :key="i" class="stats__item" data-reveal :style="{ transitionDelay: `${i * 60}ms` }">
            <div class="stats__index">{{ String(i + 1).padStart(2, '0') }}</div>
            <div class="stats__value">
              <span class="stats__num">{{ s.value }}</span>
              <span class="stats__suffix">{{ s.suffix }}</span>
            </div>
            <div class="stats__label">{{ s.label }}</div>
            <div class="stats__caption">{{ s.caption }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- ============================== FOCUS / CAROUSEL ============================== -->
    <section class="section section--tight" id="focus">
      <div class="container">
        <header class="masthead" data-reveal>
          <div>
            <div class="eyebrow eyebrow--accent">
              <span class="eyebrow__rule"></span>
              No. 01 / Focus
            </div>
            <h2 class="masthead__title">本期<em>焦点</em>与最新通告</h2>
          </div>
          <router-link to="/news" class="masthead__rule">
            浏览全部
            <el-icon :size="12"><ArrowRight /></el-icon>
          </router-link>
        </header>

        <div class="carousel" @mouseenter="carouselPaused = true" @mouseleave="carouselPaused = false">
          <div class="carousel__viewport">
            <div
              v-for="(b, i) in banners"
              :key="b.id || i"
              class="carousel__slide"
              :class="{ 'is-active': i === carouselIndex, 'is-clickable': !!b.linkUrl || !!b.id }"
              role="link"
              tabindex="0"
              @click="goBannerClick(b)"
              @keyup.enter="goBannerClick(b)"
            >
              <div class="carousel__media">
                <img v-if="b.coverImage" :src="b.coverImage" :alt="b.title" class="carousel__img" />
                <div v-else class="carousel__placeholder" :class="`is-tone-${i % 4}`">
                  <span class="carousel__placeholder-num">{{ String(i + 1).padStart(2, '0') }}</span>
                  <span class="carousel__placeholder-label">{{ b.title || '暂无标题' }}</span>
                </div>
                <div class="carousel__media-overlay" aria-hidden="true"></div>
              </div>
              <div class="carousel__copy">
                <div class="carousel__meta">
                  <span class="eyebrow eyebrow--accent">
                    <span class="eyebrow__rule"></span>
                    焦点 · {{ String(i + 1).padStart(2, '0') }} / {{ String(banners.length).padStart(2, '0') }}
                  </span>
                  <span class="carousel__date">{{ formatDate(b.publishTime) }}</span>
                </div>
                <h3 class="carousel__title">
                  {{ b.title || '暂无标题' }}
                </h3>
                <p class="carousel__desc">{{ stripHtml(b.content) }}</p>
                <div class="carousel__actions">
                  <a class="btn btn--primary" @click.stop="goBannerClick(b)" role="button">
                    {{ b.linkUrl ? '查看详情' : '阅读全文' }}
                    <el-icon class="icon" :size="14"><ArrowRight /></el-icon>
                  </a>
                  <router-link to="/news" class="btn btn--ghost" @click.stop>查看资讯</router-link>
                </div>
              </div>
            </div>

            <div v-if="!banners.length" class="carousel__empty">
              <el-icon :size="28"><Picture /></el-icon>
              <p>暂无轮播内容</p>
            </div>
          </div>

          <div class="carousel__nav">
            <div class="carousel__dots" role="tablist">
              <button
                v-for="(b, i) in banners"
                :key="'d' + (b.id || i)"
                class="carousel__dot"
                :class="{ 'is-active': i === carouselIndex }"
                :aria-label="`第 ${i + 1} 张`"
                @click="goBanner(i)"
              >
                <span class="carousel__dot-fill"></span>
              </button>
            </div>
            <div class="carousel__counter">
              <span class="carousel__counter-cur">{{ String(carouselIndex + 1).padStart(2, '0') }}</span>
              <span class="carousel__counter-sep">/</span>
              <span class="carousel__counter-tot">{{ String(banners.length || 1).padStart(2, '0') }}</span>
            </div>
            <div class="carousel__arrows">
              <button class="carousel__arrow" @click="prevBanner" aria-label="上一张">
                <el-icon :size="16"><ArrowLeft /></el-icon>
              </button>
              <button class="carousel__arrow" @click="nextBanner" aria-label="下一张">
                <el-icon :size="16"><ArrowRight /></el-icon>
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ============================== DUAL: NOTICES + NEWS ============================== -->
    <section class="section section--tight" id="latest">
      <div class="container">
        <div class="dual">

          <div class="dual__col">
            <header class="masthead masthead--compact" data-reveal>
              <div>
                <div class="eyebrow eyebrow--accent">
                  <span class="eyebrow__rule"></span>
                  No. 02 / Bulletin
                </div>
                <h2 class="masthead__title">最新<em>通告</em></h2>
              </div>
              <router-link to="/notice" class="masthead__rule">
                全部通告 · {{ noticeCount }}
                <el-icon :size="12"><ArrowRight /></el-icon>
              </router-link>
            </header>

            <ul class="notices">
              <li
                v-for="(n, idx) in notices.slice(0, 5)"
                :key="n.id"
                class="notice"
                data-reveal
                :style="{ transitionDelay: `${idx * 60}ms` }"
                @click="goArticle(n)"
              >
                <div class="notice__meta">
                  <span class="notice__date">{{ formatShortDate(n.publishTime) }}</span>
                  <span class="notice__pin">通告</span>
                </div>
                <div class="notice__body">
                  <h3 class="notice__title">{{ n.title }}</h3>
                  <p class="notice__excerpt">{{ stripHtml(n.content) }}</p>
                </div>
                <el-icon class="notice__arrow" :size="16"><ArrowRight /></el-icon>
              </li>
              <li v-if="!notices.length" class="empty" data-reveal>
                <el-icon :size="22"><BellFilled /></el-icon>
                <p>暂无通告</p>
              </li>
            </ul>
          </div>

          <div class="dual__col">
            <header class="masthead masthead--compact" data-reveal>
              <div>
                <div class="eyebrow eyebrow--accent">
                  <span class="eyebrow__rule"></span>
                  No. 03 / Journal
                </div>
                <h2 class="masthead__title">最新<em>资讯</em></h2>
              </div>
              <router-link to="/news" class="masthead__rule">
                全部资讯 · {{ newsCount }}
                <el-icon :size="12"><ArrowRight /></el-icon>
              </router-link>
            </header>

            <div class="cards">
              <article
                v-for="(n, idx) in news.slice(0, 3)"
                :key="n.id"
                class="card"
                data-reveal
                :style="{ transitionDelay: `${idx * 80}ms` }"
                @click="goArticle(n)"
              >
                <div class="card__cover">
                  <img v-if="n.coverImage" :src="n.coverImage" :alt="n.title" />
                  <div v-else class="card__plate">
                    <span class="card__plate-num">{{ String(idx + 1).padStart(2, '0') }}</span>
                    <el-icon :size="20"><Picture /></el-icon>
                  </div>
                  <span class="card__tag">
                    <el-icon :size="11"><Clock /></el-icon>
                    {{ formatDate(n.publishTime) }}
                  </span>
                </div>
                <div class="card__body">
                  <h3 class="card__title">{{ n.title }}</h3>
                  <p class="card__excerpt">{{ stripHtml(n.content) }}</p>
                  <span class="card__more">
                    <span>阅读全文</span>
                    <el-icon :size="12"><ArrowRight /></el-icon>
                  </span>
                </div>
              </article>
              <div v-if="!news.length" class="empty" data-reveal>
                <el-icon :size="22"><Picture /></el-icon>
                <p>暂无资讯</p>
              </div>
            </div>
          </div>

        </div>
      </div>
    </section>

    <!-- ============================== METHOD / WORKFLOW ============================== -->
    <section class="section section--dark" id="method">
      <div class="container">
        <header class="masthead masthead--inverse" data-reveal>
          <div>
            <div class="eyebrow eyebrow--accent">
              <span class="eyebrow__rule"></span>
              No. 04 / Workflow
            </div>
            <h2 class="masthead__title">一段克制的<em>教研工作流</em></h2>
          </div>
          <span class="masthead__rule">From plan to reflection · 五个节拍</span>
        </header>

        <ol class="workflow">
          <li v-for="(w, i) in workflow" :key="i" class="step" data-reveal :style="{ transitionDelay: `${i * 100}ms` }">
            <span class="step__num">0{{ i + 1 }}</span>
            <h4 class="step__title">{{ w.title }}</h4>
            <p class="step__desc">{{ w.desc }}</p>
          </li>
        </ol>
      </div>
    </section>

    <!-- ============================== CTA ============================== -->
    <section class="section section--tight">
      <div class="container container--narrow">
        <div class="cta" data-reveal>
          <div class="eyebrow eyebrow--accent">
            <span class="eyebrow__rule"></span>
            Begin
          </div>
          <h2 class="cta__title">
            让每一节课，<br /><em>都值得被认真对待。</em>
          </h2>
          <p class="cta__lead">
            登录管理中心，开始一段安静而专注的教研旅程。
          </p>
          <div class="cta__actions">
            <a href="http://localhost:8081" target="_blank" rel="noopener" class="btn btn--accent">
              进入管理中心
              <el-icon class="icon" :size="14"><ArrowRight /></el-icon>
            </a>
            <router-link to="/news" class="btn btn--ghost">先浏览资讯</router-link>
          </div>
        </div>
      </div>
    </section>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { ArrowRight, ArrowLeft, BellFilled, Clock, Picture, Position } from '@element-plus/icons-vue'
import { listByType } from '@/api/portal'

const router = useRouter()
const issue = computed(() => dayjs().format('YYYY.MM'))
const issueNo = computed(() => String((dayjs().month() + 1) * 7 + 3).padStart(3, '0'))

const notices = ref([])
const news = ref([])

const noticeCount = computed(() => notices.value.length)
const newsCount = computed(() => news.value.length)

const kpiCourses = ref(null)
const kpiGroups = ref(null)
const kpiResources = ref(null)
const kpiUptime = ref(null)

const kpiTargets = { courses: 96, groups: 18, resources: 1280, uptime: 99.98 }

const schedule = [
  { time: '09:00', title: '教学设计 · 第七讲 · 案例研讨', kind: 'live',   kindLabel: '直播' },
  { time: '11:30', title: '实验教学计划 · 评审会',         kind: 'review', kindLabel: '评审' },
  { time: '14:00', title: '实训周排程 · 草稿待批',         kind: 'draft',  kindLabel: '草稿' },
  { time: '16:00', title: '教研组协作 · 资料归档',         kind: 'sync',   kindLabel: '同步' }
]

const feed = [
  { time: '08:42', text: '「教学法基础」章节 3 个资源已归档' },
  { time: '08:13', text: '王 · 老师 新建 1 份课程计划草稿' },
  { time: '昨 17:21', text: '系统监控 · CPU 峰值 38% — 正常' }
]

const stats = [
  { value: '1280', suffix: '+', label: '归档资源',   caption: '覆盖课程资料 / 实验指导 / 实训案例' },
  { value: '96',   suffix: '',  label: '活跃课程',   caption: '本学期开设 · 同时在线' },
  { value: '240',  suffix: '+', label: '协同教师',   caption: '角色 · 部门 · 字典权限统一管理' },
  { value: '99.98',suffix: '%', label: '服务可用',   caption: '近 90 日平均健康度' }
]

const tickerItems = [
  '课程计划', '课程实验计划', '实训管理', '资源中心',
  '系统管理', 'RBAC 权限模型', '系统监控', '门户网站'
]
const tickerDoubled = computed(() => [...tickerItems, ...tickerItems])

// ---- Carousel / Focus ----
const banners = ref([])
const carouselIndex = ref(0)
const carouselPaused = ref(false)
let carouselTimer = null

const fetchBanners = async () => {
  try {
    // 后端类型约定：1 = 轮播图 / 2 = 通告 / 3 = 资讯
    const data = await listByType(1, 10)
    banners.value = Array.isArray(data) ? data.filter(Boolean) : []
  } catch (e) {
    banners.value = []
  }
}

const goBanner = (i) => {
  if (!banners.value.length) return
  carouselIndex.value = ((i % banners.value.length) + banners.value.length) % banners.value.length
}
const nextBanner = () => goBanner(carouselIndex.value + 1)
const prevBanner = () => goBanner(carouselIndex.value - 1)

const startCarousel = () => {
  stopCarousel()
  if (banners.value.length < 2) return
  carouselTimer = setInterval(() => {
    if (!carouselPaused.value) nextBanner()
  }, 6000)
}
const stopCarousel = () => {
  if (carouselTimer) { clearInterval(carouselTimer); carouselTimer = null }
}

watch(banners, (val) => {
  if (val.length) {
    carouselIndex.value = 0
    startCarousel()
  } else {
    stopCarousel()
  }
}, { deep: true })

// 键盘左右切换（焦点在轮播区域内时）
const onCarouselKey = (e) => {
  if (!banners.value.length) return
  if (e.key === 'ArrowLeft') prevBanner()
  else if (e.key === 'ArrowRight') nextBanner()
}


const workflow = [
  { title: '规划', desc: '按院系 / 学期快速起草课程计划与实验大纲。' },
  { title: '组织', desc: '将教学资料、章节任务与考核节点归档在同一空间。' },
  { title: '协同', desc: '教研组在线评审 · 批注 · 版本管理，无须切换工具。' },
  { title: '执行', desc: '发布通告、排定课表、收集反馈与签到记录。' },
  { title: '反思', desc: '沉淀数据 · 形成下一周期的教研迭代。' }
]

const formatDate = (t) => t ? dayjs(t).format('YYYY.MM.DD') : ''
const formatShortDate = (t) => t ? dayjs(t).format('MM.DD') : '--.--'
const stripHtml = (s) => s ? s.replace(/<[^>]+>/g, '').slice(0, 110) + '…' : '暂无摘要'
const goArticle = (item) => router.push(`/article/${item.id}`)

// 轮播图点击：管理后台若配置了 linkUrl，则跳到该链接；
// 留空则跳到门户"内容"栏目（文章详情页 /article/:id）。
// 外链（http/https）在新标签打开；站内路径走路由；非 http 开头的也按站内处理。
const isExternal = (url) => /^https?:\/\//i.test(url)
const goBannerClick = (item) => {
  const url = (item && item.linkUrl || '').trim()
  if (url) {
    if (isExternal(url)) {
      window.open(url, '_blank', 'noopener,noreferrer')
    } else {
      router.push(url)
    }
  } else if (item && item.id) {
    router.push(`/article/${item.id}`)
  }
}

// 数字递增动画
const animateNumber = (el, target, duration = 1200, decimals = 0) => {
  if (!el) return
  const start = performance.now()
  const tick = (now) => {
    const t = Math.min(1, (now - start) / duration)
    const eased = 1 - Math.pow(1 - t, 3)
    const v = target * eased
    el.textContent = decimals > 0 ? v.toFixed(decimals) : Math.round(v).toString()
    if (t < 1) requestAnimationFrame(tick)
    else el.textContent = decimals > 0 ? target.toFixed(decimals) : String(target)
  }
  requestAnimationFrame(tick)
}

let io = null
onMounted(async () => {
  try {
    // 后端类型：1 = 轮播图 / 2 = 通告 / 3 = 资讯
    const [b, n, w] = await Promise.all([listByType(1, 10), listByType(2, 10), listByType(3, 6)])
    if (Array.isArray(b) && b.length) banners.value = b
    notices.value = n || []
    news.value = w || []
  } catch (e) { /* graceful fallback */ }

  await nextTick()
  startCarousel()

  if ('IntersectionObserver' in window) {
    io = new IntersectionObserver((entries) => {
      entries.forEach((e) => {
        if (e.isIntersecting) {
          animateNumber(kpiCourses.value, kpiTargets.courses, 1200, 0)
          animateNumber(kpiGroups.value, kpiTargets.groups, 1100, 0)
          animateNumber(kpiResources.value, kpiTargets.resources, 1400, 0)
          animateNumber(kpiUptime.value, kpiTargets.uptime, 1600, 2)
          io.disconnect()
        }
      })
    }, { threshold: 0.3 })
    const target = document.querySelector('.hero__panel')
    if (target) io.observe(target)
  }
})

onBeforeUnmount(() => { if (io) io.disconnect(); stopCarousel() })

</script>

<style scoped>
/* ============================== HERO ============================== */
.hero {
  position: relative;
  padding: clamp(64px, 9vw, 120px) 0 clamp(64px, 9vw, 128px);
  border-bottom: 1px solid var(--line);
  background:
    linear-gradient(180deg, #FFFFFF 0%, #F4F7FF 100%);
  overflow: hidden;
  isolation: isolate;
}
.hero__aurora {
  position: absolute;
  inset: 0;
  z-index: -1;
  pointer-events: none;
  overflow: hidden;
}
.hero__aurora-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.55;
  animation: portal-aurora 18s var(--ease) infinite;
}
.hero__aurora-blob--1 {
  width: 520px; height: 520px;
  top: -160px; right: -120px;
  background: radial-gradient(circle, #A9C2FF 0%, transparent 60%);
}
.hero__aurora-blob--2 {
  width: 420px; height: 420px;
  bottom: -120px; left: -80px;
  background: radial-gradient(circle, #5F86FF 0%, transparent 70%);
  opacity: 0.25;
  animation-delay: -9s;
}

.hero__grid {
  display: grid;
  grid-template-columns: minmax(0, 7fr) minmax(0, 5fr);
  gap: clamp(40px, 6vw, 96px);
  align-items: stretch;
}

.hero__copy { min-width: 0; }
.hero__rubric { margin-bottom: var(--s-6); }
.hero__rubric .dot { width: 3px; height: 3px; background: var(--mute-soft); border-radius: 50%; }

.hero__title {
  font-family: var(--font-display);
  font-size: var(--fs-hero);
  font-weight: 600;
  line-height: 1.04;
  letter-spacing: -0.025em;
  color: var(--ink);
  margin: 0;
}
.hero__title em {
  font-family: var(--font-serif);
  font-style: italic;
  font-weight: 500;
  color: var(--accent);
  letter-spacing: -0.015em;
}

.hero__lead {
  margin-top: var(--s-6);
  max-width: 48ch;
  font-size: 16px;
  line-height: 1.78;
  color: var(--ink-soft);
}

.hero__actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--s-3);
  margin-top: var(--s-7);
}

.hero__index {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 0;
  margin-top: var(--s-8);
  padding-top: var(--s-5);
  border-top: 1px solid var(--line);
}
.hero__index-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 6px var(--s-3) 6px 0;
  border-right: 1px solid var(--line-soft);
  transition: padding-left var(--t-base) var(--ease);
  color: var(--ink-soft);
}
.hero__index-item:last-child { border-right: 0; }
.hero__index-item:hover { padding-left: 6px; color: var(--ink); }
.hero__index-key {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--accent);
  letter-spacing: 0.06em;
}
.hero__index-label {
  font-family: var(--font-display);
  font-size: 14px;
  font-weight: 500;
  color: inherit;
  letter-spacing: -0.005em;
}

/* ============================== HERO PANEL ============================== */
.hero__panel {
  background: var(--surface);
  border: 1px solid var(--line);
  display: flex;
  flex-direction: column;
  min-height: 480px;
  transition: border-color var(--t-base) var(--ease), box-shadow var(--t-base) var(--ease), transform var(--t-base) var(--ease);
}
.hero__panel:hover { border-color: var(--accent); box-shadow: var(--shadow-blue); }

.panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--s-3);
  padding: 12px 18px;
  border-bottom: 1px solid var(--line-soft);
  background: linear-gradient(180deg, #F4F7FF 0%, #ECF0FA 100%);
}
.panel__head-left { display: inline-flex; align-items: center; gap: 6px; }
.panel__dot { width: 7px; height: 7px; border-radius: 50%; background: var(--mute-light); }
.panel__dot:nth-child(1) { background: #C5D2F2; }
.panel__dot:nth-child(2) { background: #9EB6F0; }
.panel__dot:nth-child(3) { background: #5F86FF; }
.panel__title {
  margin-left: 8px;
  font-family: var(--font-mono);
  font-size: 10.5px;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--mute);
}
.panel__pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-family: var(--font-mono);
  font-size: 10.5px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--accent);
}
.panel__pulse {
  width: 7px; height: 7px;
  background: var(--accent);
  border-radius: 50%;
  position: relative;
  display: inline-block;
}
.panel__pulse::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: var(--accent);
  animation: portal-pulse 1800ms var(--ease-out) infinite;
}

.panel__body {
  padding: 20px 22px 24px;
  display: flex;
  flex-direction: column;
  gap: 18px;
  flex: 1;
}

.panel__row--kpi {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  border-top: 1px solid var(--line);
  border-left: 1px solid var(--line);
}
.kpi {
  padding: 14px 16px;
  border-right: 1px solid var(--line);
  border-bottom: 1px solid var(--line);
  background: var(--surface);
}
.kpi__label {
  font-size: 11px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--mute);
  margin-bottom: 8px;
}
.kpi__value {
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 600;
  letter-spacing: -0.02em;
  color: var(--ink);
  line-height: 1.05;
  display: inline-flex;
  align-items: baseline;
  gap: 2px;
}
.kpi__suffix { color: var(--accent); font-size: 0.6em; margin-left: 2px; }
.kpi__unit { color: var(--ink-muted); font-size: 0.55em; margin-left: 2px; }

.panel__sub {
  font-family: var(--font-mono);
  font-size: 10.5px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--mute);
  padding-bottom: 6px;
  border-bottom: 1px solid var(--line);
}

.panel__list { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; }
.panel__item {
  display: grid;
  grid-template-columns: 56px 1fr auto;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--line-soft);
  transition: background var(--t-fast) var(--ease), padding-left var(--t-base) var(--ease);
}
.panel__item:hover { background: #F4F7FF; padding-left: 6px; }
.panel__time {
  font-family: var(--font-mono);
  font-size: 11px;
  letter-spacing: 0.04em;
  color: var(--mute);
}
.panel__title-row {
  font-size: 13px;
  color: var(--ink);
  letter-spacing: 0.005em;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.panel__tag {
  font-family: var(--font-mono);
  font-size: 10px;
  letter-spacing: 0.08em;
  padding: 4px 8px;
  border: 1px solid var(--line);
  color: var(--ink-soft);
  background: var(--surface);
  text-transform: uppercase;
}
.panel__tag.is-live   { color: var(--accent); border-color: var(--accent-tint); background: var(--accent-tint); }
.panel__tag.is-review { color: var(--ink-muted); }
.panel__tag.is-draft  { color: var(--mute); }
.panel__tag.is-sync   { color: var(--ink-muted); }

.panel__feed { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 8px; }
.panel__feed li {
  display: grid;
  grid-template-columns: 56px 1fr;
  gap: 12px;
  font-size: 12px;
  color: var(--ink-soft);
}
.panel__feed-time {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--mute);
  letter-spacing: 0.04em;
}

/* ============================== TICKER ============================== */
.ticker {
  background: var(--ink);
  color: var(--paper);
  border-bottom: 1px solid var(--line-firm);
  overflow: hidden;
  padding: 18px 0;
  position: relative;
}
.ticker::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, rgba(95, 134, 255, 0.10), transparent 50%, rgba(95, 134, 255, 0.10));
  pointer-events: none;
}
.ticker__track {
  display: inline-flex;
  align-items: center;
  gap: 56px;
  white-space: nowrap;
  animation: portal-marquee 36s linear infinite;
  padding-left: 24px;
  position: relative;
}
.ticker__item {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  font-family: var(--font-mono);
  font-size: 12px;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  color: #B8CAFF;
}
.ticker__dot {
  width: 4px; height: 4px;
  background: var(--accent);
  border-radius: 50%;
  display: inline-block;
  box-shadow: 0 0 8px var(--accent);
}
@media (prefers-reduced-motion: reduce) {
  .ticker__track { animation: none; }
}

/* ============================== STATS STRIP ============================== */
.stats {
  border-bottom: 1px solid var(--line);
  background: var(--surface);
  padding: clamp(56px, 7vw, 80px) 0;
}
.stats__grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0;
  border-top: 1px solid var(--line);
  border-left: 1px solid var(--line);
}
.stats__item {
  padding: var(--s-6) var(--s-5) var(--s-6) var(--s-6);
  border-right: 1px solid var(--line);
  border-bottom: 1px solid var(--line);
  background: var(--surface);
  position: relative;
  transition: background var(--t-base) var(--ease);
}
.stats__item:hover { background: #F4F7FF; }
.stats__item::after {
  content: '';
  position: absolute;
  left: 0; bottom: 0;
  width: 0;
  height: 2px;
  background: var(--accent);
  transition: width var(--t-base) var(--ease);
}
.stats__item:hover::after { width: 100%; }
.stats__index {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--accent);
  letter-spacing: 0.06em;
  margin-bottom: var(--s-3);
}
.stats__value {
  display: flex;
  align-items: baseline;
  gap: 2px;
}
.stats__num {
  font-family: var(--font-display);
  font-size: clamp(36px, 4.4vw, 56px);
  font-weight: 600;
  letter-spacing: -0.025em;
  color: var(--ink);
  line-height: 1;
}
.stats__suffix {
  font-family: var(--font-display);
  font-size: 0.5em;
  color: var(--accent);
  font-weight: 500;
}
.stats__label {
  margin-top: var(--s-3);
  font-family: var(--font-display);
  font-size: 14px;
  font-weight: 500;
  color: var(--ink);
}
.stats__caption {
  margin-top: 4px;
  font-size: 12px;
  color: var(--mute);
  line-height: 1.5;
  max-width: 32ch;
}

/* ============================== FOCUS / CAROUSEL ============================== */
.carousel {
  position: relative;
  border-top: 1px solid var(--line);
  border-left: 1px solid var(--line);
  background: var(--surface);
}

.carousel__viewport {
  position: relative;
  min-height: clamp(420px, 56vw, 560px);
  border-right: 1px solid var(--line);
  border-bottom: 1px solid var(--line);
  overflow: hidden;
  background: var(--surface);
}

.carousel__slide {
  position: absolute;
  inset: 0;
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(0, 1fr);
  opacity: 0;
  pointer-events: none;
  transform: translateY(8px);
  transition:
    opacity 700ms var(--ease),
    transform 700ms var(--ease);
}
.carousel__slide.is-active {
  opacity: 1;
  pointer-events: auto;
  transform: translateY(0);
}
.carousel__slide.is-clickable { cursor: pointer; }
.carousel__slide.is-clickable:hover .carousel__title { color: var(--accent); }
.carousel__slide.is-clickable:focus-visible { outline: 2px solid var(--accent); outline-offset: -2px; }

.carousel__media {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #DEE6FF 0%, #A9C2FF 100%);
  min-height: 320px;
}
.carousel__img {
  width: 100%; height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 1.2s var(--ease);
}
.carousel__slide.is-active .carousel__img { transform: scale(1.02); }

.carousel__media-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent 60%, rgba(244, 247, 255, 0.45) 100%);
  pointer-events: none;
}

.carousel__placeholder {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--s-3);
  color: rgba(255, 255, 255, 0.95);
  background: linear-gradient(135deg, #5F86FF 0%, #2F5BFF 100%);
  text-align: center;
  padding: var(--s-7);
}
.carousel__placeholder.is-tone-0 { background: linear-gradient(135deg, #5F86FF 0%, #2F3E66 100%); }
.carousel__placeholder.is-tone-1 { background: linear-gradient(135deg, #2F5BFF 0%, #6B4FE0 100%); }
.carousel__placeholder.is-tone-2 { background: linear-gradient(135deg, #4A78E8 0%, #1B2440 100%); }
.carousel__placeholder.is-tone-3 { background: linear-gradient(135deg, #5F86FF 0%, #1B3C8C 100%); }
.carousel__placeholder-num {
  font-family: var(--font-display);
  font-size: clamp(72px, 12vw, 128px);
  font-weight: 600;
  letter-spacing: -0.04em;
  line-height: 1;
  color: #fff;
  text-shadow: 0 8px 24px rgba(0, 0, 0, 0.18);
}
.carousel__placeholder-label {
  font-family: var(--font-mono);
  font-size: 11px;
  letter-spacing: 0.24em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.78);
  max-width: 32ch;
}

.carousel__copy {
  padding: clamp(28px, 4vw, 48px);
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: var(--s-4);
  background: var(--surface);
  border-left: 1px solid var(--line);
  position: relative;
}
.carousel__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--s-3);
  margin-bottom: var(--s-2);
}
.carousel__date {
  font-family: var(--font-mono);
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--mute);
}

.carousel__title {
  font-family: var(--font-display);
  font-size: clamp(26px, 3.2vw, 38px);
  font-weight: 600;
  line-height: 1.18;
  letter-spacing: -0.022em;
  color: var(--ink);
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.carousel__desc {
  font-size: 14.5px;
  line-height: 1.78;
  color: var(--ink-soft);
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.carousel__actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--s-3);
  margin-top: var(--s-3);
}

.carousel__empty {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--s-3);
  color: var(--mute);
  background: var(--surface);
}
.carousel__empty p { margin: 0; font-size: 13px; letter-spacing: 0.06em; }

/* Nav */
.carousel__nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--s-5);
  padding: var(--s-4) var(--s-5);
  border-top: 1px solid var(--line-soft);
  background: var(--surface-soft);
}

.carousel__dots {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}
.carousel__dot {
  position: relative;
  width: 32px;
  height: 2px;
  background: var(--line);
  border: 0;
  padding: 0;
  cursor: pointer;
  overflow: hidden;
  transition: background var(--t-fast) var(--ease);
}
.carousel__dot:hover { background: var(--mute-soft); }
.carousel__dot-fill {
  position: absolute;
  left: 0; top: 0;
  width: 0%;
  height: 100%;
  background: var(--accent);
  transition: width 100ms linear;
}
.carousel__dot.is-active { background: var(--accent); }
.carousel__dot.is-active .carousel__dot-fill { width: 100%; }

.carousel__counter {
  font-family: var(--font-mono);
  font-size: 11px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--mute);
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.carousel__counter-cur { color: var(--accent); font-weight: 500; }
.carousel__counter-sep { color: var(--mute-soft); }
.carousel__counter-tot { color: var(--ink-soft); }

.carousel__arrows { display: inline-flex; gap: 6px; }
.carousel__arrow {
  width: 36px;
  height: 36px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  color: var(--ink-soft);
  border: 1px solid var(--line);
  cursor: pointer;
  transition:
    color var(--t-fast) var(--ease),
    border-color var(--t-fast) var(--ease),
    background var(--t-fast) var(--ease);
}
.carousel__arrow:hover {
  color: var(--paper);
  background: var(--accent);
  border-color: var(--accent);
}

/* Auto-progress on active dot */
.carousel__dot.is-active .carousel__dot-fill {
  animation: portal-carousel-progress 6s linear infinite;
}
@keyframes portal-carousel-progress {
  from { width: 0%; }
  to   { width: 100%; }
}

@media (prefers-reduced-motion: reduce) {
  .carousel__dot.is-active .carousel__dot-fill { animation: none; }
  .carousel__slide { transition: opacity 1ms; transform: none; }
}


/* ============================== DUAL (NOTICES + NEWS) ============================== */
.dual {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1.25fr);
  gap: clamp(40px, 5vw, 80px);
}

/* Notices */
.notices { list-style: none; margin: 0; padding: 0; }
.notice {
  display: grid;
  grid-template-columns: 100px 1fr 22px;
  gap: var(--s-5);
  align-items: flex-start;
  padding: 20px 0;
  border-top: 1px solid var(--line);
  cursor: pointer;
  transition:
    padding-left       var(--t-base) var(--ease),
    background-color  var(--t-fast) var(--ease);
}
.notice:last-child { border-bottom: 1px solid var(--line); }
.notice:hover {
  padding-left: var(--s-3);
  background: linear-gradient(to right, #F4F7FF, transparent 50%);
}
.notice__meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding-top: 4px;
}
.notice__date {
  font-family: var(--font-mono);
  font-size: 13px;
  letter-spacing: 0.04em;
  color: var(--ink);
}
.notice__pin {
  font-family: var(--font-sans);
  font-size: 10.5px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--accent);
}
.notice__body { min-width: 0; }
.notice__title {
  font-family: var(--font-display);
  font-size: 17px;
  font-weight: 500;
  line-height: 1.4;
  letter-spacing: -0.005em;
  color: var(--ink);
  margin: 0 0 6px;
  transition: color var(--t-fast) var(--ease);
}
.notice:hover .notice__title { color: var(--accent); }
.notice__excerpt {
  font-size: 13px;
  line-height: 1.65;
  color: var(--ink-muted);
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.notice__arrow {
  margin-top: 8px;
  color: var(--mute-soft);
  transition: transform var(--t-base) var(--ease), color var(--t-fast) var(--ease);
}
.notice:hover .notice__arrow { color: var(--accent); transform: translateX(4px); }

/* News cards */
.cards {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
}
.card {
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
.card:hover {
  transform: translateY(-3px);
  border-color: var(--accent);
  box-shadow: var(--shadow-blue);
}
.card__cover {
  position: relative;
  aspect-ratio: 4 / 3;
  overflow: hidden;
  background: var(--accent-tint);
}
.card__cover img {
  width: 100%; height: 100%;
  object-fit: cover;
  transition: transform var(--t-slow) var(--ease);
}
.card:hover .card__cover img { transform: scale(1.04); }
.card__plate {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--mute-soft);
  background: linear-gradient(135deg, #F4F7FF 0%, #DEE6FF 100%);
}
.card__plate-num {
  font-family: var(--font-display);
  font-size: 48px;
  font-weight: 600;
  color: var(--accent);
  letter-spacing: -0.02em;
  line-height: 1;
}
.card__tag {
  position: absolute;
  bottom: 10px; left: 10px;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 9px;
  background: rgba(244, 247, 255, 0.94);
  font-family: var(--font-mono);
  font-size: 10.5px;
  letter-spacing: 0.06em;
  color: var(--ink-soft);
}
.card__body {
  padding: 18px 18px 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}
.card__title {
  font-family: var(--font-display);
  font-size: 17px;
  font-weight: 600;
  line-height: 1.4;
  color: var(--ink);
  letter-spacing: -0.005em;
  margin: 0;
  transition: color var(--t-fast) var(--ease);
}
.card:hover .card__title { color: var(--accent); }
.card__excerpt {
  margin: 0;
  font-size: 13px;
  line-height: 1.65;
  color: var(--ink-muted);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card__more {
  margin-top: auto;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding-top: 10px;
  font-size: 11px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--accent);
  transition: gap var(--t-base) var(--ease);
}
.card:hover .card__more { gap: 12px; }

.empty {
  padding: 56px 0;
  text-align: center;
  color: var(--mute);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}
.empty p { margin: 0; font-size: 13px; letter-spacing: 0.06em; }

/* ============================== WORKFLOW ============================== */
.workflow {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  border-top: 1px solid var(--line-firm);
  border-left: 1px solid var(--line-firm);
}
.step {
  padding: var(--s-6) var(--s-5);
  border-right: 1px solid var(--line-firm);
  border-bottom: 1px solid var(--line-firm);
  position: relative;
  transition: background var(--t-base) var(--ease);
}
.step:hover { background: rgba(95, 134, 255, 0.04); }
.step__num {
  font-family: var(--font-display);
  font-size: 36px;
  font-weight: 600;
  letter-spacing: -0.02em;
  color: var(--accent);
  line-height: 1;
  display: block;
  margin-bottom: var(--s-5);
}
.step__title {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 600;
  letter-spacing: -0.01em;
  color: #F4F7FF;
  margin-bottom: var(--s-3);
}
.step__desc {
  font-size: 13px;
  line-height: 1.7;
  color: #A4B0CE;
  margin: 0;
}

/* ============================== CTA ============================== */
.cta {
  text-align: center;
  padding: clamp(64px, 9vw, 112px) clamp(24px, 4vw, 56px);
  border: 1px solid var(--line);
  background: linear-gradient(180deg, #FFFFFF 0%, #F4F7FF 100%);
  position: relative;
  overflow: hidden;
}
.cta::before {
  content: '';
  position: absolute;
  top: -100px; right: -100px;
  width: 320px; height: 320px;
  background: radial-gradient(circle, var(--accent-tint) 0%, transparent 60%);
  border-radius: 50%;
  pointer-events: none;
}
.cta__title {
  font-family: var(--font-display);
  font-size: clamp(30px, 4.4vw, 48px);
  font-weight: 600;
  line-height: 1.14;
  margin: var(--s-3) 0 var(--s-4);
  letter-spacing: -0.022em;
  position: relative;
}
.cta__title em {
  font-family: var(--font-serif);
  font-style: italic;
  font-weight: 500;
  color: var(--accent);
}
.cta__lead {
  color: var(--ink-soft);
  font-size: 15px;
  max-width: 46ch;
  margin: 0 auto var(--s-7);
  position: relative;
}
.cta__actions {
  display: inline-flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: var(--s-3);
  position: relative;
}

/* ============================== RESPONSIVE ============================== */
@media (max-width: 1100px) {
  .hero__grid { grid-template-columns: 1fr; }
  .hero__panel { min-height: 0; }
  .hero__index { grid-template-columns: repeat(3, minmax(0, 1fr)); }
  .hero__index-item:nth-child(3) { border-right: 0; }
  .hero__index-item:nth-child(4),
  .hero__index-item:nth-child(5) {
    border-top: 1px solid var(--line-soft);
  }
  .carousel__viewport { min-height: clamp(380px, 60vw, 480px); }
  .carousel__slide { grid-template-columns: minmax(0, 1fr) minmax(0, 1.1fr); }
  .stats__item { border-right: 0; padding: var(--s-5) 0; border-bottom: 1px solid var(--line); }
  .stats__item:nth-child(odd) { border-right: 1px solid var(--line); padding-right: var(--s-5); }
  .stats__item:nth-child(even) { padding-left: var(--s-5); }
  .stats__item:nth-last-child(-n+2) { border-bottom: 0; }
  .workflow { grid-template-columns: repeat(2, 1fr); }
  .step:nth-child(2n) { border-right: 0; }
  .dual { grid-template-columns: 1fr; }
}

@media (max-width: 720px) {
  .hero__title { font-size: clamp(36px, 11vw, 56px); }
  .hero__index { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .hero__index-item { padding: 12px 12px 12px 0; border-right: 0; }
  .hero__index-item:nth-child(3) { border-top: 1px solid var(--line-soft); }
  .hero__index-item:nth-child(odd) { border-right: 1px solid var(--line-soft); }
  .carousel__viewport { min-height: auto; }
  .carousel__slide { grid-template-columns: 1fr; }
  .carousel__media { min-height: 220px; }
  .carousel__copy { border-left: 0; border-top: 1px solid var(--line); padding: var(--s-5); }
  .carousel__nav { flex-wrap: wrap; gap: var(--s-3); padding: var(--s-3) var(--s-4); }
  .carousel__arrows { order: 3; }
  .stats__grid { grid-template-columns: 1fr; }
  .stats__item:nth-child(odd) { border-right: 0; padding: var(--s-4) 0; }
  .stats__item:nth-child(even) { padding: var(--s-4) 0; }
  .cards { grid-template-columns: 1fr; }
  .workflow { grid-template-columns: 1fr; }
  .step { border-right: 0; }
  .notice { grid-template-columns: 70px 1fr 16px; gap: var(--s-3); }
}
</style>

