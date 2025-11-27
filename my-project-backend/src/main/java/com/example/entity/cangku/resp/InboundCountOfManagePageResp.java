package com.example.entity.cangku.resp;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/2 01:13
 */
@Data
public class InboundCountOfManagePageResp {
    //总单数
    private Integer totalCount = 0;
    //待审核单数
    private Integer waitApproveCount = 0;
    //审核通过单数
    private Integer approvePassCount = 0;
    //审核拒绝单数
    private Integer approveRejectCount = 0;
}
