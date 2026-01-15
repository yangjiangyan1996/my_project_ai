package com.example.entity.cangku.resp;

import lombok.Data;

import java.util.Date;

@Data
public class AdjustExecuteResult {
    private Long orderId;
    private String orderNo;
    private Integer status;
    private Date executeTime;
    private Integer successCount;
    private Integer totalCount;
    private String statusDesc;
}