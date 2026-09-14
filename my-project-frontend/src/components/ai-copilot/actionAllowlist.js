/**
 * Safe business navigation allowlist.
 * Backend must send actionType + entityId — never arbitrary URLs.
 */

const ACTION_ROUTES = {
  VIEW_PRODUCT: (id) => ({ name: 'ckProductManage', query: { highlight: id } }),
  VIEW_WAREHOUSE: () => ({ name: 'ckWarehouseManage' }),
  VIEW_INVENTORY: (id) =>
    id
      ? { name: 'ckInventoryHistory', params: { id: String(id) } }
      : { name: 'ckInventoryList' },
  VIEW_INBOUND: (id) =>
    id
      ? { name: 'ckInboundPurchaseEdit', params: { id: String(id) } }
      : { name: 'ckInboundManage' },
  VIEW_OUTBOUND: (id) =>
    id
      ? { name: 'SalesOutboundCreateEdit', params: { id: String(id) } }
      : { name: 'ckOutboundManage' },
  VIEW_STOCKTAKE: () => ({ name: 'ckStockTake' })
}

export function resolveAction(action) {
  if (!action || typeof action !== 'object') return null
  const type = action.actionType || action.type
  if (!type || !ACTION_ROUTES[type]) return null
  const entityId = action.entityId || action.id || null
  try {
    return ACTION_ROUTES[type](entityId)
  } catch {
    return null
  }
}

export const ACTION_ALLOWLIST = Object.keys(ACTION_ROUTES)
