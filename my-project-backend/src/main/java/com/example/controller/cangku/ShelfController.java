package com.example.controller.cangku;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.CkCommentFacade;
import com.example.Facade.CkShelfFacade;
import com.example.annotations.LogOperation;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.cangku.req.*;
import com.example.entity.cangku.resp.ShelfEnalbedListResp;
import com.example.entity.cangku.resp.ShelfPageListResp;
import com.example.entity.cangku.resp.ShelfProductUsedAllResp;
import com.example.entity.cangku.resp.ShelfZoneResp;
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
 * @Date 2025/10/30 01:19
 */
@RestController
@Slf4j
@RequestMapping("/api/auth/shelf/")
public class ShelfController {

    @Resource
    CkShelfFacade shelfFacade;
    @Resource
    CkCommentFacade commentFacade;


    @PostMapping("/pageList")
    public RespBean<Page<ShelfPageListResp>> pageList(@RequestBody ShelfListPageReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            req.setTenantId(tenantId);
            req.setUserId(user.getId());

            Page<ShelfPageListResp> result = shelfFacade.pageList(Page.of(req.getPage(), req.getSize()), req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ShelfController#pageList,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ShelfController#pageList,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    @GetMapping("/shelfZoneList")
    public RespBean<List<ShelfZoneResp>> pageList(@RequestParam("parentId") Long parentId) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            List<ShelfZoneResp> result = shelfFacade.shelfZoneList(tenantId, parentId);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("WarehouseController#pageList,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("WarehouseController#pageList,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @GetMapping("/listEnable")
    public RespBean<List<ShelfEnalbedListResp>> listEnable(@RequestParam("warehouseId") Long warehouseId) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            List<ShelfEnalbedListResp> result = shelfFacade.listEnable(user.getTenantId(), warehouseId);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ShelfController#listEnable,req:{}",warehouseId, e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ShelfController#listEnable,req:{}",warehouseId, e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    //TODO yang delete
    @LogOperation(module = "货架管理", operation = "创建货架", description = "创建货架")
    @PostMapping("/create")
    public RespBean<Boolean> create(@RequestBody ShelfCreateReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = shelfFacade.create(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("ShelfController#create,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ShelfController#create,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @LogOperation(module = "货架管理", operation = "创建货架", description = "创建货架")
    @PostMapping("/createWithZones")
    public RespBean<Boolean> createWithZones(@RequestBody ShelviesCreateWithZoneReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = shelfFacade.createWithZones(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("ShelfController#createWithZones,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ShelfController#createWithZones,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @LogOperation(module = "货架管理", operation = "创建货架", description = "创建货架")
    @PostMapping("/updateWithZones")
    public RespBean<Boolean> updateWithZones(@RequestBody ShelviesCreateWithZoneReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = shelfFacade.updateWithZones(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("ShelfController#updateWithZones,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ShelfController#updateWithZones,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    @LogOperation(module = "货架管理", operation = "更新货架状态", description = "更新货架状态")
    @PostMapping("/updateStatus")
    public RespBean<Boolean> updateStatus(@RequestBody ShelfUpdateStatusReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = shelfFacade.updateStatus(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("ShelfController#updateStatus,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ShelfController#updateStatus,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }



    @LogOperation(module = "货架管理", operation = "更新货架", description = "更新货架")
    @PostMapping("/update")
    public RespBean<Boolean> update(@RequestBody ShelfCreateReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = shelfFacade.update(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("ShelfController#update,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ShelfController#update,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    /**
     * 采购入库自动分配货架
     * @param req
     * @return
     */
    @PostMapping("/allocateIShelfnventoryQuantity")
    public RespBean<List<ShelfProductUsedAllResp>> allocateIShelfnventoryQuantity(@RequestBody List<ShelfProductUsedAllReq> req) {
        try {

            List<ShelfProductUsedAllResp> result = commentFacade.allocateShelfInventoryQuantity(req);
            return RespBean.success(result);
        }  catch (ValidationException e) {
            log.error("ShelfController#allocateIShelfnventoryQuantity,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ShelfController#allocateIShelfnventoryQuantity,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @LogOperation(module = "货架管理", operation = "删除货架", description = "删除货架")
    @PostMapping("/delete")
    public RespBean<Boolean> delete(@RequestBody ShelfDeleteReq req) {
        try {
            Long userId = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = shelfFacade.delete(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ShelfController#delete,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ShelfController#delete,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }
}
