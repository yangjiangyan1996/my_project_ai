/**
 * Map backend CopilotChatResponse → frontend CopilotMessage view model.
 */

let seq = 0
function nextId(prefix = 'm') {
  seq += 1
  return `${prefix}_${Date.now()}_${seq}`
}

export function createUserMessage(text) {
  return {
    id: nextId('u'),
    role: 'user',
    type: 'USER',
    content: text,
    createdAt: Date.now(),
    cards: [],
    actions: [],
    toolCalls: [],
    retryPayload: null
  }
}

export function createLoadingMessage() {
  return {
    id: nextId('l'),
    role: 'assistant',
    type: 'LOADING',
    content: 'AI 正在查询...',
    createdAt: Date.now(),
    cards: [],
    actions: [],
    toolCalls: [],
    retryPayload: null
  }
}

export function adaptBackendResponse(raw, retryPayload = null) {
  if (!raw || typeof raw !== 'object') {
    return {
      id: nextId('a'),
      role: 'assistant',
      type: 'ERROR',
      content: '本次查询没有成功，请重试。',
      createdAt: Date.now(),
      cards: [],
      actions: [],
      toolCalls: [],
      retryPayload
    }
  }

  const type = normalizeType(raw.type)
  let content = raw.message || ''

  if (type === 'PERMISSION_DENIED' && !content) {
    content = '你当前没有权限查询该数据。'
  }
  if (type === 'ERROR' && !content) {
    content = '本次查询没有成功，请重试。'
  }
  if (type === 'NEED_CLARIFICATION' && !content) {
    content = '找到多个匹配结果，请选择后再继续。'
  }

  return {
    id: nextId('a'),
    role: 'assistant',
    type,
    content,
    createdAt: Date.now(),
    cards: Array.isArray(raw.cards) ? raw.cards : [],
    actions: Array.isArray(raw.actions) ? raw.actions : [],
    toolCalls: Array.isArray(raw.toolCalls) ? raw.toolCalls : [],
    draft: raw.draft || null,
    conversationId: raw.conversationId || null,
    usage: raw.usage || null,
    retryPayload: type === 'ERROR' ? retryPayload : null
  }
}

function normalizeType(t) {
  const allowed = [
    'TEXT',
    'TOOL_RESULT',
    'NEED_CLARIFICATION',
    'PERMISSION_DENIED',
    'ERROR',
    'DRAFT'
  ]
  if (allowed.includes(t)) return t
  return 'TEXT'
}

export function createErrorMessage(err, retryPayload = null) {
  const safe =
    (err && err.message && !String(err.message).includes('Exception')
      && !String(err.message).includes('at com.')
      && String(err.message).length < 120)
      ? String(err.message)
      : '本次查询没有成功，请重试。'
  return {
    id: nextId('e'),
    role: 'assistant',
    type: 'ERROR',
    content: safe,
    createdAt: Date.now(),
    cards: [],
    actions: [],
    toolCalls: [],
    retryPayload
  }
}
