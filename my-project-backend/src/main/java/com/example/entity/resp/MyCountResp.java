package com.example.entity.resp;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/26 15:49
 */
@Data
public class MyCountResp {
    //审核数量
    private Integer applyCount;

    //申请数量
    private Integer applicationCount;
}
