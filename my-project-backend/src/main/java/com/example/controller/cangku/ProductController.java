package com.example.controller.cangku;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.CKProductFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.cangku.req.*;
import com.example.entity.cangku.resp.*;
import com.example.entity.cangku.resp.excel.ProductCreateExportModel;
import com.example.filter.UserUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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


    //获取可用所有商品列表
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

    //获取可用成品商品列表
    @GetMapping("/finishedProductListEnable")
    public RespBean<List<ProductPageListResp>> finishedProductListEnable() {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            List<ProductPageListResp> productPageListResps = CKProductFacade.listEnable(user);
            List<ProductPageListResp> result = productPageListResps.stream().filter(productPageListResp -> CollectionUtils.isEmpty(productPageListResp.getBomData())).collect(Collectors.toList());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProductController#finishedProductListEnable,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProductController#finishedProductListEnable,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    //商品样式
    @GetMapping("/search")
    public RespBean<List<ProductSimpleListResp>> styleList(@RequestParam(value = "keyword", required = false) String keyword) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            List<ProductSimpleListResp> productPageListResps = CKProductFacade.productSimpleList(user,keyword);
            return RespBean.success(productPageListResps);
        } catch (ValidationException e) {
            log.error("ProductController#productSimpleList,e:{}", keyword, e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProductController#productSimpleList,e:{}", keyword,e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @GetMapping("/detail")
    public RespBean<ProductPageListResp> detail(@RequestParam("productId") Long productId) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            ProductPageListResp result = CKProductFacade.detail(productId, user);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProductController#detail,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProductController#detail,req:{}", e);
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

    @GetMapping("/exportProductCreateExcel")
    public void exportProductCreateExcel(HttpServletResponse response) throws IOException {
        try {
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");

            // 文件名
            String fileName = URLEncoder.encode("产品导入模版单", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

            // 准备数据，这里示例用空数据，如果有实际数据可以填充
            // 如果不想预填充测试数据，可传空列表 data = new ArrayList<>();
            List<ProductCreateExportModel> dataList = new ArrayList<>();

            // EasyExcel 写入
            EasyExcel.write(response.getOutputStream(), ProductCreateExportModel.class)
                    .sheet("产品导入模版单")
                    .doWrite(dataList);
        } catch (ValidationException e) {
            log.error("ProjectController#exportProductCreateExcel", e);
        } catch (Exception e) {
            log.error("ProjectController#exportProductCreateExcel,", e);
        }
    }



    @PostMapping("/importProductCreateExcel")
    public RespBean<Boolean> importProductCreateExcel(@RequestParam("file") MultipartFile file) {
        try {
            log.info("=== 导入产品创建接口开始 ===");
            log.info("接收到文件: {}. 文件大小: {} bytes, 文件类型: {}", file.getOriginalFilename(), file.getSize(), file.getContentType());

            Long userId = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            Boolean result = CKProductFacade.importOutboundSaleQuantity(file, tenantId, userId);
            log.info("=== 导入产品创建接口结束 ===导入结果: {}", result);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("ProjectController#importProductCreateExcel", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectController#importProductCreateExcel,", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }
}

