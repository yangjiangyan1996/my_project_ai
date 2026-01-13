package com.example.entity.cangku.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2026/1/2 23:04
 */
@Data
public class ShelviesCreateWithZoneReq {
    private  Long tenantId;

    private  Long userId;

    private List<ShelfDTO> zones;
    private ShelfProductUsedAllReq shelf;

    @Data
    public static class ShelfProductUsedAllReq {
        /**
         * ID（新增时为空字符串）
         */
        private String id;

        /**
         * 货架编码
         */
        @NotBlank(message = "货架编码不能为空")
        @Size(max = 50, message = "货架编码长度不能超过50")
        private String shelfCode;

        /**
         * 货架名称
         */
        @NotBlank(message = "货架名称不能为空")
        @Size(max = 100, message = "货架名称长度不能超过100")
        private String shelfName;

        /**
         * 仓库ID
         */
        @NotNull(message = "仓库ID不能为空")
        @Min(value = 1, message = "仓库ID必须大于0")
        private Long warehouseId;

        /**
         * 货架类型
         */
        @NotNull(message = "货架类型不能为空")
        private Integer shelfType;

        /**
         * 排序顺序
         */
        @NotNull(message = "排序顺序不能为空")
        private Integer sortOrder;

        /**
         * 状态：0-禁用，1-启用
         */
        @NotNull(message = "状态不能为空")
        private Integer status;

        /**
         * 备注
         */
        @Size(max = 500, message = "备注长度不能超过500")
        private String remark;
    }


    @Data
    public static class ShelfDTO {

        /**
         * ID（新增时为空字符串）
         */
        private Long id;

        /**
         * 货位编码
         */
        @NotBlank(message = "货位编码不能为空")
        @Size(max = 50, message = "货位编码长度不能超过50")
        private String zoneCode;

        /**
         * 货位名称
         */
        @NotBlank(message = "货位名称不能为空")
        @Size(max = 100, message = "货位名称长度不能超过100")
        private String zoneName;

        /**
         * 区域
         */
        @Size(max = 50, message = "区域长度不能超过50")
        private String area;

        /**
         * 行号
         */
        @Size(max = 20, message = "行号长度不能超过20")
        private String rowN;

        /**
         * 列号
         */
        @Size(max = 20, message = "列号长度不能超过20")
        private String columnN;

        /**
         * 层数
         */
        @Size(max = 20, message = "层数长度不能超过20")
        private String layer;

        /**
         * 容量
         */
        @NotNull(message = "容量不能为空")
        @Min(value = 0, message = "容量不能小于0")
        private BigDecimal capacity;

        /**
         * 容量单位
         */
        @Size(max = 20, message = "容量单位长度不能超过20")
        private String capacityUnit;

        /**
         * 排序顺序
         */
        @NotNull(message = "排序顺序不能为空")
        private Integer sortOrder;

        /**
         * 状态：0-禁用，1-启用
         */
        @NotNull(message = "状态不能为空")
        private Integer status;

        /**
         * 备注
         */
        @Size(max = 500, message = "备注长度不能超过500")
        private String remark;
    }

}