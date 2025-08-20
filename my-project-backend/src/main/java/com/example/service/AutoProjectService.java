package com.example.service;

import com.example.entity.dto.Projects;
import com.example.entity.dto.ProjectsDetail;
import com.example.enums.CommonEnum;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/19 13:53
 */
public interface AutoProjectService {

    Long autoPublishProject();

    Projects generateRandomProject(CommonEnum.IndustryCategory parent, CommonEnum.IndustryCategory.IndustrySubCategory sub);

    ProjectsDetail generateRandomProjectDetail(Long projectId, String projectName);

}
