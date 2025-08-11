package com.example.controller;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alibaba.fastjson2.JSON;
import com.example.Facade.CommonFacade;
import com.example.Facade.QuanFacade;
import com.example.Facade.TieFacade;
import com.example.annotations.TaskProgress;
import com.example.entity.base.RespBean;
import com.example.entity.base.UserInfo;
import com.example.entity.req.*;
import com.example.entity.resp.BarsInfoResp;
import com.example.entity.resp.QuanTieCommentResp;
import com.example.filter.UserUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
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
@RequestMapping("/api/auth/quan/")
public class QuanController {

    @Resource
    CommonFacade commonFacade;
    @Resource
    TieFacade tieFacade;
    @Resource
    private QuanFacade quanFacade;

    /**
     * 获取用户收藏的圈
     * @param req
     * @return
     */
    @PostMapping("/myFavoriteBar")
    public RespBean<Page<BarsInfoResp>> myFavoriteBar(@RequestBody BarMyFavoriteReq req) {
        try {
            Long userId;
            if (req.getSecrecyId() != null) {
                userId = commonFacade.getUserIdBySecrecyId(req.getSecrecyId());
            } else {
                userId = UserUtil.getCurrentUser().getId();
            }
            if (userId == null) {
                return new RespBean<>();
            }
            req.setUserId(userId);
            Page<BarsInfoResp> result = tieFacade.myFavoriteBar(req);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("QuanController#myFavoriteBar,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("QuanController#myFavoriteBar,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }


    /**
     * 帖子收藏
     * @param tieId
     * @param favorited
     * @return
     */
    @GetMapping("/favoriteTie")
    public RespBean<Boolean> favoriteTie(@RequestParam("tieId") Long tieId,
                                             @RequestParam("favorited") Boolean favorited) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Boolean result = tieFacade.favoriteTie(tieId, user.getId(), favorited);
            return RespBean.success(result);
        }catch (ValidationException e) {
            log.error("QuanController#favoriteTie,req:{},{}", tieId,favorited, e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("QuanController#favoriteTie,req:{},{}", tieId,favorited, e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    /**
     * 评论删除
     * @param projectId
     * @param commentId
     * @return
     */
    @GetMapping("/commentDeleted")
    public RespBean<Boolean> commentDeleted(@RequestParam("tieId") Long tieId,
                                            @RequestParam("commentId") Long commentId) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Boolean result = tieFacade.commentDeleted(tieId, commentId, user.getId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("QuanController#commentDeleted,req:{},{}", tieId,commentId, e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("QuanController#commentDeleted,req:{},{}", tieId,commentId, e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    /**
     * 评论点赞
     * @param tieId
     * @param commentId
     * @return
     */
    @GetMapping("/commentLike")
    public RespBean<Boolean> commentLike(@RequestParam("tieId") Long tieId,
                                         @RequestParam("commentId") Long commentId) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Boolean result = tieFacade.commentLike(commentId, tieId, user.getId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("QuanController#commentLike,req:{},{}", tieId,commentId, e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("QuanController#commentLike,req:{},{}", tieId,commentId, e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    /**
     * 帖子评论
     * @param req
     * @return
     */
    @PostMapping("/comment")
    public RespBean<Boolean> comment(@RequestBody @Valid QuanTieCommentReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Boolean result = tieFacade.comment(req, user.getId(), user.getNikeName());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("QuanController#comment,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("QuanController#comment,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    /**
     * 创建帖子
     * @param req
     * @return
     */
    @PostMapping("/createTie")
    @TaskProgress(category = {"huoyuechengyuan","shequgongxianzhe"})
    public RespBean<Boolean> createTie(@RequestBody @Valid QuanTieCreateReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Boolean result = tieFacade.createTie(req, user.getId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("QuanController#createTie,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("QuanController#createTie,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    /**
     * 创建圈子
     *
     * @param req
     * @return
     */
    @PostMapping("/createBar")
    public RespBean<Boolean> createBar(@RequestBody @Valid QuanBarCreateReq req) {
        try {
            UserInfo user = UserUtil.getCurrentUser();
            Boolean result = quanFacade.createBar(req, user.getId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("QuanController#create,req:{}", req, e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("QuanController#create,req:{}", req, e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    /**
     * 关注圈子
     *
     * @param req
     * @return
     */
    @PostMapping("/followBar")
    @TaskProgress(category = {"quanzixinren"})

    public RespBean<Boolean> followBar(@RequestBody @Valid BarFollowReq req) {
        try {
            Boolean result = quanFacade.followBar(req.getUserId(), req.getBarId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("QuanController#followBar,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("QuanController#followBar,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    /**
     * 取消关注圈子
     *
     * @param req
     * @return
     */
    @PostMapping("/unFollowBar")
    public RespBean<Boolean> unFollowBar(@RequestBody @Valid BarFollowReq req) {
        try {
            Boolean result = quanFacade.unFollowBar(req.getUserId(), req.getBarId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("QuanController#unFollowBar,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("QuanController#unFollowBar,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }

    /**
     * 判断用户是否关注圈子
     *
     * @param req
     * @return
     */
    @PostMapping("/isFollowBar")
    public RespBean<Boolean> isFollowBar(@RequestBody @Valid BarFollowReq req) {
        try {
            Boolean result = quanFacade.isFollowBar(req.getUserId(), req.getBarId());
            return RespBean.success(result);
        } catch (ValidationException e) {
            log.error("QuanController#isFollowBar,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("QuanController#isFollowBar,req:{}", JSON.toJSONString(req), e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }
}
