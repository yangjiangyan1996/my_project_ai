package com.example.ai.tool.query.supplier;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ai.context.AiExecutionContext;
import com.example.ai.exception.AiValidationException;
import com.example.ai.tool.ToolResult;
import com.example.ai.tool.query.AbstractWmsQueryTool;
import com.example.ai.tool.query.WmsQuerySupport;
import com.example.Facade.CkSupplierFacade;
import com.example.entity.cangku.req.SupplierListPageReq;
import com.example.entity.cangku.resp.SupplierPageListResp;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SearchSupplierTool extends AbstractWmsQueryTool {

    @Resource
    private CkSupplierFacade supplierFacade;

    @Override
    public String name() {
        return "search_supplier";
    }

    @Override
    public String description() {
        return "按名称或编码搜索当前租户供应商（只读）。";
    }

    @Override
    public String requiredPermission() {
        return "ck:supplier:list";
    }

    @Override
    public Map<String, Object> inputSchema() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("keyword", Map.of("type", "string"));
        props.put("limit", Map.of("type", "integer"));
        return schema(List.of("keyword"), props);
    }

    @Override
    protected ToolResult doExecute(Map<String, Object> arguments, AiExecutionContext context) {
        String keyword = WmsQuerySupport.str(arguments, "keyword");
        if (keyword == null) {
            throw new AiValidationException("缺少必填参数: keyword");
        }
        int limit = WmsQuerySupport.limit(arguments);
        LinkedHashMap<Long, Map<String, Object>> byId = new LinkedHashMap<>();

        SupplierListPageReq byName = new SupplierListPageReq();
        byName.setTenantId(context.getTenantId());
        byName.setUserId(context.getUserId());
        byName.setSupplierName(keyword);
        byName.setPage(1);
        byName.setSize(limit);
        merge(byId, supplierFacade.pageList(WmsQuerySupport.page(limit), byName));

        SupplierListPageReq byCode = new SupplierListPageReq();
        byCode.setTenantId(context.getTenantId());
        byCode.setUserId(context.getUserId());
        byCode.setSupplierCode(keyword);
        byCode.setPage(1);
        byCode.setSize(limit);
        merge(byId, supplierFacade.pageList(WmsQuerySupport.page(limit), byCode));

        List<Map<String, Object>> all = new ArrayList<>(byId.values());
        List<Map<String, Object>> items = all.size() > limit ? all.subList(0, limit) : all;
        Map<String, Object> data = new LinkedHashMap<>(WmsQuerySupport.listPayload(items, all.size()));
        data.put("ambiguous", items.size() > 1);
        return ToolResult.ok(data, items.isEmpty() ? "未找到匹配供应商" : "供应商搜索完成");
    }

    private static void merge(Map<Long, Map<String, Object>> byId, Page<SupplierPageListResp> page) {
        if (page == null || page.getRecords() == null) {
            return;
        }
        for (SupplierPageListResp s : page.getRecords()) {
            byId.putIfAbsent(s.getId(), Map.of(
                    "supplierId", s.getId(),
                    "supplierName", s.getSupplierName() == null ? "" : s.getSupplierName(),
                    "supplierCode", s.getSupplierCode() == null ? "" : s.getSupplierCode(),
                    "label", s.getSupplierName() == null ? "" : s.getSupplierName()
            ));
        }
    }
}
