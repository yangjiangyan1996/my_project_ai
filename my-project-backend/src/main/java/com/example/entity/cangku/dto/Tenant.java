package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ck_tenant")
public class Tenant extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户/企业名称
    private String name;
    //状态:0-禁用,1-启用
    private Integer status;
    //联系人
    private String contactPerson;
    //联系电话
    private String contactPhone;
    //服务到期时间
    private LocalDateTime expireAt;
}