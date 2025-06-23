package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.MyFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.dto.Projects;
import com.example.entity.req.ConcernPublisherReq;
import com.example.entity.req.MyPublishedPageReq;
import com.example.entity.resp.MyPublishedResp;
import com.example.entity.resp.ProjectsResp;
import com.example.enums.ProjectEnum;
import com.example.filter.UserUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/23 23:47
 */
@RestController
@RequestMapping("/api/auth/my/")
@Slf4j
public class MyController {
    @Resource
    private MyFacade myFacade;

    @PostMapping("/myLike")
    public RespBean<Page<MyPublishedResp>> myLike(@RequestBody MyPublishedPageReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Page<Projects> hotFuyeProjects = myFacade.myLike(req, user.getId());
            List<MyPublishedResp> collect = hotFuyeProjects.getRecords().stream().map(v -> {
                MyPublishedResp projectsResp = new MyPublishedResp();
                BeanUtils.copyProperties(v, projectsResp);
                ProjectEnum.ProjectCategoryEnum difficultyEnum = ProjectEnum.ProjectCategoryEnum.getEnum(v.getCategory());
                projectsResp.setCategoryName(difficultyEnum == null ? "未定义" : difficultyEnum.getName());
                return projectsResp;
            }).collect(Collectors.toList());

            Page<MyPublishedResp> result = Page.of(req.getPage() - 1, req.getSize());
            result.setTotal(hotFuyeProjects.getTotal());
            result.setRecords(collect);
            return RespBean.success(result);
        } catch (Exception e) {
            return RespBean.failure(999, e.getMessage());
        }
    }


    @PostMapping("/myFavorites")
    public RespBean<Page<MyPublishedResp>> myFavorites(@RequestBody MyPublishedPageReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Page<Projects> hotFuyeProjects = myFacade.myFavorites(req, user.getId());
            List<MyPublishedResp> collect = hotFuyeProjects.getRecords().stream().map(v -> {
                MyPublishedResp projectsResp = new MyPublishedResp();
                BeanUtils.copyProperties(v, projectsResp);
                ProjectEnum.ProjectCategoryEnum difficultyEnum = ProjectEnum.ProjectCategoryEnum.getEnum(v.getCategory());
                projectsResp.setCategoryName(difficultyEnum == null ? "未定义" : difficultyEnum.getName());
                return projectsResp;
            }).collect(Collectors.toList());

            Page<MyPublishedResp> result = Page.of(req.getPage() - 1, req.getSize());
            result.setTotal(hotFuyeProjects.getTotal());
            result.setRecords(collect);
            return RespBean.success(result);
        } catch (Exception e) {
            return RespBean.failure(999, e.getMessage());
        }
    }

    @PostMapping("/myPublished")
    public RespBean<Page<MyPublishedResp>> myPublished(@RequestBody MyPublishedPageReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Page<MyPublishedResp> list = myFacade.myPublished(req, user.getId());
            return RespBean.success(list);
        } catch (Exception e) {
            log.error("Mycontroller#myPublished, error",e);
            return RespBean.failure(999, e.getMessage());
        }
    }
}
