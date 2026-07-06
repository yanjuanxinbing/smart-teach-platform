<template>
  <div class="app-container">
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="query.keyword" placeholder="标题" clearable style="width: 220px" @keyup.enter="load" />
        </div>
        <el-button type="primary" :icon="Plus" @click="openForm()">新增轮播图</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column label="封面" width="160">
          <template #default="{ row }">
            <el-image v-if="row.coverImage" :src="row.coverImage" style="width: 120px; height: 60px" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="置顶" width="80">
          <template #default="{ row }"><el-tag v-if="row.top === 1" type="danger">是</el-tag><span v-else>-</span></template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="170" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="['info','success','warning'][row.status]">{{ ['草稿','已发布','已下线'][row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="openForm(row)">编辑</el-button>
            <el-button size="small" link v-if="row.status === 0" @click="publish(row)">发布</el-button>
            <el-button size="small" link v-if="row.status === 1" @click="offline(row)">下线</el-button>
            <el-button size="small" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page="query.pageNum" v-model:size="query.pageSize" :total="total" @change="load" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑轮播图' : '新增轮播图'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="标题" prop="title"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="封面图" prop="coverImage">
          <el-input v-model="form.coverImage" placeholder="图片URL" />
        </el-form-item>
        <el-form-item label="跳转链接"><el-input v-model="form.linkUrl" placeholder="留空则跳转详情页" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="置顶">
          <el-radio-group v-model="form.top"><el-radio :value="1">是</el-radio><el-radio :value="0">否</el-radio></el-radio-group>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
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
import { portalPage, portalAdd, portalEdit, portalRemove, portalPublish, portalOffline } from '@/api/portal'

const TYPE = 1
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, type: TYPE, keyword: '' })
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, type: TYPE, title: '', coverImage: '', linkUrl: '', content: '', sort: 0, top: 0, status: 0 })
const rules = { title: [{ required: true, message: '请输入标题' }], coverImage: [{ required: true, message: '请填写封面图URL' }] }

const load = async () => {
  loading.value = true
  try { const res = await portalPage(query); list.value = res.list; total.value = res.total }
  finally { loading.value = false }
}

const openForm = (row) => {
  dialogVisible.value = true
  if (row) Object.assign(form, row)
  else Object.assign(form, { id: null, type: TYPE, title: '', coverImage: '', linkUrl: '', content: '', sort: 0, top: 0, status: 0 })
}

const submit = async () => {
  await formRef.value.validate()
  if (form.id) await portalEdit(form); else await portalAdd(form)
  ElMessage.success('保存成功'); dialogVisible.value = false; load()
}
const publish = async (row) => { await portalPublish(row.id); ElMessage.success('已发布'); load() }
const offline = async (row) => { await portalOffline(row.id); ElMessage.success('已下线'); load() }
const remove = async (row) => { await ElMessageBox.confirm(`确定删除"${row.title}"？`, '提示', { type: 'warning' }); await portalRemove([row.id]); ElMessage.success('删除成功'); load() }

onMounted(load)
</script>
