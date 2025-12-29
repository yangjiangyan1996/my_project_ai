package com.example.entity.cangku.req;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 盘点任务分配请求DTO
 * 用于接收前端传来的分配任务请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAssignTaskReq {

    /**
     * 盘点单ID
     */
    @NotNull(message = "盘点单ID不能为空")
    private Long stockTakeId;

    /**
     * 分配类型
     * 1: 按人分配
     * 2: 按条件分配
     */
    @NotNull(message = "分配类型不能为空")
    private Integer assignType;

    /**
     * 分配说明/备注
     */
    private String remark;

    /**
     * 分配维度（当assignType=2时使用）
     * 1: 按批次分配
     * 2: 按货架分配
     * 3: 按商品分配
     */
    private Integer assignDimension;

    /**
     * 分配条件列表（当assignType=2时使用）
     */
    private List<AssignCondition> conditions;

    /**
     * 分配负责人ID列表（当assignType=1时使用）
     */
    private List<Long> assigneeIds;

    /**
     * 分配负责人名称列表（当assignType=1时使用）
     */
    private List<String> assigneeNames;

    /**
     * 选中的盘点项ID列表（当需要分配选中项时使用）
     */
    //private List<AssignItem> items;

    /**
     * 计划开始时间
     */
    private Date planStartTime;

    /**
     * 计划结束时间
     */
    private Date planEndTime;

    /**
     * {@link com.example.enums.CkCommonEnums.Priority}
     * 优先级
     */
    private Integer priority;

    private Long userId;
    private Long tenantId;

    /**
     * 分配条件内部类
     * 用于按维度分配时的具体条件
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AssignCondition {

        /**
         * 维度类型
         * batchNo: 批次
         * shelfCode: 货架
         * productId: 商品ID
         */
        @NotBlank(message = "维度类型不能为空")
        private String dimension;

        /**
         * 维度值
         * 如批次号、货架号、商品ID等
         */
        @NotBlank(message = "维度值不能为空")
        private String dimensionValue;

        /**
         * 负责人ID列表
         * 允许多个负责人
         */
        @NotEmpty(message = "负责人ID列表不能为空")
        private List<Long> assigneeIds;

        /**
         * 负责人名称列表
         * 用于展示
         */
        private List<String> assigneeNames;
    }

    /**
     * 分配项内部类
     * 用于分配具体的盘点项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AssignItem {

        /**
         * 批次/货架ID/商品ID
         */
        @NotNull(message = "批次/货架ID/商品ID")
        private String dimensionValue;

        /**
         * 负责人ID列表
         * 允许多个负责人
         */
        @NotEmpty(message = "负责人ID列表不能为空")
        private List<Long> assigneeIds;

        /**
         * 负责人名称列表
         * 可选，用于展示
         */
        //private List<String> assigneeNames;
    }
}