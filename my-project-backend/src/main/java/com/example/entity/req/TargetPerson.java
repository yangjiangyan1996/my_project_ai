package com.example.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TargetPerson {
    @NotBlank(message = "目标姓名不能为空")
    private String targetName;

    @NotBlank(message = "目标性别不能为空")
    @Pattern(regexp = "^(male|female)$", message = "性别必须是 male 或 female")
    private String targetGender;

    @NotBlank(message = "目标出生日期不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$",
            message = "日期格式必须为 yyyy-MM-dd（如 2025-06-16）")
    private String targetBirthDate;

    private String targetHobbies; // 可选字段
}