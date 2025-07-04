package com.example.controller;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.ProjectFacade;
import com.example.config.QqMailService;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.dto.Projects;
import com.example.entity.req.*;
import com.example.entity.resp.*;
import com.example.enums.ProjectEnum;
import com.example.filter.UserUtil;
import com.example.service.ProjectService;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/api/auth/project/")
public class ProjectController {

    @Resource
    private QqMailService qqMailService;
    @Resource
    ProjectFacade projectFacade;
    @Resource
    private ProjectService projectService;



    @GetMapping("/getMyCount")
    public RespBean<MyCountResp> getMyCount() {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            MyCountResp result = projectFacade.getMyCount(user.getId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectController#getMyCount,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectController#getMyCount,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    @PostMapping("/applyJoinProject")
    public RespBean<Boolean> applyJoinProject(@RequestBody ApplyJoinProjectReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Boolean result = projectFacade.applyJoinProject(req.getProjectId(), req.getMessage(), user.getId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectController#applyJoinProject,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectController#applyJoinProject,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @GetMapping("/cancelApplyJoinProject")
    public RespBean<Boolean> cancelApplyJoinProject(@RequestParam("projectId") Long projectId) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Boolean result = projectFacade.cancelApplyJoinProject(projectId, user.getId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectController#cancelApplyJoinProject,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectController#cancelApplyJoinProject,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @GetMapping("/changeShowStatus")
    public RespBean<Boolean> changeShowStatus(@RequestParam("projectShowId") Long projectShowId,
                                              @RequestParam("status") Integer status) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Boolean result = projectFacade.changeShowStatus(projectShowId, status, user.getId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectController#applyJoinProject,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectController#changeShowStatus,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/adminApproveList")
    public RespBean<Page<AdminApplyListResp>> adminApproveList(@RequestBody MyApplyListReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            if (!user.getRole().equals("ADMIN")) {
                return RespBean.failure(999, "无权限");
            }
            Page<AdminApplyListResp> result = projectFacade.adminApproveList(Page.of(req.getPage() - 1, req.getSize()), req, user.getId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectController#adminApproveList,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectController#adminApproveList,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/adminApprovePass")
    public RespBean<Boolean> adminApprovePass(@RequestBody AdminApproveReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            if (!user.getRole().equals("ADMIN")) {
                return RespBean.failure(999, "无权限");
            }
            Boolean result = projectFacade.adminApprovePass(req.getProjectId(), user.getId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectController#adminApprovePass,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectController#adminApprovePass,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/adminApproveNo")
    public RespBean<Boolean> adminApproveNo(@RequestBody AdminApproveReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            if (!user.getRole().equals("ADMIN")) {
                return RespBean.failure(999, "无权限");
            }
            Boolean result = projectFacade.adminApproveNo(req.getProjectId(), req.getReason(), user.getId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectController#adminApproveNo,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectController#adminApproveNo,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/myApplicationList")
    public RespBean<Page<MyApplicationListResp>> myApplicationList(@RequestBody MyApplyListReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Page<MyApplicationListResp> result = projectFacade.myApplicationList(Page.of(req.getPage() - 1, req.getSize()), req, user.getId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectController#applyJoinProject,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectController#changeShowStatus,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/myApplyList")
    public RespBean<Page<MyApplyListResp>> myApplyList(@RequestBody MyApplyListReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Page<MyApplyListResp> result = projectFacade.myApplyList(Page.of(req.getPage() - 1, req.getSize()), req, user.getId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectController#applyJoinProject,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectController#changeShowStatus,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/approveApply")
    public RespBean<Boolean> approveApply(@RequestBody ApproveApplyReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Boolean result = projectFacade.approveApply(req, user.getId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectController#approveApply,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectController#approveApply,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/cancelApply")
    public RespBean<Boolean> cancelApply(@RequestBody CancelApproveReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Boolean result = projectFacade.cancelApply(req, user.getId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectController#cancelApply,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectController#cancelApply,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/rejectApply")
    public RespBean<Boolean> rejectApply(@RequestBody ApproveApplyReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Boolean result = projectFacade.rejectApply(req, user.getId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectController#rejectApply,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectController#rejectApply,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

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

    @GetMapping("/getProjectOfMyShow")
    public RespBean<ProjectOfMyShowGetResp> getProjectOfMyShow() {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            ProjectOfMyShowGetResp result = projectFacade.getProjectOfMyShow(user.getId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectController#applyJoinProject,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectController#getProjectOfMyShow,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/updateProjectOfMyShow")
    public RespBean<Boolean> updateProjectOfMyShow(@RequestBody ProjectOfMyShowUpdateReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Boolean result = projectFacade.updateProjectOfMyShow(req, user.getId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectController#applyJoinProject,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectController#updateProjectOfMyShow,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/createFindCollage")
    public RespBean<Boolean> createFindCollage(@RequestBody CreateFindCollageReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Boolean result = projectFacade.createFindCollage(req, user.getId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectController#applyJoinProject,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectController#createFindCollage,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    @PostMapping("/concernPublisher")
    public RespBean<Boolean> concernPublisher(@RequestBody ConcernPublisherReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Boolean result = projectFacade.concernPublisher(req, user.getId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectController#applyJoinProject,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/concernPublisherCancel")
    public RespBean<Boolean> concernPublisherCancel(@RequestBody ConcernPublisherCancelReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Boolean result = projectFacade.concernPublisherCancel(req, user.getId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectController#applyJoinProject,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @GetMapping("/commentLike")
    public RespBean<Boolean> commentLike(@RequestParam("projectId") Long projectId,
                                         @RequestParam("commentId") Long commentId) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Boolean result = projectFacade.commentLike(commentId, projectId, user.getId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectController#applyJoinProject,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @GetMapping("/commentDeleted")
    public RespBean<Boolean> commentDeleted(@RequestParam("projectId") Long projectId,
                                            @RequestParam("commentId") Long commentId) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Boolean result = projectFacade.commentDeleted(projectId, commentId, user.getId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectController#applyJoinProject,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @GetMapping("/commentShow")
    public RespBean<List<ProjectCommentResp>> commentShow(@RequestParam("projectId") Long projectId) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            List<ProjectCommentResp> result = projectFacade.commentShow(projectId, user.getId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectController#commentShow,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/comment")
    public RespBean<Boolean> comment(@RequestBody UserCommentProjectReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Boolean result = projectFacade.comment(req, user.getId(), user.getNikeName());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectController#comment,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @GetMapping("/favoriteProject")
    public RespBean<Boolean> favoriteProject(@RequestParam("projectId") Long projectId,
                                             @RequestParam("liked") Boolean liked) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Boolean result = projectFacade.favoriteProject(projectId, user.getId(), liked);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectController#favoriteProject,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @GetMapping("/likeProject")
    public RespBean<Boolean> likeProject(@RequestParam("projectId") Long projectId,
                                         @RequestParam("liked") Boolean liked) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Boolean result = projectFacade.likeProject(projectId, user.getId(), liked);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectController#likeProject,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @GetMapping("/detail")
    public RespBean<ProjectsDetailResp> detail(@RequestParam("projectId") Long projectId) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Long userId = user.getId();
            if (user.getRole().equals("ADMIN")) {
                userId = null;
            }
            ProjectsDetailResp result = projectFacade.selectByProjectId(projectId, userId);
            return RespBean.success(result);
        } catch (Exception e) {
            log.error("ProjectController#detail,error,projectId:{}", projectId, e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @GetMapping("/detailForUpdate")
    public RespBean<ProjectUpdateResp> showHotFuye(@RequestParam("projectId") Long projectId) {
        try {
            ProjectUpdateResp result = projectFacade.detailForUpdate(projectId);
            return RespBean.success(result);
        } catch (Exception e) {
            log.error("ProjectController#detail,error,projectId:{}", projectId, e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @PostMapping("/show")
    public RespBean<Page<ProjectsResp>> showHotFuye(@RequestBody ProjectListReq req) {

        try {
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
        } catch (ValidationException e) {
            log.error("ProjectController#showHotFuye,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }
}