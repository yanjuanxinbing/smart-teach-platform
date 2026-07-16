<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="query.keyword" placeholder="学生姓名 / 计划名" clearable style="width: 220px" @keyup.enter="search" />
          <el-select v-model="query.className" placeholder="班级" clearable filterable style="width: 180px" @change="search">
            <el-option v-for="c in classOptions" :key="c" :label="c" :value="c" />
          </el-select>
          <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px" @change="search">
            <el-option label="未评分" :value="1" />
            <el-option label="已评分" :value="3" />
          </el-select>
          <el-button type="primary" @click="search">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </div>
      </div>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column label="实验计划">
          <template #default="{ row }">
            {{ row.planTitle || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="className" label="班级" width="160" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 3 ? 'primary' : 'success'">
              {{ row.status === 3 ? '已评分' : '未评分' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="成绩" width="100">
          <template #default="{ row }">
            <span v-if="row.score !== null && row.score !== undefined" style="font-weight: 600; color: #409eff">{{ row.score }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link type="primary" v-if="row.status === 1" @click="openComplete(row)">评分</el-button>
            <el-button size="small" link type="danger" @click="remove(row)">撤销</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="query.pageNum" v-model:size="query.pageSize" :total="total" @change="load" />
    </el-card>

    <!-- 评分弹窗：录成绩 -->
    <el-dialog v-model="completeDialog" title="评分" width="420px">
      <el-form ref="completeFormRef" :model="completeForm" :rules="completeRules" label-width="80px">
        <el-form-item label="学生">
          <span>{{ completeRow?.studentName }}（{{ completeRow?.className || '未填班级' }}）</span>
        </el-form-item>
        <el-form-item label="成绩" prop="score">
          <el-input-number v-model="completeForm.score" :min="0" :max="100" :precision="1" :step="1" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="评语">
          <el-input v-model="completeForm.comment" type="textarea" :rows="3" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeDialog = false">取消</el-button>
        <el-button type="primary" :loading="completeSubmitting" @click="submitComplete">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Pagination from '@/components/Pagination.vue'
import {
  assignPage, assignComplete, assignRemove
} from '@/api/experiment'
import { expClasses } from '@/api/experiment'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', className: '', status: null })
const classOptions = ref([])

// 评分弹窗
const completeDialog = ref(false)
const completeFormRef = ref()
const completeRow = ref(null)
const completeSubmitting = ref(false)
const completeForm = reactive({ score: null, comment: '' })
const completeRules = {
  score: [{ required: true, type: 'number', message: '请填写成绩', trigger: 'change' }]
}

const load = async () => {
  loading.value = true
  try {
    const res = await assignPage(query)
    list.value = Array.isArray(res?.list) ? res.list : []
    total.value = Number(res?.total) || 0
  } catch (e) {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const search = () => { query.pageNum = 1; load() }
const resetQuery = () => {
  query.keyword = ''
  query.className = ''
  query.status = null
  query.pageNum = 1
  load()
}

const openComplete = (row) => {
  completeRow.value = row
  completeForm.score = null
  completeForm.comment = ''
  completeDialog.value = true
}

const submitComplete = async () => {
  await completeFormRef.value.validate()
  completeSubmitting.value = true
  try {
    await assignComplete(completeRow.value.id, {
      score: completeForm.score,
      comment: completeForm.comment
    })
    ElMessage.success('已评分')
    completeDialog.value = false
    load()
  } finally {
    completeSubmitting.value = false
  }
}

const remove = async (row) => {
  loading.value = true
  try {
    await ElMessageBox.confirm(`确定删除 ${row.studentName} 的评分记录？`, '提示', { type: 'warning' })
    await assignRemove([row.id])
    ElMessage.success('已删除')
    load()
  } catch (e) {
    // 用户取消或接口报错
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  classOptions.value = (await expClasses().catch(() => [])) || []
  load()
})
</script>