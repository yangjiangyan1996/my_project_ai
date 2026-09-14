package com.example.ai.tool.query.warehouse;

import com.example.ai.context.AiExecutionContext;
import com.example.ai.exception.AiValidationException;
import com.example.ai.tool.ToolErrorCode;
import com.example.ai.tool.ToolResult;
import com.example.ai.tool.query.AbstractWmsQueryTool;
import com.example.ai.tool.query.WmsQuerySupport;
import com.example.ai.tool.query.dto.AiQueryDtos.AiWarehouseResult;
import com.example.Facade.CkWarehouseFacade;
import com.example.entity.cangku.resp.WareHouseResp;
import com.example.service.CkWareHouseService;
import com.example.entity.cangku.dto.Warehouse;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * get_warehouse: no public Facade getById — uses tenant-scoped {@link CkWareHouseService}
 * (Service layer, not Mapper). CAPABILITY_GAP noted: prefer Facade when available later.
 */
@Component
public class GetWarehouseTool extends AbstractWmsQueryTool {

    @Resource
    private CkWareHouseService wareHouseService;
    @Resource
    private CkWarehouseFacade warehouseFacade;

    @Override
    public String name() {
        return "get_warehouse";
    }

    @Override
    public String description() {
        return "按 warehouseId 查询当前租户仓库详情。";
    }

    @Override
    public String requiredPermission() {
        return "ck:warehouse:list";
    }

    @Override
    public Map<String, Object> inputSchema() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("warehouseId", Map.of("type", "integer"));
        return schema(List.of("warehouseId"), props);
    }

    @Override
    protected ToolResult doExecute(Map<String, Object> arguments, AiExecutionContext context) {
        Long warehouseId = WmsQuerySupport.lng(arguments, "warehouseId");
        if (warehouseId == null) {
            throw new AiValidationException("缺少必填参数: warehouseId");
        }
        // Prefer listEnable (Facade) match; fallback tenant-scoped Service query.
        List<WareHouseResp> enabled = warehouseFacade.listEnable(WmsQuerySupport.userInfo(context));
        if (enabled != null) {
            for (WareHouseResp w : enabled) {
                if (warehouseId.equals(w.getId())) {
                    return ToolResult.ok(Map.of("warehouse", SearchWarehouseTool.map(w)), "仓库详情");
                }
            }
        }
        Warehouse wh = wareHouseService.selectByTenantIdAndWareHouseId(context.getTenantId(), warehouseId);
        if (wh == null) {
            return ToolResult.fail(ToolErrorCode.NOT_FOUND, "仓库不存在");
        }
        WareHouseResp resp = new WareHouseResp();
        BeanUtils.copyProperties(wh, resp);
        return ToolResult.ok(Map.of("warehouse", SearchWarehouseTool.map(resp)), "仓库详情");
    }
}
