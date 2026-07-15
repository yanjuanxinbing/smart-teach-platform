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
            <el-option label="已分配" :value="1" />
            <el-option label="已完成" :value="3" />
          </el-select>
          <el-button type="primary" @click="search">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </div>
        <el-button type="primary" :icon="Plus" @click="openForm()">新增分配</el-button>
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
              {{ row.status === 3 ? '已完成' : '已分配' }}
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
            <el-button size="small" link type="primary" v-if="row.status === 1" @click="openComplete(row)">标记完成</el-button>
            <el-button size="small" link type="danger" @click="remove(row)">撤销</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="query.pageNum" v-model:size="query.pageSize" :total="total" @change="load" />
    </el-card>

    <!-- 新增分配弹窗：选计划 + 选班级 -->
    <el-dialog v-model="formDialog" title="新增实验分配" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="实验计划" prop="planId">
          <el-select v-model="form.planId" filterable style="width:100%" :no-data-text="availablePlans.length === 0 ? '暂无可分配的实验计划（仅已发布的计划可分配）' : '无匹配数据'">
            <el-option v-for="p in availablePlans" :key="p.id" :label="p.planTitle" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分配班级" prop="className">
          <el-select v-model="form.className" filterable style="width:100%" placeholder="选择班级（系统会按班级自动给所有学生铺一条已分配记录）">
            <el-option v-for="c in classOptions" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 标记完成弹窗：录成绩 -->
    <el-dialog v-model="completeDialog" title="标记完成" width="420px">
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
import { Plus } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import {
  assignPage, assignByClass, assignComplete, assignRemove
} from '@/api/experiment'
import { expPage, expClasses } from '@/api/experiment'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', className: '', status: null })
const planList = ref([])
const classOptions = ref([])
// 仅 status === 1(已发布)的实验计划才允许分配
const availablePlans = ref([])

const formDialog = ref(false)
const formRef = ref()
const submitting = ref(false)
const form = reactive({ planId: null, className: '' })
const rules = {
  planId: [{ required: true, message: '请选择实验计划' }],
  className: [{ required: true, message: '请选择分配班级' }]
}

// 标记完成
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

const openForm = () => {
  formDialog.value = true
  Object.assign(form, { planId: null, className: '' })
}

const submit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    const added = await assignByClass({ planId: form.planId, className: form.className })
    ElMessage.success(`已分配 ${added} 名学生`)
    formDialog.value = false
    load()
  } finally {
    submitting.value = false
  }
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
    ElMessage.success('已标记完成')
    completeDialog.value = false
    load()
  } finally {
    completeSubmitting.value = false
  }
}

const remove = async (row) => {
  loading.value = true
  try {
    await ElMessageBox.confirm(`确定撤销 ${row.studentName} 的实验分配？`, '提示', { type: 'warning' })
    await assignRemove([row.id])
    ElMessage.success('已撤销')
    load()
  } catch (e) {
    // 用户取消或接口报错
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  const [planRes, classRes] = await Promise.all([
    expPage({ pageNum: 1, pageSize: 1000 }).catch(() => ({ list: [] })),
    expClasses().catch(() => [])
  ])
  planList.value = Array.isArray(planRes.list) ? planRes.list : []
  availablePlans.value = planList.value.filter(p => p && p.status === 1)
  classOptions.value = Array.isArray(classRes) ? classRes : []
  load()
})
</script>