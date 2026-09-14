# Proposal: AI WMS Copilot V1 (`ai-wms-copilot-v1`)

> Status: **SPEC ONLY** — no business code in this Change  
> Branch context: WMS-only product after community cleanup; FIX-01 approve idempotency complete  
> Related prior doc: `AI_WMS_ARCHITECTURE_PROPOSAL.md` (superseded for V1 scope by this Change)

---

## Why

Operators still navigate many menus (商品 / 库存 / 出入库 / 盘点) to answer simple questions and start routine documents. The project already has multi-tenant WMS Facades and a DeepSeek client, but **no AI operation layer**.

Pain:

1. “A001 还有多少库存？” → 找菜单 → 筛条件 → 读表  
2. 登录后不知道今天先处理什么  
3. 出库失败原因散落在校验异常 / 锁库 / 库存页  
4. 建单要填大量字段，容易漏仓、漏客户、漏 SKU

---

## Product position

```text
AI WMS Copilot V1 = 基于现有多租户 WMS 的 AI 操作层
```

Not a chatbot that invents numbers. Not an Agent that mutates stock.

```text
自然语言 → LLM 选 Tool → 现有 Facade/Service → 真实数据 → 解释/草稿 → UI 确认 → 现有 WMS 执行
```

Hard rules:

```text
AI ≠ SQL
AI ≠ Mapper
AI ≠ 绕开 Service
AI ≠ 直接改库存
AI ≠ 自动审核
```

---

## V1 goals (exactly four)

| # | Capability | User value |
|---|------------|------------|
| 1 | Natural-language WMS query | Skip menus for product/warehouse/inventory/inbound/outbound/stocktake |
| 2 | AI daily workspace | “今天要处理什么？” from existing WMS counters |
| 3 | Exception explanation | e.g. why an outbound cannot proceed |
| 4 | Document drafts | inbound / outbound / stocktake draft → **UI confirm** → create |

---

## In Scope

- Logical package `com.example.ai.**` inside existing monolith (no new Maven module required)
- Tool Registry / Executor / Risk levels L0–L3 (L4 disabled)
- LLM abstraction over existing DeepSeek
- Conversation (minimal multi-turn)
- Draft + Confirm contract
- Tenant from CurrentUser only
- Tool permission declaration + enforcement strategy (see Unresolved / freeze)
- Copilot UI: global drawer + dashboard summary + optional page actions
- Audit of tool calls
- Specs / Design / Tasks / Freeze in OpenSpec

---

## Out of Scope (frozen)

- AI auto-approve inbound/outbound  
- AI direct inventory mutation / lock / unlock / adjust  
- AI auto apply stocktake variance  
- Demand forecasting / 补货模型 / 波次 / 路径 / 库位优化 / AGV / IoT / 数字孪生  
- Multi-agent mesh (Inventory Agent, Outbound Agent, …)  
- AdjustTransfer / StockMove from stash  
- Expanding FIX inventory dual-path / available formula (separate Phase 1 fixes)  
- Production deploy / schema-heavy migrations unless Draft storage explicitly chooses DB later  

---

## Business benefit

| Benefit | Measure (V1 qualitative) |
|---------|--------------------------|
| Faster lookup | Seconds vs multi-page navigation |
| Fewer create mistakes | Entity resolution + warnings before confirm |
| Better daily focus | Pending in/out/stocktake + alert summary |
| Safer AI | Confirm gate + no L4 tools |

---

## Risks

| Risk | Mitigation |
|------|------------|
| LLM invents inventory | Tool results are sole facts; prompt + response schema forbid fabrication |
| Cross-tenant leak | Tenant only from JWT CurrentUser; tools ignore AI-supplied tenantId |
| Bypass RBAC via AI API | Tool permission gate (see design); no anonymous AI |
| Accidental create | L3 only after UI Confirm Action (not “好的”) |
| Backend RBAC historically weak | Documented gap; V1 freezes interim + Phase H hardening |
| Latency / cost | Limit tool-call count; log tokens |
| Inventory field semantics CONFLICT | Tools return raw fields; no new formulas in Copilot |

---

## Success = Acceptance Criteria AC-01 … AC-10

See `freeze-baseline.md` and `specs/copilot.md`.

---

## Decision

Proceed to Design / Tasks / Spec freeze for **ai-wms-copilot-v1**. Implementation starts only after Amendment or explicit “start Phase A”.
