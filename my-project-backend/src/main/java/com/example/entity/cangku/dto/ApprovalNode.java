package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

//审核节点表
@Data
@TableName("ck_approval_node")
public class ApprovalNode extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //流程ID
    private Long flowId;
    //节点名称
    private String nodeName;
    //节点类型:1-审批节点,2-知会节点
    private Integer nodeType;
    //审批人类型:1-指定用户,2-指定角色,3-主管
    private Integer approverType;
    //审批人值(用户ID、角色ID等)
    private String approverValue;
    //节点顺序
    private Integer sortOrder;
}