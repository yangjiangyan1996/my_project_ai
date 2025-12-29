package com.example.controller.cangku;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.CkStockFacade;
import com.example.annotations.LogOperation;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.cangku.req.*;
import com.example.entity.cangku.resp.StockDetailResp;
import com.example.entity.cangku.resp.StockItemListPageResp;
import com.example.entity.cangku.resp.StockListPageResp;
import com.example.entity.resp.StockSnapResp;
import com.example.enums.CkStockTakeEnums;
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
 * @Date 2025/12/25 12:04
 */
@RestController
@Slf4j
@RequestMapping("/api/auth/stock")
public class CkStockController {
    @Resource
    private CkStockFacade stockFacade;

    /**
     * 1、创建全量盘点
     *
     * @param req
     * @return
     */
    @PostMapping("/createStockTake")
    @LogOperation(module = "仓库盘点管理", operation = "创建全量盘点",
            description = "创建全量盘点")
    public RespBean<Long> createFullStockTake(@RequestBody StockTakeCreateReq req) {
        try {
            Long userId = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Long result = stockFacade.createStockTake(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("CkStockController#createFullStockTake,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CkStockController#createFullStockTake,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    /**
     * 2、提交审批
     */
    @GetMapping("/submitApproval")
    @LogOperation(module = "仓库盘点管理", operation = "提交审批",
            description = "提交审批")
    public RespBean<Boolean> createFullStockTake(@RequestParam("id") Long id) {
        try {
            Long userId = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            Boolean result = stockFacade.submitApproval(id, userId, tenantId);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("CkStockController#submitApproval,req:{}", JSON.toJSONString(id), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CkStockController#submitApproval,req:{}", JSON.toJSONString(id), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    /**
     * 3、审核通过以及初始化
     */
    @GetMapping("/apprroveAndInitialize")
    @LogOperation(module = "仓库盘点管理", operation = "审核通过以及初始化",
            description = "审核通过以及初始化")
    public RespBean<Boolean> apprroveAndInitialize(@RequestParam("id") Long id
//            , @RequestParam("approveStatus") Integer approveStatus
    ) {
        try {
            Long userId = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            Boolean result = stockFacade.apprroveAndInitialize(id, userId, tenantId);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("CkStockController#apprroveAndInitialize,req:{}", JSON.toJSONString(id), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CkStockController#apprroveAndInitialize,req:{}", JSON.toJSONString(id), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    /**
     * 分配
     */
    /**
     * 查询库盘点待分配镜像列表
     * @param type 1=批次 2=货架 3=商品
     * @return
     */
    @GetMapping("/getListOfStockTakeToBeAssigned")
    @LogOperation(module = "仓库盘点管理", operation = "执行盘点",
            description = "执行盘点")
    public RespBean<List<StockSnapResp>> getListOfStockTakeToBeAssigned(@RequestParam("stockTakeId") Long stockTakeId,
                                                                        @RequestParam("type") Integer type) {
        try {
            CkStockTakeEnums.StockTakeScope taskScopeEnum = CkStockTakeEnums.StockTakeScope.getByCode(type);
            if (taskScopeEnum == null) {
                throw new ValidationException("盘点单范围错误");
            }
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            List<StockSnapResp> result = stockFacade.getListOfStockTakeToBeAssigned(stockTakeId, taskScopeEnum, tenantId);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("CkStockController#getListOfStockTakeToBeAssigned,req:{}", JSON.toJSONString(stockTakeId), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CkStockController#getListOfStockTakeToBeAssigned,req:{}", JSON.toJSONString(stockTakeId), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    //执行分配任务
    @PostMapping("/assignTask")
    public RespBean<Boolean> assignTask(@RequestBody StockAssignTaskReq req) {
        try {
            Long userId = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setTenantId(tenantId);
            req.setUserId(userId);

            Boolean result = stockFacade.assignTask(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("CkStockController#assignTask,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CkStockController#assignTask,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }
    /**
     * 4、执行盘点
     */
    @PostMapping("/executeStockTakeItem")
    @LogOperation(module = "仓库盘点管理", operation = "执行盘点",
            description = "执行盘点")
    public RespBean<Boolean> executeStockTakeItem(@RequestBody StockExecuteStockTakeItemReq req) {
        try {
            Long userId = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setTenantId(tenantId);
            req.setUserId(userId);

            Boolean result = stockFacade.executeStockTakeItem(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("CkStockController#executeStockTakeItem,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CkStockController#executeStockTakeItem,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    /**
     * 5、完成盘点
     */
    @GetMapping("/completedStock")
    @LogOperation(module = "库盘点管理", operation = "执行盘点",
            description = "执行盘点")
    public RespBean<Boolean> completedStock(@RequestParam("id") Long id) {
        try {
            Long userId = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            Boolean result = stockFacade.completeStockTake(id, userId, tenantId);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("CkStockController#completedStock,req:{}", JSON.toJSONString(id), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CkStockController#completedStock,req:{}", JSON.toJSONString(id), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    /**
     * 6、库存调整（库存真正变化）
     * TODO yang 这里不处理，盘点结束删除锁，在调整管理页面处理
     */
//    @GetMapping("/createAdjustmentOrder")
//    @LogOperation(module = "库盘点管理", operation = "库存调整",
//            description = "库存调整")
//    public RespBean<Boolean> stockCreateAdjustmentOrder(@RequestParam("id") Long id) {
//        try {
//            Long userId = UserUtil.getCurrentUser().getId();
//            Long tenantId = UserUtil.getCurrentUser().getTenantId();
//
//            Boolean result = stockFacade.stockCreateAdjustmentOrder(id, userId, tenantId);
//            return RespBean.success(result);
//        } catch (ValidationException e) {
//            log.error("CkStockController#stockCreateAdjustmentOrder,req:{}", JSON.toJSONString(id), e);
//            return RespBean.failure(999, e.getMessage());
//        } catch (Exception e) {
//            log.error("CkStockController#stockCreateAdjustmentOrder,req:{}", JSON.toJSONString(id), e);
//            return RespBean.failure(999, "系统异常，请联系管理员");
//        }
//    }


    /**
     * 分页查询盘点列表
     *
     * @param req 出库列表查询请求参数
     * @return 返回分页结果
     */
    @PostMapping("/pageList")
    public RespBean<Page<StockListPageResp>> pageList(@RequestBody StockListPageReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            req.setTenantId(tenantId);
            req.setUserId(user.getId());

            Page<StockListPageResp> result = stockFacade.pageList(Page.of(req.getPage(), req.getSize()), req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("CkStockController#pageList,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CkStockController#pageList,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @GetMapping("/completedStockTakeList")
    public RespBean<List<StockListPageResp>> completedStockTakeList(@RequestParam(value = "warehouseId", required = false) Long warehouseId) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            List<StockListPageResp> result = stockFacade.completedStockTakeList(warehouseId, tenantId);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("CkStockController#completedStockTakeList,req:{}", JSON.toJSONString(warehouseId), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CkStockController#completedStockTakeList,req:{}", JSON.toJSONString(warehouseId), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }





    /**
     * 分页查询出库列表
     *
     * @param req 出库列表查询请求参数
     * @return 返回分页结果
     */
    @PostMapping("/itemPageList")
    public RespBean<Page<StockItemListPageResp>> itemPageList(@RequestBody StockItemListPageReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            req.setTenantId(tenantId);
            req.setUserId(user.getId());

            Page<StockItemListPageResp> result = stockFacade.itemPageList(Page.of(req.getPage(), req.getSize()), req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("CkStockController#pageList,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CkStockController#pageList,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    /**
     * 分页查询出库列表
     *
     * @param req 出库列表查询请求参数
     * @return 返回分页结果
     */
    @PostMapping("/itemList")
    public RespBean<List<StockItemListPageResp>> itemList(@RequestBody StockItemListPageReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            req.setTenantId(tenantId);
            req.setUserId(user.getId());

            List<StockItemListPageResp> result = stockFacade.itemList(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("CkStockController#itemList,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CkStockController#itemList,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    @GetMapping("/stockDetail")
    @LogOperation(module = "库盘点管理", operation = "执行盘点",
            description = "执行盘点")
    public RespBean<StockDetailResp> stockDetail(@RequestParam("id") Long id) {
        try {
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            StockDetailResp result = stockFacade.stockDetail(id, tenantId);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("CkStockController#stockDetail,req:{}", JSON.toJSONString(id), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CkStockController#stockDetail,req:{}", JSON.toJSONString(id), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }
}
