package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.QuanTieComment;
import com.example.entity.dto.QuanTieWatch;
import com.example.entity.req.QuanTieCommentPageReq;
import com.example.enums.TieEnum;
import com.example.mapper.QuanTieCommentMapper;
import com.example.service.QuanTieCommentService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/25 17:28
 */
@Service
public class QuanTieCommentServiceImpl extends ServiceImpl<QuanTieCommentMapper, QuanTieComment> implements QuanTieCommentService {
    @Override
    public Long comment(Long tieId, Long userId, String userName, String content, Long replyToId, Long firstLevelCommonId) {
        QuanTieComment replyToComment = null;
        if (replyToId != null) {
            replyToComment = this.baseMapper.selectById(replyToId);
        }
        QuanTieComment projectComment = new QuanTieComment();
        projectComment.setTieId(tieId);
        projectComment.setUserId(userId);
        projectComment.setUsername(userName);
        projectComment.setContent(content);
        projectComment.setReplyTo(replyToId);
        projectComment.setFirstLevelCommonId(firstLevelCommonId);
        projectComment.setReplyToUserId(replyToComment != null ? replyToComment.getUserId() : null);
        projectComment.setReplyToUsername(replyToComment != null ? replyToComment.getUsername() : null);
        projectComment.setLikes(0);
        boolean save = save(projectComment);
        if (save) {
            return projectComment.getId();
        } else {
            return null;
        }
    }

    @Override
    public Page<QuanTieComment> getFirstLevelCommentPageOfBar(Page<QuanTieComment> page, QuanTieCommentPageReq req) {
        return baseMapper.selectPage(
                page,
                new QueryWrapper<QuanTieComment>()
                        .eq("tie_id", req.getTieId())
                        .eq("is_deleted", 0)
                        .eq(req.getFirstCommenId() != null, "first_level_common_id", req.getFirstCommenId())
                        .eq(req.getFirstCommenId() == null, "first_level_common_id", -1)
//                        .eq("status", TieEnum.CommentStatusEnum.ok.getCode())
                        .orderByAsc("created_at")
        );
    }

    @Override
    public Map<Long, List<QuanTieComment>> getPreviewRepliesForComments(List<Long> commentIds, int previewCount) {
        if (CollectionUtils.isEmpty(commentIds)) {
            return Collections.emptyMap();
        }
        List<QuanTieComment> replies = this.baseMapper.getPreviewRepliesForComments(commentIds, previewCount);
        return replies.stream()
                .collect(Collectors.groupingBy(QuanTieComment::getFirstLevelCommonId));
    }

    @Override
    public Long getReplyCount(Long commenId) {
        return this.baseMapper.selectCount(new QueryWrapper<QuanTieComment>()
                .eq("first_level_common_id", commenId)
                .eq("status", TieEnum.CommentStatusEnum.ok.getCode())
                .eq("is_deleted", 0));

    }

    @Override
    public QuanTieComment selectByTieIdAndCommentId(Long tieId, Long commentId) {
        return this.baseMapper.selectOne(new QueryWrapper<QuanTieComment>()
                .eq("tie_id", tieId)
                .eq("id", commentId)
                .eq("is_deleted", 0));
    }

    @Override
    public List<QuanTieComment> selectByTieIds(List<Long> tieIds) {
        return this.baseMapper.selectList(new QueryWrapper<QuanTieComment>()
                .in("tie_id", tieIds)
                .eq("is_deleted", 0));
    }

    @Override
    public List<QuanTieComment> select10Tie() {
            return this.baseMapper.selectList(new QueryWrapper<QuanTieComment>()
                    .inSql("id", "SELECT MAX(c.id) FROM quan_tie_comments c " +
                            "JOIN (SELECT tie_id FROM quan_tie_comments WHERE is_deleted = 0 " +
                            "GROUP BY tie_id ORDER BY COUNT(*) DESC LIMIT 10) t " +
                            "ON c.tie_id = t.tie_id " +
                            "GROUP BY c.tie_id")
                    .orderByDesc("created_at")
            );
    }

    @Override
    public List<QuanTieComment> selectByTieId(Long tieId) {
        return this.baseMapper.selectList(new QueryWrapper<QuanTieComment>()
                .eq("tie_id", tieId)
                .eq("is_deleted", 0));
    }

    @Override
    public int updateStatus(Long commentId, Long userId, Integer code) {
        QuanTieComment q = new QuanTieComment();
        q.setStatus(code);
        q.setModifiedAt(new Date());
        q.setModifiedBy(userId);
        return this.baseMapper.update(q,
                new QueryWrapper<QuanTieComment>().eq("id", commentId));
    }
}
