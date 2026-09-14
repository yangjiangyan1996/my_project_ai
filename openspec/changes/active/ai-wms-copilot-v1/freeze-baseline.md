# Freeze Baseline — `ai-wms-copilot-v1`

> Freeze date: 2026-09-14  
> Expanding scope requires **Amendment** under `openspec/changes/active/ai-wms-copilot-v1/amendments/`.

---

## Frozen: V1 Feature Scope

1. Natural-language WMS query (product, warehouse, inventory, inbound, outbound, stocktake)  
2. AI daily workspace (pending + risk summary from existing data)  
3. Exception explanation (`explain_outbound_failure`)  
4. Drafts: inbound / outbound / stocktake → **UI Confirm** → create  

---

## Frozen: Tool Catalog

See `specs/tool-contract.md` and `specs/tool-mapping-matrix.md`.

Product: `search_product`, `get_product`  
Warehouse: `search_warehouse`, `get_warehouse`  
Inventory: `get_inventory`, `get_inventory_detail`, `search_inventory`  
Inbound: `get_inbound_order`, `list_inbound_orders`, `list_pending_inbounds`, `prepare_inbound_draft`, `create_inbound_order`  
Outbound: `get_outbound_order`, `list_outbound_orders`, `list_pending_outbounds`, `explain_outbound_failure`, `prepare_outbound_draft`, `create_outbound_order`  
Stocktake: `get_stocktake_order`, `list_stocktake_orders`, `list_pending_stocktakes`, `prepare_stocktake_draft`, `create_stocktake_order`  
Analysis: `get_daily_warehouse_summary`, `get_my_pending_tasks`, `analyze_inventory_risk`  
Resolution helpers: `search_customer`, `search_supplier`

**No other tools in V1 without Amendment.**

---

## Frozen: Tool Risk Levels

| Level | Meaning | V1 |
|-------|---------|-----|
| L0 | READ | allowed |
| L1 | ANALYSIS | allowed |
| L2 | DRAFT | allowed |
| L3 | CONFIRM_REQUIRED | allowed only with UI confirm |
| L4 | HIGH_RISK (approve / inventory mutate / unlock / adjust) | **DISABLED — not registered** |

---

## Frozen: Draft Confirmation Contract

- L2 creates `AI_DRAFT` only  
- Real orders only via L3 after **确认创建** UI action  
- Natural language affirmation is **not** confirmation  
- Draft expires (`expireAt`); expired cannot confirm  

---

## Frozen: Tenant Isolation

- Tenant from CurrentUser / JWT only  
- AI-supplied `tenantId` ignored/rejected  
- All Facade calls use server-side tenant  

---

## Frozen: RBAC Stance

- Tools declare `permission`  
- Guard always requires authenticated user + tenant  
- Interim: no privilege escalation beyond existing REST reachability  
- Phase H must enforce declared permissions when role map available  
- US-07 remains acceptance target for Phase H exit  

---

## Frozen: No Direct SQL / Mapper / Inventory Mutation / Auto Approve

```text
LLM → Tool → Facade/Service   ONLY
LLM → Mapper                  FORBIDDEN
LLM → SQL                     FORBIDDEN
AI approveOk                  FORBIDDEN
AI InventoryHolder write      FORBIDDEN
AI stocktake variance apply   FORBIDDEN
```

---

## Frozen: Inventory field policy

Tools return **system fields as-is** (`quantity`, `lockedQuantity`, facade-computed `availableQuantity` if present).  
Copilot **must not** invent a new physical/available formula (inventory model CONFLICT is Phase 1 FIX track, not Copilot).

---

## Frozen: Frontend Entry

1. Global Copilot Drawer  
2. AI Daily Workspace on dashboard (`CkIndex` or equivalent)  
3. Business context actions — optional after 1–2  

---

## Frozen: Acceptance Criteria

AC-01 … AC-10 as in `specs/copilot.md`.

---

## Frozen: Out-of-Scope examples (developers must not add)

- 智能补货 / 销量预测  
- 多 Agent  
- 自动审核  
- 库存自动修复  
- AGV / IoT / 数字孪生  
- Stash AdjustTransfer / StockMove as Copilot tools  

---

## Spec Review Gate (self-check)

| Question | Answer |
|----------|--------|
| 是否复用现有 Service/Facade？ | **YES** — mandated |
| Tool 直接 Mapper？ | **NO** |
| AI SQL？ | **NO** |
| AI 自动改库存？ | **NO** |
| AI 自动审核？ | **NO** |
| 多租户？ | **YES** — CurrentUser |
| RBAC？ | **REQUIRED** with **documented interim** (JWT+tenant); Phase H harden |
| 结构化确认？ | **YES** — UI Confirm for L3 |
| Audit？ | **YES** — tool audit required |
| V1 Out-of-Scope defined？ | **YES** |

Gate result: **PASS → FROZEN** (interim RBAC explicitly frozen, not ignored).

---

## Amendment policy

Any of the following requires Amendment file + re-review:

- New tool name  
- Enabling any L4 tool  
- Auto-confirm via chat  
- New Maven microservice split  
- DB schema for drafts (if leaving Redis-first)  
- Multi-agent architecture  
