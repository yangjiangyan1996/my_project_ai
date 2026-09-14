package com.example.ai.tool.query.warehouse;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ai.context.AiExecutionContext;
import com.example.ai.exception.AiValidationException;
import com.example.ai.tool.ToolResult;
import com.example.ai.tool.query.AbstractWmsQueryTool;
import com.example.ai.tool.query.WmsQuerySupport;
import com.example.ai.tool.query.dto.AiQueryDtos.AiWarehouseResult;
import com.example.Facade.CkWarehouseFacade;
import com.example.entity.cangku.req.WareHouseListPageReq;
import com.example.entity.cangku.resp.WareHouseResp;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SearchWarehouseTool extends AbstractWmsQueryTool {

    @Resource
    private CkWarehouseFacade warehouseFacade;

    @Override
    public String name() {
        return "search_warehouse";
    }

    @Override
    public String description() {
        return "按名称或编码搜索当前租户仓库。";
    }

    @Override
    public String requiredPermission() {
        return "ck:warehouse:list";
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

        LinkedHashMap<Long, AiWarehouseResult> byId = new LinkedHashMap<>();

        WareHouseListPageReq byName = new WareHouseListPageReq();
        byName.setTenantId(context.getTenantId());
        byName.setUserId(context.getUserId());
        byName.setName(keyword);
        byName.setPage(1);
        byName.setSize(limit);
        merge(byId, warehouseFacade.listOfWareHouse(WmsQuerySupport.page(limit), byName));

        WareHouseListPageReq byCode = new WareHouseListPageReq();
        byCode.setTenantId(context.getTenantId());
        byCode.setUserId(context.getUserId());
        byCode.setCode(keyword);
        byCode.setPage(1);
        byCode.setSize(limit);
        merge(byId, warehouseFacade.listOfWareHouse(WmsQuerySupport.page(limit), byCode));

        List<AiWarehouseResult> all = new ArrayList<>(byId.values());
        List<AiWarehouseResult> items = all.size() > limit ? all.subList(0, limit) : all;
        Map<String, Object> data = new LinkedHashMap<>(WmsQuerySupport.listPayload(items, all.size()));
        data.put("ambiguous", items.size() > 1);
        return ToolResult.ok(data, items.isEmpty() ? "未找到匹配仓库" : "仓库搜索完成");
    }

    private void merge(Map<Long, AiWarehouseResult> byId, Page<WareHouseResp> page) {
        if (page == null || page.getRecords() == null) {
            return;
        }
        for (WareHouseResp w : page.getRecords()) {
            byId.putIfAbsent(w.getId(), map(w));
        }
    }

    static AiWarehouseResult map(WareHouseResp w) {
        return AiWarehouseResult.builder()
                .warehouseId(w.getId()).code(w.getCode()).name(w.getName())
                .address(w.getAddress()).status(w.getStatus()).type(w.getType())
                .build();
    }
}
