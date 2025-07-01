package com.example.entity.resp;

import lombok.Data;

import java.util.Date;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/27 16:14
 */
@Data
public class AdminApplyListResp {
    private Long id;
    private String projectName;
    private Integer status;
    private Date applyTime;
}
