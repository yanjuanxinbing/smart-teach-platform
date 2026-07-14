<template>
  <section class="sec">
    <header class="sec__head">
      <span class="eyebrow"><span class="eyebrow__rule"></span>Security</span>
      <h2 class="sec__title">安全<em>设置</em></h2>
      <p class="sec__lead">查看登录设备、管理账号操作，保持账号安全。</p>
    </header>

    <article class="card">
      <div class="card__head"><h3 class="card__title">登录设备</h3></div>
      <ul class="devs">
        <li v-for="(d, i) in devices" :key="i" class="dev" :class="{ 'dev--current': d.current }">
          <el-icon class="dev__ico" :size="20"><Monitor v-if="d.kind === 'desktop'" /><Iphone v-else /></el-icon>
          <div class="dev__body">
            <div class="dev__name">{{ d.name }} <span v-if="d.current" class="dev__tag">当前</span></div>
            <div class="dev__meta">{{ d.os }} · {{ d.ip }} · 最近活跃 {{ d.last }}</div>
          </div>
          <el-button size="small" :disabled="d.current" @click="kick(d, i)">退出</el-button>
        </li>
      </ul>
    </article>

    <article class="card">
      <div class="card__head"><h3 class="card__title">账号操作</h3></div>
      <ul class="ops">
        <li><span>导出我的数据</span><el-button plain size="small">导出</el-button></li>
        <li><span>注销账号</span>
          <el-popconfirm title="注销后将无法恢复，确认继续？" @confirm="onDelete">
            <template #reference><el-button size="small" type="danger" plain>注销</el-button></template>
          </el-popconfirm>
        </li>
      </ul>
    </article>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Monitor, Iphone } from '@element-plus/icons-vue'

const devices = ref([
  { name: 'MacBook Pro · Chrome', os: 'macOS', ip: '::1', last: '刚刚', current: true, kind: 'desktop' },
  { name: 'iPhone 15 · Safari', os: 'iOS 17', ip: '192.168.1.12', last: '昨天 21:34', current: false, kind: 'mobile' }
])


const kick = (d, i) => {
  if (d.current) return
  devices.value.splice(i, 1)
  ElMessage.success('已退出该设备')
}
const onDelete = () => ElMessage.warning('已记录注销申请，客服将尽快与您联系')
</script>

<style scoped>
.sec > .card { margin-top: var(--s-5); }
.sec__head { margin-bottom: var(--s-6); }
.eyebrow { display: inline-flex; align-items: center; gap: 8px; font-family: var(--font-mono); font-size: 11px; letter-spacing: 0.18em; text-transform: uppercase; color: var(--mute); }
.eyebrow__rule { display: inline-block; width: 26px; height: 1px; background: var(--accent); }
.sec__title { font-family: var(--font-display); font-size: clamp(26px, 3vw, 36px); font-weight: 600; color: var(--ink); margin: 6px 0 6px; letter-spacing: -0.018em; }
.sec__title em { font-family: var(--font-serif); font-style: italic; font-weight: 500; color: var(--accent); }
.sec__lead { color: var(--ink-soft); margin: 0; font-size: 14px; }

.card { background: var(--surface); border: 1px solid var(--line); padding: clamp(20px, 2.6vw, 32px); }
.card__head { padding-bottom: var(--s-5); margin-bottom: var(--s-5); border-bottom: 1px solid var(--line-soft); }
.card__title { font-family: var(--font-display); font-size: 18px; font-weight: 600; color: var(--ink); margin: 0; letter-spacing: -0.01em; }

.devs { list-style: none; padding: 0; margin: 0; }
.dev { display: grid; grid-template-columns: 24px 1fr auto; align-items: center; gap: 14px; padding: 16px 0; border-bottom: 1px solid var(--line-soft); }
.dev:last-child { border-bottom: none; }
.dev__ico { color: var(--ink-soft); }
.dev__body { display: flex; flex-direction: column; gap: 4px; }
.dev__name { font-size: 14px; color: var(--ink); font-weight: 500; display: inline-flex; align-items: center; gap: 8px; }
.dev__tag { font-family: var(--font-mono); font-size: 10.5px; letter-spacing: 0.08em; padding: 2px 6px; background: var(--accent); color: #fff; }
.dev__meta { font-family: var(--font-mono); font-size: 12px; color: var(--mute); }
.dev :deep(.el-button) { border-radius: 0; }

.ops { list-style: none; padding: 0; margin: 0; }
.ops li { display: flex; justify-content: space-between; align-items: center; padding: 14px 0; border-bottom: 1px solid var(--line-soft); }
.ops li:last-child { border-bottom: none; font-size: 14px; color: var(--ink); }
.ops :deep(.el-button) { border-radius: 0; }
</style>
