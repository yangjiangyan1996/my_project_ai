# Spec: Draft & Confirmation

## Principle

```text
NL → resolve entities → L2 prepare_*_draft → Draft Card → UI 确认创建 → L3 create_*
```

Natural language “好的/确认/创建吧” **never** triggers L3.

## Draft object

| Field | Required | Notes |
|-------|----------|-------|
| draftId | Y | UUID |
| draftType | Y | INBOUND / OUTBOUND / STOCKTAKE |
| tenantId | Y | from CurrentUser |
| userId | Y | creator |
| parsedInput | Y | original NL / structured parse |
| resolvedEntities | Y | ids + display names |
| payload | Y | create-req shaped JSON |
| warnings | Y | e.g. low stock, auto-warehouse |
| validationResult | Y | OK / WARN / BLOCK |
| expireAt | Y | e.g. 30–60 min |
| status | Y | OPEN / CONFIRMED / EXPIRED / CANCELLED |

## Validation

- `BLOCK` → cannot confirm (missing warehouse/customer/items).  
- `WARN` → confirm allowed; warnings shown.  
- Stock insufficient → WARN (or BLOCK if product policy Amendment).  

## Auto-bind rule

If tenant has exactly one enabled warehouse and user omitted warehouse → bind + warning “已自动选择唯一仓库”.  
Never auto-bind among multiple warehouses.

## Confirm UX

Draft Card fields:

- Type, customer/supplier, warehouse, lines (sku, name, qty), warnings  

Buttons:

- 取消 → cancel draft  
- 编辑 → FE edit payload then re-validate (optional V1.1; V1 may cancel+re-ask)  
- 确认创建 → `POST .../drafts/{id}/confirm`  

## Confirm server flow

```text
load draft (tenant+user match)
assert status=OPEN and not expired
assert validation != BLOCK
mark CONFIRMED (CAS)
call matching Facade create
audit riskLevel=L3 confirmedByUser=true
return orderId / orderNo
```

On Facade failure: draft may return to OPEN or CANCELLED per implementation note in tasks (prefer fail with error and leave OPEN once for retry — document in Phase G).

## Storage

V1 default: **Redis** (no mandatory business DB migration).  
MySQL draft table = Amendment.
