package com.example.enums;

import lombok.Getter;

/**
 * @Author YangJian
 * @Description 盘点相关枚举
 * @Email 1776080295@qq.com
 * @Date 2025/12/24 00:14
 */
public class CkStockTakeEnums {

    //任务状态:1-待分配,2-已分配,3-执行中,4-已完成,5-已取消
    @Getter
    public enum StockTakeTaskStatus {
        WAIT_ASSIGN(1, "待分配"),
        ASSIGNED(2, "已分配"),
        EXECUTING(3, "执行中"),
        COMPLETED(4, "已完成"),
        CANCELED(5, "已取消"),
        ;
        private Integer code;
        private String desc;
        StockTakeTaskStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        public static String getDescByCode(Integer code) {
            for (CkStockTakeEnums.StockTakeTaskStatus value : CkStockTakeEnums.StockTakeTaskStatus.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }
    }

    /**
     *  /**
     *      * 分配类型
     *      * 1: 按人分配
     *      * 2: 按条件分配
     */
    @Getter
    public enum AssignType {
        PERSON(1, "按人分配"),
        CONDITION(2, "按条件分配"),
        ;
        private Integer code;
        private String desc;
        AssignType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static AssignType getByCode(Integer code) {
            for (CkStockTakeEnums.AssignType value : CkStockTakeEnums.AssignType.values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            return null;
        }
    }
    /**
     * 分配维度（当assignType=2时使用）
     *      * 1: 按批次分配
     *      * 2: 按货架分配
     *      * 3: 按商品分配
     */
    @Getter
    public enum AssignConditionType {
        BATCH(1, "按批次分配"),
        SHELF(2, "按货架分配"),
        PRODUCT(3, "按商品分配"),
        ;
        private Integer code;
        private String desc;
        AssignConditionType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static AssignConditionType getByCode(Integer code) {
            for (CkStockTakeEnums.AssignConditionType value : CkStockTakeEnums.AssignConditionType.values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            return null;
        }
    }

    //锁定范围:1: '全部', 2: '批次',  3: '货架', 4: '商品'
    @Getter
    public enum StockTakeScope {
        ALL(1, "全部"),
        BATCH(2, "批次"),
        SHELF(3, "货架"),
        PRODUCT(4, "商品"),
        ;

        private Integer code;
        private String desc;

        StockTakeScope(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static String getDescByCode(Integer code) {
            for (CkStockTakeEnums.StockTakeScope value : CkStockTakeEnums.StockTakeScope.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }

        public static StockTakeScope getByCode(Integer code) {
            for (CkStockTakeEnums.StockTakeScope value : CkStockTakeEnums.StockTakeScope.values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            return null;
        }
    }
    
    // 盘点方式枚举
    @Getter
    public enum TakeType {
        DYNAMIC_TAKE(1, "动态盘点"),
        STATIC_TAKE(2, "静态盘点"),
        ;
        
        private Integer code;
        private String desc;
        
        TakeType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public static String getDescByCode(Integer code) {
            for (CkStockTakeEnums.TakeType value : CkStockTakeEnums.TakeType.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }
        
        public static TakeType getByCode(Integer code) {
            for (CkStockTakeEnums.TakeType value : CkStockTakeEnums.TakeType.values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            return null;
        }
    }

    // 审批状态枚举
    @Getter
    public enum ApprovalStatus {
        UNSUBMITTED(0, "未提交"),
        UNDER_REVIEW(1, "审核中"),
        APPROVED(2, "已批准"),
        REJECTED(3, "已驳回"),
        ;
        
        private Integer code;
        private String desc;
        
        ApprovalStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public static String getDescByCode(Integer code) {
            for (CkStockTakeEnums.ApprovalStatus value : CkStockTakeEnums.ApprovalStatus.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }
        
        public static boolean isApproved(Integer code) {
            return APPROVED.getCode().equals(code);
        }
    }

    // 11-未调整 12-部分调整 13-全部调整

    // stock主表 执行状态枚举
    //0未开始,1盘点中,3、已完成
    //
    @Getter
    public enum TakeStatus {
        NOT_STARTED(0, "未开始"),
        IN_PROGRESS(1, "盘点中"),
//        COMPLETED(2, "待确认"),
        SUSPENDED(3, "已完成"),
        CANCELLED(9, "已取消"),
        ;
        
        private Integer code;
        private String desc;

        TakeStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public static String getDescByCode(Integer code) {
            for (CkStockTakeEnums.TakeStatus value : CkStockTakeEnums.TakeStatus.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }
        
        public static boolean isInProgress(Integer code) {
            return IN_PROGRESS.getCode().equals(code);
        }
        
//        public static boolean isCompleted(Integer code) {
//            return COMPLETED.getCode().equals(code);
//        }
    }

    //盘点详情表状态 状态:1-盘点中,2-已盘,3-已确认
    @Getter
    public enum StockItemStatus {
        NOT_ADJUSTED(1, "盘点中"),
        ADJUSTING(2, "已盘"),
        ADJUSTED(3, "已确认"),
        ;

        private Integer code;
        private String desc;

        StockItemStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static String getDescByCode(Integer code) {
            for (CkStockTakeEnums.StockItemStatus value : CkStockTakeEnums.StockItemStatus.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }
    }

//    // 调整状态枚举
//    @Getter
//    public enum AdjustStatus {
//        NOT_ADJUSTED(0, "未调整"),
//        ADJUSTING(1, "调整中"),
//        ADJUSTED(2, "已调整"),
//        ;
//
//        private Integer code;
//        private String desc;
//
//        AdjustStatus(Integer code, String desc) {
//            this.code = code;
//            this.desc = desc;
//        }
//
//        public static String getDescByCode(Integer code) {
//            for (CkStockTakeEnums.AdjustStatus value : CkStockTakeEnums.AdjustStatus.values()) {
//                if (value.getCode().equals(code)) {
//                    return value.getDesc();
//                }
//            }
//            return null;
//        }
//    }
    
    // 盘点方式枚举
//    @Getter
//    public enum CountMethod {
//        MANUAL(1, "人工盘点"),
//        RFID(2, "RFID盘点"),
//        VISION(3, "视觉识别"),
//        AUTOMATED(4, "自动化设备"),
//        ;
//
//        private Integer code;
//        private String desc;
//
//        CountMethod(Integer code, String desc) {
//            this.code = code;
//            this.desc = desc;
//        }
//
//        public static String getDescByCode(Integer code) {
//            for (CkStockTakeEnums.CountMethod value : CkStockTakeEnums.CountMethod.values()) {
//                if (value.getCode().equals(code)) {
//                    return value.getDesc();
//                }
//            }
//            return null;
//        }
//    }
    
    // 差异原因枚举
//    @Getter
//    public enum DifferenceReason {
//        INPUT_ERROR(1, "录入错误"),
//        MISSED_COUNT(2, "漏盘"),
//        THEFT_LOSS(3, "偷盗丢失"),
//        DAMAGE_UNREPORTED(4, "损坏未报"),
//        NATURAL_LOSS(5, "自然损耗"),
//        SYSTEM_ERROR(6, "系统错误"),
//        DUPLICATE_COUNT(7, "多盘重复"),
//        UNIT_CONVERSION_ERROR(8, "单位换算错误"),
//        IN_TRANSIT(9, "盘点时在途"),
//        OTHER(99, "其他原因"),
//        ;
//
//        private Integer code;
//        private String desc;
//
//        DifferenceReason(Integer code, String desc) {
//            this.code = code;
//            this.desc = desc;
//        }
//
//        public static String getDescByCode(Integer code) {
//            for (CkStockTakeEnums.DifferenceReason value : CkStockTakeEnums.DifferenceReason.values()) {
//                if (value.getCode().equals(code)) {
//                    return value.getDesc();
//                }
//            }
//            return null;
//        }
//
//        public static DifferenceReason getByCode(Integer code) {
//            for (CkStockTakeEnums.DifferenceReason value : CkStockTakeEnums.DifferenceReason.values()) {
//                if (value.getCode().equals(code)) {
//                    return value;
//                }
//            }
//            return OTHER;
//        }
//    }
    
    // 差异等级枚举
//    @Getter
//    public enum DifferenceLevel {
//        NO_DIFFERENCE(0, "无差异"),
//        MINOR_DIFFERENCE(1, "微小差异"),
//        NORMAL_DIFFERENCE(2, "一般差异"),
//        MAJOR_DIFFERENCE(3, "重大差异"),
//        ;
//
//        private Integer code;
//        private String desc;
//
//        DifferenceLevel(Integer code, String desc) {
//            this.code = code;
//            this.desc = desc;
//        }
//
//        public static String getDescByCode(Integer code) {
//            for (CkStockTakeEnums.DifferenceLevel value : CkStockTakeEnums.DifferenceLevel.values()) {
//                if (value.getCode().equals(code)) {
//                    return value.getDesc();
//                }
//            }
//            return null;
//        }
//
//        public static DifferenceLevel calculateLevel(Double differenceRate, Double quantityDiff) {
//            if (differenceRate == null || Math.abs(differenceRate) < 0.01) {
//                return NO_DIFFERENCE;
//            } else if (Math.abs(differenceRate) < 0.05) {
//                return MINOR_DIFFERENCE;
//            } else if (Math.abs(differenceRate) < 0.20) {
//                return NORMAL_DIFFERENCE;
//            } else {
//                return MAJOR_DIFFERENCE;
//            }
//        }
//    }
    

//    // 触发类型枚举（策略）
//    @Getter
//    public enum TriggerType {
//        REGULAR(1, "定期盘点"),
//        INVENTORY_THRESHOLD(2, "库存阈值触发"),
//        BUSINESS_TRIGGER(3, "业务触发"),
//        RANDOM_CHECK(4, "随机抽盘"),
//        ABNORMAL_TRIGGER(5, "异常触发"),
//        ;
//
//        private Integer code;
//        private String desc;
//
//        TriggerType(Integer code, String desc) {
//            this.code = code;
//            this.desc = desc;
//        }
//
//        public static String getDescByCode(Integer code) {
//            for (CkStockTakeEnums.TriggerType value : CkStockTakeEnums.TriggerType.values()) {
//                if (value.getCode().equals(code)) {
//                    return value.getDesc();
//                }
//            }
//            return null;
//        }
//    }
    
    // 时间维度枚举（汇总报表）
//    @Getter
//    public enum TimeDimension {
//        DAILY(1, "日"),
//        WEEKLY(2, "周"),
//        MONTHLY(3, "月"),
//        QUARTERLY(4, "季度"),
//        YEARLY(5, "年"),
//        ;
//
//        private Integer code;
//        private String desc;
//
//        TimeDimension(Integer code, String desc) {
//            this.code = code;
//            this.desc = desc;
//        }
//
//        public static String getDescByCode(Integer code) {
//            for (CkStockTakeEnums.TimeDimension value : CkStockTakeEnums.TimeDimension.values()) {
//                if (value.getCode().equals(code)) {
//                    return value.getDesc();
//                }
//            }
//            return null;
//        }
//    }

}