package com.example.controller.cangku;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.CKProductFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.cangku.req.*;
import com.example.entity.cangku.resp.BomDetailListResp;
import com.example.entity.cangku.resp.ProductCategoryResp;
import com.example.entity.cangku.resp.ProductPageListResp;
import com.example.entity.cangku.resp.UnitResp;
import com.example.filter.UserUtil;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/30 21:09
 */
@RestController
@Slf4j
@RequestMapping("/api/auth/product")
public class ProductController {

    @Resource
    CKProductFacade CKProductFacade;

    @PostMapping("/import")
    public RespBean<Boolean> importProduct(@RequestParam("file") MultipartFile file) {
        try {
            log.info("=== 导入产品接口开始 ===");
            log.info("接收到文件: {}", file.getOriginalFilename());
            log.info("文件大小: {} bytes", file.getSize());
            log.info("文件类型: {}", file.getContentType());

            Long userId = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            log.info("用户ID: {}, 租户ID: {}", userId, tenantId);

            Boolean result = CKProductFacade.importProduct(file, tenantId, userId);
            log.info("导入结果: {}", result);
            log.info("=== 导入产品接口结束 ===");
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("ProductController#import", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProductController#import,", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    @PostMapping("/delete")
    public RespBean<Boolean> delete(@RequestBody ProductDeleteReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = CKProductFacade.delete(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("ProductController#delete,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProductController#delete,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/updateStatus")
    public RespBean<Boolean> updateStatus(@RequestBody ProductUpdateStatusReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = CKProductFacade.updateStatus(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("ProductController#updateStatus,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProductController#updateStatus,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/pageList")
    public RespBean<Page<ProductPageListResp>> pageList(@RequestBody ProductListPageReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            req.setTenantId(tenantId);
            req.setUserId(user.getId());

            Page<ProductPageListResp> result = CKProductFacade.pageList(Page.of(req.getPage() - 1, req.getSize()), req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProductController#pageList,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProductController#pageList,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/create")
    public RespBean<Boolean> productCreate(@RequestBody ProductCreateReq req) {
        try {
            req.setUserId(UserUtil.getCurrentUser().getId());
            req.setTenantId(UserUtil.getCurrentUser().getTenantId());

            Boolean result = CKProductFacade.productCreate(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProductController#categoryCreate,req:{}", JSON.toJSONString( req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProductController#categoryCreate,req:{}", JSON.toJSONString( req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/update")
    public RespBean<Boolean> update(@RequestBody ProductCreateReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = CKProductFacade.update(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("ProductController#update,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProductController#update,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/category/create")
    public RespBean<Boolean> categoryCreate(@RequestBody CategoryCreateReq req) {
        try {
            req.setUserId(UserUtil.getCurrentUser().getId());
            req.setTenantId(UserUtil.getCurrentUser().getTenantId());

            Boolean result = CKProductFacade.categoryCreate(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProductController#categoryCreate,", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProductController#categoryCreate,", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @GetMapping("/categoryList")
    public RespBean<List<ProductCategoryResp>> categoryList() {
        try {
            List<ProductCategoryResp> result = CKProductFacade.categoryList(UserUtil.getCurrentUser().getTenantId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProductController#categoryList,", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProductController#categoryList,", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @GetMapping("/unitList")
    public RespBean<List<UnitResp>> unitList() {
        try {
            List<UnitResp> result = CKProductFacade.unitList(UserUtil.getCurrentUser().getTenantId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProductController#unitList,", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProductController#unitList,", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    //获取可用商品列表
    @GetMapping("/listEnable")
    public RespBean<List<ProductPageListResp>> listEnable() {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            List<ProductPageListResp> result = CKProductFacade.listEnable(user);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProductController#listEnable,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProductController#listEnable,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    //获取可用商品列表
    @GetMapping("/bom/detail")
    public RespBean<List<BomDetailListResp>> bomDetail(@RequestParam("bomId") Long bomId) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            List<BomDetailListResp> result = CKProductFacade.bomDetail(bomId, user.getTenantId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProductController#listEnable,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProductController#listEnable,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }
}

