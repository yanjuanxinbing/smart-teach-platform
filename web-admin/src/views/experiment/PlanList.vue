<template>
  <div class="app-container">
    <el-card v-loading="loading">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="query.planTitle" placeholder="计划标题" clearable style="width: 220px" @keyup.enter="load" />
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
          <el-button @click="reset">重置</el-button>
        </div>
        <el-button type="primary" :icon="Plus" @click="openForm()">新增实验计划</el-button>
      </div>

      <el-table :data="list || []" v-loading="loading" border>
        <el-table-column prop="planTitle" label="计划标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="courseName" label="课程" width="160" show-overflow-tooltip />
        <el-table-column prop="semester" label="学期" width="120" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="labRoom" label="实验地点" width="140" show-overflow-tooltip />
        <el-table-column prop="teacherName" label="教师" width="100" />
        <el-table-column prop="totalExperiments" label="实验次数" width="100" />
        <el-table-column prop="totalHours" label="学时" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="openForm(row)">编辑</el-button>
            <el-button size="small" link @click="showDetail(row)">详情</el-button>
            <el-button size="small" link v-if="row.status === 0" @click="submit(row)">提交</el-button>
            <el-button size="small" link v-if="row.status === 1" @click="approve(row)">通过</el-button>
            <el-button size="small" link type="danger" v-if="row.status === 1" @click="reject(row)">驳回</el-button>
            <el-button size="small" link type="primary" v-if="row.status === 2 || row.status === 3" @click="revoke(row)">撤销审批</el-button>
            <el-button size="small" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="query.pageNum" v-model:size="query.pageSize" :total="total" @change="load" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑实验计划' : '新增实验计划'" width="900px" top="5vh">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="计划标题" prop="planTitle"><el-input v-model="form.planTitle" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="学期" prop="semester">
            <el-select v-model="form.semester" placeholder="请选择学期" style="width:100%" clearable>
              <el-option v-for="d in (dict.semester && dict.semester.value ? dict.semester.value : dict.semester) || []" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="课程" prop="courseId">
              <el-select v-model="form.courseId" filterable clearable style="width:100%"
                @change="onCourseChange">
                <el-option v-for="c in courseOptions" :key="c.id"
                  :label="`${c.courseCode} ${c.courseName}`" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8"><el-form-item label="课程名称"><el-input v-model="form.courseName" disabled /></el-form-item></el-col>
          <el-col :span="8">
            <el-form-item label="班级" prop="className">
              <el-select v-model="form.className" filterable clearable style="width:100%" placeholder="请选择您所带班级">
                <el-option v-for="c in classOptions" :key="c.id" :label="c.className" :value="c.className" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="教师"><el-input v-model="form.teacherName" disabled /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="实验地点"><el-input v-model="form.labRoom" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="实验次数"><el-input-number v-model="form.totalExperiments" :min="0" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="总学时"><el-input-number v-model="form.totalHours" :min="0" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="开始日期" prop="startDate"><el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="结束日期" prop="endDate"><el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width:100%" :disabled-date="disabledEndDate" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="计划说明"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>

        <el-divider>实验明细</el-divider>
        <div class="items-header">
          <el-button @click="addItem">新增实验</el-button>
          <span class="items-error" v-if="itemsError">{{ itemsError }}</span>
        </div>
        <el-table :data="form.items || []" border style="margin-top: 8px" :row-class-name="itemRowClass">
          <el-table-column label="序号" width="80">
            <template #default="{ $index }"><el-input-number v-model="form.items[$index].expNo" :min="1" /></template>
          </el-table-column>
          <el-table-column width="180">
            <template #header><span class="required-mark">*</span> 实验名称</template>
            <template #default="{ row, $index }">
              <el-input v-model="row.expName" placeholder="必填" :class="{ 'is-required-error': !row.expName?.trim() }" />
            </template>
          </el-table-column>
          <el-table-column width="170">
            <template #header><span class="required-mark">*</span> 上课日期</template>
            <template #default="{ row }">
              <el-date-picker
                v-model="row.classDate"
                type="date"
                value-format="YYYY-MM-DD"
                style="width:100%"
                :class="{ 'is-required-error': !row.classDate || isItemDateOutOfRange(row.classDate) }"
                :disabled-date="isItemDateDisabled"
              />
            </template>
          </el-table-column>
          <el-table-column label="节次" width="100"><template #default="{ row }"><el-input v-model="row.classPeriod" /></template></el-table-column>
          <el-table-column label="学时" width="80"><template #default="{ row }"><el-input-number v-model="row.hours" :min="0" /></template></el-table-column>
          <el-table-column label="操作" width="80"><template #default="{ $index }"><el-button size="small" link type="danger" @click="form.items.splice($index, 1)">删除</el-button></template></el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailDrawer" :title="`实验计划详情 - ${detail.plan?.planTitle || ''}`" size="700px">
      <template v-if="detail.plan">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="学期">{{ detail.plan.semester }}</el-descriptions-item>
          <el-descriptions-item label="班级">{{ detail.plan.className }}</el-descriptions-item>
          <el-descriptions-item label="地点">{{ detail.plan.labRoom }}</el-descriptions-item>
          <el-descriptions-item label="教师">{{ detail.plan.teacherName }}</el-descriptions-item>
          <el-descriptions-item label="实验次数">{{ detail.plan.totalExperiments }}</el-descriptions-item>
          <el-descriptions-item label="总学时">{{ detail.plan.totalHours }}</el-descriptions-item>
        </el-descriptions>
        <h3 style="margin-top: 16px">实验明细</h3>
        <el-table :data="detail.items || []" border>
          <el-table-column prop="expNo" label="序号" width="70" />
          <el-table-column prop="expName" label="实验名称" width="180" />
          <el-table-column prop="classDate" label="上课日期" width="120" />
          <el-table-column prop="classPeriod" label="节次" width="100" />
          <el-table-column prop="hours" label="学时" width="70" />
        </el-table>
      </template>
      <el-empty v-else description="暂无数据" />
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { useDict } from '@/hooks/useDict'
import { useUserStore } from '@/store/user'
import { expPage, expAdd, expEdit, expRemove, expSubmit, expApprove, expReject, expDetail } from '@/api/experiment'
import { listAllCourses } from '@/api/course'
import { classMyClasses } from '@/api/system'

const dict = useDict('semester')
const userStore = useUserStore()
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, planTitle: '', semester: '', status: null })

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const itemsError = ref('')
const form = reactive({
  id: null, planTitle: '', courseId: null, courseName: '',
  semester: '', className: '', teacherId: null, teacherName: '', labRoom: '',
  startDate: '', endDate: '', totalExperiments: 0, totalHours: 0,
  description: '', items: []
})
const rules = {
  planTitle: [{ required: true, message: '请输入计划标题' }],
  semester: [{ required: true, message: '请选择学期' }],
  courseId: [{ required: true, message: '请选择关联课程' }],
  className: [{ required: true, message: '请输入班级', trigger: 'blur' }],
  endDate: [{ validator: (rule, value, callback) => { if (value && form.startDate && new Date(value) <= new Date(form.startDate)) { callback(new Error('结束日期必须晚于开始日期')) } else { callback() } }, trigger: 'change' }]
}

const courseOptions = ref([])
const classOptions = ref([])
const loadCourses = async () => {
  try { courseOptions.value = await listAllCourses() } catch (e) { courseOptions.value = [] }
}
// 拉取当前教师（登录用户）所关联的班级，作为「班级」下拉的数据源
const loadMyClasses = async () => {
  try { classOptions.value = (await classMyClasses()) || [] } catch (e) { classOptions.value = [] }
}
const onCourseChange = (val) => {
  const c = courseOptions.value.find(x => x.id === val)
  form.courseName = c ? c.courseName : ''
  // 教师由当前登录用户带出，不再随课程变化
}

// 取当前登录用户信息作为教师；userInfo 在登录和布局挂载时已经填充过
const fillCurrentTeacher = () => {
  const u = userStore.userInfo || {}
  form.teacherId = u.id || null
  form.teacherName = u.realName || u.username || ''
}

// 结束日期必须晚于开始日期：禁用开始日期及之前的日期
const disabledEndDate = (time) => {
  if (!form.startDate) return false
  return time.getTime() <= new Date(form.startDate).getTime()
}

/**
 * 实验明细上课日期必须落在计划起止日期范围内(任务3)
 * picker 级别禁用 plan.startDate 之前与 plan.endDate 之后的日期,
 * 两端都按自然日判断 (endDate 取当天 23:59:59 之前都视为合法)
 */
const isItemDateDisabled = (time) => {
  const t = time && typeof time.getTime === 'function' ? time.getTime() : NaN
  if (Number.isNaN(t)) return false
  if (form.startDate) {
    const s = new Date(form.startDate).getTime()
    if (t < s) return true
  }
  if (form.endDate) {
    // 当天结束之前 (23:59:59.999) 都允许
    const e = new Date(form.endDate + 'T23:59:59.999').getTime()
    if (t > e) return true
  }
  return false
}
const isItemDateOutOfRange = (iso) => {
  if (!iso) return false
  const t = new Date(iso).getTime()
  if (Number.isNaN(t)) return false
  if (form.startDate && t < new Date(form.startDate).getTime()) return true
  if (form.endDate && t > new Date(form.endDate + 'T23:59:59.999').getTime()) return true
  return false
}

// 开始日期变化时重新校验结束日期，避免选了晚于结束日期的开始日期后看不到错误提示
watch(() => form.startDate, () => {
  if (form.endDate && formRef.value) formRef.value.validateField('endDate')
})

const detailDrawer = ref(false)
const detail = ref({ plan: null, items: [] })

// 状态映射（带防御：status 可能为 null/undefined）
const statusLabel = (s) => ['草稿', '已发布', '已完成', '驳回'][s] || '-'
const statusType = (s) => ['info', 'primary', 'success', 'danger'][s] || 'info'

const load = async () => {
  loading.value = true
  try {
    const res = await expPage(query)
    list.value = res?.list || []
    total.value = res?.total || 0
  } catch (e) {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const reset = () => {
  query.planTitle = ''
  query.semester = ''
  query.status = null
  query.pageNum = 1
  load()
}

const openForm = async (row) => {
  dialogVisible.value = true
  itemsError.value = ''
  try {
    if (row) {
      const d = await expDetail(row.id)
      Object.assign(form, { ...(d?.plan || {}), items: d?.items || [] })
    } else {
      Object.assign(form, {
        id: null, planTitle: '', courseId: null, courseName: '',
        semester: '', className: '', teacherId: null, teacherName: '', labRoom: '',
        startDate: '', endDate: '', totalExperiments: 0, totalHours: 0,
        description: '', items: []
      })
      // 新增时：教师取当前登录用户；用户信息若缺失则主动拉一次（应对刷新场景）
      if (!userStore.userInfo) {
        try { await userStore.fetchUserInfo() } catch (e) {}
      }
      fillCurrentTeacher()
    }
  } catch (e) {
    // ignore
  }
  // 每次打开都刷新课程列表与当前教师所带班级
  loadCourses()
  loadMyClasses()
}

const addItem = () => {
  if (!form.items) form.items = []
  form.items.push({
    expNo: form.items.length + 1,
    expName: '', expType: 1, purpose: '', content: '',
    classDate: '', classPeriod: '', hours: 2, teacherName: form.teacherName
  })
}

const submitForm = async () => {
  await formRef.value.validate()
  // 实验明细校验:至少 1 条;每条 expName 和 classDate 必填
  if (!form.items || form.items.length === 0) {
    itemsError.value = '实验明细不能为空,请至少添加 1 条实验'
    ElMessage.error('请至少添加 1 条实验明细')
    return
  }
  const badIdx = form.items.findIndex(it => !it?.expName?.trim() || !it?.classDate)
  if (badIdx >= 0) {
    itemsError.value = `第 ${badIdx + 1} 条实验的名称/上课日期不能为空`
    ElMessage.error(itemsError.value)
    return
  }
  // 任务3:item.classDate 必须落在 [startDate, endDate] 范围内 —— 前端兜底
  if (form.startDate && form.endDate) {
    const outIdx = form.items.findIndex(it => isItemDateOutOfRange(it.classDate))
    if (outIdx >= 0) {
      const msg = `第 ${outIdx + 1} 条实验的上课日期必须在 [${form.startDate}, ${form.endDate}] 范围内`
      itemsError.value = msg
      ElMessage.error(msg)
      return
    }
  }
  itemsError.value = ''
  submitting.value = true
  try {
    if (form.id) await expEdit(form)
    else await expAdd(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

// 表格行 class:缺失必填字段的行高亮
const itemRowClass = ({ row }) => {
  if (!row?.expName?.trim() || !row?.classDate) return 'row-required-error'
  return ''
}

const submit = async (row) => { await expSubmit(row.id); ElMessage.success('已提交'); load() }
// 撤销审批：把已通过(2)/已驳回(3) 的实验计划回滚到已发布(1)
// 后端 submit() 已放宽为接受 0/2/3 → 1,直接复用
const revoke = async (row) => {
  try {
    await ElMessageBox.confirm(
      `将实验计划「${row.planTitle}」从${row.status === 2 ? '已完成' : '已驳回'}撤销为「已发布」？撤销后可重新分配。`,
      '撤销审批',
      { type: 'warning' }
    )
    await expSubmit(row.id)
    ElMessage.success('已撤销为「已发布」')
    load()
  } catch (e) {
    // 用户点取消 —— 静默
  }
}
const approve = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('审核意见', '审核通过', { inputValue: '同意' })
    await expApprove(row.id, value)
    ElMessage.success('已通过'); load()
  } catch (e) {
    // 用户点取消 / 接口报错 —— 静默，不弹 Uncaught (in promise) cancel
  }
}
const reject = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('驳回意见', '审核驳回', { inputValue: '请完善' })
    await expReject(row.id, value)
    ElMessage.success('已驳回'); load()
  } catch (e) {
    // 用户点取消 / 接口报错 —— 静默
  }
}
const remove = async (row) => {
  await ElMessageBox.confirm(`确定删除"${row.planTitle}"？`, '提示', { type: 'warning' })
  await expRemove([row.id])
  ElMessage.success('删除成功'); load()
}
const showDetail = async (row) => {
  try {
    const d = await expDetail(row.id)
    detail.value = d || { plan: null, items: [] }
    detailDrawer.value = true
  } catch (e) {
    detail.value = { plan: null, items: [] }
    detailDrawer.value = true
  }
}

onMounted(load)
</script>

<style scoped>
.required-mark {
  color: #f56c6c;
  margin-right: 2px;
  font-weight: 600;
}
.items-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 4px;
}
.items-error {
  color: #f56c6c;
  font-size: 12px;
  font-weight: 500;
}
/* 必填字段缺失时输入框红边 */
:deep(.is-required-error .el-input__wrapper),
:deep(.is-required-error .el-date-editor) {
  box-shadow: 0 0 0 1px #f56c6c inset !important;
}
/* 必填行整体浅红底 */
:deep(.row-required-error td) {
  background-color: #fef0f0 !important;
}
</style>