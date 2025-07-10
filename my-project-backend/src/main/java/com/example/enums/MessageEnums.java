package com.example.enums;

import lombok.Getter;

@Getter
public class MessageEnums {

    /**
     * 是否接受通知，0=关闭，1=开启
     */
    @Getter

    public enum MessageAcceptStatus {
        CLOSE(0, "关闭"),
        OPEN(1, "开启");

        private final Integer code;
        private final String name;

        MessageAcceptStatus(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public static MessageAcceptStatus getByCode(Integer code) {
            for (MessageAcceptStatus status : MessageAcceptStatus.values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            return null;
        }
    }

    /**
     * 消息已读未读 0=未读，1=已读
     */
    @Getter
    public enum MessageReadStatus {
        UNREAD(0, "未读"),
        READ(1, "已读");

        private final Integer code;
        private final String name;

        MessageReadStatus(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public static MessageReadStatus getByCode(Integer code) {
            for (MessageReadStatus status : MessageReadStatus.values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            return null;
        }
    }

    /**
     * 消息类型枚举
     */
    @Getter
    public enum MessageType {
        F_POST(100, "收藏项目"),
        LIKE_POST(101, "点赞项目"),
        REPLY_PROJECT(102, "回复项目"),
        JOIN_PROJECT(103, "加入项目"),

        LIKE_COMMENT(110, "点赞评论"),
        REPLY_COMMENT(111, "回复评论"),

        PUBLISHER(120, "关注用户"),

        SYSTEM_NOTICE(99, "系统通知"),
        ;

        private final Integer code;
        private final String name;

        MessageType(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public static MessageType getByCode(Integer code) {
            for (MessageType type : MessageType.values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            return null;
        }
    }
}
