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
     * 是否招纳成员，0=不需要，1=需要
     */
    @Getter
    public enum IsNeedMemberEnum {
        NO_NEED_MEMBER(0, "不需要"),
        NEED_MEMBER(1, "需要");
        private Integer code;
        private String name;

        IsNeedMemberEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    /**
     * 项目参与申请状态
     * 状态: 0-待审核 1-已通过 2-已拒绝，3=已撤销
     */
    @Getter
    public enum ProjectApplyStatusEnum {
        WAIT_AUDIT(0, "待审核"),
        APPROVED(1, "已通过"),
        REJECTED(2, "已拒绝"),
        CANCELED(3, "已撤销");
        private final Integer code;
        private final String name;

        ProjectApplyStatusEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    /**
     * 项目成员角色
     * 角色: 0-普通成员 1-管理员，2 = 发起人
     */
    @Getter
    public enum ProjectMemberRoleEnum {

        NORMAL(0, "普通成员"),
        GROUP(1, "组长"),
        ADMIN(2, "管理员");
        private final Integer code;
        private final String name;

        ProjectMemberRoleEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }
        public static ProjectMemberRoleEnum getByCode(Integer code) {
            for (ProjectMemberRoleEnum value : values()) {
                if (value.code.equals(code)) {
                    return value;
                }
            }
            return null;
        }
    }

    /**
     * 难度等级
     */
    @Getter
    public enum ProjectStatusEnum {
        WAITING(0, "待审核"),
        PUBLISHING(1, "发布"),
        NO(2, "退回"),
        ;
        private Integer code;
        private String name;

        ProjectStatusEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    /**
     * 难度等级
     */
    @Getter
    public enum ProjectDifficultyEnum {
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
    public enum ProjectCategoryEnum {
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


    @Getter
    public enum ProjectCommentStatusEnum {
        ok(0, "正常"),
        hide(1, "隐藏"),
        ;
        private Integer code;
        private String name;

        ProjectCommentStatusEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public static ProjectEnum.ProjectCommentStatusEnum getEnum(Integer code) {
            for (ProjectEnum.ProjectCommentStatusEnum value : ProjectEnum.ProjectCommentStatusEnum.values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            return null;
        }
    }


}
