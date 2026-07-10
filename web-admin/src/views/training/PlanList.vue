<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="query.keyword" placeholder="项目/计划标题" clearable style="width: 220px" @keyup.enter="search" />
          <el-select v-model="query.semester" placeholder="学期" clearable style="width: 160px" @change="search">
            <el-option v-for="s in semesterOptions" :key="s" :label="s" :value="s" />
          </el-select>
          <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px" @change="search">
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="进行中" :value="3" />
            <el-option label="已结束" :value="4" />
            <el-option label="已驳回" :value="5" />
          </el-select>
          <el-button type="primary" @click="search">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </div>
        <el-button type="primary" :icon="Plus" @click="openForm()">新增实训计划</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="projectName" label="项目名称" width="180" />
        <el-table-column prop="semester" label="学期" width="140" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="location" label="地点" width="120" />
        <el-table-column prop="capacity" label="容纳人数" width="100" />
        <el-table-column prop="durationDays" label="天数" width="80" />
        <el-table-column prop="totalHours" label="学时" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="['info','primary','primary','warning','success','danger'][row.status]">{{ ['草稿','已发布','已发布','进行中','已结束','已驳回'][row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="showDetail(row)">详情</el-button>
            <el-button size="small" link @click="openForm(row)">编辑</el-button>
            <el-button size="small" link v-if="row.status === 0" @click="handlePublish(row)">发布</el-button>
            <el-button size="small" link v-if="row.status === 1 || row.status === 2" @click="handleApprove(row)">通过</el-button>
            <el-button size="small" link type="danger" v-if="row.status === 1 || row.status === 2" @click="handleReject(row)">驳回</el-button>
            <el-button size="small" link v-if="row.status === 3" @click="handleFinish(row)">结束</el-button>
            <el-button size="small" link type="danger" @click="handleRemove(row)">删除</el-button>
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
          <el-col :span="8"><el-form-item label="学期" prop="semester">
            <el-select v-model="form.semester" placeholder="请选择学期" style="width:100%" clearable>
              <el-option v-for="s in semesterOptions" :key="s" :label="s" :value="s" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="8"><el-form-item label="班级" prop="className"><el-input v-model="form.className" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="教师" prop="teacherName"><el-input v-model="form.teacherName" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="地点"><el-input v-model="form.location" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="容纳人数"><el-input-number v-model="form.capacity" :min="0" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="学时"><el-input-number v-model="form.totalHours" :min="0" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="开始日期" prop="startDate"><el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="结束日期" prop="endDate"><el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="持续天数"><el-input-number v-model="form.durationDays" :min="0" disabled /></el-form-item></el-col>
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

    <el-drawer v-model="detailDrawer" title="实训计划详情" size="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="计划标题">{{ detail.planTitle }}</el-descriptions-item>
        <el-descriptions-item label="项目名称">{{ detail.projectName }}</el-descriptions-item>
        <el-descriptions-item label="学期">{{ detail.semester }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ detail.className }}</el-descriptions-item>
        <el-descriptions-item label="教师">{{ detail.teacherName }}</el-descriptions-item>
        <el-descriptions-item label="地点">{{ detail.location }}</el-descriptions-item>
        <el-descriptions-item label="容纳人数">{{ detail.capacity }}</el-descriptions-item>
        <el-descriptions-item label="持续天数">{{ detail.durationDays }}</el-descriptions-item>
        <el-descriptions-item label="学时">{{ detail.totalHours }}</el-descriptions-item>
        <el-descriptions-item label="开始日期">{{ detail.startDate }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{ detail.endDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="['info','primary','warning','success','success','danger'][detail.status]">{{ ['草稿','已发布','审核中','进行中','已结束','已驳回'][detail.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="实训目标" :span="2">{{ detail.objective }}</el-descriptions-item>
        <el-descriptions-item label="实训内容" :span="2">{{ detail.content }}</el-descriptions-item>
        <el-descriptions-item label="考核方式" :span="2">{{ detail.assessment }}</el-descriptions-item>
        <el-descriptions-item label="审核人">{{ detail.approverName }}</el-descriptions-item>
        <el-descriptions-item label="审核意见" :span="2">{{ detail.approveRemark }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { trainingPage, trainingAdd, trainingEdit, trainingRemove, trainingPublish, trainingApprove, trainingReject, trainingFinish, trainingDetail } from '@/api/training'

const list = ref([])
const total = ref(0)
const loading = ref(false)
// 学年学期下拉项：2024-2025-1 ~ 2029-2030-2，共 12 项
const semesterOptions = [
  '2024-2025-1', '2024-2025-2',
  '2025-2026-1', '2025-2026-2',
  '2026-2027-1', '2026-2027-2',
  '2027-2028-1', '2027-2028-2',
  '2028-2029-1', '2028-2029-2',
  '2029-2030-1', '2029-2030-2'
]
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', semester: '', status: null })
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({ id: null, planTitle: '', projectName: '', semester: '', className: '', teacherName: '', location: '', startDate: '', endDate: '', durationDays: 0, totalHours: 0, capacity: 30, objective: '', content: '', assessment: '', status: 0 })
const rules = {
  planTitle: [{ required: true, message: '请输入计划标题' }],
  projectName: [{ required: true, message: '请输入项目名称' }],
  semester: [{ required: true, message: '请选择学期', trigger: 'change' }],
  className: [{ required: true, message: '请输入班级', trigger: 'blur' }],
  teacherName: [{ required: true, message: '请输入教师', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{
    validator: (rule, value, callback) => {
      if (!value) callback(new Error('请选择结束日期'))
      else if (form.startDate && value < form.startDate) callback(new Error('结束日期不能早于开始日期'))
      else callback()
    },
    trigger: 'change'
  }]
}

// 持续天数 = 结束日期 - 开始日期 + 1（含首尾两天）
const recomputeDuration = () => {
  if (!form.startDate || !form.endDate) {
    form.durationDays = 0
    return
  }
  const start = new Date(form.startDate).getTime()
  const end = new Date(form.endDate).getTime()
  const diffDays = Math.floor((end - start) / (1000 * 60 * 60 * 24))
  form.durationDays = diffDays < 0 ? 0 : diffDays + 1
}
watch(() => [form.startDate, form.endDate], recomputeDuration)

const detailDrawer = ref(false)
const detail = reactive({})

const load = async () => {
  loading.value = true
  try {
    const res = await trainingPage(query)
    list.value = Array.isArray(res?.list) ? res.list : []
    total.value = Number(res?.total) || 0
  } catch (e) {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}
// 搜索时统一从第 1 页开始，避免停留在无数据的页码
const search = () => { query.pageNum = 1; load() }
const resetQuery = () => {
  query.keyword = ''
  query.semester = ''
  query.status = null
  query.pageNum = 1
  load()
}

const openForm = async (row) => {
  dialogVisible.value = true
  if (row) {
    try { const d = await trainingDetail(row.id); Object.assign(form, d) }
    catch (e) { Object.assign(form, { id: null, planTitle: '', projectName: '', semester: '', className: '', teacherName: '', location: '', startDate: '', endDate: '', durationDays: 0, totalHours: 0, capacity: 30, objective: '', content: '', assessment: '', status: 0 }) }
  } else {
    // 新增场景：显式把 status 置为 0（草稿），避免之前编辑残留的"已发布"等状态被带回
    Object.assign(form, { id: null, planTitle: '', projectName: '', semester: '', className: '', teacherName: '', location: '', startDate: '', endDate: '', durationDays: 0, totalHours: 0, capacity: 30, objective: '', content: '', assessment: '', status: 0 })
  }
}

const submit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try { if (form.id) await trainingEdit(form); else await trainingAdd(form); ElMessage.success('保存成功'); dialogVisible.value = false; load() }
  finally { submitting.value = false }
}

const showDetail = async (row) => {
  const d = await trainingDetail(row.id)
  Object.assign(detail, d)
  detailDrawer.value = true
}

const handlePublish = async (row) => { try { await trainingPublish(row.id); ElMessage.success('已发布'); load() } catch (e) { if (e !== 'cancel') ElMessage.error(e.message || '操作失败') } }
const handleApprove = async (row) => { try { const { value } = await ElMessageBox.prompt('审核意见', '审核通过', { inputValue: '同意' }); await trainingApprove(row.id, value); ElMessage.success('审核通过'); load() } catch (e) { if (e !== 'cancel') ElMessage.error(e.message || '操作失败') } }
const handleReject = async (row) => { try { const { value } = await ElMessageBox.prompt('驳回意见', '审核驳回', { inputValue: '请完善' }); await trainingReject(row.id, value); ElMessage.success('已驳回'); load() } catch (e) { if (e !== 'cancel') ElMessage.error(e.message || '操作失败') } }
const handleFinish = async (row) => { try { await trainingFinish(row.id); ElMessage.success('已完结'); load() } catch (e) { if (e !== 'cancel') ElMessage.error(e.message || '操作失败') } }
const handleRemove = async (row) => { try { await ElMessageBox.confirm(`确定删除"${row.planTitle}"？`, '提示', { type: 'warning' }); await trainingRemove([row.id]); ElMessage.success('删除成功'); load() } catch (e) { if (e !== 'cancel') ElMessage.error(e.message || '操作失败') } }

onMounted(load)
</script>