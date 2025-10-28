package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

//审核历史表
@Data
@TableName("ck_approval_history")
public class ApprovalHistory extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //实例ID
    private Long instanceId;
    //节点ID
    private Long nodeId;
    //节点名称
    private String nodeName;
    //审批人ID
    private Long approverId;
    //审批人姓名
    private String approverName;
    //操作:1-通过,2-拒绝,3-转交,4-撤回
    private Integer action;
    //审批意见
    private String opinion;
}