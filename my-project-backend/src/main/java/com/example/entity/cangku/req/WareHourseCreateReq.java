package com.example.entity.cangku.req;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/29 23:21
 */
@Data
public class WareHourseCreateReq {
    private Long id;
    private String address;
    private double area;
    private Long capacity;
    private String code;
    private String contactPhone;
    private Long managerId;
    private String name;
    private String remark;
    private Integer status;
    private Integer type;

    private Long userId;
    private Long tenantId;
}
