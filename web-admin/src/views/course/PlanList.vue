<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="query.planTitle" placeholder="按计划标题搜索" clearable style="width: 220px" @keyup.enter="load" />
          <el-select v-model="query.semester" placeholder="学期" clearable style="width: 160px">
            <el-option v-for="d in (dict.semester && dict.semester.value ? dict.semester.value : dict.semester) || []" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
          <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="驳回" :value="3" />
          </el-select>
          <el-button type="primary" @click="load">搜索</el-button>
        </div>
        <el-button type="primary" :icon="Plus" @click="openForm()">新增计划</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="planTitle" label="计划标题" />
        <el-table-column prop="courseName" label="课程" width="180" />
        <el-table-column prop="semester" label="学期" width="140" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="teacherName" label="教师" width="100" />
        <el-table-column prop="totalWeeks" label="周次" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="['info','success','primary','danger'][row.status]">{{ ['草稿','已发布','已完成','驳回'][row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="openForm(row)">编辑</el-button>
            <el-button size="small" link @click="showDetail(row)">详情</el-button>
            <el-button size="small" link v-if="row.status === 0" @click="submit(row)">提交</el-button>
            <el-button size="small" link v-if="row.status === 1" @click="approve(row)">通过</el-button>
            <el-button size="small" link type="danger" v-if="row.status === 1" @click="reject(row)">驳回</el-button>
            <el-button size="small" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="query.pageNum" v-model:size="query.pageSize" :total="total" @change="load" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑计划' : '新增计划'" width="800px" top="5vh">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="计划标题" prop="planTitle"><el-input v-model="form.planTitle" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="关联课程" prop="courseId">
              <el-select v-model="form.courseId" filterable style="width:100%" @change="onCourseChange">
                <el-option v-for="c in courseList" :key="c.id" :label="c.courseName" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="学期" prop="semester">
            <el-select v-model="form.semester" placeholder="请选择学期" style="width:100%" clearable>
              <el-option v-for="d in (dict.semester && dict.semester.value ? dict.semester.value : dict.semester) || []" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="8"><el-form-item label="班级" prop="className"><el-input v-model="form.className" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="教师"><el-input v-model="form.teacherName" disabled /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="开始日期"><el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="结束日期"><el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="总周次"><el-input-number v-model="form.totalWeeks" :min="0" disabled /></el-form-item></el-col>
        </el-row>
        <el-form-item label="计划说明"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>

        <el-divider>周次明细</el-divider>
        <el-button @click="addItem">新增周次</el-button>
        <el-table :data="form.items" border style="margin-top: 8px">
          <el-table-column label="周次" width="80">
            <template #default="{ row }"><el-input-number v-model="row.weekNo" :min="1" /></template>
          </el-table-column>
          <el-table-column label="章节标题" width="200"><template #default="{ row }"><el-input v-model="row.chapterTitle" /></template></el-table-column>
          <el-table-column label="教学内容"><template #default="{ row }"><el-input v-model="row.content" /></template></el-table-column>
          <el-table-column label="教学目标" width="180"><template #default="{ row }"><el-input v-model="row.objective" /></template></el-table-column>
          <el-table-column label="学时" width="80"><template #default="{ row }"><el-input-number v-model="row.hours" :min="0" /></template></el-table-column>
          <el-table-column label="操作" width="80"><template #default="{ $index }"><el-button size="small" link type="danger" @click="form.items.splice($index, 1)">删除</el-button></template></el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitPlan">确定</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailDrawer" :title="`计划详情 - ${detail.plan?.planTitle}`" size="700px">
      <template v-if="detail.plan">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="课程">{{ detail.plan.courseName }}</el-descriptions-item>
          <el-descriptions-item label="学期">{{ detail.plan.semester }}</el-descriptions-item>
          <el-descriptions-item label="班级">{{ detail.plan.className }}</el-descriptions-item>
          <el-descriptions-item label="教师">{{ detail.plan.teacherName }}</el-descriptions-item>
          <el-descriptions-item label="周次">{{ detail.plan.totalWeeks }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ ['草稿','已发布','已完成','驳回'][detail.plan.status] }}</el-descriptions-item>
        </el-descriptions>
        <h3 style="margin-top: 16px">周次明细</h3>
        <el-table :data="detail.items" border>
          <el-table-column prop="weekNo" label="周次" width="70" />
          <el-table-column prop="chapterTitle" label="章节" width="180" />
          <el-table-column prop="content" label="内容" />
          <el-table-column prop="objective" label="目标" width="180" />
          <el-table-column prop="hours" label="学时" width="70" />
        </el-table>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { useDict } from '@/hooks/useDict'
import { planPage, planAdd, planEdit, planRemove, planSubmit, planApprove, planReject, planDetail, myCourses } from '@/api/course'

const dict = useDict('semester')
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, planTitle: '', semester: '', status: null })
const courseList = ref([])

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({ id: null, planTitle: '', courseId: null, courseName: '', semester: '', className: '', teacherName: '', startDate: '', endDate: '', totalWeeks: 0, description: '', items: [] })
const rules = { planTitle: [{ required: true, message: '请输入计划标题' }], courseId: [{ required: true, message: '请选择课程' }], semester: [{ required: true, message: '请选择学期' }], className: [{ required: true, message: '请输入班级', trigger: 'blur' }] }

const detailDrawer = ref(false)
const detail = ref({ plan: null, items: [] })

const load = async () => {
  loading.value = true
  try {
    // 用普通对象传给 axios，避免 Vue reactive proxy 在某些序列化路径下被剥离字段
    const res = await planPage({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      planTitle: (query.planTitle || '').trim(),
      semester: query.semester,
      status: query.status
    })
    list.value = res.list || []
    total.value = res.total || 0
  } finally { loading.value = false }
}

const onCourseChange = (val) => {
  const c = courseList.value.find(x => x.id === val)
  if (c) {
    form.courseName = c.courseName
    // 教师从关联课程直接带出，不必手动输入
    form.teacherName = c.teacherName || ''
  }
}

// 根据开始/结束日期自动计算总周次 = ceil((结束 - 开始) / 7)
const recomputeWeeks = () => {
  if (!form.startDate || !form.endDate) {
    form.totalWeeks = 0
    return
  }
  const start = new Date(form.startDate).getTime()
  const end = new Date(form.endDate).getTime()
  const diffDays = Math.floor((end - start) / (1000 * 60 * 60 * 24))
  form.totalWeeks = diffDays <= 0 ? 0 : Math.ceil(diffDays / 7)
}
watch(() => [form.startDate, form.endDate], recomputeWeeks)

const openForm = async (row) => {
  if (!courseList.value.length) courseList.value = await myCourses()
  dialogVisible.value = true
  if (row) {
    const d = await planDetail(row.id)
    Object.assign(form, { ...d.plan, items: d.items || [] })
  } else {
    Object.assign(form, { id: null, planTitle: '', courseId: null, courseName: '', semester: '', className: '', teacherName: '', startDate: '', endDate: '', totalWeeks: 0, description: '', items: [] })
  }
}

const addItem = () => form.items.push({ weekNo: form.items.length + 1, chapterTitle: '', content: '', objective: '', method: '', hours: 2, remark: '' })

const submitPlan = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.id) await planEdit(form); else await planAdd(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally { submitting.value = false }
}

const submit = async (row) => { await planSubmit(row.id); ElMessage.success('已提交审核'); load() }
const approve = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入审核意见', '审核通过', { inputValue: '同意' })
  await planApprove(row.id, value); ElMessage.success('审核通过'); load()
}
const reject = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入驳回意见', '审核驳回', { inputValue: '请完善' })
  await planReject(row.id, value); ElMessage.success('已驳回'); load()
}
const remove = async (row) => {
  await ElMessageBox.confirm(`确定删除计划"${row.planTitle}"吗？`, '提示', { type: 'warning' })
  await planRemove([row.id]); ElMessage.success('删除成功'); load()
}
const showDetail = async (row) => { detail.value = await planDetail(row.id); detailDrawer.value = true }

onMounted(async () => { courseList.value = await myCourses(); load() })
</script>
