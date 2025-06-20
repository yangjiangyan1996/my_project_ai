package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.base.RespBean;
import com.example.entity.dto.Projects;
import com.example.entity.req.ProjectListReq;
import com.example.entity.resp.ProjectsResp;
import com.example.enums.ProjectEnum;
import com.example.service.ProjectService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth/project/")
public class ProjectController {


    @Resource
    private ProjectService projectService;

    @PostMapping("/show")
    public RespBean<Page<ProjectsResp>> showHotFuye(@RequestBody ProjectListReq req) {
        Page<Projects> hotFuyeProjects = projectService.getHotFuyeProjects(Page.of(req.getPage() - 1, req.getSize()),req);

        List<ProjectsResp> collect = hotFuyeProjects.getRecords().stream().map(v -> {
            ProjectsResp projectsResp = new ProjectsResp();
            BeanUtils.copyProperties(v, projectsResp);
            ProjectEnum.ProjectCategoryEnum difficultyEnum = ProjectEnum.ProjectCategoryEnum.getEnum(v.getCategory());
            projectsResp.setCategoryName(difficultyEnum == null ? "未定义" : difficultyEnum.getName());
            return projectsResp;
        }).collect(Collectors.toList());

        Page<ProjectsResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(hotFuyeProjects.getTotal());
        result.setRecords(collect);
        return RespBean.success(result);
    }
}