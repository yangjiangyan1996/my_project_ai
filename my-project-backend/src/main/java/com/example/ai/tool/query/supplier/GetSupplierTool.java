package com.example.ai.tool.query.supplier;

import com.example.ai.context.AiExecutionContext;
import com.example.ai.exception.AiValidationException;
import com.example.ai.tool.ToolErrorCode;
import com.example.ai.tool.ToolResult;
import com.example.ai.tool.query.AbstractWmsQueryTool;
import com.example.ai.tool.query.WmsQuerySupport;
import com.example.Facade.CkSupplierFacade;
import com.example.entity.cangku.resp.SupplierPageListResp;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class GetSupplierTool extends AbstractWmsQueryTool {

    @Resource
    private CkSupplierFacade supplierFacade;

    @Override
    public String name() {
        return "get_supplier";
    }

    @Override
    public String description() {
        return "按 supplierId 获取当前租户启用供应商详情（只读）。";
    }

    @Override
    public String requiredPermission() {
        return "ck:supplier:list";
    }

    @Override
    public Map<String, Object> inputSchema() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("supplierId", Map.of("type", "integer"));
        return schema(List.of("supplierId"), props);
    }

    @Override
    protected ToolResult doExecute(Map<String, Object> arguments, AiExecutionContext context) {
        Long id = WmsQuerySupport.lng(arguments, "supplierId");
        if (id == null) {
            throw new AiValidationException("缺少必填参数: supplierId");
        }
        List<SupplierPageListResp> list = supplierFacade.listEnable(WmsQuerySupport.userInfo(context));
        for (SupplierPageListResp s : list) {
            if (id.equals(s.getId())) {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("supplierId", s.getId());
                data.put("supplierName", s.getSupplierName());
                data.put("supplierCode", s.getSupplierCode());
                return ToolResult.ok(data, "供应商查询完成");
            }
        }
        return ToolResult.fail(ToolErrorCode.NOT_FOUND, "供应商不存在或不可用");
    }
}
