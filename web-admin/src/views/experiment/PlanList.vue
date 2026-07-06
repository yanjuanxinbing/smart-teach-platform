<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="query.keyword" placeholder="计划标题" clearable style="width: 220px" @keyup.enter="load" />
          <el-select v-model="query.semester" placeholder="学期" clearable style="width: 160px">
            <el-option v-for="d in dict.semester" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
          <el-button type="primary" @click="load">搜索</el-button>
        </div>
        <el-button type="primary" :icon="Plus" @click="openForm()">新增实验计划</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="planTitle" label="计划标题" />
        <el-table-column prop="courseName" label="课程" width="180" />
        <el-table-column prop="semester" label="学期" width="140" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="labRoom" label="实验地点" width="140" />
        <el-table-column prop="teacherName" label="教师" width="100" />
        <el-table-column prop="totalExperiments" label="实验次数" width="100" />
        <el-table-column prop="totalHours" label="学时" width="80" />
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑实验计划' : '新增实验计划'" width="900px" top="5vh">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="计划标题" prop="planTitle"><el-input v-model="form.planTitle" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="学期" prop="semester"><el-input v-model="form.semester" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="课程名称"><el-input v-model="form.courseName" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="班级"><el-input v-model="form.className" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="教师"><el-input v-model="form.teacherName" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="实验地点"><el-input v-model="form.labRoom" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="实验次数"><el-input-number v-model="form.totalExperiments" :min="0" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="总学时"><el-input-number v-model="form.totalHours" :min="0" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="开始日期"><el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="结束日期"><el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="计划说明"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>

        <el-divider>实验明细</el-divider>
        <el-button @click="addItem">新增实验</el-button>
        <el-table :data="form.items" border style="margin-top: 8px">
          <el-table-column label="序号" width="80"><template #default="{ $index }"><el-input-number v-model="form.items[$index].expNo" :min="1" /></template></el-table-column>
          <el-table-column label="实验名称" width="180"><template #default="{ row }"><el-input v-model="row.expName" /></template></el-table-column>
          <el-table-column label="类型" width="120">
            <template #default="{ row }">
              <el-select v-model="row.expType" style="width:100%">
                <el-option v-for="d in dict.exp_type" :key="d.value" :label="d.label" :value="+d.value" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="上课日期" width="160">
            <template #default="{ row }"><el-date-picker v-model="row.classDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></template>
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

    <el-drawer v-model="detailDrawer" :title="`实验计划详情 - ${detail.plan?.planTitle}`" size="700px">
      <template v-if="detail.plan">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="学期">{{ detail.plan.semester }}</el-descriptions-item>
          <el-descriptions-item label="班级">{{ detail.plan.className }}</el-descriptions-item>
          <el-descriptions-item label="地点">{{ detail.plan.labRoom }}</el-descriptions-item>
          <el-descriptions-item label="教师">{{ detail.plan.teacherName }}</el-descriptions-item>
        </el-descriptions>
        <h3 style="margin-top: 16px">实验明细</h3>
        <el-table :data="detail.items" border>
          <el-table-column prop="expNo" label="序号" width="70" />
          <el-table-column prop="expName" label="实验名称" width="180" />
          <el-table-column prop="classDate" label="上课日期" width="120" />
          <el-table-column prop="classPeriod" label="节次" width="100" />
          <el-table-column prop="hours" label="学时" width="70" />
        </el-table>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { useDict } from '@/hooks/useDict'
import { expPage, expAdd, expEdit, expRemove, expSubmit, expApprove, expReject, expDetail } from '@/api/experiment'

const dict = useDict('exp_type', 'semester')
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', semester: '', status: null })

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({ id: null, planTitle: '', courseId: null, courseName: '', semester: '', className: '', teacherName: '', labRoom: '', startDate: '', endDate: '', totalExperiments: 0, totalHours: 0, description: '', items: [] })
const rules = { planTitle: [{ required: true, message: '请输入计划标题' }], semester: [{ required: true, message: '请输入学期' }] }

const detailDrawer = ref(false)
const detail = ref({ plan: null, items: [] })

const load = async () => {
  loading.value = true
  try { const res = await expPage(query); list.value = res.list; total.value = res.total }
  finally { loading.value = false }
}

const openForm = async (row) => {
  dialogVisible.value = true
  if (row) {
    const d = await expDetail(row.id)
    Object.assign(form, { ...d.plan, items: d.items || [] })
  } else {
    Object.assign(form, { id: null, planTitle: '', courseId: null, courseName: '', semester: '', className: '', teacherName: '', labRoom: '', startDate: '', endDate: '', totalExperiments: 0, totalHours: 0, description: '', items: [] })
  }
}

const addItem = () => form.items.push({ expNo: form.items.length + 1, expName: '', expType: 1, purpose: '', content: '', classDate: '', classPeriod: '', hours: 2, teacherName: form.teacherName })

const submitForm = async () => {
  await formRef.value.validate()
  submitting.value = true
  try { if (form.id) await expEdit(form); else await expAdd(form); ElMessage.success('保存成功'); dialogVisible.value = false; load() }
  finally { submitting.value = false }
}

const submit = async (row) => { await expSubmit(row.id); ElMessage.success('已提交'); load() }
const approve = async (row) => { const { value } = await ElMessageBox.prompt('审核意见', '审核通过', { inputValue: '同意' }); await expApprove(row.id, value); load() }
const reject = async (row) => { const { value } = await ElMessageBox.prompt('驳回意见', '审核驳回', { inputValue: '请完善' }); await expReject(row.id, value); load() }
const remove = async (row) => { await ElMessageBox.confirm(`确定删除"${row.planTitle}"？`, '提示', { type: 'warning' }); await expRemove([row.id]); ElMessage.success('删除成功'); load() }
const showDetail = async (row) => { detail.value = await expDetail(row.id); detailDrawer.value = true }

onMounted(load)
</script>
