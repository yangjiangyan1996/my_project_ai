package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.CommonFacade;
import com.example.Facade.ProjectFacade;
import com.example.annotations.TaskProgress;
import com.example.config.AsyncTaskUtil;
import com.example.config.QqMailService;
import com.example.entity.RestBean;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.dto.Projects;
import com.example.entity.req.MatchUserReq;
import com.example.entity.req.ProjectListReq;
import com.example.entity.req.ProjectShowListReq;
import com.example.entity.req.ShowHotProjectListPageReq;
import com.example.entity.resp.*;
import com.example.entity.vo.request.EmailRegisterVO;
import com.example.enums.CommonEnum;
import com.example.filter.UserUtil;
import com.example.service.AccountService;
import com.example.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
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
    private AccountService accountService;
    @Resource
    ProjectFacade projectFacade;
    @Resource
    private ProjectService projectService;

    /**
     * 请求邮件验证码
     *
     * @param email   请求邮件
     * @param type    类型
     * @param request 请求
     * @return 是否请求成功
     */
    @GetMapping("/askPhoneCode")
    @Operation(summary = "请求手机验证码")
    public RestBean<Boolean> askPhoneCode(@RequestParam @Email String phone,
                                          @RequestParam @Pattern(regexp = "(register|reset)") String type,
                                          HttpServletRequest request) {
        try{
            accountService.askPhoneCode(String.valueOf(phone), request.getRemoteAddr());
        }catch (Exception e){
            return RestBean.failure(400, "发送失败");
        }
        return RestBean.success();
    }

    /**
     * 进行用户注册操作，需要先请求邮件验证码
     *
     * @param vo 注册信息
     * @return 是否注册成功
     */
    @TaskProgress(category = {"denglu"})
    @PostMapping("/register")
    @Operation(summary = "用户注册操作")
    public RestBean<Void> register(@RequestBody @Valid EmailRegisterVO vo) {
        return this.messageHandle(() ->
                accountService.registerEmailAccount(vo));
    }

    /**
     * 针对于返回值为String作为错误信息的方法进行统一处理
     *
     * @param action 具体操作
     * @param <T>    响应结果类型
     * @return 响应结果
     */
    private <T> RestBean<T> messageHandle(Supplier<String> action) {
        String message = action.get();
        if (message == null)
            return RestBean.success();
        else
            return RestBean.failure(400, message);
    }

    /**
     * 获取平均评价列表
     * @param req
     * @return
     */
    @GetMapping("/getAvgEvaluateList")
    public RespBean<List<ProjectAvgEvaluateListResp>> getAvgEvaluateList(@RequestParam("projectId") Long projectId) {
        try {
            List<ProjectAvgEvaluateListResp> result = projectFacade.getAvgEvaluateList(projectId);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("UnauthProjectController#getAvgEvaluateList,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("UnauthProjectController#getAvgEvaluateList,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    @PostMapping("/projectShowList")
    public RespBean<Page<ProjectOfMyShowGetResp>> projectShowList(@RequestBody ProjectShowListReq req) {
        try {
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
                userId = -888L;
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
