package com.example.ai.tool.query.product;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ai.context.AiExecutionContext;
import com.example.ai.exception.AiValidationException;
import com.example.ai.tool.ToolResult;
import com.example.ai.tool.query.AbstractWmsQueryTool;
import com.example.ai.tool.query.WmsQuerySupport;
import com.example.ai.tool.query.dto.AiQueryDtos.AiProductResult;
import com.example.Facade.CKProductFacade;
import com.example.entity.cangku.req.ProductListPageReq;
import com.example.entity.cangku.resp.ProductPageListResp;
import com.example.entity.cangku.resp.ProductSimpleListResp;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SearchProductTool extends AbstractWmsQueryTool {

    @Resource
    private CKProductFacade productFacade;

    @Override
    public String name() {
        return "search_product";
    }

    @Override
    public String description() {
        return "按关键字搜索当前租户商品（名称/SKU）。返回候选项，多结果时需用户选择。";
    }

    @Override
    public String requiredPermission() {
        return "ck:product:list";
    }

    @Override
    public Map<String, Object> inputSchema() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("keyword", Map.of("type", "string", "description", "商品名称或SKU关键字"));
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

        LinkedHashMap<Long, AiProductResult> byId = new LinkedHashMap<>();
        List<ProductSimpleListResp> byName = productFacade.productSimpleList(
                WmsQuerySupport.userInfo(context), keyword);
        if (byName != null) {
            for (ProductSimpleListResp p : byName) {
                byId.putIfAbsent(p.getId(), AiProductResult.builder()
                        .productId(p.getId()).sku(p.getSku()).name(p.getName())
                        .spec(p.getSpec()).color(p.getColor()).build());
            }
        }

        ProductListPageReq req = new ProductListPageReq();
        req.setTenantId(context.getTenantId());
        req.setUserId(context.getUserId());
        req.setSku(keyword);
        req.setPage(1);
        req.setSize(limit);
        Page<ProductPageListResp> bySku = productFacade.pageList(WmsQuerySupport.page(limit), req);
        if (bySku != null && bySku.getRecords() != null) {
            for (ProductPageListResp p : bySku.getRecords()) {
                byId.putIfAbsent(p.getId(), AiProductResult.builder()
                        .productId(p.getId()).sku(p.getSku()).name(p.getName())
                        .spec(p.getSpec()).color(p.getColor()).unitName(p.getUnitName())
                        .status(p.getStatus()).build());
            }
        }

        List<AiProductResult> all = new ArrayList<>(byId.values());
        long total = all.size();
        List<AiProductResult> items = all.size() > limit ? all.subList(0, limit) : all;
        boolean ambiguous = items.size() > 1;
        Map<String, Object> data = new LinkedHashMap<>(WmsQuerySupport.listPayload(items, total));
        data.put("ambiguous", ambiguous);
        return ToolResult.ok(data, items.isEmpty() ? "未找到匹配商品" : "商品搜索完成");
    }
}
