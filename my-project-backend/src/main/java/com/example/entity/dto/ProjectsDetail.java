package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Author YangJian
 * @Description 副业项目详情实体
 * @Email 1776080295@qq.com
 * @Date 2025/6/18 22:55
 */
@Data
@TableName("projects_detail")
public class ProjectsDetail extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectsId;
    private String steps;
    private String tools;
    private Long timePerDay;
    private Long incomeEstimateMin;
    private Long incomeEstimateMax;
    private Long targetAudience;
    private String riskWarning;
    private String tags;
    private Integer needMember;
    private Integer memberNum;
}