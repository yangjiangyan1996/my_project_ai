package com.example.controller.cangku;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.CKProductFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.cangku.req.*;
import com.example.entity.cangku.resp.*;
import com.example.entity.cangku.resp.excel.ProductBomExcelModel;
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
        } catch (ValidationException e) {
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
            Long userId = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = CKProductFacade.delete(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
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
            Long userId = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = CKProductFacade.updateStatus(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
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

            Page<ProductPageListResp> result = CKProductFacade.pageList(Page.of(req.getPage() , req.getSize()), req);
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
            log.error("ProductController#categoryCreate,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProductController#categoryCreate,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/update")
    public RespBean<Boolean> update(@RequestBody ProductCreateReq req) {
        try {
            Long userId = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = CKProductFacade.update(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
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


    //获取可用所有商品列表， 成品包含原料时，原料也会展示到bom中
    @GetMapping("/listEnable")
    public RespBean<List<ProductPageListResp>> listEnable() {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            List<ProductPageListResp> result = CKProductFacade.listEnable(user);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProductController#listEnable,", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProductController#listEnable,", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    //获取可用所有商品列表， 成品包含原料时，原料也会展示到bom中, 但是没有包装件的产品列表
    @GetMapping("/listEnableNoPackaging")
    public RespBean<List<ProductListNoPackageResp>> listEnableNoPackaging() {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            List<ProductListNoPackageResp> result = CKProductFacade.listEnableNoPackaging(user);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProductController#listEnable,", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProductController#listEnable,", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    //获取可用所有商品列表， 成品包含原料时，原料也会展示到bom中
    @GetMapping("/listEnableNotBom")
    public RespBean<List<ProductPageListResp>> listEnableNotBom() {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            List<ProductPageListResp> result = CKProductFacade.listEnable(user);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProductController#listEnableNotBom", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProductController#listEnableNotBom,", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    //没有地方用
    @GetMapping("/finishedProductListEnable")
    public RespBean<List<ProductPageListResp>> finishedProductListEnable() {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            List<ProductPageListResp> productPageListResps = CKProductFacade.listEnableNotBom(user);
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
            List<ProductSimpleListResp> productPageListResps = CKProductFacade.productSimpleList(user, keyword);
            return RespBean.success(productPageListResps);
        } catch (ValidationException e) {
            log.error("ProductController#productSimpleList,e:{}", keyword, e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProductController#productSimpleList,e:{}", keyword, e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    /**
     * 这个接口现在在库存历史页面中使用， 产品详情用realDetail接口
     * @param productId
     * @return
     */
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

    /**
     * 这个接口现在在库存历史页面中使用， 产品详情用realDetail接口
     * @param productId
     * @return
     */
    @GetMapping("/bomDetailWholeInfo")
    public RespBean<BomListOfProductResp> bomDetailWholeInfo(@RequestParam("productId") Long productId) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            BomListOfProductResp result = CKProductFacade.bomDetailWholeInfo(productId, user);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProductController#bomDetail,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProductController#bomDetail,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    //获取可用商品列表
    @GetMapping("/bom/detail")
    public RespBean<List<BomDetailAndWarehouseListResp>> bomDetail(@RequestParam("bomId") Long bomId) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            List<BomDetailAndWarehouseListResp> result = CKProductFacade.bomDetail(bomId, user.getTenantId());
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
        } catch (ValidationException e) {
            log.error("ProjectController#importProductCreateExcel", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectController#importProductCreateExcel,", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    @GetMapping("/bom/exportBomExcel")
    public void exportBomExcel(HttpServletResponse response) throws IOException {
        Long tenantId = UserUtil.getCurrentUser().getTenantId();
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");

        // 文件名
        String fileName = URLEncoder.encode("成品原材料导入模板", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        // 准备数据，这里示例用空数据，如果有实际数据可以填充
        // 如果不想预填充测试数据，可传空列表 data = new ArrayList<>();
        List<ProductBomExcelModel> dataList = new ArrayList<>();

        // EasyExcel 写入
        EasyExcel.write(response.getOutputStream(), ProductBomExcelModel.class)
                .sheet("成品原材料导入模板")
                .doWrite(dataList);
    }


    @PostMapping("/bom/importBomExcel")
    public RespBean<Boolean> importBomExcel(@RequestParam("file") MultipartFile file, @RequestParam("productId") Long productId) {
        try {
            log.info("=== 导入成品原材料导入模板开始 ===");
            log.info("接收到文件: {}", file.getOriginalFilename());
            log.info("文件大小: {} bytes", file.getSize());
            log.info("文件类型: {}", file.getContentType());

            Long userId = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            log.info("用户ID: {}, 租户ID: {}", userId, tenantId);

            Boolean result = CKProductFacade.importBomExcel(file, tenantId, userId, productId);
            log.info("导入结果: {}", result);
            log.info("=== 导入sku接口结束 ===");
            return RespBean.success(true);
        } catch (ValidationException e) {
            log.error("ProductController#importBomExcel, req:{}", productId, e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProductController#importBomExcel,req:{}", productId, e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

}

