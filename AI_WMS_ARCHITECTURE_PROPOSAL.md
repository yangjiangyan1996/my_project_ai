# AI WMS Architecture Proposal

> 基于本轮瘦身后的纯 WMS 单体，规划 AI Native 演进。  
> 原则：LLM 不直连生产 SQL；所有写操作走现有 Application/Facade + 多租户 + 事务。

---

## 1. 目标愿景

```text
传统 WMS → 结构化数据 → AI Copilot → 分析/预警 → 决策建议 → Agent（有限自动）
```

当前阶段：**基础设施保留，业务能力只读规划，不大规模开发。**

---

## 2. 目标架构

```text
                 Vue3 (CkIndex + Copilot Panel)
                   │
                   ▼
            /api/auth/ai/**  (未来)
                   │
            AI Orchestrator
                   │
        ┌──────────┼──────────┐
        ▼          ▼          ▼
   Query Tools  Analytics   Action Tools
   (READ)       (READ)      (DRAFT/CONFIRM)
        │          │          │
        └──────────┼──────────┘
                   ▼
        现有 Ck*Facade / InventoryHolder
                   │
             MySQL / Redis
```

**禁止：** `LLM → 直接 JDBC/SQL`  
**必须：** `LLM → Tool → Facade → Domain → Mapper`

---

## 3. 八大能力映射

| 能力 | 阶段 | 依赖现有能力 |
|------|------|--------------|
| 1 WMS Copilot 问答 | Phase 3 | Inventory/Inbound/Outbound Facade 查询 |
| 2 库存智能分析 | Phase 5 | `CkInventoryFacade` 预警/批次 |
| 3 智能补货建议 | Phase 5 | 流水 + 库存 +（未来销量视图） |
| 4 异常预警 | Phase 5 | alerts + lock + stock-take diff |
| 5 AI 单据助手（草稿） | Phase 4 | Inbound/Outbound create API |
| 6 原因解释 | Phase 3 | Lock/Available 明细 |
| 7 自然语言报表 | Phase 5 | 聚合查询 + LLM 总结 |
| 8 WMS Agent | Phase 6 | 确认态 + 审计 + RBAC |

---

## 4. Tool Catalog（优先复用现有 Service）

| Tool Name | 用途 | 调用 | 输入 | 输出 | 权限 | 只读 | 风险 | 人工确认 |
|-----------|------|------|------|------|------|------|------|----------|
| `get_inventory` | 商品库存总览 | `CkInventoryFacade` | tenantId, productId/sku | qty/available/locked | tenant | Y | READ | N |
| `get_available_inventory` | 可用量 | Lock + Inventory | warehouse/shelf/batch | available | tenant | Y | READ | N |
| `get_inventory_by_warehouse` | 分仓库存 | InventoryWarehouse | warehouseId | list | tenant | Y | READ | N |
| `get_inventory_batches` | 批次 | `/inventory/batches` | productId | batches | tenant | Y | READ | N |
| `list_pending_inbounds` | 待审核入库 | InboundFacade pageList | status=WaitAudit | page | tenant | Y | READ | N |
| `list_pending_outbounds` | 待审核出库 | OutboundFacade pageList | status | page | tenant | Y | READ | N |
| `get_inbound_order` | 入库详情 | InboundFacade detail | id | detail | tenant | Y | READ | N |
| `get_outbound_order` | 出库详情 | OutboundFacade detail | id | detail | tenant | Y | READ | N |
| `explain_outbound_block` | 为何不能出库 | checkBatchAllocation + locks | orderId/sku | 结构化原因 | tenant | Y | READ | N |
| `analyze_inventory_alerts` | 低库存/异常 | alerts/alertStats | level | list+stats | tenant | Y | READ | N |
| `analyze_slow_moving` | 呆滞（未来） | transaction 聚合 | days | list | tenant | Y | READ | N |
| `create_inbound_draft` | 入库草稿 | InboundFacade.create(status草稿) | draft DTO | draftId | tenant+role | N | DRAFT | Y |
| `create_outbound_draft` | 出库草稿 | Outbound create | draft DTO | draftId | tenant+role | N | DRAFT | Y |
| `create_transfer_draft` | 转移草稿 | AdjustTransferExt | draft | draftId | tenant+role | N | DRAFT | Y |
| `create_stocktake_draft` | 盘点草稿 | StockFacade.create | scope | draftId | tenant+role | N | DRAFT | Y |
| `confirm_execute_draft` | 确认执行草稿 | 对应 approve/execute | draftId | result | elevated | N | CONFIRM_REQUIRED | Y |
| `auto_approve_inbound` | 自动审核（远期） | approveOk | id | ok | agent+policy | N | AUTO_EXECUTE | Policy |

风险级别：`READ < SUGGEST < DRAFT < CONFIRM_REQUIRED < AUTO_EXECUTE`

---

## 5. AI Infrastructure（本轮状态）

| 组件 | 状态 |
|------|------|
| `DeepSeekUtils` / `DeepSeekConfig` / WebClient | **保留** |
| `JimengFacade` / `JMConfig` | **保留**（图片能力壳） |
| `DeepSeekContentService` / AutoPublish 社区文案 | **已删除** |
| Token Usage / Gateway / Tool Registry | **待建**（Phase 3） |

---

## 6. Roadmap

### Phase 0 — 业务瘦身 ✅（本轮）
删除社区/圈子/聊天/成就，保留 WMS + AI Client。

### Phase 1 — WMS P0
库存一致性、出库状态校验、盘点锁库、JWT/密钥、测试基线。

### Phase 2 — 模块化单体
`master-data` / `inbound` / `outbound` / `inventory` / `stocktake` / `tenant` / `reporting`

### Phase 3 — AI Read-Only Copilot
仅 Query + Explain Tools；UI 侧边对话；全量审计日志。

### Phase 4 — AI Draft
生成入出库/调拨/盘点草稿；人工确认后走现有 Facade。

### Phase 5 — AI Intelligence
缺货/超储/呆滞/库龄/周转/补货建议/异常检测。

### Phase 6 — WMS Agent
按 Tool 风险分级开放有限 AUTO_EXECUTE；强 RBAC + 二次确认 + 可回放审计。

---

## 7. 安全约束

1. 每个 Tool 强制注入 `tenantId`（来自 JWT，禁止模型改写）。  
2. 写工具默认 `CONFIRM_REQUIRED`。  
3. 禁止模型拼 SQL；分析用预置聚合查询或只读 View。  
4. Prompt 不落密钥；调用统一 Gateway。  
5. 所有 Agent 动作写入 `ck_operation_log`（或独立 `ai_audit_log`）。
