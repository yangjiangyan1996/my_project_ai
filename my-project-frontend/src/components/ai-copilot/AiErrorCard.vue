<template>
  <div class="ai-error-card">
    <el-alert :title="message" type="error" :closable="false" show-icon />
    <el-button v-if="canRetry" size="small" type="primary" link @click="$emit('retry')">
      重新发送
    </el-button>
  </div>
</template>

<script setup>
defineProps({
  message: { type: String, default: '本次查询没有成功，请重试。' },
  canRetry: { type: Boolean, default: false }
})
defineEmits(['retry'])
</script>

<style scoped>
.ai-error-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
</style>
