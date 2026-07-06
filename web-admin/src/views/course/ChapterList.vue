<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-select v-model="courseId" placeholder="选择课程" filterable @change="load" style="width: 280px">
            <el-option v-for="c in courseList" :key="c.id" :label="`${c.courseCode} ${c.courseName}`" :value="c.id" />
          </el-select>
          <el-button @click="load" :disabled="!courseId">刷新</el-button>
        </div>
        <el-button type="primary" :icon="Plus" :disabled="!courseId" @click="openForm()">新增章节</el-button>
      </div>

      <el-table :data="chapters" v-loading="loading" row-key="id" border default-expand-all
        :tree-props="{ children: 'children' }">
        <el-table-column prop="chapterTitle" label="章节标题" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="hours" label="学时" width="80" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" link @click="openForm(row)">编辑</el-button>
            <el-button size="small" link @click="openContent(row)">内容</el-button>
            <el-button size="small" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑章节' : '新增章节'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="所属课程">
          <el-input :value="courseId" disabled />
        </el-form-item>
        <el-form-item label="父章节" prop="parentId">
          <el-tree-select v-model="form.parentId" :data="chapterOptions" :props="{ value: 'id', label: 'chapterTitle' }"
            check-strictly clearable placeholder="顶级章节" style="width:100%" />
        </el-form-item>
        <el-form-item label="章节标题" prop="chapterTitle"><el-input v-model="form.chapterTitle" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="学时"><el-input-number v-model="form.hours" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="contentDialog" :title="`章节内容 - ${currentChapter?.chapterTitle || ''}`" width="700px">
      <div class="toolbar">
        <el-button type="primary" :icon="Plus" @click="openContentForm()">新增内容</el-button>
      </div>
      <el-table :data="contents" v-loading="contentLoading" border>
        <el-table-column prop="contentTitle" label="标题" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ ['PPT','视频','文档','链接','富文本'][row.contentType - 1] || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="70" />
        <el-table-column prop="hours" label="学时" width="70" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" link @click="openContentForm(row)">编辑</el-button>
            <el-button size="small" link type="danger" @click="removeContent(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="contentFormDialog" :title="contentForm.id ? '编辑内容' : '新增内容'" width="500px">
      <el-form ref="contentFormRef" :model="contentForm" :rules="contentRules" label-width="100px">
        <el-form-item label="标题" prop="contentTitle"><el-input v-model="contentForm.contentTitle" /></el-form-item>
        <el-form-item label="类型" prop="contentType">
          <el-select v-model="contentForm.contentType" style="width:100%">
            <el-option label="课件PPT" :value="1" />
            <el-option label="视频" :value="2" />
            <el-option label="文档" :value="3" />
            <el-option label="链接" :value="4" />
            <el-option label="富文本" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="资源URL"><el-input v-model="contentForm.resourceUrl" /></el-form-item>
        <el-form-item label="富文本内容"><el-input v-model="contentForm.richText" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="contentForm.sort" :min="0" /></el-form-item>
        <el-form-item label="学时"><el-input-number v-model="contentForm.hours" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="contentFormDialog = false">取消</el-button>
        <el-button type="primary" @click="submitContent">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { chapterList, chapterAdd, chapterEdit, chapterRemove, myCourses } from '@/api/course'
import { contentByChapter, contentAdd, contentEdit, contentRemove } from '@/api/course'

const courseList = ref([])
const courseId = ref(null)
const chapters = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, courseId: null, parentId: 0, chapterTitle: '', sort: 0, hours: 0 })
const rules = { chapterTitle: [{ required: true, message: '请输入章节标题' }] }

const contentDialog = ref(false)
const currentChapter = ref(null)
const contents = ref([])
const contentLoading = ref(false)
const contentFormDialog = ref(false)
const contentFormRef = ref()
const contentForm = reactive({ id: null, courseId: null, chapterId: null, contentTitle: '', contentType: 1, resourceUrl: '', richText: '', sort: 0, hours: 0, status: 1 })
const contentRules = { contentTitle: [{ required: true, message: '请输入内容标题' }], contentType: [{ required: true, message: '请选择类型' }] }

const chapterOptions = computed(() => [{ id: 0, chapterTitle: '顶级章节' }, ...chapters.value])

const load = async () => {
  if (!courseId.value) return
  loading.value = true
  try {
    chapters.value = await chapterList(courseId.value)
  } finally { loading.value = false }
}

const openForm = (row) => {
  dialogVisible.value = true
  if (row) Object.assign(form, row)
  else Object.assign(form, { id: null, courseId: courseId.value, parentId: 0, chapterTitle: '', sort: 0, hours: 0 })
}

const submit = async () => {
  await formRef.value.validate()
  form.courseId = courseId.value
  if (form.id) await chapterEdit(form); else await chapterAdd(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确定删除章节"${row.chapterTitle}"吗？`, '提示', { type: 'warning' })
  await chapterRemove(row.id)
  ElMessage.success('删除成功')
  load()
}

const openContent = async (row) => {
  currentChapter.value = row
  contentDialog.value = true
  contentLoading.value = true
  try { contents.value = await contentByChapter(row.id) }
  finally { contentLoading.value = false }
}

const openContentForm = (row) => {
  contentFormDialog.value = true
  if (row) Object.assign(contentForm, row)
  else Object.assign(contentForm, { id: null, courseId: courseId.value, chapterId: currentChapter.value.id, contentTitle: '', contentType: 1, resourceUrl: '', richText: '', sort: 0, hours: 0, status: 1 })
}

const submitContent = async () => {
  await contentFormRef.value.validate()
  if (contentForm.id) await contentEdit(contentForm); else await contentAdd(contentForm)
  ElMessage.success('保存成功')
  contentFormDialog.value = false
  contents.value = await contentByChapter(currentChapter.value.id)
}

const removeContent = async (row) => {
  await ElMessageBox.confirm('确定删除该内容吗？', '提示', { type: 'warning' })
  await contentRemove([row.id])
  contents.value = await contentByChapter(currentChapter.value.id)
}

onMounted(async () => {
  courseList.value = await myCourses()
  if (courseList.value.length) { courseId.value = courseList.value[0].id; load() }
})
</script>
