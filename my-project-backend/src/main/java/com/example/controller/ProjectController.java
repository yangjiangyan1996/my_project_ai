package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.ProjectFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.dto.Projects;
import com.example.entity.req.ProjectListReq;
import com.example.entity.resp.ProjectsDetailResp;
import com.example.entity.resp.ProjectsResp;
import com.example.enums.ProjectEnum;
import com.example.filter.UserUtil;
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
    ProjectFacade projectFacade;
    @Resource
    private ProjectService projectService;


    @GetMapping("/likeProject")
    public RespBean<Boolean> likeProject(@RequestParam("projectId") Long projectId,
                                         @RequestParam("liked") Boolean liked) {
        UserInfo user = UserUtil.getCurrentUser();
        Boolean result = projectFacade.likeProject(projectId, user.getId(), liked);
        return RespBean.success(result);
    }

    @GetMapping("/detail")
    public RespBean<ProjectsDetailResp> showHotFuye(@RequestParam("projectId") Long projectId) {
        UserInfo user = UserUtil.getCurrentUser();
        ProjectsDetailResp result = projectFacade.selectByProjectId(projectId);
        return RespBean.success(result);
    }

    @PostMapping("/show")
    public RespBean<Page<ProjectsResp>> showHotFuye(@RequestBody ProjectListReq req) {
        Page<Projects> hotFuyeProjects = projectService.getHotFuyeProjects(Page.of(req.getPage() - 1, req.getSize()), req);

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