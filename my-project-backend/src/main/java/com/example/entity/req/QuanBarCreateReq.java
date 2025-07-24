package com.example.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/24 16:38
 */
@Data
public class QuanBarCreateReq {
    @NotBlank(message = "名称不能为空")
    private String name;
    @NotNull(message = "一级分类不能为空")
    private Integer firstCategory;
    @NotNull(message = "二级分类不能为空")
    private Integer secondCategory;
    @NotBlank(message = "标签不能为空")
    private String avatar;
    @NotBlank(message = "描述不能为空")
    private String description;
}
