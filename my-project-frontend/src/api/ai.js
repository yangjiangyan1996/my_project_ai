import { post, get } from '@/net'

/**
 * AI Copilot API — never send tenantId / userId / permissions.
 */
export function chat({ conversationId, message, context }) {
  return post('/api/auth/ai/chat', {
    conversationId: conversationId || null,
    message,
    context: context || {}
  })
}

export function getDailyWorkspace() {
  return get('/api/auth/ai/daily-workspace')
}

/**
 * Confirm draft — server reloads Draft; do not send business payload.
 */
export function confirmDraft(draftId, confirmToken) {
  return post(`/api/auth/ai/drafts/${draftId}/confirm`, {
    confirmToken
  })
}

export function cancelDraft(draftId) {
  return post(`/api/auth/ai/drafts/${draftId}/cancel`, {})
}

