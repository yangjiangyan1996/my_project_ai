package com.example.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TaohuaRequest {
    // 姓名（可选）
    private String name;

    // 性别（必填）
    @NotBlank(message = "性别不能为空")
    private String gender;

    // 出生日期（必填）
    @NotNull(message = "出生日期不能为空")
    private LocalDate birthDate;

    // 出生时辰（0-23）
    @NotNull(message = "出生时辰不能为空")
    private Integer birthHour;

    // 感情状态（必填）
    @NotBlank(message = "感情状态不能为空")
    private String relationship;

    // 附加说明（可选）
    private String additionalInfo;
}