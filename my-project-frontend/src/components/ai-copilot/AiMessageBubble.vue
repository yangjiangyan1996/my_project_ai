<template>
  <div class="ai-bubble" :class="[`ai-bubble--${message.role}`, `ai-bubble--${message.type}`]">
    <div class="ai-bubble__meta">
      <span class="ai-bubble__who">{{ message.role === 'user' ? '我' : 'AI' }}</span>
      <span class="ai-bubble__time">{{ timeText }}</span>
    </div>

    <AiLoadingMessage v-if="message.type === 'LOADING'" :text="message.content" />

    <AiPermissionCard
      v-else-if="message.type === 'PERMISSION_DENIED'"
      :description="message.content"
    />

    <AiErrorCard
      v-else-if="message.type === 'ERROR'"
      :message="message.content"
      :can-retry="!!message.retryPayload"
      @retry="$emit('retry', message)"
    />

    <template v-else-if="message.role === 'user'">
      <div class="ai-bubble__text">{{ message.content }}</div>
    </template>

    <template v-else>
      <div
        v-if="message.content && message.type !== 'NEED_CLARIFICATION'"
        class="ai-bubble__md"
        v-html="html"
      />
      <div
        v-else-if="message.type === 'NEED_CLARIFICATION' && message.content"
        class="ai-bubble__text"
      >
        {{ message.content }}
      </div>

      <AiCardRenderer
        :message="message"
        @clarify="$emit('clarify', $event)"
        @action="$emit('action', $event)"
      />
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import dayjs from 'dayjs'
import { renderSafeMarkdown } from './markdown'
import AiLoadingMessage from './AiLoadingMessage.vue'
import AiPermissionCard from './AiPermissionCard.vue'
import AiErrorCard from './AiErrorCard.vue'
import AiCardRenderer from './AiCardRenderer.vue'

const props = defineProps({
  message: { type: Object, required: true }
})
defineEmits(['retry', 'clarify', 'action'])

const html = computed(() =>
  props.message.role === 'user'
    ? ''
    : renderSafeMarkdown(props.message.content || '')
)

const timeText = computed(() =>
  dayjs(props.message.createdAt || Date.now()).format('HH:mm')
)
</script>

<style scoped>
.ai-bubble {
  max-width: 92%;
  margin-bottom: 14px;
  padding: 10px 12px;
  border-radius: 10px;
  font-size: 14px;
  line-height: 1.5;
}
.ai-bubble--user {
  margin-left: auto;
  background: var(--el-color-primary-light-7);
}
.ai-bubble--assistant {
  margin-right: auto;
  background: var(--el-fill-color-blank);
  border: 1px solid var(--el-border-color-lighter);
}
.ai-bubble__meta {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  font-size: 11px;
  color: var(--el-text-color-secondary);
  margin-bottom: 4px;
}
.ai-bubble__text {
  white-space: pre-wrap;
  word-break: break-word;
}
.ai-bubble__md :deep(p) {
  margin: 0 0 0.5em;
}
.ai-bubble__md :deep(p:last-child) {
  margin-bottom: 0;
}
.ai-bubble__md :deep(a) {
  color: var(--el-color-primary);
}
.ai-bubble__md :deep(pre) {
  overflow-x: auto;
  padding: 8px;
  background: var(--el-fill-color);
  border-radius: 4px;
}
</style>
