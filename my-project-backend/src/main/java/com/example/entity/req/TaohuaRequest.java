package com.example.entity.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class TaohuaRequest {
    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotBlank(message = "性别不能为空")
    @Pattern(regexp = "^(male|female)$", message = "性别必须是 male 或 female")
    private String gender;

    @NotBlank(message = "出生日期不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$",
            message = "日期格式必须为 ISO 8601（如 2025-06-03T00:00:00.000Z）")
    private String birthDate;

    @NotNull(message = "出生时辰不能为空")
    @Min(value = 0, message = "时辰最小值为0")
    @Max(value = 23, message = "时辰最大值为23")
    private Integer birthHour;

    @NotBlank(message = "关系类型不能为空")
    private String relationship;

    private String additionalInfo; // 可选字段，不加校验

    @Valid // 启用嵌套对象校验
    @NotEmpty(message = "目标对象列表不能为空")
    private List<@Valid TargetPerson> targets; // 嵌套校验
}