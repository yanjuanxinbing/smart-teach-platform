<template>
  <footer class="pf">
    <div class="pf__sheen" aria-hidden="true"></div>
    <div class="container pf__inner">
      <div class="pf__top">
        <div class="pf__brand">
          <div class="pf__brand-mark" aria-hidden="true">
            <svg viewBox="0 0 32 32" width="24" height="24" focusable="false">
              <path d="M4 27 L4 5 L11.5 19 L18 5 L18 27" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linejoin="miter" stroke-linecap="square"/>
              <path d="M18 5 L25.5 19 L28 5" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linejoin="miter" stroke-linecap="square"/>
            </svg>
          </div>
          <div class="pf__brand-text">
            <div class="pf__brand-name">Methōdus</div>
            <div class="pf__brand-desc">智能教学平台</div>
          </div>
        </div>
        <p class="pf__statement">
          一座教研工作台<br />
           致力于可被监管的教学协作
        </p>
      </div>

      <div class="pf__cols">
        <div class="pf__col">
          <div class="pf__col-title">平台</div>
          <router-link to="/" class="pf__link">首页</router-link>
          <router-link to="/notice" class="pf__link">通告公告</router-link>
          <router-link to="/news" class="pf__link">新闻资讯</router-link>
          <a class="pf__link" :href="adminHome" target="_blank" rel="noopener">管理中心</a>
        </div>
        <div class="pf__col">
          <div class="pf__col-title">教研模块</div>
          <a class="pf__link" :href="adminCourse" target="_blank" rel="noopener">课程计划</a>
          <a class="pf__link" :href="adminExperiment" target="_blank" rel="noopener">课程实验</a>
          <a class="pf__link" :href="adminTraining" target="_blank" rel="noopener">实训管理</a>
          <a class="pf__link" :href="adminResource" target="_blank" rel="noopener">资源中心</a>
        </div>
        <div class="pf__col">
          <div class="pf__col-title">系统</div>
          <a class="pf__link" :href="adminUser" target="_blank" rel="noopener">用户与权限</a>
          <a class="pf__link" :href="adminDict" target="_blank" rel="noopener">字典与参数</a>
          <a class="pf__link" :href="adminMonitor" target="_blank" rel="noopener">系统监控</a>
          <a class="pf__link" :href="adminLog" target="_blank" rel="noopener">登录与操作日志</a>
        </div>
        <div class="pf__col">
          <div class="pf__col-title">联系</div>
          <span class="pf__link pf__link--mute">support@methodus.cn</span>
          <span class="pf__link pf__link--mute">周一至周五 09:00–18:00</span>
        </div>
      </div>

      <div class="pf__bottom">
        <div class="pf__copy">
          <span class="pf__copy-mark">&copy;</span>
          <span>{{ year }}  智能教学支持服务平台</span>
        </div>
        <div class="pf__legal">
          <span class="pf__legal-text">Crafted for higher education.</span>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { computed } from 'vue'
import { buildAdminHref } from '@/utils/adminHref'

const year = new Date().getFullYear()

// 所有跳转 8081 的链接都走 buildAdminHref,统一带上 access_token + 时间戳,
// 避免被 8081 残留的旧 cookie 串号
const adminHome       = computed(() => buildAdminHref('/dashboard'))
const adminCourse     = computed(() => buildAdminHref('/course/manage'))
const adminExperiment = computed(() => buildAdminHref('/experiment/plan'))
const adminTraining   = computed(() => buildAdminHref('/training/plan'))
const adminResource   = computed(() => buildAdminHref('/resource/list'))
const adminUser       = computed(() => buildAdminHref('/system/user'))
const adminDict       = computed(() => buildAdminHref('/system/dict'))
const adminMonitor    = computed(() => buildAdminHref('/monitor/server'))
const adminLog        = computed(() => buildAdminHref('/monitor/login-log'))
</script>

<style scoped>
.pf {
  position: relative;
  background: linear-gradient(180deg, #13192C 0%, #0A0E1A 100%);
  color: #A4B0CE;
  margin-top: clamp(80px, 12vw, 144px);
  border-top: 1px solid var(--line-firm);
  overflow: hidden;
}
.pf__sheen {
  position: absolute;
  top: -200px; left: 50%;
  width: 800px; height: 600px;
  transform: translateX(-50%);
  background: radial-gradient(ellipse at center, rgba(95, 134, 255, 0.10) 0%, transparent 70%);
  pointer-events: none;
}

.pf__inner {
  position: relative;
  padding: clamp(56px, 8vw, 96px) clamp(20px, 4.5vw, 56px) 28px;
}

.pf__top {
  display: grid;
  grid-template-columns: 1.1fr 2fr;
  gap: clamp(40px, 6vw, 96px);
  padding-bottom: clamp(40px, 6vw, 72px);
  border-bottom: 1px solid var(--line-firm);
  align-items: end;
}

.pf__brand {
  display: flex;
  align-items: flex-start;
  gap: var(--s-3);
  color: #F4F7FF;
}
.pf__brand-mark {
  display: inline-flex;
  width: 44px; height: 44px;
  align-items: center; justify-content: center;
  border: 1px solid #2A355A;
  color: var(--accent-soft);
  background: rgba(95, 134, 255, 0.08);
  flex-shrink: 0;
}
.pf__brand-name {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 600;
  color: #F4F7FF;
  letter-spacing: -0.01em;
  line-height: 1.1;
}
.pf__brand-desc {
  margin-top: 6px;
  font-size: 11px;
  letter-spacing: 0.24em;
  text-transform: uppercase;
  color: #8A99C2;
}

.pf__statement {
  margin: 0;
  font-family: var(--font-serif);
  font-style: italic;
  font-size: 18px;
  line-height: 1.55;
  color: #B8C2D9;
  max-width: 56ch;
}

.pf__cols {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--s-6);
  padding: clamp(40px, 5vw, 64px) 0;
}
.pf__col-title {
  font-family: var(--font-sans);
  font-size: 10.5px;
  letter-spacing: 0.24em;
  text-transform: uppercase;
  color: #8A99C2;
  margin-bottom: 18px;
}
.pf__link {
  display: block;
  font-size: 13.5px;
  color: #D7DEEC;
  padding: 6px 0;
  position: relative;
  transition: color var(--t-fast) var(--ease), padding-left var(--t-base) var(--ease);
}
.pf__link::before {
  content: '';
  position: absolute;
  left: -14px;
  top: 50%;
  width: 6px;
  height: 1px;
  background: var(--accent);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform var(--t-base) var(--ease);
}
.pf__link:hover {
  color: #fff;
  padding-left: 14px;
}
.pf__link:hover::before { transform: scaleX(1); }
.pf__link--mute { color: #8A99C2; cursor: default; padding-left: 0; }
.pf__link--mute::before { display: none; }
.pf__link--mute:hover { padding-left: 0; }

.pf__bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--s-4);
  padding-top: 28px;
  border-top: 1px solid var(--line-firm);
  font-size: 11px;
  letter-spacing: 0.04em;
  color: #6B7BA0;
  text-transform: uppercase;
}
.pf__copy { display: inline-flex; align-items: center; gap: 6px; }
.pf__copy-mark { font-size: 12px; }
.pf__legal-text {
  font-family: var(--font-serif);
  font-style: italic;
  font-size: 13px;
  text-transform: none;
  letter-spacing: 0.01em;
  color: #8A99C2;
}

@media (max-width: 960px) {
  .pf__top { grid-template-columns: 1fr; gap: var(--s-7); align-items: start; }
  .pf__cols { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: var(--s-6); }
  .pf__bottom { flex-direction: column; align-items: flex-start; gap: var(--s-3); }
}

@media (max-width: 540px) {
  .pf__cols { grid-template-columns: 1fr 1fr; }
}
</style>
