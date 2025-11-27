package com.example.controller.cangku;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.CkInventoryFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.cangku.req.InventoryAlertReq;
import com.example.entity.cangku.req.InventoryListPageReq;
import com.example.entity.cangku.req.InventoryTransactionListPageReq;
import com.example.entity.cangku.req.OutBoundBatchAllocationCheckRequest;
import com.example.entity.cangku.resp.*;
import com.example.filter.UserUtil;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/3 22:49
 */
@RestController
@Slf4j
@RequestMapping("/api/auth/inventory")
public class InventoryController {
    @Resource
    private CkInventoryFacade inventoryFacade;


    /**
     * 检查批次货架分配数量是否超出库存
     */
    @PostMapping("/checkBatchAllocation")
    public RespBean<OutBoundBatchAllocationCheckResponse> checkBatchAllocation(
            @RequestBody OutBoundBatchAllocationCheckRequest request) {
        try {
            request.setTenantId(UserUtil.getCurrentUser().getTenantId());
            OutBoundBatchAllocationCheckResponse response = inventoryFacade.checkBatchAllocation(request);
            return RespBean.success(response);
        } catch (ValidationException e) {
            log.error("InventoryController#checkBatchAllocation,req:{}", JSON.toJSONString( request), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("InventoryController#checkBatchAllocation,req:{}", JSON.toJSONString( request), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    /**
     * 获取批次货架的实时可用数量（考虑其他商品的分配）
     */
//    @PostMapping("/getRealTimeAvailable")
//    public RespBean<List<OutBoundBatchAllocationCheckResponse.BatchShelfAvailableDTO>> getRealTimeAvailable(
//            @RequestBody OutBoundBatchAllocationCheckRequest request) {
//        try {
//            request.setTenantId(UserUtil.getCurrentUser().getTenantId());
//            List<OutBoundBatchAllocationCheckResponse.BatchShelfAvailableDTO> availableList = inventoryFacade.getRealTimeAvailableQuantities(request);
//            return RespBean.success(availableList);
//        } catch (ValidationException e) {
//            log.error("InventoryController#getRealTimeAvailable,req:{}", JSON.toJSONString( request), e);
//            return RespBean.failure(999, e.getMessage());
//        } catch (Exception e) {
//            log.error("InventoryController#getRealTimeAvailable,req:{}", JSON.toJSONString( request), e);
//            return RespBean.failure(999, "系统异常，请联系管理员");
//        }
//    }

    @GetMapping("/productInventoryDetail")
    public RespBean<InventoryProductDetailResp> productInventoryDetail(@RequestParam("productId") Long productId) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            InventoryProductDetailResp result = inventoryFacade.productInventoryDetail(productId, user);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("InventoryController#productInventoryDetail,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("InventoryController#productInventoryDetail,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

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

    @PostMapping("/transaction/pageList")
    public RespBean<Page<InventoryComprehensiveHistoryResp>> transactionPageList(@RequestBody InventoryTransactionListPageReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            req.setTenantId(tenantId);
            req.setUserId(user.getId());

            Page<InventoryComprehensiveHistoryResp> result = inventoryFacade.comprehensiveHistory(Page.of(req.getPage() - 1, req.getSize()), req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("InventoryController#transactionPageList,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("InventoryController#transactionPageList,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @GetMapping("/listOfWarehouse")
    public RespBean<List<InventoryListResp>> listOfWarehouse(@RequestParam("warehouseId") Long warehouseId) {
        try {
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            List<InventoryListResp> result = inventoryFacade.List(warehouseId,tenantId);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("InventoryController#listOfWarehouse,req:{}",warehouseId, e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("InventoryController#listOfWarehouse,req:{}",warehouseId, e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    @GetMapping("/batches")
    public RespBean<List<InventoryBatchResp>> batches(@RequestParam("productId") Long productId,
                                                      @RequestParam("warehouseId") Long warehouseId) {
        try {
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            List<InventoryBatchResp> result = inventoryFacade.batches(warehouseId,productId,tenantId);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("InventoryController#batches,req:{}",warehouseId, e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("InventoryController#batches,req:{}",warehouseId, e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    ///lowProductCountChat

    @GetMapping("/lowProductCountChat")
    public RespBean<List<InventoryBatchResp>> lowProductCountChat() {
        try {
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            List<InventoryBatchResp> result = inventoryFacade.lowProductCountChat(tenantId);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("InventoryController#lowProductCountChat", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("InventoryController#lowProductCountChat", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    /**
     * 获取库存预警数据
     */
    @PostMapping("/alerts")
    public RespBean<List<InventoryAlertResp>> getInventoryAlerts(@RequestBody InventoryAlertReq req) {
        try {
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            req.setTenantId(tenantId);
            List<InventoryAlertResp> result = inventoryFacade.getInventoryAlerts(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("InventoryController#getInventoryAlerts", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("InventoryController#getInventoryAlerts", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    /**
     * 获取库存预警统计
     */
    @GetMapping("/alertStats")
    public RespBean<InventoryAlertStatsResp> getAlertStats() {
        try {
            InventoryAlertReq req = new InventoryAlertReq();

            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            req.setTenantId(tenantId);
            List<InventoryAlertResp> result = inventoryFacade.getInventoryAlerts(req);

            InventoryAlertStatsResp resp = new InventoryAlertStatsResp();
            resp.setTotalProducts(result.size());
            resp.setUrgentAlerts(result.stream().filter(item -> "urgent".equals(item.getAlertLevel())).count());
            resp.setNormalProducts(result.stream().filter(item -> "normal".equals(item.getAlertLevel())).count());
            resp.setWarningAlerts(result.stream().filter(item -> "warning".equals(item.getAlertLevel())).count());

            return RespBean.success(resp);
        } catch (ValidationException e) {
            log.error("InventoryController#getInventoryAlerts", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("InventoryController#getInventoryAlerts", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


}
