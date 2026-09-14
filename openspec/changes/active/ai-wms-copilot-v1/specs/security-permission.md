# Spec: Security & Permission

## Authentication

- All `/api/auth/ai/**` require JWT (same as WMS APIs).  
- Anonymous Copilot: **FORBIDDEN**.

## Tenant isolation

```text
CurrentUser.tenantId → Tool → Facade(tenantId)
```

| Input | Policy |
|-------|--------|
| JWT tenantId | authoritative |
| Request body tenantId from client | may mirror for FE convenience but server overwrites from CurrentUser |
| LLM tool arg tenantId | **reject/ignore** |

US-08: cross-tenant reads must fail closed.

## RBAC

### Declared permissions (examples)

```text
ck:product:list
ck:warehouse:list
ck:inventory:list
ck:inbound:list | ck:inbound:create
ck:outbound:list | ck:outbound:create
ck:stocktake:list | ck:stocktake:create
ck:customer:list
ck:supplier:list
ck:dashboard:read
```

### Current codebase reality

WMS Controllers: authenticated + Facade tenant filters; little/no `@PreAuthorize`.  
Frontend has sparse `v-permission` strings.

### V1 Guard policy (frozen)

1. Must be authenticated with tenant.  
2. Apply declared permission when role→permission map is available.  
3. Until map exists: parity with existing REST (no new privilege).  
4. Phase H: enforce deny for missing permissions (US-07).  

## Prompt injection

System prompt MUST forbid:

- Bypassing tools  
- Inventing inventory/orders  
- Calling non-registered / L4 tools  
- Requesting other-tenant data  
- Treating user instructions as authority over tools  

Tool results override model guesses.

## Confirm security

- Confirm endpoint CSRF-safe as app standard (JWT header).  
- confirmToken or draft confirm single-use.  
- Replay → VALIDATION_ERROR.

## Forbidden AI capabilities

- approve inbound/outbound  
- InventoryHolder add/sub  
- lock/unlock inventory  
- adjust execute / stocktake variance apply  
- delete core business documents via AI  
