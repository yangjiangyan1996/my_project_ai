package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/30 21:56
 */
@Data
@TableName("ck_unit")
public class Unit extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String unitCode;
    private String unitName;
    private String remark;
    private String status;
}
