package com.example.entity.req;

import lombok.Data;

@Data
public class UserInput {
    /**
     * 姓
     */
    private String lastName;
    /**
     * 性别
     */
    private String gender;
    /**
     * 年月日
     */
    private String birthDay;
    /**
     * 时
     */
    private int birthHour;
    /**
     * 风格
     */
    private String style;
    /**
     * 偏好字符
     */
    private String preferredChars;
    /**
     * 其他信息
     */
    private String additionalInfo;
}