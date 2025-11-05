package com.example.enums;

import lombok.Getter;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/5 21:12
 */
public class CkCommonEnums {
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
