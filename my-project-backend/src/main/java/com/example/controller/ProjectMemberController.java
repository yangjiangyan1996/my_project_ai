package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.ProjectMemberFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.req.AddMemberByManagerReq;
import com.example.entity.req.MyMemberGroupsReq;
import com.example.entity.req.RemoveMemberReq;
import com.example.entity.resp.MemberListResp;
import com.example.entity.resp.MyCountResp;
import com.example.entity.resp.MyMemberGroupsResp;
import com.example.filter.UserUtil;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/addMemberByManager")
    public RespBean<Boolean> addMemberByManager(@RequestBody AddMemberByManagerReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Boolean result = projectMemberFacade.addMemberByManager(req, user.getId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectMemberController#addMemberByManager,req:{}", req,e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectMemberController#addMemberByManager, error",e);
            return RespBean.failure(999, e.getMessage());
        }
    }

    @PostMapping("/myMemberGroups")
    public RespBean<Page<MyMemberGroupsResp>> myMemberGroups(@RequestBody MyMemberGroupsReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Page<MyMemberGroupsResp> list = projectMemberFacade.myMemberGroups(req, user.getId());
            return RespBean.success(list);
        } catch (ValidationException e) {
            log.error("ProjectMemberController#myMemberGroups,req:{}", req,e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectMemberController#myPublished, error",e);
            return RespBean.failure(999, e.getMessage());
        }
    }


    @PostMapping("/removeMember")
    public RespBean<Boolean> removeMember(@RequestBody RemoveMemberReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Boolean result = projectMemberFacade.removeMember(req,user.getId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("ProjectMemberController#removeMember,req:{}", req,e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("ProjectMemberController#removeMember,req:{}", req,e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    @GetMapping("/memberList")
    public RespBean<MemberListResp> memberList(@RequestParam Long projectId) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            MemberListResp result = projectMemberFacade.memberList(projectId,user.getId());
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
