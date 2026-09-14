# Spec: Tool Contract

## Risk levels

| Code | Name | Auto-run | Confirm |
|------|------|----------|---------|
| L0 | READ | yes | no |
| L1 | ANALYSIS | yes | no |
| L2 | DRAFT | yes | produces draft only |
| L3 | CONFIRM_REQUIRED | no | UI Confirm Action |
| L4 | HIGH_RISK | n/a | **not in V1 registry** |

## Required metadata

```text
name, description, riskLevel, permission,
inputSchema, outputSchema, timeoutMs, audit=true
```

## Catalog (V1)

### Product — L0
- `search_product` — name/sku/code  
- `get_product` — productId  

### Warehouse — L0
- `search_warehouse`  
- `get_warehouse`  

### Inventory — L0
- `get_inventory` — productId/sku, optional warehouseId; output raw quantity/lockedQuantity (+ available if Facade provides)  
- `get_inventory_detail` — warehouse/batch/shelf  
- `search_inventory` — low stock / product / warehouse filters  

### Inbound
- `get_inbound_order` L0  
- `list_inbound_orders` L0  
- `list_pending_inbounds` L0 (status=WaitAudit unless Amendment)  
- `prepare_inbound_draft` L2  
- `create_inbound_order` L3  

### Outbound
- `get_outbound_order` L0  
- `list_outbound_orders` L0  
- `list_pending_outbounds` L0  
- `explain_outbound_failure` L1  
- `prepare_outbound_draft` L2  
- `create_outbound_order` L3 (type-specific Facade)  

### Stocktake
- `get_stocktake_order` L0  
- `list_stocktake_orders` L0  
- `list_pending_stocktakes` L0 (semantics documented in tool description)  
- `prepare_stocktake_draft` L2  
- `create_stocktake_order` L3  

### Analysis — L1
- `get_daily_warehouse_summary`  
- `get_my_pending_tasks`  
- `analyze_inventory_risk` — alerts rules only; LLM does not invent risk metrics  

### Resolution helpers — L0
- `search_customer`  
- `search_supplier`  

## Execution rules

1. Executor injects tenant/user from context.  
2. Strip/ignore model-provided `tenantId`.  
3. L3 requires valid confirm binding.  
4. On Facade exception → typed ToolError; do not swallow.  
5. Tool results are facts for LLM summarization only.

## Mapping

See `tool-mapping-matrix.md`.
