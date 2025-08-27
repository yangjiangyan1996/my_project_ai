package com.example.service;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/19 13:53
 */
public interface AutoProjectService {

    //自动发布项目
    Long autoPublishProject();

    //自动在圈子中发布内容
    void autoQuanTieProject();
}
