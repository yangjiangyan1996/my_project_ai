package com.example.controller;

import com.example.Facade.ProjectMemberFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.resp.MemberListResp;
import com.example.entity.resp.MyCountResp;
import com.example.filter.UserUtil;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/3 14:50
 */
@RestController
@Slf4j
@RequestMapping("/api/auth/projectMember/")
public class ProjectMemberController {

    @Resource
    private ProjectMemberFacade projectMemberFacade;
    @GetMapping("/memberList")
    public RespBean<MemberListResp> memberList(@RequestParam Long projectId) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            MemberListResp result = projectMemberFacade.memberList(projectId);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectMemberController#memberList,req:{}", projectId,e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectMemberController#memberList,req:{}", projectId,e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }
}
