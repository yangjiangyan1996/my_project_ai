package com.example.entity.resp;

import java.math.BigDecimal;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/28 13:59
 */
@lombok.Data
public class StockSnapResp {
    private String id;
    private String name;
    private BigDecimal quantity;
}
