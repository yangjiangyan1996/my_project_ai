# AI WMS Copilot V1 — Reality Test Checklist

> Company / staging environment. Not required for Phase CODE COMPLETE.  
> Fill results when real DB + DeepSeek + browser are available.

## Phase A–E (existing)

| ID | Scenario | Result |
|----|----------|--------|
| RT-A01 | AI health authenticated | |
| RT-C01 | search_product real tenant | |
| RT-C02 | get_inventory real qty/locked | |
| RT-D01 | Multi-step tool calling via chat | |
| RT-E01 | FAB + Drawer on logged-in page | |

## Phase F — Daily Workspace

| ID | Scenario | Result |
|----|----------|--------|
| RT-F01 | 首页真实待入库数量 | NOT_TESTED |
| RT-F02 | 首页真实待出库数量 | NOT_TESTED |
| RT-F03 | 首页真实待盘点数量 | NOT_TESTED |
| RT-F04 | AI Summary 与真实数据一致（不夸大数字） | NOT_TESTED |
| RT-F05 | DeepSeek 失败时结构化数据仍显示 | NOT_TESTED |
| RT-F06 | 无权限数据不泄露 | NOT_TESTED |
| RT-F07 | tenant isolation（跨租户不可见） | NOT_TESTED |

### Notes

- Pending inbound/outbound counts use **WaitAudit(1)** (aligned with Phase C pending tools).
- Index page `todo*Approval` historically uses **WaitSubmit(0)** — may differ; see `dataWarnings`.
- Stocktake pending post-filters `UNDER_REVIEW` due to service filter CAPABILITY_GAP.
- Inventory risk V1 = `countsOfIndexPage.lowStock` (minStock rule), not demand forecast.

## Phase G — AI Draft Assistant

| ID | Scenario | Result |
|----|----------|--------|
| RT-G01 | 真实出库自然语言生成草稿 | NOT_TESTED |
| RT-G02 | 草稿商品/客户/仓库解析正确 | NOT_TESTED |
| RT-G03 | 点击确认真实创建出库单 | NOT_TESTED |
| RT-G04 | 重复点击不会重复创建 | NOT_TESTED |
| RT-G05 | 入库草稿 + 确认 | NOT_TESTED |
| RT-G06 | 盘点草稿 + 确认 | NOT_TESTED |
| RT-G07 | Draft TTL | NOT_TESTED |
| RT-G08 | Draft tenant isolation | NOT_TESTED |
| RT-G09 | Draft permission | NOT_TESTED |
| RT-G10 | 创建失败后 Draft recovery（保持 OPEN） | NOT_TESTED |

### Notes

- NL “好的/确认” must **not** create orders; only Draft Card **确认创建**.
- Confirm API body: `confirmToken` only (server reloads Draft).
- Create failure → Draft stays OPEN (Phase G decision).
