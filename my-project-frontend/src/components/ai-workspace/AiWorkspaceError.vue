<template>
  <el-alert
    type="error"
    :title="message || '今日工作台加载失败'"
    show-icon
    :closable="false"
  >
    <template #default>
      <el-button size="small" type="primary" link @click="$emit('retry')">重试</el-button>
    </template>
  </el-alert>
</template>

<script setup>
defineProps({
  message: { type: String, default: '' }
})
defineEmits(['retry'])
</script>
