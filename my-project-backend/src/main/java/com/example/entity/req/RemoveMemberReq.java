package com.example.entity.req;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/3 17:46
 */
@Data
public class RemoveMemberReq {
    private Long projectId;
    private Long memberId;

    /**
     * @Link ProjectEnum.MemberStatusEnum
     */
    private Integer status;
    private String exitMessage;
}
