package com.example.entity.req;

import lombok.Data;

import java.util.List;

@Data
public class TaohuaRequest {
    // 姓名
    private String name;
    // 性别 (female/male)
    private String gender;
    // 出生日期 (ISO 8601格式)
    private String birthDate;
    // 出生时辰 (0-23)
    private Integer birthHour;
    // 关系类型
    private String relationship;
    // 附加信息
    private String additionalInfo;
    // 目标姓名（可选）
    private String targetName;
    // 目标性别（可选）
    private String targetGender;
    // 目标出生日期（可选）
    private String targetBirthDate;
    // 目标人员列表
    private List<TargetPerson> targets;
}