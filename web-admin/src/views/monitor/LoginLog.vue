<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="query.keyword" placeholder="账号" clearable style="width: 220px" @keyup.enter="load" />
          <el-button type="primary" @click="load">搜索</el-button>
        </div>
      </div>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="username" label="账号" width="160" />
        <el-table-column prop="ip" label="IP" width="140" />
        <el-table-column prop="location" label="登录地" width="180" />
        <el-table-column prop="browser" label="浏览器" width="100" />
        <el-table-column prop="os" label="操作系统" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '成功' : '失败' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="消息" />
        <el-table-column prop="loginTime" label="登录时间" width="170" />
      </el-table>

      <Pagination v-model:page="query.pageNum" v-model:size="query.pageSize" :total="total" @change="load" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import Pagination from '@/components/Pagination.vue'
import { loginLogPage } from '@/api/monitor'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

const load = async () => { loading.value = true; try { const res = await loginLogPage(query); list.value = res.list; total.value = res.total } finally { loading.value = false } }
onMounted(load)
</script>
