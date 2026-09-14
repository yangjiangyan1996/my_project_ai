package com.example.ai.tool.query.product;

import com.example.ai.context.AiExecutionContext;
import com.example.ai.exception.AiValidationException;
import com.example.ai.tool.ToolErrorCode;
import com.example.ai.tool.ToolResult;
import com.example.ai.tool.query.AbstractWmsQueryTool;
import com.example.ai.tool.query.WmsQuerySupport;
import com.example.ai.tool.query.dto.AiQueryDtos.AiProductResult;
import com.example.Facade.CKProductFacade;
import com.example.entity.cangku.resp.ProductPageListResp;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class GetProductTool extends AbstractWmsQueryTool {

    @Resource
    private CKProductFacade productFacade;

    @Override
    public String name() {
        return "get_product";
    }

    @Override
    public String description() {
        return "按 productId 查询当前租户商品详情。";
    }

    @Override
    public String requiredPermission() {
        return "ck:product:list";
    }

    @Override
    public Map<String, Object> inputSchema() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("productId", Map.of("type", "integer"));
        return schema(List.of("productId"), props);
    }

    @Override
    protected ToolResult doExecute(Map<String, Object> arguments, AiExecutionContext context) {
        Long productId = WmsQuerySupport.lng(arguments, "productId");
        if (productId == null) {
            throw new AiValidationException("缺少必填参数: productId");
        }
        ProductPageListResp p = productFacade.detail(productId, WmsQuerySupport.userInfo(context));
        if (p == null) {
            return ToolResult.fail(ToolErrorCode.NOT_FOUND, "商品不存在");
        }
        AiProductResult result = AiProductResult.builder()
                .productId(p.getId()).sku(p.getSku()).name(p.getName())
                .spec(p.getSpec()).color(p.getColor()).unitName(p.getUnitName())
                .status(p.getStatus()).build();
        return ToolResult.ok(Map.of("product", result), "商品详情");
    }
}
