package com.example.controller.cangku;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.CkOutboundFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.cangku.req.OutboundApproveOkReq;
import com.example.entity.cangku.req.OutboundCreateReq;
import com.example.entity.cangku.req.OutboundDeleteReq;
import com.example.entity.cangku.req.OutboundListPageReq;
import com.example.entity.cangku.resp.OutBoundDetailOfProductionResp;
import com.example.entity.cangku.resp.OutBoundSaleQuantityImportResp;
import com.example.entity.cangku.resp.OutboundDetailResp;
import com.example.entity.cangku.resp.OutboundListPageResp;
import com.example.entity.cangku.resp.excel.OutboundOderExcelModel;
import com.example.entity.cangku.resp.excel.OutboundSaleExcelModel;
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
 * @Date 2025/11/7 01:03
 */
@RestController
@Slf4j
@RequestMapping("/api/auth/outbound/")
public class OutboundContorller {

    @Resource
    CkOutboundFacade outboundFacade;


    /**
     * 分页查询出库列表
     * @param req 出库列表查询请求参数
     * @return 返回分页结果
     */
    @PostMapping("/pageList") // POST映射到/pageList路径
    public RespBean<Page<OutboundListPageResp>> pageList(@RequestBody OutboundListPageReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            req.setTenantId(tenantId);
            req.setUserId(user.getId());

            Page<OutboundListPageResp> result = outboundFacade.pageList(Page.of(req.getPage() - 1, req.getSize()), req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("OutboundContorller#pageList,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("OutboundContorller#pageList,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }



    /**
     * 获取出库详情
     * @param orderId 出库订单ID
     * @return 返回出库详情
     */
    @GetMapping("/detail") // GET映射到/detail路径
    public RespBean<OutboundDetailResp> detail(@RequestParam("orderId") Long orderId) {
        try {
            OutboundDetailResp result = outboundFacade.detail(orderId, UserUtil.getCurrentUser().getTenantId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("OutboundContorller#detail,req:{}", JSON.toJSONString(orderId), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("OutboundContorller#detail,req:{}", JSON.toJSONString(orderId), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    /**
     * 获取出库详情 (新的)
     * @param orderId 出库订单ID
     * @return 返回出库详情
     */
    @GetMapping("/detailNew") //
    public RespBean<OutBoundDetailOfProductionResp> detailNew(@RequestParam("orderId") Long orderId) {
        try {
            OutBoundDetailOfProductionResp result = outboundFacade.detailNew(orderId, UserUtil.getCurrentUser().getTenantId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("OutboundContorller#detailNew,req:{}", JSON.toJSONString(orderId), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("OutboundContorller#detailNew,req:{}", JSON.toJSONString(orderId), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }



    /**
     * 创建出库单
     * @param req 创建出库单请求参数
     * @return 返回是否创建成功
     */
    @PostMapping("/create") // POST映射到/create路径
    public RespBean<Boolean> create(@RequestBody OutboundCreateReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = outboundFacade.create(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("OutboundContorller#create,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("OutboundContorller#create,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }





    /**
     * 更新出库单
     * @param req 更新出库单请求参数
     * @return 返回是否更新成功
     */
    @PostMapping("/update") // POST映射到/update路径
    public RespBean<Boolean> update(@RequestBody OutboundCreateReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = outboundFacade.update(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("OutboundContorller#update,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("OutboundContorller#update,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }




    /**
     * 审核通过出库单
     * @param req 审核通过请求参数
     * @return 返回是否审核成功
     */
    @PostMapping("/approveOk") // POST映射到/approveOk路径
    public RespBean<Boolean> approveOk(@RequestBody OutboundApproveOkReq req) {
        try {
            req.setUserId(UserUtil.getCurrentUser().getId());
            req.setTenantId(UserUtil.getCurrentUser().getTenantId());
            Boolean result = outboundFacade.approveOk(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("OutboundContorller#approveOk,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("OutboundContorller#approveOk,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }



    /**
     * 删除出库单
     * @param req 删除出库单请求参数
     * @return 返回是否删除成功
     */
    @PostMapping("/delete") // POST映射到/delete路径
    public RespBean<Boolean> delete(@RequestBody OutboundDeleteReq req) {
        try {
            Long userId = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = outboundFacade.delete(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("OutboundContorller#delete,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("OutboundContorller#delete,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    @GetMapping("/exportExcel")
    public void exportSkuExcel(HttpServletResponse response, @RequestParam("warehouseId") Long warehouseId) throws IOException {
        try{
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");

            // 文件名
            String fileName = URLEncoder.encode("销售出库数量导入模版", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

            // 准备数据，这里示例用空数据，如果有实际数据可以填充
            // 如果不想预填充测试数据，可传空列表 data = new ArrayList<>();
            List<OutboundSaleExcelModel> dataList = outboundFacade.getOutboundSaleExportData(tenantId, warehouseId);

            // EasyExcel 写入
            EasyExcel.write(response.getOutputStream(), OutboundSaleExcelModel.class)
                    .sheet("销售出库数量导入模版")
                    .doWrite(dataList);
        }catch (ValidationException e) {
            log.error("OutboundContorller#exportSkuExcel", e);
        } catch (Exception e) {
            log.error("OutboundContorller#exportSkuExcel,", e);
        }
    }


    @GetMapping("/exportOutboundOrderExcel")
    public void exportOutboundOrderExcel(HttpServletResponse response, @RequestParam("orderId") Long orderId) throws IOException {
        try{
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");

            // 文件名
            String fileName = URLEncoder.encode("出库单", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

            // 准备数据，这里示例用空数据，如果有实际数据可以填充
            // 如果不想预填充测试数据，可传空列表 data = new ArrayList<>();
            List<OutboundOderExcelModel> dataList = outboundFacade.exportOutboundOrderExcel(tenantId, orderId);

            // EasyExcel 写入
            EasyExcel.write(response.getOutputStream(), OutboundOderExcelModel.class)
                    .sheet("出库单")
                    .doWrite(dataList);
        }catch (ValidationException e) {
            log.error("OutboundContorller#exportSkuExcel", e);
        } catch (Exception e) {
            log.error("OutboundContorller#exportSkuExcel,", e);
        }
    }


    @PostMapping("/importOutboundSaleQuantity")
    public RespBean<List<OutBoundSaleQuantityImportResp>> importOutboundSaleQuantity(@RequestParam("file") MultipartFile file, @RequestParam("warehouseId") Long warehouseId) {
        try {
            log.info("=== 导入销售出货接口开始 ===");
            log.info("接收到文件: {}. 文件大小: {} bytes, 文件类型: {}", file.getOriginalFilename(), file.getSize(), file.getContentType());

            Long userId = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            List<OutBoundSaleQuantityImportResp> result = outboundFacade.importOutboundSaleQuantity(file, tenantId, userId,warehouseId);
            log.info("=== 导入销售出货接口结束 ===导入结果: {}", result);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("OutboundContorller#importSku", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("OutboundContorller#importSku,", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

}
