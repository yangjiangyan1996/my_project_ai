# Phase C Implementation Report — WMS Query Tools

> Branch: `feature/ai-wms-copilot-v1`  
> Phase B commit: `be9b3153b0af540408e222b19ff5d9cddc98eb23`  
> Phase C: **uncommitted** (await review)  
> Do **not** enter Phase D until approved

---

## Goals achieved

First production WMS capabilities for AI Copilot: **15 L0 read-only tools** calling existing Facades/Services, mapped to AI DTOs, registered into `ToolRegistry` via `WmsQueryToolRegistrar`.

Package: `com.example.ai.tool.query.*`

---

## Mapping verification (vs `specs/tool-mapping-matrix.md`)

| Tool | Spec mapping | Live verification | Adjustment |
|------|--------------|-------------------|------------|
| search/get product | YES | Confirmed `CKProductFacade` | SKU via `pageList` compose |
| search warehouse | YES | `listOfWareHouse` name+code | — |
| get_warehouse | PARTIAL | No Facade getById | Service tenant-scoped fallback |
| get_inventory | PARTIAL | `pageList` fields as-is | No productId on Req — resolve sku |
| get_inventory_detail | YES | `batches(warehouseId, productId, tenantId)` | — |
| inbound/outbound L0 | YES | pageList + detail; pending=`WaitAudit` | — |
| stocktake L0 | PARTIAL | `stockDetail` / `pageList` | Post-filter for pending/filters (**CAPABILITY_GAP**) |

**Spec Deviation:** NONE (product Frozen Scope unchanged). Adjustments are Implementation Mapping only.

---

## Security / tenancy / permission

| Control | Status |
|---------|--------|
| Tenant from JWT/`AiExecutionContext` | PASS |
| LLM `tenantId` / `companyId` not trusted | PASS (`ToolExecutor.sanitizeArgs`) |
| Declared `requiredPermission` on each tool | YES (`ck:*:list` targets from matrix) |
| Full RBAC enforcement | **RBAC_MAPPING_GAP** — REST today is JWT-only; Guard enforces codes only when `permissions` set non-empty (Phase H) |
| Not wider than REST for anonymous | PASS (auth+tenant required) |

---

## Side-effect / architecture gate

- Tools depend on **Facades** (and one **CkWareHouseService** query method for get_warehouse).
- Static test `sideEffectGate_toolsDoNotImportMapperOrRepository` in `WmsQueryToolsPhaseCTest`.
- Spring Boot context loads catalog size **15** (verified in `MyProjectBackendApplicationTests` logs).

---

## Tests

| Suite | Result |
|-------|--------|
| `WmsQueryToolsPhaseCTest` | 15 PASS (mocked Facades; tenant assert; NOT_FOUND; pending status; side-effect gate) |
| Phase A/B + Facade approve + App context | PASS |
| Full `mvn clean compile` + `mvn test` | **PASS** |
| DEV_DB integration | **NOT ADDED** (would be `DEV_DB_DEPENDENT`) |
| Real DeepSeek E2E | **NOT_REQUIRED** (orchestrator multi-step loop = Phase D) |

---

## Capability Gaps

1. **CAPABILITY_GAP / get_warehouse:** no public `CkWarehouseFacade.getById` — Service query used.  
2. **CAPABILITY_GAP / stocktake list filters:** `CkStockTakeServiceImpl.getStockPage` ignores most filters; tools post-filter.  
3. **RBAC_MAPPING_GAP:** no reliable menu/permission codes wired from Account yet; declared codes are Phase H targets.  
4. **AMBIGUOUS:** returned as success payload (`ambiguous=true` + candidates), not a separate `ToolErrorCode` (framework has no AMBIGUOUS enum).  
5. Deferred tools: `search_inventory`, customer/supplier search, `explain_outbound_failure`, drafts, Daily Workspace.

---

## Explicitly not done (correct)

- Phase D tool-calling loop  
- Frontend / Vue  
- Draft / Create / L2–L4 tools  
- Inventory formula reinterpretation  
- Business DB / InventoryHolder changes  
- Commit / Push

---

## Reports

- `PHASE_C_WMS_TOOL_CATALOG.md`  
- `PHASE_C_IMPLEMENTATION_REPORT.md` (this file)

---

## Phase D Ready

**YES** — production L0 tools are independently executable through `ToolExecutor`; next phase can wire LLM function-calling loop without expanding write scope.
