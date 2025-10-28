package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

//序列号管理表
@Data
@TableName("ck_serial_number")
public class SerialNumber extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //产品ID
    private Long productId;
    //序列号
    private String serialNo;
    //当前所在仓库ID
    private Long warehouseId;
    //状态:1-在库,2-出库,3-返修
    private Integer status;
    //入库单ID
    private Long inboundOrderId;
    //出库单ID
    private Long outboundOrderId;
}