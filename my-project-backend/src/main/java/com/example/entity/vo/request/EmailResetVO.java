package com.example.entity.vo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 密码重置表单实体
 */
@Data
public class EmailResetVO {
    @NotBlank
    String phone;
    @Length(max = 6, min = 6)
    String code;
    @Length(min = 6, max = 20)
    String password;
}
