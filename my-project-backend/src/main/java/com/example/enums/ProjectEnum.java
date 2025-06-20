package com.example.enums;

import lombok.Getter;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/19 20:41
 */
public class ProjectEnum {
    /**
     * 难度等级
     */
    @Getter
    public enum ProjectDifficultyEnum{
        ONE(1, "★"),
        TWO(2, "★★"),
        THREE(3, "★★★"),
        FOUR(4, "★★★★"),
        FIVE(5, "★★★★★"),
        ;
        private Integer code;
        private String name;
        ProjectDifficultyEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    /**
     * 副业分类，如
     */
    @Getter
    public enum ProjectCategoryEnum{
        ONE(1, "电商"),
        TWO(2, "AI"),
        THREE(3, "新媒体"),
        ;
        private Integer code;
        private String name;
        ProjectCategoryEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }
        public static ProjectCategoryEnum getEnum(Integer code) {
            for (ProjectCategoryEnum value : ProjectCategoryEnum.values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            return null;
        }
    }


}
