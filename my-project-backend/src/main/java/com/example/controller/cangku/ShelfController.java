package com.example.controller.cangku;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.CkCommentFacade;
import com.example.Facade.CkShelfFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.cangku.req.ShelfCreateReq;
import com.example.entity.cangku.req.ShelfListPageReq;
import com.example.entity.cangku.req.ShelfProductUsedAllReq;
import com.example.entity.cangku.req.ShelfUpdateStatusReq;
import com.example.entity.cangku.resp.ShelfPageListResp;
import com.example.entity.cangku.resp.ShelfProductUsedAllResp;
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
            log.error("WarehouseController#pageList,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("WarehouseController#pageList,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @GetMapping("/listEnable")
    public RespBean<List<ShelfPageListResp>> listEnable(@RequestParam("warehouseId") Long warehouseId) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            List<ShelfPageListResp> result = shelfFacade.listEnable(user.getTenantId(), warehouseId);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("WarehouseController#listEnable,req:{}",warehouseId, e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("WarehouseController#listEnable,req:{}",warehouseId, e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

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
            log.error("InboundController#allocateIShelfnventoryQuantity,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("InboundController#allocateIShelfnventoryQuantity,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }
}
