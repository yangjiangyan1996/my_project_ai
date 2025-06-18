package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/18 22:36 AI副业推荐记录表
 */
@Data
@TableName("ai_recommend_logs")
public class AiRecommendLogs {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String inputData;
    private String recommendedProjectIds;
    private java.util.Date createdAt;
}
