package com.example.controller.cangku;

import com.example.Facade.CkStockFacade;
import com.example.Facade.CkStockTakeTaskFacade;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/28 13:07
 */
@RestController
@Slf4j
@RequestMapping("/api/auth/stockTakeTask")
public class CkStockTakeTaskController {

    @Resource
    CkStockTakeTaskFacade ckStockTakeTaskFacade;
    @Resource
    CkStockFacade stockFacade;

//    @PostMapping("/pageList")
//    public RespBean<Page<StockListPageResp>> pageList(@RequestBody StockListPageReq req) {
//        try {
//            UserInfo user = UserUtil.getCurrentUser();
//            Long tenantId = UserUtil.getCurrentUser().getTenantId();
//            req.setTenantId(tenantId);
//            req.setUserId(user.getId());
//
//            Page<StockListPageResp> result = stockFacade.pageList(Page.of(req.getPage(), req.getSize()), req);
//            return RespBean.success(result);
//        } catch (ValidationException e) {
//            log.error("CkStockController#pageList,req:{}", JSON.toJSONString(req), e);
//            return RespBean.failure(999, e.getMessage());
//        } catch (Exception e) {
//            log.error("CkStockController#pageList,req:{}", JSON.toJSONString(req), e);
//            return RespBean.failure(999, "系统异常，请联系管理员");
//        }
//    }
}
