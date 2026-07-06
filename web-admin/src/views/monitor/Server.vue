<template>
  <div class="app-container">
    <el-row :gutter="16">
      <el-col :span="8">
        <el-card>
          <template #header><span>CPU</span></template>
          <div v-if="info.cpu">
            <p>核心数：{{ info.cpu.cpuNum }}</p>
            <p>系统使用率：{{ info.cpu.sysUsage }}%</p>
            <p>用户使用率：{{ info.cpu.userUsage }}%</p>
            <p>等待率：{{ info.cpu.waitUsage }}%</p>
            <p>空闲率：{{ info.cpu.freeUsage }}%</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header><span>内存</span></template>
          <div v-if="info.memory">
            <p>总内存：{{ info.memory.total }} GB</p>
            <p>已用：{{ info.memory.used }} GB</p>
            <p>空闲：{{ info.memory.free }} GB</p>
            <el-progress :percentage="info.memory.usageRate" :stroke-width="20" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header><span>JVM</span></template>
          <div v-if="info.jvm">
            <p>JDK版本：{{ info.jvm.jdkVersion }}</p>
            <p>JDK路径：{{ info.jvm.jdkHome }}</p>
            <p>JVM总内存：{{ info.jvm.total }} GB</p>
            <p>JVM已用：{{ info.jvm.used }} GB</p>
            <p>启动时间：{{ info.jvm.startTime }}</p>
            <p>运行时长：{{ info.jvm.runTime }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="14">
        <el-card>
          <template #header><span>磁盘</span></template>
          <el-table :data="info.disks || []" border>
            <el-table-column prop="dirName" label="挂载点" />
            <el-table-column prop="sysTypeName" label="类型" width="100" />
            <el-table-column prop="total" label="总(GB)" width="100" />
            <el-table-column prop="used" label="已用(GB)" width="100" />
            <el-table-column prop="free" label="空闲(GB)" width="100" />
            <el-table-column label="使用率" width="180">
              <template #default="{ row }">
                <el-progress :percentage="row.usageRate" :stroke-width="16" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card>
          <template #header><span>系统信息</span></template>
          <div v-if="info.sys">
            <p>操作系统：{{ info.sys.osName }}</p>
            <p>系统架构：{{ info.sys.osArch }}</p>
            <p>服务器IP：{{ info.sys.serverIp }}</p>
            <p>项目路径：{{ info.sys.projectDir }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { serverInfo } from '@/api/monitor'

const info = ref({})
let timer = null

const load = async () => { try { info.value = await serverInfo() } catch (e) {} }

onMounted(() => { load(); timer = setInterval(load, 10000) })
onUnmounted(() => { if (timer) clearInterval(timer) })
</script>
