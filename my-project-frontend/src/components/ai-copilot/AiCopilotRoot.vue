<template>
  <div v-if="showEntry" class="ai-copilot-root">
    <AiCopilotTrigger :visible="!drawerVisible" @click="onOpen" />
    <AiCopilotDrawer
      ref="drawerRef"
      :visible="drawerVisible"
      :messages="messages"
      :sending="sending"
      :prefill="inputPrefill"
      @close="onClose"
      @send="onSend"
      @pick="onSend"
      @retry="onRetry"
      @clarify="onClarify"
      @action="onAction"
      @new-chat="onNewChat"
    />
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { unauthorized } from '@/net'
import { chat as aiChat } from '@/api/ai'
import { useAiCopilot } from '@/composables/useAiCopilot'
import {
  adaptBackendResponse,
  createErrorMessage,
  createLoadingMessage,
  createUserMessage
} from './adaptResponse'
import { resolveAction } from './actionAllowlist'
import { buildPageContext } from './pageContext'
import AiCopilotTrigger from './AiCopilotTrigger.vue'
import AiCopilotDrawer from './AiCopilotDrawer.vue'

const route = useRoute()
const router = useRouter()
const drawerRef = ref(null)
const inputPrefill = ref('')

const {
  _state,
  open,
  close,
  newConversation,
  setConversationId,
  addMessage,
  removeMessage,
  setSending,
  consumePrefill,
  setPageContext
} = useAiCopilot()

const welcomeNames = new Set(['welcome', 'welcome-login', 'welcome-register', 'welcome-forget'])

const showEntry = computed(() => {
  if (unauthorized()) return false
  if (welcomeNames.has(route.name)) return false
  return true
})

const drawerVisible = computed(() => _state.open)
const messages = computed(() => _state.messages)
const sending = computed(() => _state.sending)

watch(
  () => route.fullPath,
  () => {
    setPageContext(buildPageContext(route))
  },
  { immediate: true }
)

watch(
  () => _state.open,
  (v) => {
    if (!v) return
    const { text, autoSend } = consumePrefill()
    if (text) {
      inputPrefill.value = text
      if (autoSend) {
        onSend(text)
      }
    }
  }
)

function onOpen() {
  setPageContext(buildPageContext(route))
  open()
}

function onClose() {
  close()
}

function onNewChat() {
  newConversation()
  inputPrefill.value = ''
  drawerRef.value?.clearDraft?.()
}

async function onSend(text) {
  const trimmed = (text || '').trim()
  if (!trimmed || _state.sending) return

  setSending(true)
  addMessage(createUserMessage(trimmed))
  drawerRef.value?.clearDraft?.()
  inputPrefill.value = ''

  const loading = createLoadingMessage()
  addMessage(loading)

  const retryPayload = {
    message: trimmed,
    conversationId: _state.conversationId,
    context: { ..._state.pageContext }
  }

  try {
    const raw = await aiChat({
      conversationId: _state.conversationId,
      message: trimmed,
      context: { ..._state.pageContext }
    })
    removeMessage(loading.id)
    const adapted = adaptBackendResponse(raw, retryPayload)
    if (adapted.conversationId) {
      setConversationId(adapted.conversationId)
    }
    addMessage(adapted)
  } catch (e) {
    removeMessage(loading.id)
    addMessage(createErrorMessage(e, retryPayload))
  } finally {
    setSending(false)
  }
}

function onRetry(msg) {
  if (!msg?.retryPayload?.message || _state.sending) return
  onSend(msg.retryPayload.message)
}

function onClarify({ label, option }) {
  // Prefer human-readable label; attach id hint in natural language when present
  let text = `我选择：${label}`
  if (option && typeof option === 'object') {
    const id =
      option.warehouseId ||
      option.productId ||
      option.orderId ||
      option.stockTakeId ||
      option.id
    if (id != null) {
      text = `我选择：${label}（id=${id}）`
    }
  }
  onSend(text)
}

function onAction(action) {
  const loc = resolveAction(action)
  if (!loc) return
  router.push(loc).catch(() => {})
}
</script>
