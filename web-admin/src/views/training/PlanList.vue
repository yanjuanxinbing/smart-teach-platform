<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="query.keyword" placeholder="项目/计划标题" clearable style="width: 220px" @keyup.enter="load" />
          <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="进行中" :value="2" />
            <el-option label="已结束" :value="3" />
            <el-option label="驳回" :value="4" />
          </el-select>
          <el-button type="primary" @click="load">搜索</el-button>
        </div>
        <el-button type="primary" :icon="Plus" @click="openForm()">新增实训计划</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="planTitle" label="计划标题" />
        <el-table-column prop="projectName" label="项目名称" width="180" />
        <el-table-column prop="semester" label="学期" width="140" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="location" label="地点" width="120" />
        <el-table-column prop="capacity" label="容纳人数" width="100" />
        <el-table-column prop="durationDays" label="天数" width="80" />
        <el-table-column prop="totalHours" label="学时" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="['info','primary','success','warning','danger'][row.status]">{{ ['草稿','已发布','进行中','已结束','驳回'][row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="openForm(row)">编辑</el-button>
            <el-button size="small" link v-if="row.status === 0" @click="publish(row)">发布</el-button>
            <el-button size="small" link v-if="row.status === 2" @click="finish(row)">完结</el-button>
            <el-button size="small" link v-if="row.status === 0" @click="approve(row)">审核</el-button>
            <el-button size="small" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="query.pageNum" v-model:size="query.pageSize" :total="total" @change="load" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑实训计划' : '新增实训计划'" width="800px" top="5vh">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="计划标题" prop="planTitle"><el-input v-model="form.planTitle" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="项目名称" prop="projectName"><el-input v-model="form.projectName" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="学期"><el-input v-model="form.semester" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="班级"><el-input v-model="form.className" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="教师"><el-input v-model="form.teacherName" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="地点"><el-input v-model="form.location" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="容纳人数"><el-input-number v-model="form.capacity" :min="0" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="学时"><el-input-number v-model="form.totalHours" :min="0" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="开始日期"><el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="结束日期"><el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="持续天数"><el-input-number v-model="form.durationDays" :min="0" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="实训目标"><el-input v-model="form.objective" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="实训内容"><el-input v-model="form.content" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="考核方式"><el-input v-model="form.assessment" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { trainingPage, trainingAdd, trainingEdit, trainingRemove, trainingPublish, trainingFinish, trainingApprove, trainingReject, trainingDetail } from '@/api/training'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: null })
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({ id: null, planTitle: '', projectName: '', semester: '', className: '', teacherName: '', location: '', startDate: '', endDate: '', durationDays: 0, totalHours: 0, capacity: 30, objective: '', content: '', assessment: '' })
const rules = { planTitle: [{ required: true, message: '请输入计划标题' }], projectName: [{ required: true, message: '请输入项目名称' }] }

const load = async () => {
  loading.value = true
  try { const res = await trainingPage(query); list.value = res.list; total.value = res.total }
  finally { loading.value = false }
}

const openForm = async (row) => {
  dialogVisible.value = true
  if (row) { const d = await trainingDetail(row.id); Object.assign(form, d) }
  else { Object.assign(form, { id: null, planTitle: '', projectName: '', semester: '', className: '', teacherName: '', location: '', startDate: '', endDate: '', durationDays: 0, totalHours: 0, capacity: 30, objective: '', content: '', assessment: '' }) }
}

const submit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try { if (form.id) await trainingEdit(form); else await trainingAdd(form); ElMessage.success('保存成功'); dialogVisible.value = false; load() }
  finally { submitting.value = false }
}

const publish = async (row) => { await trainingPublish(row.id); ElMessage.success('已发布'); load() }
const finish = async (row) => { await trainingFinish(row.id); ElMessage.success('已完结'); load() }
const approve = async (row) => { const { value } = await ElMessageBox.prompt('审核意见', '审核通过', { inputValue: '同意' }); await trainingApprove(row.id, value); load() }
const reject = async (row) => { const { value } = await ElMessageBox.prompt('驳回意见', '审核驳回', { inputValue: '请完善' }); await trainingReject(row.id, value); load() }
const remove = async (row) => { await ElMessageBox.confirm(`确定删除"${row.planTitle}"？`, '提示', { type: 'warning' }); await trainingRemove([row.id]); ElMessage.success('删除成功'); load() }

onMounted(load)
</script>
