package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

//17. 操作日志表
@Data
@TableName("ck_operation_log")
public class OperationLog extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //操作用户ID
    private Long userId;
    //操作模块
    private String module;
    //操作类型
    private String operation;
    //操作目标ID
    private Long targetId;
    //操作描述
    private String description;
    //IP地址
    private String ipAddress;
    //用户代理
    private String userAgent;
}