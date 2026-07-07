<template>
  <div class="pagination-container">
    <el-pagination
      :current-page="page"
      :page-size="size"
      :page-sizes="[10, 20, 50, 100]"
      :total="total"
      background
      layout="total, sizes, prev, pager, next, jumper"
      @current-change="onPageChange"
      @size-change="onSizeChange"
    />
  </div>
</template>

<script setup>
const props = defineProps({
  page: { type: Number, default: 1 },
  size: { type: Number, default: 10 },
  total: { type: Number, default: 0 }
})
const emit = defineEmits(['update:page', 'update:size', 'change'])

const onPageChange = (val) => {
  emit('update:page', val)
  emit('change')
}
const onSizeChange = (val) => {
  emit('update:size', val)
  emit('change')
}
</script>