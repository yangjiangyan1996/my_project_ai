package com.example.controller.cangku;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.CkOutboundFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.cangku.dto.OutboundOrder;
import com.example.entity.cangku.req.*;
import com.example.entity.cangku.resp.*;
import com.example.entity.cangku.resp.excel.OutboundOderExcelModel;
import com.example.entity.cangku.resp.excel.OutboundSaleExcelModel;
import com.example.filter.UserUtil;
import com.example.annotations.LogOperation;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
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

            Page<OutboundListPageResp> result = outboundFacade.pageList(Page.of(req.getPage(), req.getSize()), req);
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
     * 获取销售出库详情
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
     * 获取生产领料出库详情
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
     * 获取出库详情简化版 ，数量是根据 ck_production_task 的 remaining_quantity字段
     * 目前使用的地方是 ： 创建生产入库单时导入出库单时使用
     * @param orderId 出库订单ID
     * @return 返回出库详情
     */
    @GetMapping("/simpleDetailOfProductionOutboundDetail")
    public RespBean<OutBoundDetailOfProductionResp> simpleDetailOfProductionOutboundDetail(@RequestParam("orderId") Long orderId) {
        try {
            OutBoundDetailOfProductionResp result = outboundFacade.simpleDetailOfProductionOutboundDetail(orderId, UserUtil.getCurrentUser().getTenantId());
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
     * 创建生产领料出库单
     * @param req 创建出库单请求参数
     * @return 返回是否创建成功
     */
    @LogOperation(module = "出库管理", operation = "创建出库单",
            description = "创建生产领料出库单")
    @PostMapping("/createProductionPickingOutBound") // POST映射到/create路径
    public RespBean<Boolean> createProductionPicking(@RequestBody OutboundCreateReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = outboundFacade.createProductionPickingOutBound(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("OutboundContorller#createProductionPicking,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("OutboundContorller#createProductionPicking,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }



    /**
     * 创建销售出库单
     * @param req 创建出库单请求参数
     * @return 返回是否创建成功
     */
    @LogOperation(module = "出库管理", operation = "创建出库单",
            description = "创建销售出库单")
    @PostMapping("/createProductionSaleOutBound") // POST映射到/create路径
    public RespBean<Boolean> createProductionPicking(@RequestBody OutboundCreateSaleProductReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = outboundFacade.createProductionSaleOutBound(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("OutboundContorller#createProductionSaleOutBound,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("OutboundContorller#createProductionSaleOutBound,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }



    /**
     * 更新生产领料出库单
     * @param req 更新出库单请求参数
     * @return 返回是否更新成功
     */
    @LogOperation(module = "出库管理", operation = "更新出库单",
            description = "更新生产领料出库单")
    @PostMapping("/updateProductionPickingOutBound") // POST映射到/update路径
    public RespBean<Boolean> updateProductionPickingOutBound(@RequestBody OutboundCreateReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = outboundFacade.updateProductionPickingOutBound(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("OutboundContorller#updateProductionPickingOutBound,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("OutboundContorller#updateProductionPickingOutBound,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    /**
     * 更新销售出库单
     * @param req 更新出库单请求参数
     * @return 返回是否更新成功
     */
    @LogOperation(module = "出库管理", operation = "更新出库单",
            description = "更新销售出库单")
    @PostMapping("/updateProductionSaleOutBound") // POST映射到/update路径
    public RespBean<Boolean> updateProductionPickingOutBound(@RequestBody OutboundCreateSaleProductReq req) {
        try {
            Long userId  = UserUtil.getCurrentUser().getId();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();

            req.setUserId(userId);
            req.setTenantId(tenantId);
            Boolean result = outboundFacade.updateProductionSaleOutBound(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("OutboundContorller#updateProductionSaleOutBound,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("OutboundContorller#updateProductionSaleOutBound,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }




    /**
     * 审核通过出库单
     * @param req 审核通过请求参数
     * @return 返回是否审核成功
     */
    @LogOperation(module = "出库管理", operation = "审核通过出库单",
            description = "审核通过出库单")
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
    @LogOperation(module = "出库管理", operation = "删除出库单",
            description = "删除出库单")
    @PostMapping("/delete")
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


    @PostMapping("/exportExcel")
    @LogOperation(module = "出库管理", operation = "导出销售出库数量模版",
            description = "导出销售出库数量模版")
    public void exportSkuExcel(HttpServletResponse response, @RequestBody OutboundExportSaleProductReq req) throws IOException {
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
            List<OutboundSaleExcelModel> dataList = outboundFacade.getOutboundSaleExportData(tenantId, req.getWarehouseId(), req.getProductIds());

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
    @LogOperation(module = "出库管理", operation = "导出销售出库数量模版",
            description = "导出销售出库数量模版")
    public void exportOutboundOrderExcel(HttpServletResponse response,
                                         @RequestParam(required = false, value = "orderId") Long orderId,
                                         @RequestParam(required = false, value = "orderIds") String orderIds) throws IOException {
        try{
            if (orderId == null && StringUtils.isEmpty(orderIds)) {
                throw new ValidationException("请选择出库单");
            }
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");


            // 文件名
            String fileName = null;

            // 准备数据，这里示例用空数据，如果有实际数据可以填充
            List<OutboundOderExcelModel> dataList = new ArrayList<>();
            // 如果不想预填充测试数据，可传空列表 data = new ArrayList<>();
            if (StringUtils.isNotBlank(orderIds)) {
                String[] ids = orderIds.split(",");
                StringBuilder sb = new StringBuilder();
                sb.append("出库单-");
                for (String id : ids) {
                    if (StringUtils.isNotBlank(id)) {
                        OutboundOrder o = outboundFacade.exportOutboundOrderExcelName(tenantId, Long.valueOf(id));
                        sb.append(o.getRelatedOrderNo());
                        sb.append("-");
                        List<OutboundOderExcelModel> outboundOderExcelModels = outboundFacade.exportOutboundOrderExcel(tenantId, Long.valueOf(id));
                        if (CollUtil.isNotEmpty(outboundOderExcelModels)) {
                            dataList.addAll(outboundOderExcelModels);
                        }
                    }
                }
                fileName = sb.toString();
            } else {
                OutboundOrder o = outboundFacade.exportOutboundOrderExcelName(tenantId, orderId);
                fileName = "出库单-"+ o.getRelatedOrderNo();
                dataList = outboundFacade.exportOutboundOrderExcel(tenantId, orderId);
            }

            // URL编码文件名，防止中文乱码
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName + ".xlsx");


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


    @LogOperation(module = "出库管理", operation = "导入商品及数量", description = "导入商品及数量")
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


    @GetMapping("/listCompletedOutBoundProduction")
    public RespBean<List<OutBoundComplateProductResp>> listCompletedOutBoundProduction(@RequestParam("orderType") Integer orderType) {
        try {
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            List<OutBoundComplateProductResp> result = outboundFacade.listCompletedOutBoundProduction(tenantId, orderType);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("OutboundContorller#listCompletedOutBoundProduction,orderType:{}",orderType, e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("OutboundContorller#listCompletedOutBoundProduction,orderType:{}",orderType, e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    /**
     * 入库管理页面展示数量
     *
     * @param req
     * @return
     */
    @PostMapping("/countsOfManagePage")
    public RespBean<OutboundCountOfManagePageResp> countsOfManagePage(@RequestBody OutboundListPageReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Long tenantId = UserUtil.getCurrentUser().getTenantId();
            req.setTenantId(tenantId);
            req.setUserId(user.getId());

            OutboundCountOfManagePageResp result = outboundFacade.countsOfManagePage(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("OutboundContorller#countsOfManagePage", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("OutboundContorller#countsOfManagePage", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }
}
