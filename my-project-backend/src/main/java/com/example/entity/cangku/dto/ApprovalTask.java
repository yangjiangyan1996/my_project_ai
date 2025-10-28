package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

import java.time.LocalDateTime;

//审核任务表
@Data
@TableName("ck_approval_task")
public class ApprovalTask extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //实例ID
    private Long instanceId;
    //节点ID
    private Long nodeId;
    //审批人ID
    private Long approverId;
    //状态:0-待审批,1-已通过,2-已拒绝,3-已转交
    private Integer status;
    //审批意见
    private String approveOpinion;
    //处理期限
    private LocalDateTime deadline;
}