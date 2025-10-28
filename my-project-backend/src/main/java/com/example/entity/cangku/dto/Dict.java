package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

@Data
@TableName("ck_dict")
public class Dict extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //字典类型
    private String dictType;
    //字典编码
    private String dictCode;
    //字典值
    private String dictValue;
    //排序
    private Integer sortOrder;
    //状态:0-禁用,1-启用
    private Integer status;
    //备注
    private String remark;
}