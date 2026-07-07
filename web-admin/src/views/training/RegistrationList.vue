<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="query.keyword" placeholder="学生姓名" clearable style="width: 180px" @keyup.enter="load" />
          <el-select v-model="query.planId" placeholder="选择实训计划" clearable filterable style="width: 280px">
            <el-option v-for="p in planList" :key="p.id" :label="`${p.projectName} - ${p.className || ''}`" :value="p.id" />
          </el-select>
          <el-button type="primary" @click="load">搜索</el-button>
        </div>
        <el-button type="primary" :icon="Plus" @click="openForm()">新增报名</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="planTitle" label="实训计划" />
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="['info','success','danger','primary'][row.status]">{{ ['待审核','已通过','已驳回','已完成'][row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="signInTime" label="签到时间" width="170" />
        <el-table-column prop="score" label="成绩" width="80" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link v-if="row.status === 0" @click="review(row, 1)">通过</el-button>
            <el-button size="small" link type="danger" v-if="row.status === 0" @click="review(row, 2)">驳回</el-button>
            <el-button size="small" link v-if="!row.signInTime" @click="signIn(row)">签到</el-button>
            <el-button size="small" link v-if="row.signInTime && !row.signOutTime" @click="signOut(row)">签退</el-button>
            <el-button size="small" link v-if="row.status === 1" @click="grade(row)">评分</el-button>
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
        <el-form-item label="学生ID" prop="studentId"><el-input-number v-model="form.studentId" :min="1" /></el-form-item>
        <el-form-item label="班级"><el-input v-model="form.className" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialog = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { regPage, regAdd, regRemove, regReview, regSignIn, regSignOut, regGrade } from '@/api/training'
import { trainingPage } from '@/api/training'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', planId: null })
const planList = ref([])

const formDialog = ref(false)
const formRef = ref()
const form = reactive({ planId: null, studentName: '', studentId: null, className: '', phone: '' })
const rules = { planId: [{ required: true, message: '请选择实训计划' }], studentName: [{ required: true, message: '请输入学生姓名' }], studentId: [{ required: true, message: '请输入学生ID' }] }

const load = async () => {
  loading.value = true
  try { const res = await regPage(query.planId, query); list.value = res.list; total.value = res.total }
  finally { loading.value = false }
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
const review = async (row, status) => { await regReview(row.id, { status, comment: '' }); ElMessage.success('已审核'); load() }
const signIn = async (row) => { await regSignIn(row.id); ElMessage.success('签到成功'); load() }
const signOut = async (row) => { await regSignOut(row.id); ElMessage.success('签退成功'); load() }
const grade = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入成绩（0-100）', '登记成绩', { inputPattern: /^(\d{1,3}(\.\d{1,2})?)$/, inputErrorMessage: '请输入有效分数' })
  await regGrade(row.id, { score: value, comment: '' })
  ElMessage.success('成绩已记录'); load()
}
const remove = async (row) => { await ElMessageBox.confirm('确定删除该报名？', '提示', { type: 'warning' }); await regRemove([row.id]); ElMessage.success('删除成功'); load() }

onMounted(async () => {
  const res = await trainingPage({ pageNum: 1, pageSize: 1000 })
  planList.value = res.list
  load()
})
</script>
