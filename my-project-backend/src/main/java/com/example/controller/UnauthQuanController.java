package com.example.controller;

import com.example.Facade.QuanFacade;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.resp.BarsByCategoryResp;
import com.example.entity.resp.ProjectCommentResp;
import com.example.filter.UserUtil;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/18 17:38
 */
@RestController
@Slf4j
@RequestMapping("/api/unauth/quan")
public class UnauthQuanController {

    @Resource
    QuanFacade quanFacade;

    @GetMapping("/getBarsByCategory")
    public RespBean<List<BarsByCategoryResp>> getBarsByCategory(@RequestParam("categoryId") Long categoryId) {
        try {
            List<BarsByCategoryResp> result = quanFacade.getBarsByCategory(categoryId);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("UnauthQuanController#getBarsByCategory,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("UnauthQuanController#getBarsByCategory,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }
}
