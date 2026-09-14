import MarkdownIt from 'markdown-it'
import DOMPurify from 'dompurify'

const md = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true
})

/**
 * Render markdown → sanitized HTML (no script / iframe / handlers).
 */
export function renderSafeMarkdown(text) {
  if (!text) return ''
  const raw = md.render(String(text))
  return DOMPurify.sanitize(raw, {
    ALLOWED_TAGS: [
      'p', 'br', 'strong', 'em', 'ul', 'ol', 'li', 'code', 'pre',
      'blockquote', 'a', 'h1', 'h2', 'h3', 'h4', 'span'
    ],
    ALLOWED_ATTR: ['href', 'title', 'target', 'rel', 'class'],
    ALLOW_DATA_ATTR: false
  })
}
