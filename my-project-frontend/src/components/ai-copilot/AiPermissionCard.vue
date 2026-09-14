<template>
  <div class="ai-permission-card">
    <el-alert
      title="你当前没有权限查询该数据。"
      type="warning"
      :description="description"
      :closable="false"
      show-icon
    />
  </div>
</template>

<script setup>
defineProps({
  description: { type: String, default: '' }
})
</script>
