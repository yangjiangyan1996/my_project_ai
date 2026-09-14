<template>
  <el-drawer
    :model-value="visible"
    title="AI 仓储助手"
    direction="rtl"
    size="420px"
    append-to-body
    destroy-on-close
    class="ai-copilot-drawer"
    @close="$emit('close')"
  >
    <template #header>
      <div class="ai-drawer-header">
        <span class="ai-drawer-header__title">AI 仓储助手</span>
        <el-button size="small" text type="primary" @click="$emit('new-chat')">
          + 新对话
        </el-button>
      </div>
    </template>

    <div class="ai-drawer-body">
      <AiMessageList
        :messages="messages"
        :draft-busy="draftBusy"
        @pick="$emit('pick', $event)"
        @retry="$emit('retry', $event)"
        @clarify="$emit('clarify', $event)"
        @action="$emit('action', $event)"
        @draft-edit="$emit('draft-edit', $event)"
        @draft-cancel="$emit('draft-cancel', $event)"
        @draft-confirm="$emit('draft-confirm', $event)"
      />
      <AiInputBox
        ref="inputBox"
        v-model="draft"
        :sending="sending"
        @send="$emit('send', $event)"
      />
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import AiMessageList from './AiMessageList.vue'
import AiInputBox from './AiInputBox.vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  messages: { type: Array, default: () => [] },
  sending: { type: Boolean, default: false },
  draftBusy: { type: Boolean, default: false },
  prefill: { type: String, default: '' }
})
defineEmits([
  'close',
  'send',
  'pick',
  'retry',
  'clarify',
  'action',
  'new-chat',
  'draft-edit',
  'draft-cancel',
  'draft-confirm'
])

const draft = ref('')
const inputBox = ref(null)

watch(
  () => props.prefill,
  (v) => {
    if (v) draft.value = v
  },
  { immediate: true }
)

watch(
  () => props.visible,
  async (v) => {
    if (v) {
      await nextTick()
      inputBox.value?.focus?.()
    }
  }
)

function clearDraft() {
  draft.value = ''
}

defineExpose({ clearDraft, setDraft: (t) => { draft.value = t || '' } })
</script>

<style scoped>
.ai-drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding-right: 8px;
}
.ai-drawer-header__title {
  font-weight: 600;
  font-size: 16px;
}
.ai-drawer-body {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 80px);
  margin: -20px;
}
</style>

<style>
.ai-copilot-drawer .el-drawer__body {
  padding: 0 !important;
  overflow: hidden;
}
@media (max-width: 768px) {
  .ai-copilot-drawer.el-drawer.rtl {
    width: 100% !important;
  }
}
</style>
