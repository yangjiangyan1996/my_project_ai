package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @Author YangJian
 * @Description 任务定义表实体类
 * @Email 1776080295@qq.com
 * @Date 2025/6/18 22:36
 */
@Data
@TableName("task_user_point")
public class TaskUserPoint extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer points;
}