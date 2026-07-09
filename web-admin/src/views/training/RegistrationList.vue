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
        <el-table-column prop="score" label="成绩" width="80" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link v-if="row.status === 0" @click="review(row, 1)">通过</el-button>
            <el-button size="small" link type="danger" v-if="row.status === 0" @click="review(row, 2)">驳回</el-button>
            <el-button size="small" link v-if="row.status === 1" @click="openGradeDialog(row)">评分</el-button>
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

    <el-dialog v-model="gradeDialog" title="登记成绩" width="960px" top="6vh" append-to-body>
      <el-table :data="gradeForm.items" border v-loading="gradeLoading" :cell-style="{ verticalAlign: 'top' }">
        <el-table-column label="评分项" min-width="160">
          <template #default="{ row }"><el-input v-model="row.itemName" placeholder="如：出勤" /></template>
        </el-table-column>
        <el-table-column label="分数" min-width="150">
          <template #default="{ row }">
            <el-input-number v-model="row.itemScore" :min="0" :max="row.maxScore || 100" :precision="1" controls-position="right" style="width:100%" />
          </template>
        </el-table-column>
        <el-table-column label="满分" min-width="130">
          <template #default="{ row }">
            <el-input-number v-model="row.maxScore" :min="0" :precision="1" controls-position="right" style="width:100%" />
          </template>
        </el-table-column>
        <el-table-column label="权重(%)" min-width="130">
          <template #default="{ row }">
            <el-input-number v-model="row.weight" :min="0" :max="100" :precision="2" controls-position="right" style="width:100%" />
            <span style="position:absolute;right:30px;top:50%;transform:translateY(-50%);color:#909399;font-size:12px;pointer-events:none">%</span>
          </template>
        </el-table-column>
        <el-table-column label="评语" min-width="200">
          <template #default="{ row }"><el-input v-model="row.comment" /></template>
        </el-table-column>
        <el-table-column label="操作" width="70" fixed="right">
          <template #default="{ $index }"><el-button size="small" link type="danger" @click="gradeForm.items.splice($index, 1)">删</el-button></template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 12px">
        <el-button size="small" @click="addGradeItem">新增评分项</el-button>
        <span style="margin-left: 12px; color: #909399">
          合计：{{ totalScore }} / {{ totalMax }}
          （权重合计：{{ totalWeight }}%）
        </span>
      </div>
      <template #footer>
        <el-button @click="gradeDialog = false">取消</el-button>
        <el-button type="primary" :loading="gradeSubmitting" @click="submitGrade">提交评分</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { regPage, regAdd, regRemove, regReview } from '@/api/training'
import { trainingPage } from '@/api/training'
import { scoreList, regGradeItems } from '@/api/training'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', planId: null })
const planList = ref([])

const formDialog = ref(false)
const formRef = ref()
const form = reactive({ planId: null, studentName: '', studentId: null, className: '', phone: '' })
const rules = { planId: [{ required: true, message: '请选择实训计划' }], studentName: [{ required: true, message: '请输入学生姓名' }], studentId: [{ required: true, message: '请输入学生ID' }] }

const gradeDialog = ref(false)
const gradeLoading = ref(false)
const gradeSubmitting = ref(false)
const gradeForm = reactive({ items: [] })
const gradingRegId = ref(null)

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

const review = async (row, status) => {
  const action = status === 1 ? '通过' : '驳回'
  await ElMessageBox.confirm(`确定${action}该报名？`, '确认', { type: 'warning' })
  await regReview(row.id, { status, comment: '' })
  ElMessage.success(`已${action}`)
  load()
}

const remove = async (row) => {
  loading.value = true
  try { await ElMessageBox.confirm('确定删除该报名？', '提示', { type: 'warning' }); await regRemove([row.id]); ElMessage.success('删除成功'); load() }
  finally { loading.value = false }
}

const openGradeDialog = async (row) => {
  gradingRegId.value = row.id
  gradeDialog.value = true
  gradeLoading.value = true
  try {
    const items = await scoreList(row.id)
    if (items && items.length > 0) {
      gradeForm.items = items.map(i => ({
        itemName: i.itemName, itemScore: i.itemScore,
        maxScore: i.maxScore || 100, weight: i.weight || 1, comment: i.comment
      }))
    } else {
      gradeForm.items = [
        { itemName: '出勤', itemScore: null, maxScore: 100, weight: 20, comment: '' },
        { itemName: '日报质量', itemScore: null, maxScore: 100, weight: 30, comment: '' },
        { itemName: '最终答辩', itemScore: null, maxScore: 100, weight: 50, comment: '' }
      ]
    }
  } finally {
    gradeLoading.value = false
  }
}

const addGradeItem = () => {
  gradeForm.items.push({ itemName: '', itemScore: null, maxScore: 100, weight: 1, comment: '' })
}

const totalScore = computed(() => {
  const total = gradeForm.items.reduce((s, it) => s + (Number(it.itemScore) || 0) * (Number(it.weight) || 0), 0)
  return total.toFixed(1)
})
const totalMax = computed(() => {
  const total = gradeForm.items.reduce((s, it) => s + (Number(it.maxScore) || 0) * (Number(it.weight) || 0), 0)
  return total.toFixed(1)
})

const submitGrade = async () => {
  gradeSubmitting.value = true
  try {
    await regGradeItems(gradingRegId.value, gradeForm.items)
    ElMessage.success('成绩已保存')
    gradeDialog.value = false
    load()
  } finally {
    gradeSubmitting.value = false
  }
}

onMounted(async () => {
  const res = await trainingPage({ pageNum: 1, pageSize: 1000 })
  planList.value = res.list
  load()
})
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap; }
.toolbar-left { display: flex; gap: 8px; flex-wrap: wrap; align-items: center; }
</style>
