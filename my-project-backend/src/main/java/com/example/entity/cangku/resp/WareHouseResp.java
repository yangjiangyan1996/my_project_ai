package com.example.entity.cangku.resp;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/29 23:55
 */
@Data
public class WareHouseResp {
    Long id;
    //仓库编码
    private String code;
    //仓库名称
    private String name;
    //仓库地址
    private String address;
    //负责人ID
    private Long managerId;
    private String managerName;
    private String managerAvatar;
    //状态:0-停用,1-启用
    private Integer status;

    // { value: 1, label: '普通仓库' },
    //  { value: 2, label: '冷链仓库' },
    //  { value: 3, label: '危险品仓库' },
    //  { value: 4, label: '保税仓库' },
    //  { value: 5, label: '立体仓库' }
    private Integer type;
    private String remark;
    //面积(平方)
    private Double area;
    //容量
    private Long capacity;

    private Integer defaultWareHouse;
}
