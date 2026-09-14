package com.example.ai.draft;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.CKProductFacade;
import com.example.Facade.CkCustomerFacade;
import com.example.Facade.CkInventoryFacade;
import com.example.Facade.CkShelfFacade;
import com.example.Facade.CkSupplierFacade;
import com.example.Facade.CkWarehouseFacade;
import com.example.ai.context.AiExecutionContext;
import com.example.ai.tool.query.WmsQuerySupport;
import com.example.entity.base.UserInfo;
import com.example.entity.cangku.req.CustomerListPageReq;
import com.example.entity.cangku.req.ProductListPageReq;
import com.example.entity.cangku.req.SupplierListPageReq;
import com.example.entity.cangku.req.WareHouseListPageReq;
import com.example.entity.cangku.resp.CustomerEnabledListResp;
import com.example.entity.cangku.resp.CustomerPageListResp;
import com.example.entity.cangku.resp.InventoryBatchResp;
import com.example.entity.cangku.resp.ProductPageListResp;
import com.example.entity.cangku.resp.ProductSimpleListResp;
import com.example.entity.cangku.resp.ShelfEnalbedListResp;
import com.example.entity.cangku.resp.SupplierPageListResp;
import com.example.entity.cangku.resp.WareHouseResp;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolve customer / supplier / warehouse / product via existing Facades only.
 */
@Component
public class DraftEntityResolver {

    @Resource
    private CkCustomerFacade customerFacade;
    @Resource
    private CkSupplierFacade supplierFacade;
    @Resource
    private CkWarehouseFacade warehouseFacade;
    @Resource
    private CKProductFacade productFacade;
    @Resource
    private CkInventoryFacade inventoryFacade;
    @Resource
    private CkShelfFacade shelfFacade;

    public ResolveOutcome resolveCustomer(AiExecutionContext ctx, Long id, String keyword) {
        if (id != null) {
            List<CustomerEnabledListResp> all = customerFacade.listEnable(WmsQuerySupport.userInfo(ctx));
            for (CustomerEnabledListResp c : all) {
                if (id.equals(c.getId())) {
                    return ResolveOutcome.one(Map.of(
                            "customerId", c.getId(),
                            "customerName", nullToEmpty(c.getCustomerName()),
                            "customerCode", nullToEmpty(c.getCustomerCode())
                    ));
                }
            }
            return ResolveOutcome.none("客户不存在或不可用");
        }
        if (keyword == null || keyword.isBlank()) {
            return ResolveOutcome.missing("请选择客户");
        }
        CustomerListPageReq req = new CustomerListPageReq();
        req.setTenantId(ctx.getTenantId());
        req.setUserId(ctx.getUserId());
        req.setCustomerName(keyword.trim());
        req.setPage(1);
        req.setSize(20);
        Page<CustomerPageListResp> page = customerFacade.pageList(WmsQuerySupport.page(20), req);
        List<Map<String, Object>> items = new ArrayList<>();
        if (page != null && page.getRecords() != null) {
            for (CustomerPageListResp c : page.getRecords()) {
                items.add(Map.of(
                        "customerId", c.getId(),
                        "customerName", nullToEmpty(c.getCustomerName()),
                        "customerCode", nullToEmpty(c.getCustomerCode()),
                        "label", nullToEmpty(c.getCustomerName())
                ));
            }
        }
        return fromCandidates(items, "找到多个客户，请选择", "未找到匹配客户");
    }

    public ResolveOutcome resolveSupplier(AiExecutionContext ctx, Long id, String keyword) {
        if (id != null) {
            List<SupplierPageListResp> all = supplierFacade.listEnable(WmsQuerySupport.userInfo(ctx));
            for (SupplierPageListResp s : all) {
                if (id.equals(s.getId())) {
                    return ResolveOutcome.one(Map.of(
                            "supplierId", s.getId(),
                            "supplierName", nullToEmpty(s.getSupplierName()),
                            "supplierCode", nullToEmpty(s.getSupplierCode())
                    ));
                }
            }
            return ResolveOutcome.none("供应商不存在或不可用");
        }
        if (keyword == null || keyword.isBlank()) {
            return ResolveOutcome.missing("请选择供应商");
        }
        SupplierListPageReq req = new SupplierListPageReq();
        req.setTenantId(ctx.getTenantId());
        req.setUserId(ctx.getUserId());
        req.setSupplierName(keyword.trim());
        req.setPage(1);
        req.setSize(20);
        Page<SupplierPageListResp> page = supplierFacade.pageList(WmsQuerySupport.page(20), req);
        List<Map<String, Object>> items = new ArrayList<>();
        if (page != null && page.getRecords() != null) {
            for (SupplierPageListResp s : page.getRecords()) {
                items.add(Map.of(
                        "supplierId", s.getId(),
                        "supplierName", nullToEmpty(s.getSupplierName()),
                        "supplierCode", nullToEmpty(s.getSupplierCode()),
                        "label", nullToEmpty(s.getSupplierName())
                ));
            }
        }
        return fromCandidates(items, "找到多个供应商，请选择", "未找到匹配供应商");
    }

    public ResolveOutcome resolveWarehouse(AiExecutionContext ctx, Long id, String keyword) {
        UserInfo user = WmsQuerySupport.userInfo(ctx);
        if (id != null) {
            WareHouseListPageReq req = new WareHouseListPageReq();
            req.setTenantId(ctx.getTenantId());
            req.setUserId(ctx.getUserId());
            req.setPage(1);
            req.setSize(50);
            Page<WareHouseResp> page = warehouseFacade.listOfWareHouse(WmsQuerySupport.page(50), req);
            if (page != null && page.getRecords() != null) {
                for (WareHouseResp w : page.getRecords()) {
                    if (id.equals(w.getId())) {
                        return ResolveOutcome.one(whMap(w));
                    }
                }
            }
            return ResolveOutcome.none("仓库不存在或不可用");
        }
        if (keyword != null && !keyword.isBlank()) {
            LinkedHashMap<Long, Map<String, Object>> byId = new LinkedHashMap<>();
            WareHouseListPageReq byName = new WareHouseListPageReq();
            byName.setTenantId(ctx.getTenantId());
            byName.setUserId(ctx.getUserId());
            byName.setName(keyword.trim());
            byName.setPage(1);
            byName.setSize(20);
            mergeWh(byId, warehouseFacade.listOfWareHouse(WmsQuerySupport.page(20), byName));
            WareHouseListPageReq byCode = new WareHouseListPageReq();
            byCode.setTenantId(ctx.getTenantId());
            byCode.setUserId(ctx.getUserId());
            byCode.setCode(keyword.trim());
            byCode.setPage(1);
            byCode.setSize(20);
            mergeWh(byId, warehouseFacade.listOfWareHouse(WmsQuerySupport.page(20), byCode));
            return fromCandidates(new ArrayList<>(byId.values()), "找到多个仓库，请选择", "未找到匹配仓库");
        }
        // Auto-bind only when exactly one enabled warehouse
        List<WareHouseResp> enabled = warehouseFacade.listEnable(user);
        if (enabled != null && enabled.size() == 1) {
            WareHouseResp w = enabled.get(0);
            Map<String, Object> m = whMap(w);
            m.put("autoBound", true);
            return ResolveOutcome.one(m, "已自动选择唯一仓库");
        }
        return ResolveOutcome.missing("请选择仓库");
    }

    public ResolveOutcome resolveProduct(AiExecutionContext ctx, Long id, String keyword) {
        if (id != null) {
            ProductListPageReq req = new ProductListPageReq();
            req.setTenantId(ctx.getTenantId());
            req.setUserId(ctx.getUserId());
            req.setPage(1);
            req.setSize(50);
            Page<ProductPageListResp> page = productFacade.pageList(WmsQuerySupport.page(50), req);
            if (page != null && page.getRecords() != null) {
                for (ProductPageListResp p : page.getRecords()) {
                    if (id.equals(p.getId())) {
                        return ResolveOutcome.one(productMap(p.getId(), p.getSku(), p.getName(), p.getSpec(), p.getUnitName()));
                    }
                }
            }
            return ResolveOutcome.none("商品不存在");
        }
        if (keyword == null || keyword.isBlank()) {
            return ResolveOutcome.missing("请指定商品");
        }
        LinkedHashMap<Long, Map<String, Object>> byId = new LinkedHashMap<>();
        List<ProductSimpleListResp> byName = productFacade.productSimpleList(
                WmsQuerySupport.userInfo(ctx), keyword.trim());
        if (byName != null) {
            for (ProductSimpleListResp p : byName) {
                byId.putIfAbsent(p.getId(), productMap(p.getId(), p.getSku(), p.getName(), p.getSpec(), null));
            }
        }
        ProductListPageReq skuReq = new ProductListPageReq();
        skuReq.setTenantId(ctx.getTenantId());
        skuReq.setUserId(ctx.getUserId());
        skuReq.setSku(keyword.trim());
        skuReq.setPage(1);
        skuReq.setSize(20);
        Page<ProductPageListResp> bySku = productFacade.pageList(WmsQuerySupport.page(20), skuReq);
        if (bySku != null && bySku.getRecords() != null) {
            for (ProductPageListResp p : bySku.getRecords()) {
                byId.putIfAbsent(p.getId(), productMap(p.getId(), p.getSku(), p.getName(), p.getSpec(), p.getUnitName()));
            }
        }
        return fromCandidates(new ArrayList<>(byId.values()), "找到多个商品，请选择", "未找到匹配商品");
    }

    public List<InventoryBatchResp> batches(Long warehouseId, Long productId, Long tenantId) {
        return inventoryFacade.batches(warehouseId, productId, tenantId);
    }

    public List<ShelfEnalbedListResp> shelves(Long tenantId, Long warehouseId) {
        return shelfFacade.listEnable(tenantId, warehouseId);
    }

    private static Map<String, Object> productMap(Long id, String sku, String name, String spec, String unit) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("productId", id);
        m.put("sku", nullToEmpty(sku));
        m.put("productName", nullToEmpty(name));
        m.put("name", nullToEmpty(name));
        m.put("spec", nullToEmpty(spec));
        m.put("unit", nullToEmpty(unit));
        m.put("label", nullToEmpty(name) + (sku == null || sku.isBlank() ? "" : " (" + sku + ")"));
        return m;
    }

    private static Map<String, Object> whMap(WareHouseResp w) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("warehouseId", w.getId());
        m.put("warehouseName", nullToEmpty(w.getName()));
        m.put("warehouseCode", nullToEmpty(w.getCode()));
        m.put("label", nullToEmpty(w.getName()));
        return m;
    }

    private static void mergeWh(Map<Long, Map<String, Object>> byId, Page<WareHouseResp> page) {
        if (page == null || page.getRecords() == null) {
            return;
        }
        for (WareHouseResp w : page.getRecords()) {
            byId.putIfAbsent(w.getId(), whMap(w));
        }
    }

    private static ResolveOutcome fromCandidates(List<Map<String, Object>> items, String multiMsg, String noneMsg) {
        if (items == null || items.isEmpty()) {
            return ResolveOutcome.none(noneMsg);
        }
        if (items.size() == 1) {
            return ResolveOutcome.one(items.get(0));
        }
        return ResolveOutcome.ambiguous(items, multiMsg);
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static Long readLong(Object bean, String... getters) {
        if (bean instanceof Map<?, ?> map) {
            for (String g : getters) {
                Object v = map.get(g);
                if (v instanceof Number n) {
                    return n.longValue();
                }
            }
            return null;
        }
        for (String g : getters) {
            try {
                var m = bean.getClass().getMethod("get" + Character.toUpperCase(g.charAt(0)) + g.substring(1));
                Object v = m.invoke(bean);
                if (v instanceof Number n) {
                    return n.longValue();
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static String readString(Object bean, String... getters) {
        if (bean instanceof Map<?, ?> map) {
            for (String g : getters) {
                Object v = map.get(g);
                if (v != null) {
                    return String.valueOf(v);
                }
            }
            return null;
        }
        for (String g : getters) {
            try {
                var m = bean.getClass().getMethod("get" + Character.toUpperCase(g.charAt(0)) + g.substring(1));
                Object v = m.invoke(bean);
                if (v != null) {
                    return String.valueOf(v);
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public enum Kind { OK, MISSING, NONE, AMBIGUOUS }

    public record ResolveOutcome(Kind kind, Map<String, Object> entity, List<Map<String, Object>> candidates,
                                 String message, String autoWarning) {
        static ResolveOutcome one(Map<String, Object> entity) {
            return new ResolveOutcome(Kind.OK, entity, List.of(), null, null);
        }

        static ResolveOutcome one(Map<String, Object> entity, String warning) {
            return new ResolveOutcome(Kind.OK, entity, List.of(), null, warning);
        }

        static ResolveOutcome missing(String msg) {
            return new ResolveOutcome(Kind.MISSING, null, List.of(), msg, null);
        }

        static ResolveOutcome none(String msg) {
            return new ResolveOutcome(Kind.NONE, null, List.of(), msg, null);
        }

        static ResolveOutcome ambiguous(List<Map<String, Object>> items, String msg) {
            return new ResolveOutcome(Kind.AMBIGUOUS, null, items, msg, null);
        }

        public boolean ok() {
            return kind == Kind.OK;
        }
    }

    /** Allocate outbound qty across batch/shelf without inventing inventory math beyond listed quantities. */
    public List<Map<String, Object>> allocateOutboundBatches(Long warehouseId, Long productId, Long tenantId,
                                                             BigDecimal needQty) {
        List<Map<String, Object>> allocations = new ArrayList<>();
        if (needQty == null || needQty.compareTo(BigDecimal.ZERO) <= 0) {
            return allocations;
        }
        BigDecimal remaining = needQty;
        List<InventoryBatchResp> batches = batches(warehouseId, productId, tenantId);
        for (InventoryBatchResp batch : batches) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            if (batch.getShelfList() == null) {
                continue;
            }
            for (InventoryBatchResp.ShelfInfo shelf : batch.getShelfList()) {
                if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }
                BigDecimal avail = shelf.getQuantity() == null ? BigDecimal.ZERO : shelf.getQuantity();
                if (avail.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                BigDecimal take = avail.min(remaining);
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("batchNo", batch.getBatchNo());
                row.put("shelfId", shelf.getShelfId());
                row.put("shelfName", shelf.getShelfName());
                row.put("quantity", take);
                row.put("price", BigDecimal.ZERO);
                allocations.add(row);
                remaining = remaining.subtract(take);
            }
        }
        return allocations;
    }
}
