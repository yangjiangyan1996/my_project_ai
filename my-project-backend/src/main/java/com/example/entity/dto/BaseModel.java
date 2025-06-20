package com.example.entity.dto;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/19 09:44
 */
@Data
public class BaseModel {
    private Long createdBy;
    private java.util.Date createdAt;
    private Long modifiedBy;
    private java.util.Date modifiedAt;
    private Integer isDeleted;
}
