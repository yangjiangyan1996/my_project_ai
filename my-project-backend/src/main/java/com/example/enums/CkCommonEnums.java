package com.example.enums;

import lombok.Getter;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/5 21:12
 */
public class CkCommonEnums {

    //状态:1-锁定中,2-已释放
    @Getter
    public enum LockStatus {
        Locking(1, "锁定中"),
        Released(2, "已释放");
        private Integer code;
        private String desc;

        /**
         * 构造方法，用于创建LockStatus对象
         *
         * @param code 锁定状态对应的整数值
         * @param desc 锁定状态的描述信息
         */
        LockStatus(Integer code, String desc) {
            // 使用传入的code值初始化对象的code属性
            this.code = code;
            // 使用传入的desc值初始化对象的desc属性
            this.desc = desc;
        }
    }

    /**
     * 紧急程度
     */
    @Getter
    public enum Priority {
        Urgent(1, "紧急"),
        High(2, "高"),
        Middle(3, "中"),
        Low(4, "低");
        private Integer code;
        private String desc;

        /**
         * 构造方法，用于创建Priority对象
         *
         * @param code 优先级对应的整数值
         * @param desc 优先级的描述信息
         */
        Priority(Integer code, String desc) {
            // 使用传入的code值初始化对象的code属性
            this.code = code;
            // 使用传入的desc值初始化对象的desc属性
            this.desc = desc;
        }
    }

    /**
     * 是否删除
     */
    @Getter
    public enum IsDeleted {
        NoDelete(0, "正常"),
        Delete(1, "被删除");
        private Integer code;
        private String desc;

        IsDeleted(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }

    /**
     * 状态
     */
    @Getter
    public enum Status {
        Enable(1, "启用"),
        Disable(0, "禁用");
        private Integer code;
        private String desc;

        Status(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }
}
