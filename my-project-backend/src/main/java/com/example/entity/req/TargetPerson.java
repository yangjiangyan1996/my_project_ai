package com.example.entity.req;

import lombok.Data;

@Data
public class TargetPerson {
    // 目标姓名
    private String name;
    // 目标性别
    private String gender;
    // 目标出生日期
    private String birthDate;
}