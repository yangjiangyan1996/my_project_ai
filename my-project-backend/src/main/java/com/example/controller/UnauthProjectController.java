package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.CommonFacade;
import com.example.Facade.ProjectFacade;
import com.example.config.AsyncTaskUtil;
import com.example.config.QqMailService;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.dto.Projects;
import com.example.entity.req.MatchUserReq;
import com.example.entity.req.ProjectListReq;
import com.example.entity.req.ProjectShowListReq;
import com.example.entity.req.ShowHotProjectListPageReq;
import com.example.entity.resp.*;
import com.example.enums.CommonEnum;
import com.example.filter.UserUtil;
import com.example.service.ProjectService;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/18 17:38
 */
@RestController
@Slf4j
@RequestMapping("/api/unauth/project/")
public class UnauthProjectController {

    @Resource
    CommonFacade commonFacade;
    @Resource
    private QqMailService qqMailService;
    @Resource
    ProjectFacade projectFacade;
    @Resource
    private ProjectService projectService;

    @PostMapping("/projectShowList")
    public RespBean<Page<ProjectOfMyShowGetResp>> projectShowList(@RequestBody ProjectShowListReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Page<ProjectOfMyShowGetResp> result = projectFacade.projectShowList(Page.of(req.getPage() - 1, req.getSize()), req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectController#applyJoinProject,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectController#changeShowStatus,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    /**
     * 匹配用户
     * @param req
     * @return
     */
    @PostMapping("/matchUser")
    public RespBean<Page<MatchUserResp>> matchUser(@RequestBody MatchUserReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Page<MatchUserResp> result = projectFacade.matchUser(Page.of(req.getPage(), req.getSize()), req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectController#applyJoinProject,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectController#changeShowStatus,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    @GetMapping("/commentShow")
    public RespBean<List<ProjectCommentResp>> commentShow(@RequestParam("projectId") Long projectId) {
        try {
            Long userId = null;
            UserInfo user = UserUtil.getCurrentUser();
            if(user != null) {
                userId = user.getId();
            }
            List<ProjectCommentResp> result = projectFacade.commentShow(projectId, userId);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectController#commentShow,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectController#commentShow,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    @GetMapping("/detail")
    public RespBean<ProjectsDetailResp> detail(@RequestParam("projectId") Long projectId) {
        try {
            Long userId = null;
            try{
                UserInfo user = UserUtil.getCurrentUser();
                if (user.getRole().equals("ADMIN")) {
                    userId = null;
                } else {
                    userId = user.getId();
                }
            }catch (Exception e) {
                userId = null;
                log.error("ProjectController#detail,error,projectId:{}", projectId, e);
            }

            ProjectsDetailResp result = projectFacade.selectByProjectId(projectId, userId);

            Long finalUserId = userId;
            AsyncTaskUtil.execute(() -> {
                projectFacade.addProjectWatch(projectId, finalUserId);
            });
            return RespBean.success(result);
        } catch (Exception e) {
            log.error("ProjectController#detail,error,projectId:{}", projectId, e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }



    @PostMapping("/showHotProjectList")
    public RespBean<Page<ShowHotProjectListPageResp>> showHotProjectList(@RequestBody ShowHotProjectListPageReq req) {

        try {
            Page<ShowHotProjectListPageResp> list = projectFacade.showHotProjectList(req, null);
            return RespBean.success(list);
        } catch (ValidationException e) {
            log.error("ProjectController#showHotProjectList,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectController#showHotProjectList,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/show")
    public RespBean<Page<ProjectsResp>> showHotFuye(@RequestBody ProjectListReq req) {

        try {
            List<Integer> firstLevel = new ArrayList<>();
            List<Integer> secondLevel = new ArrayList<>();
            List<Integer> categories = req.getCategories();
            if (!CollectionUtils.isEmpty(categories)) {
                for (Integer c : categories) {
                    int i = CommonEnum.IndustryCategory.checkCodeLevel(c);
                    if (i == 1) {
                        firstLevel.add(c);
                    } else if (i == 2) {
                        secondLevel.add(c);
                    }
                }
            }

            Page<Projects> hotFuyeProjects = projectService.getHotFuyeProjects(Page.of(req.getPage() - 1, req.getSize()), req,firstLevel,secondLevel);

            List<ProjectsResp> collect = hotFuyeProjects.getRecords().stream().map(v -> {
                ProjectsResp projectsResp = new ProjectsResp();
                BeanUtils.copyProperties(v, projectsResp);
                String firstCategoryName = CommonEnum.IndustryCategory.getNameByCode(v.getFirstCategory());
                String secondCategoryName = CommonEnum.IndustryCategory.getNameByCode(v.getSecondCategory());
                projectsResp.setFirstCategoryName(firstCategoryName);
                projectsResp.setSecondCategoryName(secondCategoryName);
                return projectsResp;
            }).collect(Collectors.toList());

            Page<ProjectsResp> result = Page.of(req.getPage() - 1, req.getSize());
            result.setTotal(hotFuyeProjects.getTotal());
            result.setRecords(collect);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectController#showHotFuye,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectController#showHotFuye,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }
}
