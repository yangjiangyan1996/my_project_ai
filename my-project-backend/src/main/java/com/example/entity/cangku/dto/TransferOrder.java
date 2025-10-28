package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

//调拨单主表
@Data
@TableName("ck_transfer_order")
public class TransferOrder extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //调拨单号
    private String orderNo;
    //调出仓库ID
    private Long fromWarehouseId;
    //调入仓库ID
    private Long toWarehouseId;
    //状态:0-待提交,1-审核中,2-已通过,3-已完成,4-已拒绝,9-已取消
    private Integer status;
    //总数量
    private Integer totalQuantity;
    //备注
    private String remark;
}