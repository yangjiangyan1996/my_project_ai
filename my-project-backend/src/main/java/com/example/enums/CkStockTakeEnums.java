package com.example.enums;

import lombok.Getter;

/**
 * @Author YangJian
 * @Description 盘点相关枚举
 * @Email 1776080295@qq.com
 * @Date 2025/12/24 00:14
 */
public class CkStockTakeEnums {
    
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
    
    // 盘点策略枚举
    @Getter
    public enum TakeStrategy {
        FULL_WAREHOUSE(1, "全库盘点"),
        AREA_WISE(2, "区域盘点"),
        CATEGORY_WISE(3, "分类盘点"),
        CYCLE_COUNT(4, "循环盘点"),
        RANDOM_SAMPLE(5, "随机抽盘"),
        ;
        
        private Integer code;
        private String desc;
        
        TakeStrategy(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public static String getDescByCode(Integer code) {
            for (CkStockTakeEnums.TakeStrategy value : CkStockTakeEnums.TakeStrategy.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }
    }
    
    // 盘点周期类型枚举
    @Getter
    public enum CycleType {
        DAILY(1, "日盘"),
        WEEKLY(2, "周盘"),
        MONTHLY(3, "月盘"),
        QUARTERLY(4, "季度盘"),
        YEARLY(5, "年盘"),
        ;
        
        private Integer code;
        private String desc;
        
        CycleType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public static String getDescByCode(Integer code) {
            for (CkStockTakeEnums.CycleType value : CkStockTakeEnums.CycleType.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
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
    
    // 执行状态枚举
    @Getter
    public enum ExecuteStatus {
        NOT_STARTED(0, "未开始"),
        IN_PROGRESS(1, "进行中"),
        COMPLETED(2, "已完成"),
        SUSPENDED(3, "已暂停"),
        CANCELLED(9, "已取消"),
        ;
        
        private Integer code;
        private String desc;
        
        ExecuteStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public static String getDescByCode(Integer code) {
            for (CkStockTakeEnums.ExecuteStatus value : CkStockTakeEnums.ExecuteStatus.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }
        
        public static boolean isInProgress(Integer code) {
            return IN_PROGRESS.getCode().equals(code);
        }
        
        public static boolean isCompleted(Integer code) {
            return COMPLETED.getCode().equals(code);
        }
    }
    
    // 调整状态枚举
    @Getter
    public enum AdjustStatus {
        NOT_ADJUSTED(0, "未调整"),
        ADJUSTING(1, "调整中"),
        ADJUSTED(2, "已调整"),
        ;
        
        private Integer code;
        private String desc;
        
        AdjustStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public static String getDescByCode(Integer code) {
            for (CkStockTakeEnums.AdjustStatus value : CkStockTakeEnums.AdjustStatus.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }
    }
    
    // 盘点明细状态枚举
    @Getter
    public enum CountStatus {
        PENDING(0, "待盘点"),
        FIRST_COUNTED(1, "已初盘"),
        SECOND_COUNTED(2, "已复盘"),
        CONFIRMED(3, "已确认"),
        DIFFERENCE_PENDING(4, "差异待处理"),
        ADJUSTED(5, "已调整"),
        ;
        
        private Integer code;
        private String desc;
        
        CountStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public static String getDescByCode(Integer code) {
            for (CkStockTakeEnums.CountStatus value : CkStockTakeEnums.CountStatus.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }
        
        public static boolean canEdit(Integer code) {
            return PENDING.getCode().equals(code) || FIRST_COUNTED.getCode().equals(code);
        }
    }
    
    // 库存类型枚举
    @Getter
    public enum InventoryType {
        NORMAL_INVENTORY(1, "普通库存"),
        BATCH_INVENTORY(2, "批次库存"),
        SHELF_INVENTORY(3, "货架库存"),
        ;
        
        private Integer code;
        private String desc;
        
        InventoryType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public static String getDescByCode(Integer code) {
            for (CkStockTakeEnums.InventoryType value : CkStockTakeEnums.InventoryType.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }
    }
    
    // 盘点方式枚举
    @Getter
    public enum CountMethod {
        MANUAL(1, "人工盘点"),
        RFID(2, "RFID盘点"),
        VISION(3, "视觉识别"),
        AUTOMATED(4, "自动化设备"),
        ;
        
        private Integer code;
        private String desc;
        
        CountMethod(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public static String getDescByCode(Integer code) {
            for (CkStockTakeEnums.CountMethod value : CkStockTakeEnums.CountMethod.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }
    }
    
    // 差异原因枚举
    @Getter
    public enum DifferenceReason {
        INPUT_ERROR(1, "录入错误"),
        MISSED_COUNT(2, "漏盘"),
        THEFT_LOSS(3, "偷盗丢失"),
        DAMAGE_UNREPORTED(4, "损坏未报"),
        NATURAL_LOSS(5, "自然损耗"),
        SYSTEM_ERROR(6, "系统错误"),
        DUPLICATE_COUNT(7, "多盘重复"),
        UNIT_CONVERSION_ERROR(8, "单位换算错误"),
        IN_TRANSIT(9, "盘点时在途"),
        OTHER(99, "其他原因"),
        ;
        
        private Integer code;
        private String desc;
        
        DifferenceReason(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public static String getDescByCode(Integer code) {
            for (CkStockTakeEnums.DifferenceReason value : CkStockTakeEnums.DifferenceReason.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }
        
        public static DifferenceReason getByCode(Integer code) {
            for (CkStockTakeEnums.DifferenceReason value : CkStockTakeEnums.DifferenceReason.values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            return OTHER;
        }
    }
    
    // 差异等级枚举
    @Getter
    public enum DifferenceLevel {
        NO_DIFFERENCE(0, "无差异"),
        MINOR_DIFFERENCE(1, "微小差异"),
        NORMAL_DIFFERENCE(2, "一般差异"),
        MAJOR_DIFFERENCE(3, "重大差异"),
        ;
        
        private Integer code;
        private String desc;
        
        DifferenceLevel(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public static String getDescByCode(Integer code) {
            for (CkStockTakeEnums.DifferenceLevel value : CkStockTakeEnums.DifferenceLevel.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }
        
        public static DifferenceLevel calculateLevel(Double differenceRate, Double quantityDiff) {
            if (differenceRate == null || Math.abs(differenceRate) < 0.01) {
                return NO_DIFFERENCE;
            } else if (Math.abs(differenceRate) < 0.05) {
                return MINOR_DIFFERENCE;
            } else if (Math.abs(differenceRate) < 0.20) {
                return NORMAL_DIFFERENCE;
            } else {
                return MAJOR_DIFFERENCE;
            }
        }
    }
    
    // 审核状态枚举（盘点明细）
    @Getter
    public enum VerifyStatus {
        NOT_VERIFIED(0, "未审核"),
        VERIFIED_PASS(1, "审核通过"),
        VERIFIED_REJECT(2, "审核驳回"),
        ;
        
        private Integer code;
        private String desc;
        
        VerifyStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public static String getDescByCode(Integer code) {
            for (CkStockTakeEnums.VerifyStatus value : CkStockTakeEnums.VerifyStatus.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }
    }
    
    // 任务状态枚举
    @Getter
    public enum TaskStatus {
        PENDING_START(0, "待开始"),
        IN_PROGRESS(1, "进行中"),
        COMPLETED(2, "已完成"),
        INTERRUPTED(3, "异常中断"),
        CANCELLED(4, "已取消"),
        ;
        
        private Integer code;
        private String desc;
        
        TaskStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public static String getDescByCode(Integer code) {
            for (CkStockTakeEnums.TaskStatus value : CkStockTakeEnums.TaskStatus.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }
    }
    
    // 触发类型枚举（策略）
    @Getter
    public enum TriggerType {
        REGULAR(1, "定期盘点"),
        INVENTORY_THRESHOLD(2, "库存阈值触发"),
        BUSINESS_TRIGGER(3, "业务触发"),
        RANDOM_CHECK(4, "随机抽盘"),
        ABNORMAL_TRIGGER(5, "异常触发"),
        ;
        
        private Integer code;
        private String desc;
        
        TriggerType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public static String getDescByCode(Integer code) {
            for (CkStockTakeEnums.TriggerType value : CkStockTakeEnums.TriggerType.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }
    }
    
    // 时间维度枚举（汇总报表）
    @Getter
    public enum TimeDimension {
        DAILY(1, "日"),
        WEEKLY(2, "周"),
        MONTHLY(3, "月"),
        QUARTERLY(4, "季度"),
        YEARLY(5, "年"),
        ;
        
        private Integer code;
        private String desc;
        
        TimeDimension(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public static String getDescByCode(Integer code) {
            for (CkStockTakeEnums.TimeDimension value : CkStockTakeEnums.TimeDimension.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }
    }
    
    // 是否枚举
    @Getter
    public enum YesNoEnum {
        NO(0, "否"),
        YES(1, "是"),
        ;
        
        private Integer code;
        private String desc;
        
        YesNoEnum(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public static String getDescByCode(Integer code) {
            for (CkStockTakeEnums.YesNoEnum value : CkStockTakeEnums.YesNoEnum.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }
        
        public static boolean isYes(Integer code) {
            return YES.getCode().equals(code);
        }
    }
    
    // 盘点结果枚举
    @Getter
    public enum TakeResult {
        ACCURATE(1, "准确"),
        DIFFERENCE(2, "有差异"),
        NOT_COUNTED(3, "未盘点"),
        ;
        
        private Integer code;
        private String desc;
        
        TakeResult(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public static String getDescByCode(Integer code) {
            for (CkStockTakeEnums.TakeResult value : CkStockTakeEnums.TakeResult.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }
    }
    
    // 优先级枚举
    @Getter
    public enum PriorityLevel {
        HIGHEST(1, "最高"),
        HIGH(2, "高"),
        MEDIUM(3, "中"),
        LOW(4, "低"),
        LOWEST(5, "最低"),
        ;
        
        private Integer code;
        private String desc;
        
        PriorityLevel(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public static String getDescByCode(Integer code) {
            for (CkStockTakeEnums.PriorityLevel value : CkStockTakeEnums.PriorityLevel.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }
    }
    
    // 盘点异常类型枚举
    @Getter
    public enum ExceptionType {
        DEVICE_FAILURE(1, "设备故障"),
        NETWORK_ERROR(2, "网络异常"),
        DATA_LOSS(3, "数据丢失"),
        PERSONNEL_ABSENCE(4, "人员缺席"),
        OTHER_EXCEPTION(99, "其他异常"),
        ;
        
        private Integer code;
        private String desc;
        
        ExceptionType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public static String getDescByCode(Integer code) {
            for (CkStockTakeEnums.ExceptionType value : CkStockTakeEnums.ExceptionType.values()) {
                if (value.getCode().equals(code)) {
                    return value.getDesc();
                }
            }
            return null;
        }
    }
}