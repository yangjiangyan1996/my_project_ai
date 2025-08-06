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

        public static AchievementEnums.TaskStatus getByCode(Integer code) {
            for (AchievementEnums.TaskStatus status : AchievementEnums.TaskStatus.values()) {
                if (status.code.equals(code)) {
                    return status;
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
        ACHIEVEMENT(4, "成就");
        private final Integer code;
        private final String name;

        TaskType(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public static AchievementEnums.TaskType getByCode(Integer code) {
            for (AchievementEnums.TaskType taskType : AchievementEnums.TaskType.values()) {
                if (taskType.code.equals(code)) {
                    return taskType;
                }
            }
            return null;
        }
    }

    /**
     * 分类标识，如login/like/comment等
     */
    @Getter
    public enum Category {
        LOGIN("login", "登录"),
        LIKE("like", "点赞"),
        COMMENT("comment", "评论"),
        JOIN("join", "加入"),
        APPROVE("approve", "审批"),
        CREATE("create", "创建");
        private final String code;
        private final String name;

        Category(String code, String name) {
            this.code = code;
            this.name = name;
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

        public static AchievementEnums.RewardType getByCode(Integer code) {
            for (AchievementEnums.RewardType rewardType : AchievementEnums.RewardType.values()) {
                if (rewardType.code.equals(code)) {
                    return rewardType;
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
        public static AchievementEnums.ProgressStatus getByCode(Integer code) {
            for (AchievementEnums.ProgressStatus progressStatus : AchievementEnums.ProgressStatus.values()) {
                if (progressStatus.code.equals(code)) {
                    return progressStatus;
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
        public static AchievementEnums.RewardStatus getByCode(Integer code) {
            for (AchievementEnums.RewardStatus rewardStatus : AchievementEnums.RewardStatus.values()) {
                if (rewardStatus.code.equals(code)) {
                    return rewardStatus;
                }
            }
            return null;
        }
    }
}
