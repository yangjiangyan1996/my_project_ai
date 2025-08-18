package com.example.Facade;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.config.AsyncTaskUtil;
import com.example.entity.dto.*;
import com.example.entity.req.*;
import com.example.entity.resp.*;
import com.example.enums.ProjectEnum;
import com.example.enums.QuanEnum;
import com.example.enums.TieEnum;
import com.example.service.*;
import com.example.utils.DateUtils;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/25 14:42
 */
@Service
public class TieFacade {
    @Resource
    QuanTieWatchService quanTieWatchService;
    @Resource
    MessageFacade messageFacade;
    @Resource
    QuanTieCommentLikeService quanTieCommentLikeService;
    @Resource
    QuanTieFavoriteService quanTieFavoriteService;
    @Resource
    QuanTieCommentService quanTieCommentService;
    @Resource
    QuanUserBarFollowsService quanUserBarFollowsService;
    @Resource
    AccountService accountService;
    @Resource
    QuanBarsService quanBarsService;
    @Resource
    private QuanBarTieService quanBarTieService;

    public Boolean createTie(QuanTieCreateReq req, Long userId) {
        QuanBarTie e = new QuanBarTie();
        e.setTitle(req.getTitle());
        e.setContent(req.getContent());
        if (!CollectionUtils.isEmpty(req.getImages())) {
            e.setAvatar(JSON.toJSONString(req.getImages()));
        }else {
            e.setAvatar("");
        }
        e.setStatus(QuanEnum.TieStatusEnums.NORMAL.getCode());
        e.setBarId(req.getBarId());
        e.setCreatedAt(new Date());
        e.setCreatedBy(userId);
        e.setModifiedAt(new Date());
        e.setModifiedBy(userId);
        boolean save = quanBarTieService.save(e);
        AsyncTaskUtil.execute(() -> updateBarFollowCount(req.getBarId()));
        return save;
    }

    public void updateBarFollowCount(Long barId) {
        QuanBars bar = quanBarsService.getById(barId);
        if (bar == null) {
            return;
        }
        Long tieCount = quanBarTieService.selectTieCountByBarId(barId);
        bar.setPostCount(tieCount);
        quanBarsService.updateById(bar);
    }

    public Page<QuanTieListPageResp> getTiePageOfBar(QuanTieListPageReq req) {
        Page<QuanBarTie> page = quanBarTieService.getTiePageOfBar(Page.of(req.getPage(), req.getSize()), req);
        if (page.getRecords().isEmpty()) {
            return Page.of(req.getPage(), req.getSize());
        }

        List<Long> userIds = page.getRecords().stream().map(v -> v.getCreatedBy()).distinct().collect(Collectors.toList());
        List<Account> userInfoList = accountService.selectByIds(userIds);
        Map<Long, Account> userId2UserInfoMap = userInfoList.stream().collect(Collectors.toMap(v -> v.getId(), v -> v));

        List<Long> tieIds = page.getRecords().stream().map(v -> v.getId()).distinct().collect(Collectors.toList());
        List<Long> barIds = page.getRecords().stream().map(v -> v.getBarId()).distinct().collect(Collectors.toList());

        List<QuanTieWatch> quanTieWatches = quanTieWatchService.selectByTieIds(tieIds);
        Map<Long, List<QuanTieWatch>> tieId2WatchListMap = quanTieWatches.stream().collect(Collectors.groupingBy(v -> v.getTieId()));

        List<QuanTieFavorite> quanTieFavoriteList = quanTieFavoriteService.selectByTieIds(tieIds);
        Map<Long, List<QuanTieFavorite>> tieId2FavoriteListMap = quanTieFavoriteList.stream().collect(Collectors.groupingBy(v -> v.getTieId()));

        List<QuanTieComment> quanTieComments = quanTieCommentService.selectByTieIds(tieIds);
        Map<Long, List<QuanTieComment>> tieId2CommentListMap = quanTieComments.stream().collect(Collectors.groupingBy(v -> v.getTieId()));

        List<QuanBars> quanBars = quanBarsService.selectByTieIds(barIds);
        Map<Long, QuanBars> barId2BarInfoMap = quanBars.stream().collect(Collectors.toMap(v -> v.getId(), v -> v));

        Map<Long, QuanTieFavorite> tieId2FavoriteMap = new HashMap<>();
        if (req.getUserId() != null ) {
            List<QuanTieFavorite> tieFavoriteList = quanTieFavoriteService.selectByTieIdsAndUserId(tieIds, req.getUserId());
            tieId2FavoriteMap = tieFavoriteList.stream().collect(Collectors.toMap(v -> v.getTieId(), v -> v));
        }

        Map<Long, QuanTieFavorite> finalTieId2FavoriteMap = tieId2FavoriteMap;
        List<QuanTieListPageResp> collect = page.getRecords().stream().map(v -> {
            QuanTieListPageResp r = new QuanTieListPageResp();
            r.setId(v.getId());
            r.setTitle(v.getTitle());
            if (!StringUtils.isEmpty(v.getAvatar())) {
                r.setAvatar(JSON.parseArray(v.getAvatar(),String.class));
            }
            r.setCreatedName(userId2UserInfoMap.get(v.getCreatedBy()).getNickname());
            r.setCreatedTime(DateUtils.date2Str(v.getCreatedAt()));

            if (tieId2WatchListMap != null && tieId2WatchListMap.containsKey(v.getId())) {
                r.setViews(tieId2WatchListMap.get(v.getId()).size());
            }
            if (tieId2CommentListMap != null && tieId2CommentListMap.containsKey(v.getId())) {
                r.setComments(tieId2CommentListMap.get(v.getId()).size());
            }
            if (tieId2FavoriteListMap != null && tieId2FavoriteListMap.containsKey(v.getId())) {
                r.setLikes(tieId2FavoriteListMap.get(v.getId()).size());
            }
            if (barId2BarInfoMap != null && barId2BarInfoMap.containsKey(v.getBarId())) {
                r.setBarName(barId2BarInfoMap.get(v.getBarId()).getName());
            }
            if (finalTieId2FavoriteMap != null && finalTieId2FavoriteMap.containsKey(v.getId())) {
                r.setLiked(finalTieId2FavoriteMap.containsKey(v.getId()));
            }
            return r;
        }).collect(Collectors.toList());

        Page<QuanTieListPageResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(collect);
        return result;
    }

    public QuanTieBaseInfoResp getTieBaseInfo(Long tieId) {
        QuanBarTie e = quanBarTieService.getById(tieId);
        if (e == null) {
            return null;
        }

        List<QuanTieWatch> quanTieWatches = quanTieWatchService.selectByTieId(tieId);

        List<QuanTieFavorite> quanTieFavoriteList = quanTieFavoriteService.selectByTieId(tieId);

        List<QuanTieComment> quanTieComments = quanTieCommentService.selectByTieId(tieId);


        Account account = accountService.selectById(e.getCreatedBy());
        QuanTieBaseInfoResp r = new QuanTieBaseInfoResp();
        r.setId(e.getId());
        r.setTitle(e.getTitle());
        if (!StringUtils.isEmpty(e.getAvatar())) {
            r.setAvatar(JSON.parseArray(e.getAvatar(),String.class));
        }
        r.setContent(e.getContent());
        if (account != null) {
            r.setCreatedName(account.getNickname());
            r.setCreatedAvatar(account.getAvatarUrl());
            r.setSecrecyId(account.getSecrecyId());
        }
        if (!CollectionUtils.isEmpty(quanTieComments)) {
            r.setComments(quanTieComments.size());
        }
        if (!CollectionUtils.isEmpty(quanTieFavoriteList)) {
            r.setLikes(quanTieFavoriteList.size());
        }
        if (!CollectionUtils.isEmpty(quanTieWatches)) {
            r.setViews(quanTieWatches.size());
        }
        r.setCreatedTime(DateUtils.date2Str(e.getCreatedAt()));
        return r;
    }

    public Boolean comment(QuanTieCommentReq req, Long userId, String userName) {
        Long commentId = quanTieCommentService.comment(req.getTieId(), userId, userName, req.getContent(), req.getReplyTo(), req.getFirstLevelCommonId());
        if (commentId != null) {
            //TODO yang 这里要处理下，提供一个公用的
            //messageFacade.createMessageOfComment(req.getTieId(), commentId, req.getReplyTo(), userId);
            return true;
        } else {
            return false;
        }
    }

    public Page<QuanTieCommentResp> commentShow(QuanTieCommentPageReq req, Long currentUserId) {
        Page<QuanTieComment> firstLevelCommentPage = quanTieCommentService.getFirstLevelCommentPageOfBar(Page.of(req.getPage(), req.getSize()), req);
        if (firstLevelCommentPage.getRecords().isEmpty()) {
            return Page.of(req.getPage(), req.getSize());
        }

        QuanBarTie tie = quanBarTieService.selectByTieId(req.getTieId());

        List<Long> userIds = firstLevelCommentPage.getRecords().stream().map(v -> v.getUserId()).distinct().collect(Collectors.toList());
        List<Account> userInfoList = accountService.selectByIds(userIds);
        Map<Long, Account> userId2UserInfoMap = userInfoList.stream().collect(Collectors.toMap(v -> v.getId(), v -> v));

        List<QuanTieCommentLike> likes = quanTieCommentLikeService.selectByTieId(req.getTieId());
        Map<Long, List<QuanTieCommentLike>> commentId2LikeListMap = likes.stream().collect(Collectors.groupingBy(QuanTieCommentLike::getCommentId));

        int previewCount = req.getPreviewReplyCount() != null ? req.getPreviewReplyCount() : 2;
        List<Long> commentIds = firstLevelCommentPage.getRecords().stream().map(comment -> comment.getId()).collect(Collectors.toList());
        Map<Long, List<QuanTieComment>> previewRepliesMap = quanTieCommentService.getPreviewRepliesForComments(commentIds, previewCount);


        // 获取回复相关的用户信息
        List<Long> replyUserIds = previewRepliesMap.values().stream()
                .flatMap(List::stream)
                .map(QuanTieComment::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Account> replyUserMap = accountService.selectByIds(replyUserIds).stream()
                .collect(Collectors.toMap(Account::getId, v -> v));

        // 组装评论树
        List<QuanTieCommentResp> result = new ArrayList<>();

        for (QuanTieComment record : firstLevelCommentPage.getRecords()) {
            QuanTieCommentResp r = convertToVO(record, currentUserId, commentId2LikeListMap, userId2UserInfoMap,tie);
            if (req.getWithPreviewReplies() != null && req.getWithPreviewReplies()) {
                // 转换回复为VO并设置到主评论
                List<QuanTieComment> previewReplies = previewRepliesMap.getOrDefault(r.getId(), Collections.emptyList());
                List<QuanTieCommentResp> replyVOs = previewReplies.stream()
                        .map(s -> convertToVO(s, currentUserId, commentId2LikeListMap, replyUserMap, tie))
                        .collect(Collectors.toList());
                r.setReplies(replyVOs);
                r.setReplyCount(quanTieCommentService.getReplyCount(r.getId()));
            }
            result.add(r);
        }

        Page<QuanTieCommentResp> pageResult = Page.of(req.getPage(), req.getSize());
        pageResult.setTotal(firstLevelCommentPage.getTotal());
        pageResult.setRecords(result);
        return pageResult;
    }


    private QuanTieCommentResp convertToVO(QuanTieComment comment, Long currentUserId, Map<Long, List<QuanTieCommentLike>> commentId2LikeListMap, Map<Long, Account> userId2UserInfoMap,QuanBarTie tie) {
        QuanTieCommentResp vo = new QuanTieCommentResp();
        vo.setId(comment.getId());
        vo.setTieId(comment.getTieId());
        vo.setContent(comment.getStatus().equals(ProjectEnum.ProjectCommentStatusEnum.hide.getCode()) ? "本条评论已被用户删除～" : comment.getContent());
        vo.setCreatedAt(comment.getCreatedAt());
        vo.setReplyTo(comment.getReplyTo());
        vo.setReplyToName(comment.getReplyToUsername());
        vo.setDeleted(comment.getIsDeleted() != 0);
        vo.setLikes(commentId2LikeListMap.containsKey(comment.getId()) ? commentId2LikeListMap.get(comment.getId()).size() : 0);
        vo.setIsMine(comment.getUserId().equals(currentUserId));
        vo.setFirstLevelCommonId(comment.getFirstLevelCommonId());
        vo.setSecrecyId(userId2UserInfoMap.get(comment.getUserId()).getSecrecyId());

        // 获取用户昵称头像
        Account account = accountService.selectById(comment.getUserId());
        vo.setUsername(account.getNickname());
        vo.setAvatar(account.getAvatarUrl());
        vo.setIsAuth(tie.getCreatedBy().equals(comment.getUserId()));
        return vo;
    }

    public Boolean commentLike(Long commentId, Long tieId, Long userId) {
        QuanTieCommentLike l = quanTieCommentLikeService.selectByCommentIdAndUserId(commentId, tieId, userId);
        if (l != null) {
            throw new ValidationException("已点赞，无需重复操作！");
        }
        Boolean result = quanTieCommentLikeService.insert(commentId, tieId, userId);
//        messageFacade.createMessageOfLike(tieId, commentId, userId, false); TODO yang
        return result;
    }

    public Boolean commentDeleted(Long tieId, Long commentId, Long userId) {
        //判断评论是不是自己的，如果不是，返回"不是自己评论的，无法删除！"
        QuanTieComment pc = quanTieCommentService.selectByTieIdAndCommentId(tieId, commentId);
        if (pc == null) {
            throw new ValidationException("没有此评论！");
        }
        if (!pc.getUserId().equals(userId)) {
            throw new ValidationException("不是自己评论的，无法删除！");
        }
        if (pc.getStatus().equals(ProjectEnum.ProjectCommentStatusEnum.hide.getCode())) {
            throw new ValidationException("已删除，无需重复操作！");
        }
        //删除评论
        return quanTieCommentService.updateStatus(commentId, userId, TieEnum.CommentStatusEnum.hide.getCode()) == 1;
    }

    public void addProjectWatch(Long tieId, Long finalUserId) {
        if (null == tieId || null == finalUserId) {
            return;
        }
        quanTieWatchService.insert(tieId, finalUserId);
    }

    public Boolean favoriteTie(Long tieId, Long userId, Boolean favorited) {
        boolean exists = quanTieFavoriteService.selectByTieIdAndUserId(tieId, userId);
        if (Boolean.TRUE.equals(favorited)) {
            //先判断数据库是否存在数据，有的话直接返回true
            if (exists) {
                return true;
            }
            return quanTieFavoriteService.insertOne(new QuanTieFavorite(null, userId, tieId));
        } else {
            if (!exists) {
                return true;
            }
            return quanTieFavoriteService.removeOne(tieId, userId);
        }
    }

    public List<QuanTieTodayHotResp> getTodayHotTie() {
        List<QuanTieComment> qs = quanTieCommentService.select10Tie();
        List<Long> tieids = qs.stream().map(v -> v.getTieId()).distinct().collect(Collectors.toList());
        List<QuanTieWatch> quanTieWatches = quanTieWatchService.selectByTieIds(tieids);
        Map<Long, List<QuanTieWatch>> tieId2WatchListMap = quanTieWatches.stream().collect(Collectors.groupingBy(v -> v.getTieId()));
        List<QuanBarTie> quanBarTies = quanBarTieService.selectByTieIds(tieids);
        Map<Long, QuanBarTie> tieId2BarTieMap = quanBarTies.stream().collect(Collectors.toMap(v -> v.getId(), v -> v));

        List<QuanTieComment> quanTieWatchList = quanTieCommentService.selectByTieIds(tieids);
        Map<Long, List<QuanTieComment>> quanTieCommentListMap = quanTieWatchList.stream().collect(Collectors.groupingBy(v -> v.getTieId()));

        List<QuanTieTodayHotResp> result = new ArrayList<>();
        for (QuanTieComment q : qs) {
            QuanTieTodayHotResp r = new QuanTieTodayHotResp();
            r.setId(q.getTieId());
            r.setComments(quanTieCommentListMap.get(q.getTieId()).size());
            r.setTitle(tieId2BarTieMap.get(q.getTieId()).getTitle());
            r.setViews(tieId2WatchListMap.get(q.getTieId()).size());
            result.add(r);
        }
        return result;
    }

    public Page<BarsInfoResp> myFavoriteBar(BarMyFavoriteReq req) {
        Page<QuanUserBarFollows> page = quanUserBarFollowsService.getMyFavoriteBar(Page.of(req.getPage(), req.getSize()), req);
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return Page.of(req.getPage(), req.getSize());
        }

        List<Long> barIds = page.getRecords().stream().map(v -> v.getBarId()).collect(Collectors.toList());
        List<QuanBars> quanBars = quanBarsService.selectByIds(barIds);
        List<BarsInfoResp> list = quanBars.stream().map(v -> {
            BarsInfoResp r = new BarsInfoResp();
            r.setId(v.getId());
            r.setName(v.getName());
            r.setAvatar(v.getAvatar());
            r.setFollowerCount(v.getFollowerCount());
            r.setPostCount(v.getPostCount());
            r.setDescription(v.getDescription());
            return r;
        }).collect(Collectors.toList());


        Page<BarsInfoResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(list);
        return result;
    }
}
