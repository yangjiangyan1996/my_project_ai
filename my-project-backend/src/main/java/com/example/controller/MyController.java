package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Facade.CommonFacade;
import com.example.Facade.MyFacade;
import com.example.entity.base.RespBean;
import com.example.entity.req.MyFolloweesPageReq;
import com.example.entity.req.MyFollowersPageReq;
import com.example.entity.req.MyPublishedPageReq;
import com.example.entity.resp.MyFollowCountResp;
import com.example.entity.resp.MyFolloweesPageResp;
import com.example.entity.resp.MyFollowersPageResp;
import com.example.entity.resp.MyPublishedResp;
import com.example.filter.UserUtil;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/23 23:47
 */
@RestController
@RequestMapping("/api/auth/my/")
@Slf4j
public class MyController {
    @Resource
    CommonFacade commonFacade;
    @Resource
    private MyFacade myFacade;

    @GetMapping("/myFollowCount")
    public RespBean<MyFollowCountResp> myFollowCount(@RequestParam(value = "secrecyId", required = false) Long secrecyId) {
        try {
            Long userId = null;
            if (secrecyId != null) {
                userId = commonFacade.getUserIdBySecrecyId(secrecyId);
            } else {
                userId = UserUtil.getCurrentUser().getId();
            }
            MyFollowCountResp hotFuyeProjects = myFacade.myFollowCount(userId);
            return RespBean.success(hotFuyeProjects);
        } catch (Exception e) {
            return RespBean.failure(999, e.getMessage());
        }
    }

    @PostMapping("/myLike")
    public RespBean<Page<MyPublishedResp>> myLike(@RequestBody MyPublishedPageReq req) {
        try {
            Long userId = null;
            if (req.getSecrecyId() != null) {
                userId = commonFacade.getUserIdBySecrecyId(req.getSecrecyId());
            } else {
                userId = UserUtil.getCurrentUser().getId();
            }
            Page<MyPublishedResp> hotFuyeProjects = myFacade.myLike(req, userId);
            return RespBean.success(hotFuyeProjects);
        } catch (Exception e) {
            return RespBean.failure(999, e.getMessage());
        }
    }


    @PostMapping("/myFavorites")
    public RespBean<Page<MyPublishedResp>> myFavorites(@RequestBody MyPublishedPageReq req) {
        try {
            Long userId = null;
            if (req.getSecrecyId() != null) {
                userId = commonFacade.getUserIdBySecrecyId(req.getSecrecyId());
            } else {
                userId = UserUtil.getCurrentUser().getId();
            }

            Page<MyPublishedResp> hotFuyeProjects = myFacade.myFavorites(req, userId);
            return RespBean.success(hotFuyeProjects);
        } catch (Exception e) {
            return RespBean.failure(999, e.getMessage());
        }
    }

    @PostMapping("/myPublished")
    public RespBean<Page<MyPublishedResp>> myPublished(@RequestBody MyPublishedPageReq req) {
        try {
            Long userId = null;
            if (req.getSecrecyId() != null) {
                userId = commonFacade.getUserIdBySecrecyId(req.getSecrecyId());
            } else {
                userId = UserUtil.getCurrentUser().getId();
            }

            Page<MyPublishedResp> list = myFacade.myPublished(req, userId);
            return RespBean.success(list);
        } catch (Exception e) {
            log.error("Mycontroller#myPublished, error", e);
            return RespBean.failure(999, e.getMessage());
        }
    }


    /**
     * 我的粉丝
     * @param req
     * @return
     */
    @PostMapping("/myFollowers")
    public RespBean<Page<MyFollowersPageResp>> myFollowers(@RequestBody MyFollowersPageReq req) {
        try {
            Long userId = null;
            if (req.getSecrecyId() != null) {
                userId = commonFacade.getUserIdBySecrecyId(req.getSecrecyId());
            } else {
                userId = UserUtil.getCurrentUser().getId();
            }

            Page<MyFollowersPageResp> list = myFacade.myFollowers(req, userId);
            return RespBean.success(list);
        } catch (ValidationException e) {
            log.error("Mycontroller#myFollowers,req:{}", req, e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("Mycontroller#myFollowers, error", e);
            return RespBean.failure(999, e.getMessage());
        }
    }

    /**
     * 访问别人主页查看-我的粉丝
     * @param req
     * @return
     */
    @PostMapping("/otherFollowers")
    public RespBean<Page<MyFollowersPageResp>> otherFollowers(@RequestBody MyFollowersPageReq req) {
        try {
            Long otherUserId = commonFacade.getUserIdBySecrecyId(req.getSecrecyId());
            Long currentUserId = UserUtil.getCurrentUser().getId();

            Page<MyFollowersPageResp> list = myFacade.otherFollowers(req, otherUserId, currentUserId);
            return RespBean.success(list);
        } catch (ValidationException e) {
            log.error("Mycontroller#otherFollowers,req:{}", req, e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("Mycontroller#otherFollowers, error", e);
            return RespBean.failure(999, e.getMessage());
        }
    }

    /**
     * 我的关注列表
     * @param req
     * @return
     */
    @PostMapping("/myFollowees")
    public RespBean<Page<MyFolloweesPageResp>> myFollowees(@RequestBody MyFolloweesPageReq req) {
        try {
            Long userId = null;
            if (req.getSecrecyId() != null) {
                userId = commonFacade.getUserIdBySecrecyId(req.getSecrecyId());
            } else {
                userId = UserUtil.getCurrentUser().getId();
            }

            Page<MyFolloweesPageResp> list = myFacade.myFollowees(req, userId);
            return RespBean.success(list);
        } catch (ValidationException e) {
            log.error("Mycontroller#myFollowees,req:{}", req, e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("Mycontroller#myFollowees, error", e);
            return RespBean.failure(999, e.getMessage());
        }
    }

    /**
     * 访问别人主页查看-我的关注列表
     * @param req
     * @return
     */
    @PostMapping("/otherFollowees")
    public RespBean<Page<MyFolloweesPageResp>> otherFollowees(@RequestBody MyFolloweesPageReq req) {
        try {
            Long otherUserId = commonFacade.getUserIdBySecrecyId(req.getSecrecyId());
            Long currentUserId = UserUtil.getCurrentUser().getId();
            Page<MyFolloweesPageResp> list = myFacade.otherFollowees(req,otherUserId, currentUserId);
            return RespBean.success(list);
        } catch (ValidationException e) {
            log.error("Mycontroller#otherFollowees,req:{}", req, e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("Mycontroller#otherFollowees, error", e);
            return RespBean.failure(999, e.getMessage());
        }
    }

    /**
     * 是否关注
     * @param req
     * @return  接口返回true表示未关注，false表示已关注
     */
    @GetMapping("/isFollewer")
    public RespBean<Boolean> isFollewer(@RequestParam(value = "followeeId") Long followeeId) {
        try {
            Boolean list = myFacade.isFollewer(followeeId, UserUtil.getCurrentUser().getId());
            return RespBean.success(list);
        } catch (ValidationException e) {
            log.error("Mycontroller#isFollewer,req:{}", followeeId, e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("Mycontroller#isFollewer, error", e);
            return RespBean.failure(999, e.getMessage());
        }
    }
}
