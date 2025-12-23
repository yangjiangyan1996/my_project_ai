package com.example.entity.vo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
public class ConfirmResetVO {
    @NotBlank
    String phone;
    @Length(max = 6, min = 6)
    String code;
}
