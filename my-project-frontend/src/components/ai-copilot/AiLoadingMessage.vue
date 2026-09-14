<template>
  <div class="ai-loading">
    <el-icon class="is-loading"><Loading /></el-icon>
    <span>{{ text }}</span>
  </div>
</template>

<script setup>
import { Loading } from '@element-plus/icons-vue'
defineProps({
  text: { type: String, default: 'AI 正在查询...' }
})
</script>

<style scoped>
.ai-loading {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  padding: 8px 12px;
}
</style>
