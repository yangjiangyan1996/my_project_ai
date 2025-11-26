package com.example.entity.cangku.resp;

import lombok.Data;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/27 00:10
 */
@Data
public class BomListOfProductResp {
    private Long bomId;
    //BOM编号
    private String bomCode;
    //版本号（例如V1.0，用于版本管理）
    private String version;
    //状态：0-禁用，1-启用
    private Integer status;
    //备注说明，例如工艺或用途说明
    private String remark;

    //bom详情
    List<BomDetailListOfProductResp> list;
}
