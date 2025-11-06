package com.example.entity.cangku.req;

import lombok.Data;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/6 00:54
 */
@Data
public class ProductBomReq {
    String bomCode;
    String remark;
    Integer status;
    String version;
    List<ProductBomDetailReq> details;
}
