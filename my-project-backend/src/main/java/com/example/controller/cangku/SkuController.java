package com.example.controller.cangku;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.CkSkuFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.cangku.req.SkuDeleteReq;
import com.example.entity.cangku.req.SkuListPageReq;
import com.example.entity.cangku.resp.SkuPageListResp;
import com.example.filter.UserUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/3 22:49
 */
@RestController
@Slf4j
@RequestMapping("/api/auth/sku")
public class SkuController {
    @Resource
    private CkSkuFacade skuFacade;


    @PostMapping("/pageList")
    public RespBean<Page<SkuPageListResp>> pageList(@RequestBody SkuListPageReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            req.setTenantId(tenantId);
            req.setUserId(user.getId());
            Page<SkuPageListResp> result = skuFacade.pageList(Page.of(req.getPage() - 1, req.getSize()), req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("SkuController#pageList,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("SkuController#pageList,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    @PostMapping("/delete")
    public RespBean<Boolean> delete(@RequestBody SkuDeleteReq req) {
        try {
            Long userId = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = skuFacade.delete(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("SkuController#delete,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("SkuController#delete,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    /**
     * 导出SKU映射Excel
     */
    @GetMapping("/exportExcel")
    public void exportExcel(HttpServletResponse response) {
        try {
            skuFacade.exportExcel(response);
        } catch (Exception e) {
            log.error("导出SKU映射Excel失败", e);
            throw new RuntimeException("导出失败");
        }
    }


    @PostMapping("/import")
    public RespBean<Boolean> importSku(@RequestParam("file") MultipartFile file, @RequestParam("customerId") Long customerId) {
        try {
            log.info("=== 导入产品接口开始 ===");
            log.info("接收到文件: {}", file.getOriginalFilename());
            log.info("文件大小: {} bytes", file.getSize());
            log.info("文件类型: {}", file.getContentType());

            Long userId = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            log.info("用户ID: {}, 租户ID: {}", userId, tenantId);

            Boolean result = skuFacade.importSku(file, tenantId, userId,customerId);
            log.info("导入结果: {}", result);
            log.info("=== 导入产品接口结束 ===");
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("SkuController#importSku", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("SkuController#importSku,", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


//    @PostMapping("/update")
//    public RespBean<Boolean> update(@RequestBody ShelfCreateReq req) {
//        try {
//            Long userId  = UserUtil.getCurrentUser().getId();
//            Long tenantId = UserUtil.getCurrentUser().getTenantId();
//
//            req.setUserId(userId);
//            req.setTenantId(tenantId);
//            Boolean result = skuFacade.update(req);
//            return RespBean.success(result);
//        }catch (ValidationException e) {
//            log.error("ShelfController#update,req:{}", JSON.toJSONString(req), e);
//            return RespBean.failure(999, e.getMessage());
//        } catch (Exception e) {
//            log.error("ShelfController#update,req:{}", JSON.toJSONString(req), e);
//            return RespBean.failure(999, "系统异常，请联系管理员");
//        }
//    }
}
