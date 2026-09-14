package com.example.ai.tool.query.customer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ai.context.AiExecutionContext;
import com.example.ai.exception.AiValidationException;
import com.example.ai.tool.ToolResult;
import com.example.ai.tool.query.AbstractWmsQueryTool;
import com.example.ai.tool.query.WmsQuerySupport;
import com.example.Facade.CkCustomerFacade;
import com.example.entity.cangku.req.CustomerListPageReq;
import com.example.entity.cangku.resp.CustomerPageListResp;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SearchCustomerTool extends AbstractWmsQueryTool {

    @Resource
    private CkCustomerFacade customerFacade;

    @Override
    public String name() {
        return "search_customer";
    }

    @Override
    public String description() {
        return "按名称或编码搜索当前租户客户（只读）。";
    }

    @Override
    public String requiredPermission() {
        return "ck:customer:list";
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

        CustomerListPageReq byName = new CustomerListPageReq();
        byName.setTenantId(context.getTenantId());
        byName.setUserId(context.getUserId());
        byName.setCustomerName(keyword);
        byName.setPage(1);
        byName.setSize(limit);
        merge(byId, customerFacade.pageList(WmsQuerySupport.page(limit), byName));

        CustomerListPageReq byCode = new CustomerListPageReq();
        byCode.setTenantId(context.getTenantId());
        byCode.setUserId(context.getUserId());
        byCode.setCustomerCode(keyword);
        byCode.setPage(1);
        byCode.setSize(limit);
        merge(byId, customerFacade.pageList(WmsQuerySupport.page(limit), byCode));

        List<Map<String, Object>> all = new ArrayList<>(byId.values());
        List<Map<String, Object>> items = all.size() > limit ? all.subList(0, limit) : all;
        Map<String, Object> data = new LinkedHashMap<>(WmsQuerySupport.listPayload(items, all.size()));
        data.put("ambiguous", items.size() > 1);
        return ToolResult.ok(data, items.isEmpty() ? "未找到匹配客户" : "客户搜索完成");
    }

    private static void merge(Map<Long, Map<String, Object>> byId, Page<CustomerPageListResp> page) {
        if (page == null || page.getRecords() == null) {
            return;
        }
        for (CustomerPageListResp c : page.getRecords()) {
            byId.putIfAbsent(c.getId(), Map.of(
                    "customerId", c.getId(),
                    "customerName", c.getCustomerName() == null ? "" : c.getCustomerName(),
                    "customerCode", c.getCustomerCode() == null ? "" : c.getCustomerCode(),
                    "label", c.getCustomerName() == null ? "" : c.getCustomerName()
            ));
        }
    }
}
