package com.example.controller;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.QuanFacade;
import com.example.Facade.TieFacade;
import com.example.config.AsyncTaskUtil;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.req.BarRelationPageReq;
import com.example.entity.req.QuanTieListPageReq;
import com.example.entity.resp.BarsInfoResp;
import com.example.entity.resp.MyMemberGroupsResp;
import com.example.entity.resp.QuanTieBaseInfoResp;
import com.example.entity.resp.QuanTieListPageResp;
import com.example.filter.UserUtil;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
    TieFacade tieFacade;
    @Resource
    QuanFacade quanFacade;

    /**
     * 获取关联的
     * @return
     */
    @PostMapping("/getRelationBar")
    public RespBean<Page<BarsInfoResp>> getRelationBar(@RequestBody BarRelationPageReq req){
        try {
            Page<BarsInfoResp> result = quanFacade.getRelationBar(req);
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("UnauthQuanController#getRelationBar,req:{}",JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("UnauthQuanController#getRelationBar,req:{}", JSON.toJSONString(req),e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    /**
     * 获取帖子基本信息
     * @param tieId
     * @return
     */
    @GetMapping("/getTieBaseInfo")
    public RespBean<QuanTieBaseInfoResp> getTieBaseInfo(@RequestParam("tieId") Long tieId) {
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
                log.error("UnauthQuanController#getTieBaseInfo,error,projectId:{}", tieId, e);
            }

            QuanTieBaseInfoResp result = tieFacade.getTieBaseInfo(tieId);

            Long finalUserId = userId;
            AsyncTaskUtil.execute(() -> {
                tieFacade.addProjectWatch(tieId, finalUserId);
            });
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("UnauthQuanController#getTieBaseInfo,req:{}",tieId, e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("UnauthQuanController#getTieBaseInfo,req:{}", tieId,e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    /**
     * 获取分类下的所有圈子
     * @param categoryCode
     * @return
     */
    @GetMapping("/getBarsByCategory")
    public RespBean<List<BarsInfoResp>> getBarsByCategory(@RequestParam("categoryCode") Integer categoryCode) {
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

    @PostMapping("/getTiePageOfBar")
    public RespBean<Page<QuanTieListPageResp>> getTiePageOfBar(@RequestBody QuanTieListPageReq req) {
        try {
            Page<QuanTieListPageResp> list = tieFacade.getTiePageOfBar(req);
            return RespBean.success(list);
        } catch (ValidationException e) {
            log.error("UnauthQuanController#getTiePageOfBar,req:{}", JSON.toJSONString(req),e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("UnauthQuanController#getTiePageOfBar,req:{}", JSON.toJSONString(req),e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }
}
