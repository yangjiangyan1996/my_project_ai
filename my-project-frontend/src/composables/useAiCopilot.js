import { reactive, readonly } from 'vue'

/**
 * Global Copilot UI state (singleton).
 * Pages call open({ prefill, context }) without owning the drawer.
 */
const state = reactive({
  open: false,
  conversationId: null,
  messages: [],
  sending: false,
  /** Page business context — hints only, never auth */
  pageContext: {},
  /** Prefill draft for input box */
  pendingPrefill: '',
  /** Auto-send after open when set */
  pendingAutoSend: false
})

export function useAiCopilot() {
  function open(options = {}) {
    state.open = true
    if (options.context && typeof options.context === 'object') {
      state.pageContext = { ...options.context }
    }
    if (options.prefill) {
      state.pendingPrefill = String(options.prefill)
      state.pendingAutoSend = !!options.autoSend
    }
  }

  function close() {
    state.open = false
    state.pendingPrefill = ''
    state.pendingAutoSend = false
  }

  function newConversation() {
    state.conversationId = null
    state.messages = []
    state.pendingPrefill = ''
    state.pendingAutoSend = false
  }

  function setConversationId(id) {
    if (id) state.conversationId = id
  }

  function addMessage(msg) {
    state.messages.push(msg)
  }

  function updateLastMessage(patch) {
    if (!state.messages.length) return
    const last = state.messages[state.messages.length - 1]
    Object.assign(last, patch)
  }

  function removeMessage(id) {
    const i = state.messages.findIndex(m => m.id === id)
    if (i >= 0) state.messages.splice(i, 1)
  }

  function setSending(v) {
    state.sending = !!v
  }

  function consumePrefill() {
    const text = state.pendingPrefill
    const auto = state.pendingAutoSend
    state.pendingPrefill = ''
    state.pendingAutoSend = false
    return { text, autoSend: auto }
  }

  function setPageContext(ctx) {
    state.pageContext = ctx && typeof ctx === 'object' ? { ...ctx } : {}
  }

  return {
    state: readonly(state),
    /** mutable access for internal drawer */
    _state: state,
    open,
    close,
    newConversation,
    setConversationId,
    addMessage,
    updateLastMessage,
    removeMessage,
    setSending,
    consumePrefill,
    setPageContext
  }
}
