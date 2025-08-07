package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/7 19:27
 */
@Data
@TableName("task_badge")
public class TaskBadge extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private String name;
    private String description;
    private String iconUrl;
    private String colorCode;
    private Integer sortOrder;
}
