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
    //操作描述
    private String description;
    //IP地址
    private String ipAddress;
    //用户代理
    private String userAgent;

    private String requestParams;

    private Integer status;           // 0-失败，1-成功，2-客户端错误，3-其他
    private String statusMessage;     // 状态描述
    private String requestUrl;        // 请求URL
    private String requestMethod;     // 请求方法
    private Long responseTime;        // 响应时间(毫秒)
    private String responseData;      // 响应数据
}