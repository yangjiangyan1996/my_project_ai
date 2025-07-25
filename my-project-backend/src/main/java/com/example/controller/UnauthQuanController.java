package com.example.controller;

import com.example.Facade.QuanFacade;
import com.example.entity.base.RespBean;
import com.example.entity.resp.BarsInfoResp;
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


    /**
     * 获取分类下的所有圈子
     * @param categoryCode
     * @return
     */
    @GetMapping("/getBarsByCategory")
    public RespBean<List<BarsInfoResp>> getBarsByCategory(@RequestParam("categoryCode") Long categoryCode) {
        try {
            List<BarsInfoResp> result = quanFacade.getBarsByCategory(categoryCode);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("UnauthQuanController#getBarsByCategory,req:{}", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("UnauthQuanController#getBarsByCategory,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    /**
     * 获取圈子信息
     * @param barId
     * @return
     */
    @GetMapping("/getBarInfo")
    public RespBean<BarsInfoResp> getBarInfo(@RequestParam("barId") Long barId) {
        try {
            BarsInfoResp result = quanFacade.getBarInfo(barId);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("UnauthQuanController#getBarInfo,req:{}", barId,e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("UnauthQuanController#getBarInfo,req:{}", barId,e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }
}
