<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="query.keyword" placeholder="项目名称" clearable style="width: 200px" @keyup.enter="search" />
          <el-select v-model="query.className" placeholder="班级" clearable filterable style="width: 180px" @change="search">
            <el-option v-for="c in classOptions" :key="c" :label="c" :value="c" />
          </el-select>
          <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px" @change="search">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已驳回" :value="2" />
            <el-option label="已完成" :value="3" />
          </el-select>
          <el-button type="primary" @click="search">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </div>
        <el-button type="primary" :icon="Plus" @click="openForm()">新增报名</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column label="项目名称">
          <template #default="{ row }">
            {{ planMap[row.planId] || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="['info','success','danger','primary'][row.status]">{{ ['待审核','已通过','已驳回','已完成'][row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="regularScore" label="平时成绩" width="100" />
        <el-table-column prop="examScore" label="考核成绩" width="100" />
        <el-table-column prop="score" label="最终成绩" width="100">
          <template #default="{ row }">
            <span v-if="row.score !== null && row.score !== undefined" style="font-weight: 600; color: #409eff">{{ row.score }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link v-if="row.status === 0" @click="review(row, 1)">通过</el-button>
            <el-button size="small" link type="danger" v-if="row.status === 0" @click="review(row, 2)">驳回</el-button>
            <el-button size="small" link v-if="row.signInTime && !row.signOutTime" @click="signOut(row)">签退</el-button>
            <el-button size="small" link v-if="row.status === 1" @click="openGrade(row)">评分</el-button>
            <el-button size="small" link v-if="row.status === 3" @click="openGrade(row)">修改成绩</el-button>
            <el-button size="small" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="query.pageNum" v-model:size="query.pageSize" :total="total" @change="load" />
    </el-card>

    <el-dialog v-model="formDialog" title="新增报名" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="实训计划" prop="planId">
          <el-select v-model="form.planId" filterable style="width:100%">
            <el-option v-for="p in planList" :key="p.id" :label="p.projectName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学生姓名" prop="studentName"><el-input v-model="form.studentName" /></el-form-item>
        <el-form-item label="学生ID" prop="studentId"><el-input-number v-model="form.studentId" :min="1" controls-position="right" style="width:100%" /></el-form-item>
        <el-form-item label="班级"><el-input v-model="form.className" /></el-form-item>
        <el-form-item label="联系电话" prop="phone"><el-input v-model="form.phone" maxlength="11" placeholder="请输入11位手机号" @input="form.phone = (form.phone || '').replace(/\D/g, '').slice(0, 11)" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialog = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="gradeDialog" :title="gradeRow?.status === 3 ? '修改成绩' : '登记成绩'" width="640px">
      <el-form ref="gradeFormRef" :model="gradeForm" :rules="gradeRules" label-width="90px">
        <el-form-item label="学生">
          <span>{{ gradeRow?.studentName }}（{{ gradeRow?.className || '未填班级' }}）</span>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="平时成绩" prop="regularScore">
              <el-input-number v-model="gradeForm.regularScore" :min="0" :max="100" :precision="1" :step="1" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="平时占比" prop="regularWeight">
              <el-input v-model.number="gradeForm.regularWeight" type="number" :min="0" :max="100" @change="onRegularWeightChange">
                <template #suffix><span style="color: #909399">%</span></template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="考核成绩" prop="examScore">
              <el-input-number v-model="gradeForm.examScore" :min="0" :max="100" :precision="1" :step="1" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="考核占比" prop="examWeight">
              <el-input v-model.number="gradeForm.examWeight" type="number" :min="0" :max="100" @change="onExamWeightChange">
                <template #suffix><span style="color: #909399">%</span></template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="最终成绩">
          <span style="font-size: 20px; font-weight: 600; color: #409eff">
            {{ previewScore }}
          </span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="gradeDialog = false">取消</el-button>
        <el-button type="primary" :loading="gradeSubmitting" @click="submitGrade">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { regPage, regAdd, regRemove, regReview, regSignOut, regGrade } from '@/api/training'
import { trainingPage, trainingClasses } from '@/api/training'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', className: '', status: null })
const planList = ref([])
const classOptions = ref([])
// planId → 项目名称 映射，用于在表格中根据 planId 查找对应的项目名称展示
const planMap = computed(() => {
  const m = {}
  planList.value.forEach(p => { if (p && p.id != null) m[p.id] = p.projectName })
  return m
})

const formDialog = ref(false)
const formRef = ref()
const form = reactive({ planId: null, studentName: '', studentId: null, className: '', phone: '' })
const rules = {
  planId: [{ required: true, message: '请选择实训计划' }],
  studentName: [{ required: true, message: '请输入学生姓名' }],
  studentId: [{ required: true, message: '请输入学生ID' }],
  phone: [{ pattern: /^\d{11}$/, message: '联系电话必须是11位数字', trigger: 'blur' }]
}

// 评分弹窗状态
const gradeDialog = ref(false)
const gradeFormRef = ref()
const gradeRow = ref(null)
const gradeSubmitting = ref(false)
const gradeForm = reactive({ regularScore: 0, examScore: 0, regularWeight: 60, examWeight: 40 })
const gradeRules = {
  regularScore: [{ required: true, type: 'number', message: '请输入平时成绩', trigger: 'change' }],
  examScore: [{ required: true, type: 'number', message: '请输入考核成绩', trigger: 'change' }],
  regularWeight: [{ required: true, type: 'number', message: '请输入平时占比', trigger: 'change' }],
  examWeight: [{ required: true, type: 'number', message: '请输入考核占比', trigger: 'change' }]
}

// 任一占比改变，另一个自动补成 100 - 当前值，保证两个加起来始终 = 100%
const onRegularWeightChange = (val) => {
  // 空值/无效值不联动，避免清空时把另一个强制改到 100
  if (val === '' || val === null || val === undefined) return
  const n = Number(val)
  if (Number.isNaN(n)) return
  const clamped = Math.max(0, Math.min(100, n))
  gradeForm.regularWeight = clamped
  gradeForm.examWeight = 100 - clamped
}
const onExamWeightChange = (val) => {
  if (val === '' || val === null || val === undefined) return
  const n = Number(val)
  if (Number.isNaN(n)) return
  const clamped = Math.max(0, Math.min(100, n))
  gradeForm.examWeight = clamped
  gradeForm.regularWeight = 100 - clamped
}
// 占比已通过 onRegularWeightChange/onExamWeightChange 强制合计 100，这里只做实时预览
const previewScore = computed(() => {
  const r = Number(gradeForm.regularScore) || 0
  const e = Number(gradeForm.examScore) || 0
  const rw = Number(gradeForm.regularWeight) || 0
  const ew = Number(gradeForm.examWeight) || 0
  const score = (r * rw + e * ew) / 100
  return Number(score.toFixed(1))
})

const load = async () => {
  loading.value = true
  try {
    const res = await regPage(query)
    list.value = Array.isArray(res?.list) ? res.list : []
    total.value = Number(res?.total) || 0
  } catch (e) {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}
// 搜索时统一从第 1 页开始
const search = () => { query.pageNum = 1; load() }
const resetQuery = () => {
  query.keyword = ''
  query.className = ''
  query.status = null
  query.pageNum = 1
  load()
}

const openForm = () => { formDialog.value = true; Object.assign(form, { planId: null, studentName: '', studentId: null, className: '', phone: '' }) }

const submit = async () => {
  await formRef.value.validate()
  const plan = planList.value.find(p => p.id === form.planId)
  const data = { ...form, planTitle: plan ? plan.planTitle : '' }
  await regAdd(data)
  ElMessage.success('报名成功')
  formDialog.value = false
  load()
}

const review = async (row, status) => {
  const action = status === 1 ? '通过' : '驳回'
  await ElMessageBox.confirm(`确定${action}该报名？`, '确认', { type: 'warning' })
  await regReview(row.id, { status, comment: '' })
  ElMessage.success(`已${action}`)
  load()
}

const signOut = async (row) => {
  loading.value = true
  try { await regSignOut(row.id); ElMessage.success('签退成功'); load() }
  finally { loading.value = false }
}

const grade = async (row) => {
  // 保留旧函数以防调用方未及时更新，但新流程统一走 openGrade
  await openGrade(row)
}

const openGrade = (row) => {
  gradeRow.value = row
  // 如果已有记录则回填
  gradeForm.regularScore = row.regularScore ?? 0
  gradeForm.examScore = row.examScore ?? 0
  gradeForm.regularWeight = row.regularWeight ?? 60
  gradeForm.examWeight = row.examWeight ?? 40
  gradeDialog.value = true
}

const submitGrade = async () => {
  await gradeFormRef.value.validate()
  gradeSubmitting.value = true
  try {
    await regGrade(gradeRow.value.id, {
      regularScore: gradeForm.regularScore,
      examScore: gradeForm.examScore,
      regularWeight: gradeForm.regularWeight,
      examWeight: gradeForm.examWeight,
      comment: ''
    })
    ElMessage.success('成绩已记录')
    gradeDialog.value = false
    load()
  } finally { gradeSubmitting.value = false }
}

const remove = async (row) => {
  loading.value = true
  try { await ElMessageBox.confirm('确定删除该报名？', '提示', { type: 'warning' }); await regRemove([row.id]); ElMessage.success('删除成功'); load() }
  finally { loading.value = false }
}

onMounted(async () => {
  const [planRes, classRes] = await Promise.all([
    trainingPage({ pageNum: 1, pageSize: 1000 }),
    trainingClasses().catch(() => [])
  ])
  planList.value = planRes.list
  classOptions.value = Array.isArray(classRes) ? classRes : []
  load()
})
</script>