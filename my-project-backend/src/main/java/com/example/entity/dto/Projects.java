package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/18 22:05 副业项目表
 */
@Data
@TableName("projects")
public class Projects extends BaseModel{
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer category;
    private String description;
    private Integer difficulty;
    private String imageUrl;
    private Integer status;
    private String reason;
}
