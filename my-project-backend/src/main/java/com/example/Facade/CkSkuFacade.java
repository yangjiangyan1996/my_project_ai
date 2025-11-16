package com.example.Facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.cangku.dto.Customer;
import com.example.entity.cangku.dto.CustomerSkuMapping;
import com.example.entity.cangku.dto.Product;
import com.example.entity.cangku.req.SkuDeleteReq;
import com.example.entity.cangku.req.SkuImportDto;
import com.example.entity.cangku.req.SkuListPageReq;
import com.example.entity.cangku.resp.SkuPageListResp;
import com.example.service.CkCustomerService;
import com.example.service.CkCustomerSkuMappingService;
import com.example.service.CkProductService;
import com.example.utils.ExcelUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/16 23:15
 */
@Service
@Slf4j
public class CkSkuFacade {
    @Resource
    CkCustomerService customerService;
    @Resource
    CkProductService productService;
    @Resource
    private CkCustomerSkuMappingService customerSkuMappingService;

    public Page<SkuPageListResp> pageList(Page<CustomerSkuMapping> page, SkuListPageReq req) {
        if (!StringUtils.isEmpty(req.getProductName())) {
            List<Product> products = productService.selectByProductNameLike(req.getTenantId(), req.getProductName());
            List<String> skuCodes = products.stream().map(v -> v.getSku()).distinct().collect(Collectors.toList());
            req.setProductSkus(skuCodes);
        }
        Page<CustomerSkuMapping> list = customerSkuMappingService.getPage(page, req);
        if (list.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }
        List<String> skuList = list.getRecords().stream().map(v -> v.getProductSku()).distinct().collect(Collectors.toList());
        Map<String, Product> productSku2ProductMap = new HashMap<>();
        List<Product> products = productService.selectBySkus(req.getTenantId(), skuList);
        if (!CollectionUtils.isEmpty(products)) {
            productSku2ProductMap = products.stream().collect(Collectors.toMap(Product::getSku, v -> v));
        }


        List<Long> customerIds = list.getRecords().stream().map(v -> v.getCustomerId()).distinct().collect(Collectors.toList());
        List<Customer> customers = customerService.selectByTenantIdAndCustomerIds(req.getTenantId(), customerIds);
        Map<Long, Customer> customerId2CustomerMap = customers.stream().collect(Collectors.toMap(Customer::getId, v -> v));

        Map<String, Product> finalProductSku2ProductMap = productSku2ProductMap;
        List<SkuPageListResp> collect = list.getRecords().stream().map(v -> {
            SkuPageListResp p = new SkuPageListResp();
            BeanUtils.copyProperties(v, p);

            if (finalProductSku2ProductMap.containsKey(v.getProductSku())) {
                Product product = finalProductSku2ProductMap.get(v.getProductSku());
                p.setProductName(product.getName());
                p.setColor(product.getColor());
                p.setSpec(product.getSpec());
            }

            if (customerId2CustomerMap.containsKey(v.getCustomerId())) {
                Customer customer = customerId2CustomerMap.get(v.getCustomerId());
                p.setCustomerName(customer.getCustomerName());
            }
            return p;
        }).collect(Collectors.toList());

        Page<SkuPageListResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;
    }

    public Boolean delete(SkuDeleteReq req) {
        CustomerSkuMapping p = customerSkuMappingService.getById(req.getId());
        if (p == null) {
            throw new ValidationException("sku映射不存在");
        }

        CustomerSkuMapping save = new CustomerSkuMapping();
        save.setId(req.getId());
        save.setIsDeleted(1);
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());
        return customerSkuMappingService.updateById(save);
    }

    public void exportExcel(HttpServletResponse response) {
        try {
            // 设置响应头
            setupResponseHeader(response);

            // 生成Excel
            generateExcel(response);

        } catch (Exception e) {
            log.error("导出SKU映射Excel失败", e);
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    private void setupResponseHeader(HttpServletResponse response) {
        try {
            String fileName = "导入sku模版.xlsx";
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8")
                    .replaceAll("\\+", "%20");

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("文件名编码失败", e);
        }
    }

    private void generateExcel(HttpServletResponse response)
            throws IOException {

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("SKU映射数据");

            // 设置列宽
            sheet.setColumnWidth(0, 20 * 256);  // 产品名称
            sheet.setColumnWidth(1, 15 * 256);  // 规格
            sheet.setColumnWidth(2, 10 * 256);  // 颜色
            sheet.setColumnWidth(3, 20 * 256);  // 客户SKU

            // 创建标题行样式
            CellStyle headerStyle = createHeaderStyle(workbook);

            // 创建标题行
            createHeaderRow(sheet, headerStyle);

            // 写入响应流
            try {
                workbook.write(response.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // 设置字体
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setFontName("微软雅黑");
        style.setFont(font);

        // 设置背景色
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 设置边框
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // 居中对齐
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    private void createHeaderRow(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);

        String[] headers = {"产品名称", "规格", "颜色", "客户SKU"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    public Boolean importSku(MultipartFile file, Long tenantId, Long userId, Long customerId) {
        // 1. 读取Excel数据
        List<SkuImportDto> importDataList = ExcelUtils.readExcel(file, SkuImportDto.class);
        if (CollectionUtils.isEmpty(importDataList) || importDataList.size() < 1) {
            throw new ValidationException("Excel文件数据不足");
        }

        List<CustomerSkuMapping> customerSkuMappings = customerSkuMappingService.selectByCustomerId(customerId, tenantId);
        Map<String, CustomerSkuMapping> productSku2CustomerSkuMappingMap = customerSkuMappings.stream().collect(Collectors.toMap(CustomerSkuMapping::getProductSku, v -> v));

        List<SkuImportDto> productImportList = importDataList.subList(0, importDataList.size());

        List<CustomerSkuMapping> collect = productImportList.stream().map(v -> {
            if (productSku2CustomerSkuMappingMap.containsKey(v.getProductSku())) {
               //跳过
                return null;
            }
            CustomerSkuMapping s = new CustomerSkuMapping();
            s.setTenantId(tenantId);
            s.setProductSku(v.getProductSku());
            s.setCustomerSku(v.getCustomerSku());
            s.setCustomerId(customerId);
            s.setModifiedAt(new Date());
            s.setModifiedBy(userId);
            s.setCreatedAt(new Date());
            s.setCreatedBy(userId);
            s.setStatus(1);
            return s;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        return customerSkuMappingService.saveBatch(collect);
    }
}
