<template>
  <div class="app-container">
    <el-card>
      <el-tabs v-model="tab" @tab-change="onTabChange">
        <el-tab-pane label="字典类型" name="type">
          <div class="toolbar">
            <div class="toolbar-left">
              <el-input v-model="typeQuery.keyword" placeholder="名称/类型" clearable style="width: 220px" @keyup.enter="loadType" />
              <el-button type="primary" @click="loadType">搜索</el-button>
            </div>
            <el-button type="primary" :icon="Plus" @click="openTypeForm()">新增类型</el-button>
          </div>
          <el-table :data="typeList" v-loading="typeLoading" border>
            <el-table-column prop="dictName" label="字典名称" />
            <el-table-column prop="dictType" label="字典类型" width="200" />
            <el-table-column prop="description" label="描述" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button size="small" link @click="openDataByType(row.dictType)">数据</el-button>
                <el-button size="small" link @click="openTypeForm(row)">编辑</el-button>
                <el-button size="small" link type="danger" @click="removeType(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <Pagination v-model:page="typeQuery.pageNum" v-model:size="typeQuery.pageSize" :total="typeTotal" @change="loadType" />
        </el-tab-pane>

        <el-tab-pane label="字典数据" name="data">
          <div class="toolbar">
            <div class="toolbar-left">
              <el-select v-model="dataQuery.dictType" placeholder="选择字典类型" filterable style="width: 240px">
                <el-option v-for="t in typeList" :key="t.dictType" :label="`${t.dictName} (${t.dictType})`" :value="t.dictType" />
              </el-select>
              <el-button type="primary" @click="loadData">加载</el-button>
            </div>
            <el-button type="primary" :icon="Plus" @click="openDataForm()">新增数据</el-button>
          </div>
          <el-table :data="dataList" v-loading="dataLoading" border>
            <el-table-column prop="dictLabel" label="标签" />
            <el-table-column prop="dictValue" label="值" width="160" />
            <el-table-column prop="listClass" label="样式" width="120">
              <template #default="{ row }">
                <el-tag v-if="row.listClass" :type="row.listClass">{{ row.dictLabel }}</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="sort" label="排序" width="80" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button size="small" link @click="openDataForm(row)">编辑</el-button>
                <el-button size="small" link type="danger" @click="removeData(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <Pagination v-model:page="dataQuery.pageNum" v-model:size="dataQuery.pageSize" :total="dataTotal" @change="loadData" />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="typeDialog" :title="typeForm.id ? '编辑字典类型' : '新增字典类型'" width="500px">
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="100px">
        <el-form-item label="字典名称" prop="dictName"><el-input v-model="typeForm.dictName" /></el-form-item>
        <el-form-item label="字典类型" prop="dictType"><el-input v-model="typeForm.dictType" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="typeForm.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="状态"><el-radio-group v-model="typeForm.status"><el-radio :value="1">启用</el-radio><el-radio :value="0">禁用</el-radio></el-radio-group></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialog = false">取消</el-button>
        <el-button type="primary" @click="submitType">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="dataDialog" :title="dataForm.id ? '编辑字典数据' : '新增字典数据'" width="500px">
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="100px">
        <el-form-item label="字典类型" prop="dictType"><el-input v-model="dataForm.dictType" /></el-form-item>
        <el-form-item label="字典标签" prop="dictLabel"><el-input v-model="dataForm.dictLabel" /></el-form-item>
        <el-form-item label="字典值" prop="dictValue"><el-input v-model="dataForm.dictValue" /></el-form-item>
        <el-form-item label="样式">
          <el-select v-model="dataForm.listClass" style="width:100%">
            <el-option label="default" value="default" />
            <el-option label="primary" value="primary" />
            <el-option label="success" value="success" />
            <el-option label="info" value="info" />
            <el-option label="warning" value="warning" />
            <el-option label="danger" value="danger" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="dataForm.sort" :min="0" /></el-form-item>
        <el-form-item label="状态"><el-radio-group v-model="dataForm.status"><el-radio :value="1">启用</el-radio><el-radio :value="0">禁用</el-radio></el-radio-group></el-form-item>
        <el-form-item label="备注"><el-input v-model="dataForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataDialog = false">取消</el-button>
        <el-button type="primary" @click="submitData">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { dictTypePage, dictTypeAdd, dictTypeEdit, dictTypeRemove, dictDataPage, dictDataAdd, dictDataEdit, dictDataRemove } from '@/api/system'

const tab = ref('type')

const typeList = ref([]); const typeTotal = ref(0); const typeLoading = ref(false)
const typeQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const typeDialog = ref(false); const typeFormRef = ref()
const typeForm = reactive({ id: null, dictName: '', dictType: '', description: '', status: 1 })
const typeRules = { dictName: [{ required: true, message: '请输入' }], dictType: [{ required: true, message: '请输入' }] }

const dataList = ref([]); const dataTotal = ref(0); const dataLoading = ref(false)
const dataQuery = reactive({ pageNum: 1, pageSize: 10, dictType: '' })
const dataDialog = ref(false); const dataFormRef = ref()
const dataForm = reactive({ id: null, dictType: '', dictLabel: '', dictValue: '', listClass: 'default', sort: 0, status: 1, isDefault: 0, remark: '' })
const dataRules = { dictType: [{ required: true, message: '请输入' }], dictLabel: [{ required: true, message: '请输入' }], dictValue: [{ required: true, message: '请输入' }] }

const loadType = async () => { typeLoading.value = true; try { const res = await dictTypePage(typeQuery); typeList.value = res.list; typeTotal.value = res.total } finally { typeLoading.value = false } }
const openTypeForm = (row) => { typeDialog.value = true; if (row) Object.assign(typeForm, row); else Object.assign(typeForm, { id: null, dictName: '', dictType: '', description: '', status: 1 }) }
const submitType = async () => { await typeFormRef.value.validate(); if (typeForm.id) await dictTypeEdit(typeForm); else await dictTypeAdd(typeForm); ElMessage.success('保存成功'); typeDialog.value = false; loadType() }
const removeType = async (row) => { await ElMessageBox.confirm(`确定删除"${row.dictName}"？`, '提示', { type: 'warning' }); await dictTypeRemove([row.id]); ElMessage.success('删除成功'); loadType() }

const loadData = async () => { if (!dataQuery.dictType) return; dataLoading.value = true; try { const res = await dictDataPage(dataQuery); dataList.value = res.list; dataTotal.value = res.total } finally { dataLoading.value = false } }
const openDataByType = (dictType) => { dataQuery.dictType = dictType; tab.value = 'data'; loadData() }
const openDataForm = (row) => { dataDialog.value = true; if (row) Object.assign(dataForm, row); else Object.assign(dataForm, { id: null, dictType: dataQuery.dictType, dictLabel: '', dictValue: '', listClass: 'default', sort: 0, status: 1, isDefault: 0, remark: '' }) }
const submitData = async () => { await dataFormRef.value.validate(); if (dataForm.id) await dictDataEdit(dataForm); else await dictDataAdd(dataForm); ElMessage.success('保存成功'); dataDialog.value = false; loadData() }
const removeData = async (row) => { await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }); await dictDataRemove([row.id]); ElMessage.success('删除成功'); loadData() }

const onTabChange = (n) => { if (n === 'data' && dataQuery.dictType) loadData() }

onMounted(loadType)
</script>
