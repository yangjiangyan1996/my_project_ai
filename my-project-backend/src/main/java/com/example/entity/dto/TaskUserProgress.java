package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @Author YangJian
 * @Description 用户任务状态表实体类
 * @Email 1776080295@qq.com
 * @Date 2025/6/18 22:36
 */
@Data
@TableName("task_user_progress")
public class TaskUserProgress extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id; // 主键ID
    private Long userId; // 用户ID
    private Long taskId; // 任务ID
    private Long currentValue; // 当前进度数值
    private Integer status; // 状态 0=未开始，1=进行中  2=已完成
    private Integer rewardClaimed; // 是否领取奖励  0=未领取 1=已领取
}