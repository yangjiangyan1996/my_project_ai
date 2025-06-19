package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/18 22:19 副业榜单数据表
 */
@Data
@TableName("project_rankings")
public class ProjectRankings extends BaseModel{
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String rankType;
    private Integer rankValue;
}
