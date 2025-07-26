package com.example.enums;

import lombok.Getter;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/26 11:42
 */
public class TieEnum {
    @Getter
    public enum CommentStatusEnum {
        ok(0, "正常"),
        hide(1, "隐藏"),
        ;
        private Integer code;
        private String name;

        CommentStatusEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public static CommentStatusEnum getEnum(Integer code) {
            for (CommentStatusEnum value : CommentStatusEnum.values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            return null;
        }
    }
}
