package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("account_show")
public class AccountShow extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long timePerDay;
    private Long audience;
    private String skills;
    private String resources;
    private Integer status;
}