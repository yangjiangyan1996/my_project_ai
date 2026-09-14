# Phase E Implementation Report — Frontend Copilot

> Branch: `feature/ai-wms-copilot-v1`  
> Phase D backend: still **uncommitted** (kept dirty per review preference)  
> Phase E: **uncommitted** (await review)  
> Do **not** enter Phase F until approved

---

## Scope (Frozen tasks E1–E3)

| Task | Status |
|------|--------|
| E1 Global Drawer Copilot UI | DONE |
| E2 Typed response renderers | DONE |
| E3 Layout entry “AI 仓储助手” | DONE (`App.vue` FAB + Drawer) |

**Not in this Phase (per tasks.md / user Phase E scope):**

- AI Daily Workspace → Phase F  
- Draft / Create / Confirm → Phase G  
- Redis history center / multi-session list  

Optional business context actions (frontend-interaction.md): **implemented** on outbound/inbound/inventory pages.

---

## UX delivered

```text
Logged-in WMS pages
  → fixed FAB「AI 助手」(bottom-right)
  → el-drawer RTL「AI 仓储助手」
  → message list + input + new chat
```

- Login/welcome routes: FAB hidden  
- Mobile: drawer full width; FAB icon-only  
- Enter send / Shift+Enter newline  
- Sending lock prevents double-submit  
- Loading: “AI 正在查询...” (no internal tool names)  
- Quick prompts + empty-state examples  

---

## Response types

| type | UI |
|------|----|
| TEXT | sanitized Markdown |
| TOOL_RESULT | generic / inventory-ish cards |
| NEED_CLARIFICATION | radio options → auto follow-up message |
| PERMISSION_DENIED | warning alert (no stack traces) |
| ERROR | alert + 重新发送 |
| DRAFT | reserved (no create UI) |

Markdown: `markdown-it` + `DOMPurify` (no script/iframe/handlers).

---

## API / security

- `src/api/ai.js` → `POST /api/auth/ai/chat` via `@/net`  
- Request: `conversationId`, `message`, `context` only  
- **Never** sends `tenantId` / `userId` / `permissions`  
- Action allowlist: `VIEW_PRODUCT|WAREHOUSE|INVENTORY|INBOUND|OUTBOUND|STOCKTAKE` → router names (no arbitrary URLs)  
- Page context from route (`pageType`, `entityId`) as hints only  

---

## Components

```text
src/components/ai-copilot/
  AiCopilotRoot.vue
  AiCopilotTrigger.vue
  AiCopilotDrawer.vue
  AiMessageList.vue / AiMessageBubble.vue / AiInputBox.vue
  AiLoadingMessage.vue / AiEmptyState.vue
  AiCardRenderer.vue / AiToolResultCard.vue
  AiClarificationCard.vue / AiErrorCard.vue / AiPermissionCard.vue
  AiContextActionButton.vue
  adaptResponse.js / markdown.js / actionAllowlist.js / pageContext.js
src/api/ai.js
src/composables/useAiCopilot.js
```

Mounted in `App.vue` so detail routes (sibling to CkIndex) still see the FAB.

---

## Business context actions

| Page | Action |
|------|--------|
| Sales outbound edit | AI 分析 (prefill orderNo) |
| Purchase inbound edit | AI 分析 |
| Outbound manage | AI 分析 |
| Inbound manage | AI 分析 |
| Inventory list | AI 分析库存 |

---

## Build / verification

| Check | Result |
|-------|--------|
| `npm run build` | PASS |
| Browser E2E (logged-in WMS) | NOT_TESTED (no browser automation / company env) |
| Real DeepSeek through UI | NOT_TESTED |

---

## Known Gaps

1. Backend rarely emits rich `cards`/`actions` yet — FE renders TEXT + clarification; inventory card heuristics ready when cards arrive  
2. No SSE / stage events — single loading copy only  
3. Conversation history list / Redis UI deferred  
4. Daily Workspace deferred to Phase F  
5. Phase D backend still uncommitted alongside Phase E FE  

---

## Phase F Ready

**YES** — global Copilot shell is in place; Daily Workspace can call the same `useAiCopilot().open(...)` API.

---

## Commit

Phase E: **NO**
