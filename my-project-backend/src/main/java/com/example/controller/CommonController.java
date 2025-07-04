package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.CommonFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.req.MyMemberGroupsReq;
import com.example.entity.req.SearchUserReq;
import com.example.entity.resp.EnumResp;
import com.example.entity.resp.MyMemberGroupsResp;
import com.example.entity.resp.UserSearchResp;
import com.example.enums.ProjectEnum;
import com.example.filter.UserUtil;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/20 14:31
 */
@RestController
@Slf4j
@RequestMapping("/api/auth/common/")
public class CommonController {

    @Resource
    CommonFacade commonFacade;

    @PostMapping("/searchUser")
    public RespBean<List<UserSearchResp>> searchUser(@RequestBody SearchUserReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            List<UserSearchResp> list = commonFacade.myMemberGroups(req);
            return RespBean.success(list);
        } catch (ValidationException e) {
            log.error("CommonController#searchUser,req:{}", req,e);
            return RespBean.failure(999, e.getMessage());
        }  catch (Exception e) {
            log.error("CommonController#searchUser, error",e);
            return RespBean.failure(999, e.getMessage());
        }
    }


    @GetMapping("/category")
    public RespBean<List<EnumResp>> getAllCategories() {
        List<EnumResp> collect = Arrays.stream(ProjectEnum.ProjectCategoryEnum.values()).map(v -> {
                    EnumResp r = new EnumResp();
                    r.setCode(v.getCode());
                    r.setDesc(v.getName());
                    return r;
                }).sorted(Comparator.comparingInt(EnumResp::getCode))
                .collect(Collectors.toList());
        return RespBean.success(collect);
    }

    @GetMapping("/difficulty")
    public RespBean<List<EnumResp>> getAllDifficulties() {
        List<EnumResp> collect = Arrays.stream(ProjectEnum.ProjectDifficultyEnum.values()).map(v -> {
                    EnumResp r = new EnumResp();
                    r.setCode(v.getCode());
                    r.setDesc(v.getName());
                    return r;
                }).sorted(Comparator.comparingInt(EnumResp::getCode))
                .collect(Collectors.toList());
        return RespBean.success(collect);
    }

}
