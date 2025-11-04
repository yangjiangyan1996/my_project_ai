package com.example.controller.cangku;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.CkInventoryFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.cangku.req.InventoryListPageReq;
import com.example.entity.cangku.resp.InventoryPageListResp;
import com.example.entity.cangku.resp.SupplierPageListResp;
import com.example.filter.UserUtil;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/3 22:49
 */
@RestController
@Slf4j
@RequestMapping("/api/auth/inventory/")
public class InventoryController {
    @Resource
    private CkInventoryFacade inventoryFacade;


    @PostMapping("/pageList")
    public RespBean<Page<InventoryPageListResp>> pageList(@RequestBody InventoryListPageReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            req.setTenantId(tenantId);
            req.setUserId(user.getId());

            Page<InventoryPageListResp> result = inventoryFacade.pageList(Page.of(req.getPage() - 1, req.getSize()), req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("InventoryController#pageList,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("InventoryController#pageList,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }
}
