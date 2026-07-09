<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="query.keyword" placeholder="项目/计划标题" clearable style="width: 220px" @keyup.enter="load" />
          <el-select v-model="query.semester" placeholder="学期" clearable style="width: 160px">
            <el-option v-for="d in (dict.semester && dict.semester.value ? dict.semester.value : dict.semester) || []" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
          <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="审核中" :value="2" />
            <el-option label="进行中" :value="3" />
            <el-option label="已结束" :value="4" />
            <el-option label="已驳回" :value="5" />
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
            <el-tag :type="['info','primary','warning','success','success','danger'][row.status]">{{ ['草稿','已发布','审核中','进行中','已结束','已驳回'][row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="showDetail(row)">详情</el-button>
            <el-button size="small" link @click="openForm(row)">编辑</el-button>
            <el-button size="small" link v-if="row.status === 0" @click="handlePublish(row)">发布</el-button>
            <el-button size="small" link v-if="row.status === 1" @click="handleSubmitReview(row)">提交审核</el-button>
            <el-button size="small" link v-if="row.status === 2" @click="handleApprove(row)">审核通过</el-button>
            <el-button size="small" link type="danger" v-if="row.status === 2" @click="handleReject(row)">审核驳回</el-button>
            <el-button size="small" link v-if="row.status === 3" @click="handleFinish(row)">结束</el-button>
            <el-button size="small" link type="danger" @click="handleRemove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="query.pageNum" v-model:size="query.pageSize" :total="total" @change="load" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑实训计划' : '新增实训计划'" width="900px" top="5vh">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="计划标题" prop="planTitle"><el-input v-model="form.planTitle" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="项目名称" prop="projectName"><el-input v-model="form.projectName" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="学期" prop="semester">
              <el-select v-model="form.semester" placeholder="请选择学期" style="width:100%" clearable>
                <el-option v-for="d in (dict.semester && dict.semester.value ? dict.semester.value : dict.semester) || []" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8"><el-form-item label="班级" prop="className"><el-input v-model="form.className" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="教师"><el-input v-model="form.teacherName" /></el-form-item></el-col>
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

        <el-divider>实训阶段</el-divider>
        <el-button @click="addStage">新增阶段</el-button>
        <el-table :data="form.stages" border style="margin-top: 8px">
          <el-table-column label="阶段名称" width="200">
            <template #default="{ row }"><el-input v-model="row.stageName" placeholder="如：需求分析" /></template>
          </el-table-column>
          <el-table-column label="开始日期" width="160">
            <template #default="{ row }"><el-date-picker v-model="row.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></template>
          </el-table-column>
          <el-table-column label="结束日期" width="160">
            <template #default="{ row }"><el-date-picker v-model="row.endDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></template>
          </el-table-column>
          <el-table-column label="持续天数" width="100">
            <template #default="{ row }"><el-input-number v-model="row.durationDays" :min="0" disabled /></template>
          </el-table-column>
          <el-table-column label="任务内容">
            <template #default="{ row }"><el-input v-model="row.content" /></template>
          </el-table-column>
          <el-table-column label="备注" width="160">
            <template #default="{ row }"><el-input v-model="row.remark" /></template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ $index }"><el-button size="small" link type="danger" @click="form.stages.splice($index, 1)">删除</el-button></template>
          </el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">确定</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailDrawer" title="实训计划详情" size="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="计划标题">{{ detail.plan?.planTitle }}</el-descriptions-item>
        <el-descriptions-item label="项目名称">{{ detail.plan?.projectName }}</el-descriptions-item>
        <el-descriptions-item label="学期">{{ detail.plan?.semester }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ detail.plan?.className }}</el-descriptions-item>
        <el-descriptions-item label="教师">{{ detail.plan?.teacherName }}</el-descriptions-item>
        <el-descriptions-item label="地点">{{ detail.plan?.location }}</el-descriptions-item>
        <el-descriptions-item label="容纳人数">{{ detail.plan?.capacity }}</el-descriptions-item>
        <el-descriptions-item label="持续天数">{{ detail.plan?.durationDays }}</el-descriptions-item>
        <el-descriptions-item label="学时">{{ detail.plan?.totalHours }}</el-descriptions-item>
        <el-descriptions-item label="开始日期">{{ detail.plan?.startDate }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{ detail.plan?.endDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="['info','primary','warning','success','success','danger'][detail.plan?.status]">{{ ['草稿','已发布','审核中','进行中','已结束','已驳回'][detail.plan?.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="实训目标" :span="2">{{ detail.plan?.objective }}</el-descriptions-item>
        <el-descriptions-item label="实训内容" :span="2">{{ detail.plan?.content }}</el-descriptions-item>
        <el-descriptions-item label="考核方式" :span="2">{{ detail.plan?.assessment }}</el-descriptions-item>
        <el-descriptions-item label="审核人">{{ detail.plan?.approverName }}</el-descriptions-item>
        <el-descriptions-item label="审核意见" :span="2">{{ detail.plan?.approveRemark }}</el-descriptions-item>
      </el-descriptions>
      <h3 style="margin-top: 16px">实训阶段</h3>
      <el-table :data="detail.stages || []" border>
        <el-table-column prop="sortNo" label="序号" width="70" />
        <el-table-column prop="stageName" label="阶段名称" width="180" />
        <el-table-column prop="startDate" label="开始" width="120" />
        <el-table-column prop="endDate" label="结束" width="120" />
        <el-table-column prop="durationDays" label="天数" width="70" />
        <el-table-column prop="content" label="任务内容" />
        <el-table-column prop="remark" label="备注" width="180" />
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { useDict } from '@/hooks/useDict'
import { trainingPage, trainingAdd, trainingEdit, trainingRemove, trainingPublish, trainingSubmitReview, trainingApprove, trainingReject, trainingFinish, trainingDetail } from '@/api/training'

const dict = useDict('semester')
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', semester: '', status: null })
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  planTitle: '',
  projectName: '',
  semester: '',
  className: '',
  teacherName: '',
  location: '',
  startDate: '',
  endDate: '',
  durationDays: 0,
  totalHours: 0,
  capacity: 30,
  objective: '',
  content: '',
  assessment: '',
  stages: []
})
const rules = {
  planTitle: [{ required: true, message: '请输入计划标题' }],
  projectName: [{ required: true, message: '请输入项目名称' }],
  semester: [{ required: true, message: '请选择学期', trigger: 'change' }],
  className: [{ required: true, message: '请输入班级', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{
    required: true,
    message: '请选择结束日期',
    trigger: 'change'
  }, {
    validator: (rule, value, callback) => {
      if (value && form.startDate && value < form.startDate) callback(new Error('结束日期不能早于开始日期'))
      else callback()
    },
    trigger: 'change'
  }]
}

const detailDrawer = ref(false)
const detail = reactive({ plan: null, stages: [] })

const load = async () => {
  loading.value = true
  try {
    const res = await trainingPage({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      keyword: (query.keyword || '').trim(),
      semester: query.semester,
      status: query.status
    })
    list.value = res.list || []
    total.value = res.total || 0
  } finally { loading.value = false }
}

// 根据开始/结束日期自动计算主计划持续天数
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

// 联动计算每个阶段的持续天数
const recomputeStageDuration = (row) => {
  if (!row.startDate || !row.endDate) {
    row.durationDays = 0
    return
  }
  const start = new Date(row.startDate).getTime()
  const end = new Date(row.endDate).getTime()
  const diffDays = Math.floor((end - start) / (1000 * 60 * 60 * 24))
  row.durationDays = diffDays < 0 ? 0 : diffDays + 1
}

const openForm = async (row) => {
  dialogVisible.value = true
  if (row) {
    const d = await trainingDetail(row.id)
    Object.assign(form, {
      id: d.plan.id,
      planTitle: d.plan.planTitle,
      projectName: d.plan.projectName,
      semester: d.plan.semester,
      className: d.plan.className,
      teacherName: d.plan.teacherName,
      location: d.plan.location,
      startDate: d.plan.startDate,
      endDate: d.plan.endDate,
      durationDays: d.plan.durationDays,
      totalHours: d.plan.totalHours,
      capacity: d.plan.capacity,
      objective: d.plan.objective,
      content: d.plan.content,
      assessment: d.plan.assessment,
      stages: (d.stages || []).map(s => ({ ...s }))
    })
  } else {
    Object.assign(form, {
      id: null,
      planTitle: '',
      projectName: '',
      semester: '',
      className: '',
      teacherName: '',
      location: '',
      startDate: '',
      endDate: '',
      durationDays: 0,
      totalHours: 0,
      capacity: 30,
      objective: '',
      content: '',
      assessment: '',
      stages: []
    })
  }
}

const addStage = () => {
  form.stages.push({
    stageName: '',
    startDate: '',
    endDate: '',
    durationDays: 0,
    content: '',
    remark: '',
    sortNo: form.stages.length + 1
  })
}

const submit = async () => {
  await formRef.value.validate()
  // 计算每个阶段的持续天数
  form.stages.forEach(recomputeStageDuration)
  submitting.value = true
  try {
    const payload = {
      ...form,
      // 清空只读字段，避免后端被覆盖
      durationDays: form.durationDays
    }
    if (form.id) await trainingEdit(payload); else await trainingAdd(payload)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally { submitting.value = false }
}

const showDetail = async (row) => {
  const d = await trainingDetail(row.id)
  detail.plan = d.plan
  detail.stages = d.stages || []
  detailDrawer.value = true
}

const handlePublish = async (row) => { try { await trainingPublish(row.id); ElMessage.success('已发布'); load() } catch (e) { if (e !== 'cancel') ElMessage.error(e.message || '操作失败') } }
const handleSubmitReview = async (row) => { try { await trainingSubmitReview(row.id); ElMessage.success('已提交审核'); load() } catch (e) { if (e !== 'cancel') ElMessage.error(e.message || '操作失败') } }
const handleApprove = async (row) => { try { const { value } = await ElMessageBox.prompt('审核意见', '审核通过', { inputValue: '同意' }); await trainingApprove(row.id, value); ElMessage.success('审核通过'); load() } catch (e) { if (e !== 'cancel') ElMessage.error(e.message || '操作失败') } }
const handleReject = async (row) => { try { const { value } = await ElMessageBox.prompt('驳回意见', '审核驳回', { inputValue: '请完善' }); await trainingReject(row.id, value); ElMessage.success('已驳回'); load() } catch (e) { if (e !== 'cancel') ElMessage.error(e.message || '操作失败') } }
const handleFinish = async (row) => { try { await trainingFinish(row.id); ElMessage.success('已完结'); load() } catch (e) { if (e !== 'cancel') ElMessage.error(e.message || '操作失败') } }
const handleRemove = async (row) => { try { await ElMessageBox.confirm(`确定删除"${row.planTitle}"？`, '提示', { type: 'warning' }); await trainingRemove([row.id]); ElMessage.success('删除成功'); load() } catch (e) { if (e !== 'cancel') ElMessage.error(e.message || '操作失败') } }

onMounted(load)
</script>
