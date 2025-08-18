package com.example.controller;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.CommonFacade;
import com.example.Facade.ProjectFacade;
import com.example.annotations.TaskProgress;
import com.example.config.QqMailService;
import com.example.config.UserNotLoggedInException;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.req.*;
import com.example.entity.resp.*;
import com.example.enums.AchievementEnums;
import com.example.filter.UserUtil;
import com.example.service.ProjectService;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/auth/project/")
public class ProjectController {

    @Resource
    CommonFacade commonFacade;
    @Resource
    private QqMailService qqMailService;
    @Resource
    ProjectFacade projectFacade;
    @Resource
    private ProjectService projectService;

    /**
     * 项目评价其他人
     *
     * @param req
     * @return
     */
    @PostMapping("/evaluate")
    public RespBean<Boolean> evaluate(@RequestBody @Validated ProjectEvaluateReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Boolean result = projectFacade.evaluate(req, user.getId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectController#evaluate,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectController#evaluate,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    /**
     * 项目是否评价
     */
    @GetMapping("/hasEvaluate")
    public RespBean<Boolean> hasEvaluate(@RequestParam("projectId") Long projectId) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Boolean result = projectFacade.hasEvaluate(projectId, user.getId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectController#hasEvaluate,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectController#hasEvaluate,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    /**
     * 完结
     *
     * @param req
     * @return
     */
    @PostMapping("/closed")
    public RespBean<Boolean> closed(@RequestBody ProjectFinishReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Boolean result = projectFacade.closed(req, user.getId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectController#closed,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectController#closed,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

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


    /**
     * 申请加入项目
     *
     * @param req
     * @return
     */
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

    /**
     * 管理员审核通过
     *
     * @param req
     * @return
     */
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

    /**
     * 管理员审核不通过
     *
     * @param req
     * @return
     */
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


    /**
     * @param req
     * @return
     */
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

    /**
     * 我的申请列表
     *
     * @param req
     * @return
     */
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

    /**
     * 加入项目申请，审批通过
     *
     * @param req
     * @return
     */
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

    /**
     * 取消申请
     *
     * @param req
     * @return
     */
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

    /**
     * 拒绝申请
     *
     * @param req
     * @return
     */
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


    @GetMapping("/getProjectOfMyShow")
    public RespBean<ProjectOfMyShowGetResp> getProjectOfMyShow(@RequestParam(value = "secrecyId", required = false) Long secrecyId) {
        try {
            Long userId = null;
            if (secrecyId != null) {
                userId = commonFacade.getUserIdBySecrecyId(secrecyId);
            } else {
                userId = UserUtil.getCurrentUser().getId();
            }

            ProjectOfMyShowGetResp result = projectFacade.getProjectOfMyShow(userId);
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
    @TaskProgress(category = {"qingtongzuozhe", "baiyinzuozhe", "huangjinzuozhe", "zuanshizuozhe", "wangzhezuozhe"})
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


    /**
     * 关注
     *
     * @param req
     * @return
     */
    @PostMapping("/concernPublisher")
    @TaskProgress(category = {"shejiao"})
    public RespBean<Boolean> concernPublisher(@RequestBody @Validated ConcernPublisherReq req) {
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

    /**
     * 取消关注
     *
     * @param req
     * @return
     */
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
            log.error("ProjectController#applyJoinProject,req:{}", e);
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


    @PostMapping("/comment")
    @TaskProgress(category = {"pinglun"})
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
    @TaskProgress(category = {"shoucang"})
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
    @TaskProgress(category = {"dianzan"})
    public RespBean<Boolean> likeProject(@RequestParam("projectId") Long projectId,
                                         @RequestParam("liked") Boolean liked) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Boolean result = projectFacade.likeProject(projectId, user.getId(), liked);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectController#likeProject,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (UserNotLoggedInException e) {
            log.error("ProjectController#likeProject,req:{}", e);
            return RespBean.failure(401, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectController#likeProject,req:{}", e);
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
}