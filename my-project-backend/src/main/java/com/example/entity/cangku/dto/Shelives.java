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
 * @Date 2026/1/2 22:15
 */
//ck_shelves
@Data
@TableName("ck_shelves")
public class Shelives extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //仓库ID
    private Long warehouseId;
    //货架编码
    private String shelfCode;
    //货架名称
    private String shelfName;
    //货架类型（0:普通货架,1:自动化货架等）
    private Integer shelfType;
    //状态：0-禁用，1-启用
    private Integer status;
    //排序
    private Integer sortOrder;
    //备注
    private String remark;
}
