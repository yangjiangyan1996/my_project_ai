# WMS Phase 1 — P0 Correctness & Security Baseline Audit

> Branch: `fix/wms-p0-baseline-20260914`  
> Baseline commit: `ff080f78c9a43a404ce43cd27ae80b2851e4302b`  
> Workspace: CLEAN · Stash NOT restored · Business code NOT modified  
> Companion: `WMS_INVENTORY_MUTATION_AUDIT.md`, `WMS_PHASE1_FIX_PLAN.md`  
> Prior docs re-checked against HEAD (not trusted blindly): `PROJECT_REFACTOR_AUDIT.md`, `WMS_REFACTOR_BACKLOG.md`, `WMS_CLEANUP_FINAL_AUDIT.md`

---

## Executive verdict (short)

Inventory is mutated through **multiple Java paths** with **select→arithmetic→updateById**, no CAS, and **broken approve status gates** on both outbound and inbound. Static stocktake lock is a stub. Security baseline issues (JWT blacklist off, plaintext secrets, anonymous upload whitelist, anonymous tenant list) are real but **inventory correctness is first**.

---

## Prior findings — re-verification

| # | Prior claim | HEAD verdict |
|---|-------------|--------------|
| 1 | Inventory dual-track | **CONFIRMED** — Holder vs `CkInboundFacade` legacy (+ ProductFacade / Adjust helper) |
| 2 | Outbound approve status check fails | **CONFIRMED** — computes `statusName`, never throws |
| 3 | Stocktake lock empty | **CONFIRMED** — `lockInventoryForStock` empty; soft lock only |
| 4 | JWT logout blacklist off | **CONFIRMED** — Redis write/read commented; logout returns success without server invalidation |
| 5 | Dev secrets plaintext | **CONFIRMED** — `application-dev.yml` |
| 6 | Anonymous upload | **CONFIRMED** — `/api/auth/common/**` in `WHITE_URL`; UploadController under that path |
| 7 | Anonymous tenant list | **CONFIRMED** — `GET /api/unauth/tenant/getTenantList`; used by RegisterPage |
| 8 | Tests baseline broken | **CONFIRMED** — only `contextLoads` + needs live MySQL DataSource |

---

## Inventory summary

See `WMS_INVENTORY_MUTATION_AUDIT.md` for full write-entry table, dual-path Q&A, matrix, TX/concurrency/idempotency.

```text
CURRENT_SOURCE_OF_TRUTH:
ck_inventory* balance rows (quantity, lockedQuantity)

EXPECTED_SOURCE_OF_TRUTH:
same tables via ONE gateway + safe updates; transaction table = ledger

InventoryHolder: KEEP (harden)
Direct Facade / ProductFacade mutations: MIGRATE then REMOVE
```

```text
physical / available / locked / frozen:
NOT_FORMALLY_DEFINED — lock path moves qty↔locked while UI uses quantity−locked (CONFLICT)
```

**Negative inventory risk:** YES (race + re-approve)  
**Cross-tenant mutation:** UNKNOWN (queries usually tenant-scoped; updates by id)  
**Idempotency:** HIGH risk

---

## Outbound approve bug (detail)

**Chain:** `approveOk` API → `CkOutboundFacade.approveOk` → `validateAndGetOrder` → unlock → `InventoryHolder.updateSubInventoryForApprove` → status update.

1. Intended allow: `WaitAudit` (1)  
2. Actual allow: any non-missing order that passes tenant check  
3. Dead code: `statusName` assigned, **no throw**  
4. Repeat approve: unlock often no-op; **deduct repeats** → P0-A  

Inbound twin: `validateOrderStatusForApprove` body fully commented → re-approve can **re-add** → P0-A.

---

## Stocktake lock stub (detail)

1. Called from `approveAndInitialize` for `STATIC_TAKE`  
2. Soft row in `ck_stock_take_lock` exists; qty lock does not  
3. Empty because deferred (“consider check in InventoryHolder”) — never done  
4. Intended: block concurrent warehouse ops (comment); soft lock blocks **new stocktake** only  
5. Inbound/outbound/adjust: **not blocked** by stocktake lock  
6. Soft unlock on complete exists; no inventory unlock companion for stub  
7. To implement later: need lock scope (warehouse/SKU/shelf) + Holder/Facade gate or real `lockedQuantity` / freeze flag — **not this audit’s implement step**

---

## Security baseline

### Anonymous upload → recommendation **`REQUIRE_AUTH`**

| Check | Finding |
|-------|---------|
| JWT required? | Path whitelisted → filter skips JWT |
| What files? | Any MultipartFile; extension from original name |
| Size | Global `max-file-size: 5MB` |
| MIME / suffix | No MIME whitelist; extension unchecked |
| Storage | Tencent COS; returns permanent-ish bucket URL |
| Tenant | Uses `UserUtil.getCurrentUser().getId()` in key → **anon likely NPE** today |
| Abuse | Whitelist + weak validation = public upload surface if NPE fixed or auth partial; SVG/HTML risk if COS serves inline |
| Severity | **P1** (broken anon path reduces exploitability; whitelist still wrong) |

### Anonymous tenant list

| Check | Finding |
|-------|---------|
| Login page | Does **not** call it |
| Register page | **Yes** — tenant selector |
| Fields | `id`, `name`, `image` |
| Need anonymous? | Register UX needs some tenant discovery |
| Recommendation | **保留但最小化字段** + **需要验证码/Rate Limit** (or migrate to invite-code / domain login later) |
| Severity | **P1** (tenant enumeration / branding leak) |

### JWT logout

| Item | Finding |
|------|---------|
| Sign | Stateless JWT HMAC, claims include `tenantId`, jti UUID |
| Expire | `spring.security.jwt.expire` hours (dev 72) |
| Logout | Calls `invalidateJwt` → `deleteToken` |
| Blacklist | Redis set **commented**; `isInvalidToken` always `false` |
| Reality | **服务端声称成功，实际仅前端丢弃 Token 才有效**；被盗 Token 在过期前仍可用 |
| Refresh | No separate refresh-token flow found in this pass |
| Redis | Constant `JWT_BLACK_LIST` exists; wiring ready when uncommented |
| Severity | **P1** (elevate P0 if production threat model requires immediate revoke) |

### Secrets (no values in this report)

| Location | Type | Plaintext? | Migrate |
|----------|------|------------|---------|
| `application-dev.yml` | JWT key | YES | env / local overrides gitignored |
| same | DB password | YES | env |
| same | Mail password | YES | env / secret manager |
| same | RabbitMQ password | YES | env |
| same | DeepSeek API key | YES | env |
| same | Tencent secretId/Key | YES | env; rotate if ever committed remotely |
| same | Jimeng access keys | YES | env |
| `application-prod.yml` | same types | mostly `${ENV}` | keep; ensure CI injects |
| Java Config | WHITE_URL only | N/A | — |

**Severity: P0** for any committed real cloud keys in git history exposure; treat as **rotate + stop committing**.

### Test baseline

- `MyProjectBackendApplicationTests.contextLoads` needs real MySQL (`fuye`).  
- MyBatis + MySQL dialect → **prefer Testcontainers MySQL** over H2.  
- Design only this phase (no mass tests).

---

## P0 grading (inventory + security)

### P0-A

- Outbound approve validation no-op → repeat deduct  
- Inbound approve validation empty → repeat add  
- Concurrent inventory races (no CAS)  
- Lock vs available formula conflict (wrong availability)  
- Plaintext third-party secrets in repo (credential leak / abuse)

### P0-B

- Static stocktake inventory lock stub  
- Legacy inbound status=3 path skips shelf layer  

### P1

- JWT blacklist disabled  
- Anonymous upload whitelist  
- Anonymous tenant list (enumeration)  
- Dual/triple mutation paths beyond approve bugs  
- Test suite non-runnable without local DB  

---

## Recommended Change split

| Change | Name | Intent |
|--------|------|--------|
| 1 | `outbound-inbound-approve-status-cas` | Throw on bad status + status CAS update |
| 2 | `inventory-available-model-clarify` | Document + align lock/display formula (minimal) |
| 3 | `stocktake-soft-lock-enforce` | Soft-lock check on inbound/outbound/adjust (minimal) |
| 4 | `inventory-legacy-path-retire` | Stop status=3 / ProductFacade private writers (migrate) |
| 5 | `inventory-cas-update` | `WHERE quantity >=` / version on critical deduct |
| 6 | `wms-security-baseline` | Auth upload, rate-limit tenant list, JWT blacklist, secrets |
| 7 | `wms-test-baseline` | Testcontainers + smoke approve tests |

---

## Fix order (see FIX plan for detail)

```text
FIX-01 outbound approve throw + status CAS
FIX-02 inbound approve status restore + CAS
FIX-03 inventory available/lock model decision (doc + minimal align)
FIX-04 stocktake soft-lock gate on mutations
FIX-05 retire/disable inbound legacy status=3 shelf-incomplete path
FIX-06 deduct/lock CAS concurrency
FIX-07 security baseline
FIX-08 testcontainers baseline
```

**Do not start FIX-01 until explicitly requested.**

---

## PHASE 1 P0 AUDIT — Final block

```text
PHASE 1 P0 AUDIT

Inventory Source of Truth:
ck_inventory / warehouse / batch / shelf quantity(+lockedQuantity); txn = ledger only

Inventory Mutation Paths:
InventoryHolder (approve), CkInboundFacade legacy (status=3), CkInventoryLockService (lock qty move),
InventoryUpdateHelper (adjust), CKProductFacade import helpers

Double-write / Dual-path:
CONFIRMED

Outbound Validation Bug:
CONFIRMED

Stocktake Lock Stub:
CONFIRMED

Negative Inventory Risk:
YES

Cross-tenant Mutation Risk:
UNKNOWN

Idempotency Risk:
HIGH

Anonymous Upload:
P1

Anonymous Tenant API:
P1

JWT Logout:
P1

Plaintext Secrets:
P0

Test Baseline Recommendation:
Testcontainers MySQL (+ later approve/idempotency smoke); avoid H2 as primary

Recommended Fix Order:
1. Outbound approve status throw + CAS
2. Inbound approve status restore + CAS
3. Clarify/align available vs lock quantity model
4. Enforce stocktake soft-lock on in/out/adjust
5. Security baseline (secrets rotate, upload auth, JWT blacklist, tenant rate-limit)
   (+ parallel track: CAS on deduct; retire legacy inbound writer; Testcontainers)

Recommended First Change:
outbound-inbound-approve-status-cas (outbound first if splitting)

Files:
WMS_PHASE1_P0_AUDIT.md
WMS_INVENTORY_MUTATION_AUDIT.md
WMS_PHASE1_FIX_PLAN.md

Business code modified:
NO

Database modified:
NO

Stash restored:
NO

Commit:
NO

Push:
NO
```
