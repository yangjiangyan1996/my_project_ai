package com.example.entity.cangku.dto;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;


//审核实例表
@Data
@TableName("ck_approval_instance")
public class ApprovalInstance extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //流程ID
    private Long flowId;
    //业务类型
    private Integer bizType;
    //业务单据ID
    private Long bizId;
    //业务单号
    private String bizNo;
    //申请人ID
    private Long applicantId;
    //当前节点ID
    private Long currentNodeId;
    //状态:0-审批中,1-已通过,2-已拒绝,3-已撤回,9-已取消
    private Integer status;
}