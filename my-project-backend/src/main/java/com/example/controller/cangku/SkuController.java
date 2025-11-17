package com.example.controller.cangku;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.CkSkuFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.cangku.req.SkuDeleteReq;
import com.example.entity.cangku.req.SkuListPageReq;
import com.example.entity.cangku.req.SkuUpdateStatusReq;
import com.example.entity.cangku.resp.SkuCreateReq;
import com.example.entity.cangku.resp.SkuPageListResp;
import com.example.entity.cangku.resp.excel.SkuExcelModel;
import com.example.filter.UserUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

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

    @GetMapping("/exportExcel")
    public void exportSkuExcel(HttpServletResponse response) throws IOException {
        Long tenantId = UserUtil.getCurrentUser().getTenantId();
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");

        // 文件名
        String fileName = URLEncoder.encode("sku导入模版", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        // 准备数据，这里示例用空数据，如果有实际数据可以填充
        // 如果不想预填充测试数据，可传空列表 data = new ArrayList<>();
        List<SkuExcelModel> dataList = skuFacade.getSkuExportData(tenantId);

        // EasyExcel 写入
        EasyExcel.write(response.getOutputStream(), SkuExcelModel.class)
                .sheet("SKU模板")
                .doWrite(dataList);
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
            return RespBean.success(true);
        }catch (ValidationException e) {
            log.error("SkuController#importSku", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("SkuController#importSku,", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    @PostMapping("/create")
    public RespBean<Boolean> create(@RequestBody SkuCreateReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = skuFacade.create(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("SkuController#create,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("SkuController#create,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/update")
    public RespBean<Boolean> update(@RequestBody SkuCreateReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = skuFacade.update(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("SkuController#update,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("SkuController#update,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/updateStatus")
    public RespBean<Boolean> updateStatus(@RequestBody SkuUpdateStatusReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = skuFacade.updateStatus(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("SkuController#updateStatus,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("SkuController#updateStatus,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }
}
