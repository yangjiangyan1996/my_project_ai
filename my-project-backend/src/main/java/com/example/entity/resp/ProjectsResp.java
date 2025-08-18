package com.example.entity.resp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/18 22:05 副业项目表
 */
@Data
public class ProjectsResp {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    String firstCategoryName ;
    String secondCategoryName;
    private String description;
    private Integer difficulty;
    private String imageUrl;
}
