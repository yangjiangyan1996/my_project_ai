package com.example.ai.tool.query.customer;

import com.example.ai.context.AiExecutionContext;
import com.example.ai.exception.AiValidationException;
import com.example.ai.tool.ToolErrorCode;
import com.example.ai.tool.ToolResult;
import com.example.ai.tool.query.AbstractWmsQueryTool;
import com.example.ai.tool.query.WmsQuerySupport;
import com.example.Facade.CkCustomerFacade;
import com.example.entity.cangku.resp.CustomerEnabledListResp;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class GetCustomerTool extends AbstractWmsQueryTool {

    @Resource
    private CkCustomerFacade customerFacade;

    @Override
    public String name() {
        return "get_customer";
    }

    @Override
    public String description() {
        return "按 customerId 获取当前租户启用客户详情（只读）。";
    }

    @Override
    public String requiredPermission() {
        return "ck:customer:list";
    }

    @Override
    public Map<String, Object> inputSchema() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("customerId", Map.of("type", "integer"));
        return schema(List.of("customerId"), props);
    }

    @Override
    protected ToolResult doExecute(Map<String, Object> arguments, AiExecutionContext context) {
        Long id = WmsQuerySupport.lng(arguments, "customerId");
        if (id == null) {
            throw new AiValidationException("缺少必填参数: customerId");
        }
        List<CustomerEnabledListResp> list = customerFacade.listEnable(WmsQuerySupport.userInfo(context));
        for (CustomerEnabledListResp c : list) {
            if (id.equals(c.getId())) {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("customerId", c.getId());
                data.put("customerName", c.getCustomerName());
                data.put("customerCode", c.getCustomerCode());
                data.put("customerLevel", c.getCustomerLevel());
                data.put("customerType", c.getCustomerType());
                return ToolResult.ok(data, "客户查询完成");
            }
        }
        return ToolResult.fail(ToolErrorCode.NOT_FOUND, "客户不存在或不可用");
    }
}
