# Spec: Observability

## Metrics / logs (V1)

Per chat turn / tool call:

| Field | Required |
|-------|----------|
| conversationId | Y |
| userId | Y |
| tenantId | Y |
| userQuestion | Y (truncate) |
| model | Y |
| toolName | Y if tool |
| toolInput summary | Y redacted |
| toolResult summary | Y size-capped |
| latencyMs | Y |
| success | Y |
| riskLevel | Y |
| confirmedByUser | Y for L3 |
| inputToken / outputToken | Y if available |
| toolCallCount | Y |

## Sinks

1. Application logs (structured)  
2. Optional persist: Redis list or extend `ck_operation_log` with module=`AI_COPILOT`  
3. No full PII dumps / full stock tables  

## Performance targets

| Path | Target |
|------|--------|
| Single L0 tool | P95 < 3s |
| Full NL answer | 3–8s typical |
| Heavy L1 | may exceed; log slow calls |

## Cost

Record tokens; **do not** change global billing system in V1.

## Error taxonomy (for dashboards)

`BUSINESS_ERROR | PERMISSION_ERROR | VALIDATION_ERROR | NOT_FOUND | TIMEOUT | SYSTEM_ERROR`
