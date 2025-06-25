package com.example.enums;

import lombok.Getter;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/23 16:33
 */
public class UserEnums {
    //是否互相关注
    @Getter
    public enum AccountShowEnum {
        No(0, "未公开"),
        Yes(1, "公开"),
        ;
        private Integer code;
        private String name;

        AccountShowEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    //是否互相关注
    @Getter
    public enum FollowEnum {
        No(0, "否"),
        Yes(1, "是"),
        ;
        private Integer code;
        private String name;

        FollowEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public static UserEnums.FollowEnum getEnum(Integer code) {
            for (UserEnums.FollowEnum value : UserEnums.FollowEnum.values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            return null;
        }
    }
}
