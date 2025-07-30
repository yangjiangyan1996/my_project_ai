package com.example.entity.req;

import com.example.entity.base.PageReq;
import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/28 21:48
 */
@Data
public class BarRelationPageReq extends PageReq {
    Long barId;
    Long userId;
}
