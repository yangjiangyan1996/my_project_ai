package com.example.validhandle;

import com.example.entity.cangku.req.OutboundCreateSaleProductReq;
import com.example.entity.cangku.vo.SaleOutBoundItemExtVO;
import jakarta.validation.ValidationException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OutboundCreateSaleProductReqValidator {

    /**
     * 基础字段校验
     */
    public static ValidationResult validateBasicFields(OutboundCreateSaleProductReq req) {
        ValidationResult result = new ValidationResult();

        // 1. 必填字段校验
        if (req.getOrderType() == null) {
            throw new ValidationException("出库类型不能为空");
        }

        if (req.getWarehouseId() == null) {
            throw new ValidationException("仓库不能为空");
        }

        if (req.getCustomerId() == null) {
            throw new ValidationException("客户不能为空");
        }

        if (!StringUtils.hasText(req.getExpectedDate())) {
            throw new ValidationException("预计出库日期不能为空");
        }

        // 2. 枚举值校验
        if (req.getOrderType() != null) {
            // 出库类型只能是销售出库(1)
            if (req.getOrderType() != 1) {
                throw new ValidationException("出库类型值无效，只能是销售出库(1)");
            }
        }

        // 3. 状态校验
        if (req.getStatus() != null) {
            // 0=草稿, 1=待审核, 2=已审核, 3=已出库, 4=已取消
            if (!ArrayUtils.contains(new Integer[]{0, 1, 2, 3, 4}, req.getStatus())) {
                throw new ValidationException("状态值无效，只能是0-4");
            }
        }

        // 4. 字符串长度校验
        if (req.getRemark() != null && req.getRemark().length() > 500) {
            throw new ValidationException("备注不能超过500个字符");
        }

        if (req.getRelatedOrderNo() != null && req.getRelatedOrderNo().length() > 50) {
            throw new ValidationException("关联单号不能超过50个字符");
        }

        return result;
    }

    /**
     * 产品明细校验
     */
    public static ValidationResult validateItems(OutboundCreateSaleProductReq req) {
        ValidationResult result = new ValidationResult();

        if (CollectionUtils.isEmpty(req.getItems())) {
            throw new ValidationException("产品明细不能为空");
        }

        // 检查是否有重复的产品
        Set<Long> productIds = new HashSet<>();
        for (int i = 0; i < req.getItems().size(); i++) {
            OutboundCreateSaleProductReq.OrderItemInner item = req.getItems().get(i);
            String indexPrefix = "items[" + i + "].";

            // 1. 必填字段校验
            if (item.getProductId() == null) {
                throw new ValidationException("产品ID不能为空");
            }

            if (!StringUtils.hasText(item.getProductName())) {
                throw new ValidationException("产品名称不能为空");
            }

            if (!StringUtils.hasText(item.getSku())) {
                throw new ValidationException("SKU不能为空");
            }

            if (item.getQuantity() == null) {
                throw new ValidationException("出库数量不能为空");
            } else if (item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException( "出库数量必须大于0");
            }

            if (item.getPrice() == null) {
                throw new ValidationException("单价不能为空");
            } else if (item.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("单价不能为负数");
            }

            if (item.getPriceUnitUsd() == null) {
                throw new ValidationException("USD单价不能为空");
            } else if (item.getPriceUnitUsd().compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("USD单价不能为负数");
            }

            // 2. 重复产品校验
//            if (item.getProductId() != null) {
//                if (productIds.contains(item.getProductId())) {
//                    throw new ValidationException("产品ID重复:" + item.getProductId());
//                } else {
//                    productIds.add(item.getProductId());
//                }
//            }

            // 3. 金额计算校验
            if (item.getQuantity() != null && item.getPrice() != null) {
                BigDecimal calculatedTotal = item.getQuantity().multiply(item.getPrice());
                if (item.getPriceTotal() != null &&
                        item.getPriceTotal().compareTo(calculatedTotal) != 0) {
                    throw new ValidationException("金额计算错误，应为: " + calculatedTotal + ", 实际: " + item.getPriceTotal());
                }
            }

            if (item.getQuantity() != null && item.getPriceUnitUsd() != null) {
                BigDecimal calculatedUsdTotal = item.getQuantity().multiply(item.getPriceUnitUsd());
                if (item.getPriceTotalUsd() != null &&
                        item.getPriceTotalUsd().compareTo(calculatedUsdTotal) != 0) {
                    throw new ValidationException("USD总金额计算错误，应为: " + calculatedUsdTotal + ", 实际: " + item.getPriceTotalUsd());
                }
            }

            // 4. 批次分配校验
            validateBatchAllocations(item, indexPrefix, result);

            // 5. 扩展信息校验
            validateExtension(item.getExtension(), indexPrefix, result);

            // 6. 字符串长度校验
            if (item.getRemark() != null && item.getRemark().length() > 100) {
                throw new ValidationException("产品备注不能超过100个字符");
            }

            if (item.getSpec() != null && item.getSpec().length() > 50) {
                throw new ValidationException("规格不能超过50个字符");
            }

            if (item.getColor() != null && item.getColor().length() > 20) {
                throw new ValidationException("颜色不能超过20个字符");
            }
        }

        return result;
    }

    /**
     * 批次分配校验
     */
    private static void validateBatchAllocations(OutboundCreateSaleProductReq.OrderItemInner item, String prefix, ValidationResult result) {
        if (CollectionUtils.isEmpty(item.getBatchAllocations())) {
            // 如果没有批次分配，检查是否强制需要
            if (item.getQuantity() != null && item.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                throw new ValidationException("出库数量大于0时，必须进行批次分配");
            }
            return;
        }

        BigDecimal totalAllocated = BigDecimal.ZERO;
        Set<String> batchShelfKeys = new HashSet<>();

        for (int j = 0; j < item.getBatchAllocations().size(); j++) {
            OutboundCreateSaleProductReq.BatchAllocationInner allocation = item.getBatchAllocations().get(j);
            String allocPrefix = prefix + "batchAllocations[" + j + "].";

            // 必填字段校验
            if (!StringUtils.hasText(allocation.getBatchNo())) {
                throw new ValidationException("批次号不能为空");
            }

            if (allocation.getShelfId() == null) {
                throw new ValidationException("货架ID不能为空");
            }

            if (!StringUtils.hasText(allocation.getShelfName())) {
                throw new ValidationException("货架名称不能为空");
            }

            if (allocation.getQuantity() == null) {
                throw new ValidationException("分配数量不能为空");
            } else if (allocation.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("分配数量必须大于0");
            }

            // 批次货架唯一性校验
            String batchShelfKey = allocation.getBatchNo() + "_" + allocation.getShelfId();
            if (batchShelfKeys.contains(batchShelfKey)) {
                throw new ValidationException(batchShelfKey + "批次货架已存在");
            } else {
                batchShelfKeys.add(batchShelfKey);
            }

            // 累加分配数量
            if (allocation.getQuantity() != null) {
                totalAllocated = totalAllocated.add(allocation.getQuantity());
            }
        }

        // 检查分配总数是否等于出库数量
        if (item.getQuantity() != null && totalAllocated.compareTo(item.getQuantity()) != 0) {
            throw new ValidationException("批次分配总数(" + totalAllocated + ")必须等于出库数量(" + item.getQuantity() + ")");
        }
    }

    /**
     * 扩展信息校验
     */
    private static void validateExtension(SaleOutBoundItemExtVO extension, String prefix, ValidationResult result) {
        if (extension == null) {
            throw new ValidationException("扩展信息不能为空");
        }

        // 扩展信息必填字段校验
        if (extension.getProductId() == null) {
            throw new ValidationException(" 扩展信息中的产品ID不能为空 ");
        }

        if (extension.getIsTriggerProduct() == null) {
            throw new ValidationException( " 是否触发产品不能为空 ");
        }

        if (extension.getIsRecommendProduct() == null) {
            throw new ValidationException(" 是否推荐产品不能为空 ");
        }

        if (extension.getTriggerProductId() == null) {
            // 如果是推荐产品，必须有触发产品ID
            if (extension.getIsRecommendProduct() == 0) {
                throw new ValidationException(" 推荐产品必须指定触发产品ID ");
            }
        }
    }

    /**
     * 总额校验
     */
    public static ValidationResult validateTotals(OutboundCreateSaleProductReq req) {
        ValidationResult result = new ValidationResult();

        if (CollectionUtils.isEmpty(req.getItems())) {
            return result;
        }

        // 计算所有产品的小计
        BigDecimal calculatedTotalQuantity = BigDecimal.ZERO;
        BigDecimal calculatedTotalAmount = BigDecimal.ZERO;
        BigDecimal calculatedTotalAmountUsd = BigDecimal.ZERO;

        for (OutboundCreateSaleProductReq.OrderItemInner item : req.getItems()) {
            if (item.getQuantity() != null) {
                calculatedTotalQuantity = calculatedTotalQuantity.add(item.getQuantity());
            }

            if (item.getPriceTotal() != null) {
                calculatedTotalAmount = calculatedTotalAmount.add(item.getPriceTotal());
            }

            if (item.getPriceTotalUsd() != null) {
                calculatedTotalAmountUsd = calculatedTotalAmountUsd.add(item.getPriceTotalUsd());
            }
        }

        // 校验总额
        if (req.getTotalQuantity() != null &&
                req.getTotalQuantity().compareTo(calculatedTotalQuantity) != 0) {
            throw new ValidationException(
                    "总数量计算错误，应为: " + calculatedTotalQuantity + ", 实际: " + req.getTotalQuantity());
        }

        if (req.getTotalAmount() != null &&
                req.getTotalAmount().compareTo(calculatedTotalAmount) != 0) {
            throw new ValidationException(
                    "总金额计算错误，应为: " + calculatedTotalAmount + ", 实际: " + req.getTotalAmount());
        }

        if (req.getTotalAmountUsd() != null &&
                req.getTotalAmountUsd().compareTo(calculatedTotalAmountUsd) != 0) {
            throw new ValidationException(
                    "USD总金额计算错误，应为: " + calculatedTotalAmountUsd + ", 实际: " + req.getTotalAmountUsd());
        }

        return result;
    }

    /**
     * 业务规则校验
     */
    public static ValidationResult validateBusinessRules(OutboundCreateSaleProductReq req) {
        ValidationResult result = new ValidationResult();

        // 1. 草稿状态可以不进行完整校验
        if (req.getStatus() != null && req.getStatus() == 0) {
            // 草稿状态允许不完整数据
            return result;
        }

        // 2. 提交审核状态的额外校验
        if (req.getStatus() != null && req.getStatus() >= 1) {
            // 必须有产品明细
            if (CollectionUtils.isEmpty(req.getItems())) {
                throw new ValidationException("提交审核时必须包含产品明细");
            }

            // 检查是否有产品
            boolean hasValidItems = false;
            for (OutboundCreateSaleProductReq.OrderItemInner item : req.getItems()) {
                if (item.getQuantity() != null && item.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                    hasValidItems = true;
                    break;
                }
            }

            if (!hasValidItems) {
                throw new ValidationException("提交审核时必须有出库数量大于0的产品");
            }
        }

        // 3. 用户和租户信息校验
        if (req.getUserId() == null) {
            throw new ValidationException("用户ID不能为空");
        }

        if (req.getTenantId() == null) {
            throw new ValidationException("租户ID不能为空");
        }

        return result;
    }

    /**
     * 完整校验
     */
    public static ValidationResult validateAll(OutboundCreateSaleProductReq req) {
        ValidationResult result = new ValidationResult();

        // 基础字段校验
        ValidationResult basicResult = validateBasicFields(req);
        result.merge(basicResult);

        // 产品明细校验
        if (CollectionUtils.isNotEmpty(req.getItems())) {
            ValidationResult itemsResult = validateItems(req);
            result.merge(itemsResult);
        }

        // 总额校验
        ValidationResult totalsResult = validateTotals(req);
        result.merge(totalsResult);

        // 业务规则校验
        ValidationResult businessResult = validateBusinessRules(req);
        result.merge(businessResult);

        return result;
    }

    /**
     * 校验结果类
     */
    public static class ValidationResult {
        private boolean valid = true;
        private List<ValidationError> errors;

        public void addError(String field, String message) {
            if (errors == null) {
                errors = new ArrayList<>();
            }
            errors.add(new ValidationError(field, message));
            valid = false;
        }

        public void merge(ValidationResult other) {
            if (!other.isValid()) {
                this.valid = false;
                if (this.errors == null) {
                    this.errors = new ArrayList<>();
                }
                if (other.getErrors() != null) {
                    this.errors.addAll(other.getErrors());
                }
            }
        }

        public boolean isValid() {
            return valid;
        }

        public List<ValidationError> getErrors() {
            return errors;
        }

        public String getErrorMessage() {
            if (errors == null || errors.isEmpty()) {
                return "";
            }
            return errors.stream()
                    .map(e -> e.getField() + ": " + e.getMessage())
                    .collect(Collectors.joining("; "));
        }
    }

    /**
     * 校验错误类
     */
    public static class ValidationError {
        private String field;
        private String message;

        public ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }
    }
}