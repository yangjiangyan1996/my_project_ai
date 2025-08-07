package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/7 20:05
 */
@Data
@TableName("task_user_badge")
public class TaskUserBadge extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long badgeId;
    private Date achievedAt;
}
