package com.example.enums;

import lombok.Getter;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/13 14:41
 */
public class ChatEnums {

    //状态 0=正常 1=撤回
    @Getter
    public enum StatusEnum {
        NORMAL(0, "正常"),
        RECALL(1, "撤回");
        private Integer code;
        private String name;
        StatusEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }
        public static String getNameByCode(Integer code) {
            for (StatusEnum value : StatusEnum.values()) {
                if (value.code.equals(code)) {
                    return value.name;
                }
            }
            return null;
        }
    }

    //消息类型 1=文本 2=图片 3=语音 4=视频 5=文件 6=系统消息
    @Getter
    public enum MessageTypeEnum {
        TEXT(1, "文本"),
        IMAGE(2, "图片"),
        VOICE(3, "语音"),
        VIDEO(4, "视频"),
        FILE(5, "文件"),
        SYSTEM(6, "系统消息");
        private Integer code;
        private String name;
        MessageTypeEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }
        public static String getMessageByCode(Integer code) {
            for (MessageTypeEnum value : MessageTypeEnum.values()) {
                if (value.getCode().equals(code)) {
                    return value.getName();
                }
            }
            return null;
        }
    }


    //会话类型 1=单聊 2=群聊
    @Getter
    public enum TypeEnum {
        //1=单聊 2=群聊
        SINGLE(1, "单聊"),
        GROUP(2, "群聊"),
        ;
        private Integer code;
        private String name;

        TypeEnum(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public static String getByCode(Integer v) {
            for (ChatEnums.TypeEnum value : ChatEnums.TypeEnum.values()) {
                if (value.code.equals(v)) {
                    return value.name;
                }
            }
            return null;
        }
    }

}
