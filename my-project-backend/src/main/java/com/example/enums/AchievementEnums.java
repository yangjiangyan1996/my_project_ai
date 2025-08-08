package com.example.enums;

import lombok.Getter;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/6 15:24
 */
public class AchievementEnums {
    /**
     * 状态 0=禁用 1=启用
     */
    @Getter
    public enum TaskStatus {
        DISABLED(0, "禁用"),
        ENABLED(1, "启用");
        private final Integer code;
        private final String name;

        TaskStatus(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public static String getByCode(Integer code) {
            for (AchievementEnums.TaskStatus status : AchievementEnums.TaskStatus.values()) {
                if (status.code.equals(code)) {
                    return status.getName();
                }
            }
            return null;
        }
    }

    /**
     * 任务类型 1=日常 2=每周 3=一次性 4=成就
     */
    @Getter
    public enum TaskType {
        DAILY(1, "日常"),
        WEEKLY(2, "每周"),
        ONETIME(3, "一次性"),
        ;
        private final Integer code;
        private final String name;

        TaskType(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public static String getByCode(Integer code) {
            for (AchievementEnums.TaskType taskType : AchievementEnums.TaskType.values()) {
                if (taskType.code.equals(code)) {
                    return taskType.getName();
                }
            }
            return null;
        }
    }

    /**
     * 分类标识，
     * daily_login 每日签到
     * content_like 每日点赞
     * post_comment 每日发布有意义的评论
     * first_blood 完成账号信息
     * content_creator 发布优质内容
     * legendary_support 获得他人点赞
     * exp_skill_tech 发布内容获得经验
     * exp_featured 内容被评为精华
     * exp_team 创建团队获得经验
     * combo_daily 连续完成所有日常任务
     * achievement_set 集齐创作类勋章
     *
     * icon地址 https://element-plus.org/zh-CN/component/icon.html
     */
    @Getter
    public enum Category {
        DAILY_LOGIN("daily_login", "每日签到", "#409EFF", "Calendar"),
        CONTENT_LIKE("content_like", "每日点赞", "#E6A23C", "Star"),
        POST_COMMENT("post_comment", "每日发布有意义的评论", "#F56C6C", "ChatDotRound"),
        FIRST_BLOOD("first_blood", "完成账号信息", "#909399", "UserFilled"),//
        CONTENT_CREATOR("content_creator", "发布优质内容", "#67C23A", "Edit"),
        LEGENDARY_SUPPORT("legendary_support", "获得他人点赞", "#8E44AD", "ChromeFilled"),//
        EXP_SKILL_TECH("exp_skill_tech", "发布内容获得经验", "#3498DB", "Notebook"),//
        EXP_FEATURED("exp_featured", "内容被评为精华", "#E74C3C", "QuestionFilled"),//
        EXP_TEAM("exp_team", "创建团队获得经验", "#16A085", "User"),//
        COMBO_DAILY("combo_daily", "连续完成所有日常任务", "#3498DB", "Clock"),
        ACHIEVEMENT_SET("achievement_set", "集齐创作类勋章", "#E74C3C", "Trophy"),
        First_FEATURED("firt_featured", "首次发布内容", "#E74C3C", "Edit"),
        ;
        private final String code;
        private final String name;
        private final String color;
        private final String icon;
        

        Category(String code, String name, String color, String icon) {
            this.code = code;
            this.name = name;
            this.color = color;
            this.icon = icon;
        }

        public static AchievementEnums.Category getByCode(String code) {
            for (AchievementEnums.Category category : AchievementEnums.Category.values()) {
                if (category.code.equals(code)) {
                    return category;
                }
            }
            return null;
        }
    }


    /**
     * 奖励类型 1=积分 2=勋章 3=经验值
     */
    @Getter
    public enum RewardType {
        POINTS(1, "积分"),
        MEDAL(2, "勋章"),
        EXPERIENCE(3, "经验值");
        private final Integer code;
        private final String name;

        RewardType(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public static String getByCode(Integer code) {
            for (AchievementEnums.RewardType rewardType : AchievementEnums.RewardType.values()) {
                if (rewardType.code.equals(code)) {
                    return rewardType.getName();
                }
            }
            return null;
        }
    }

    /**
     * 状态 0=未开始 1=进行中 2=已完成
     */
    @Getter
    public enum ProgressStatus {
        NOT_STARTED(0, "未开始"),
        IN_PROGRESS(1, "进行中"),
        COMPLETED(2, "已完成");
        private final Integer code;
        private final String name;
        ProgressStatus(Integer code, String name) {
            this.code = code;
            this.name = name;
        }
        public static String getByCode(Integer code) {
            for (AchievementEnums.ProgressStatus progressStatus : AchievementEnums.ProgressStatus.values()) {
                if (progressStatus.code.equals(code)) {
                    return progressStatus.getName();
                }
            }
            return null;
        }
    }

    /**
     *  是否领取奖励 0=未领取 1=已领取
     */
    @Getter
    public enum RewardStatus {
        NOT_CLAIMED(0, "未领取"),
        CLAIMED(1, "已领取");
        private final Integer code;
        private final String name;
        RewardStatus(Integer code, String name) {
            this.code = code;
            this.name = name;
        }
        public static String getByCode(Integer code) {
            for (AchievementEnums.RewardStatus rewardStatus : AchievementEnums.RewardStatus.values()) {
                if (rewardStatus.code.equals(code)) {
                    return rewardStatus.getName();
                }
            }
            return null;
        }
    }
}
