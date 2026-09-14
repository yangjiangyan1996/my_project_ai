<template>
  <div ref="listEl" class="ai-message-list">
    <AiEmptyState v-if="!messages.length" @pick="$emit('pick', $event)" />
    <AiMessageBubble
      v-for="m in messages"
      :key="m.id"
      :message="m"
      @retry="$emit('retry', $event)"
      @clarify="$emit('clarify', $event)"
      @action="$emit('action', $event)"
    />
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import AiEmptyState from './AiEmptyState.vue'
import AiMessageBubble from './AiMessageBubble.vue'

const props = defineProps({
  messages: { type: Array, default: () => [] }
})
defineEmits(['pick', 'retry', 'clarify', 'action'])

const listEl = ref(null)

watch(
  () => props.messages.length,
  async () => {
    await nextTick()
    if (listEl.value) {
      listEl.value.scrollTop = listEl.value.scrollHeight
    }
  }
)
</script>

<style scoped>
.ai-message-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px 14px;
  min-height: 0;
}
</style>
