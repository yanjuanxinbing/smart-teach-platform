<template>
  <div class="pagination-container">
    <el-pagination
      v-model:current-page="page"
      v-model:page-size="size"
      :page-sizes="[10, 20, 50, 100]"
      :total="total"
      background
      layout="total, sizes, prev, pager, next, jumper"
      @current-change="$emit('change')"
      @size-change="$emit('change')"
    />
  </div>
</template>

<script setup>
defineProps({
  page: { type: Number, default: 1 },
  size: { type: Number, default: 10 },
  total: { type: Number, default: 0 }
})
defineEmits(['update:page', 'update:size', 'change'])
</script>
