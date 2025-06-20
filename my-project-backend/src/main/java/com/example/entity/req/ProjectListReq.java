package com.example.entity.req;

import com.example.entity.base.PageReq;
import lombok.Data;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/19 14:27
 */
@Data
public class ProjectListReq extends PageReq {
    // 项目分类
    private Integer category;
    // 项目难度
    private List<Integer> difficulty;
    // 项目名称
    private String projectName;
}
