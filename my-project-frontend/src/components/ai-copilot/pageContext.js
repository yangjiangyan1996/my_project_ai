/**
 * Build page context from current route — hints only.
 */
export function buildPageContext(route) {
  if (!route) return {}
  const name = route.name || ''
  const id = route.params?.id || null
  const ctx = {
    pageType: mapPageType(name),
    routeName: name,
    path: route.path
  }
  if (id) {
    ctx.entityId = String(id)
  }
  return ctx
}

function mapPageType(name) {
  if (!name) return 'UNKNOWN'
  if (String(name).includes('Outbound') || String(name).includes('outbound')) {
    return name.toString().includes('Edit') || name.toString().includes('Create')
      ? 'OUTBOUND_DETAIL'
      : 'OUTBOUND_LIST'
  }
  if (String(name).includes('Inbound') || String(name).includes('inbound')) {
    return String(name).includes('Edit') || String(name).includes('Create')
      ? 'INBOUND_DETAIL'
      : 'INBOUND_LIST'
  }
  if (String(name).includes('Inventory') || String(name).includes('inventory')) {
    return String(name).includes('History') ? 'INVENTORY_DETAIL' : 'INVENTORY_LIST'
  }
  if (String(name).includes('Stock') || String(name).includes('stock')) {
    return 'STOCKTAKE'
  }
  if (name === 'ckIndex') return 'DASHBOARD'
  return 'OTHER'
}
