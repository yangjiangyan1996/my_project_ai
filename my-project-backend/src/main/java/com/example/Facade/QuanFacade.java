package com.example.Facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.config.AsyncTaskUtil;
import com.example.entity.dto.QuanBars;
import com.example.entity.dto.QuanUserBarFollows;
import com.example.entity.req.BarRelationPageReq;
import com.example.entity.req.QuanBarCreateReq;
import com.example.entity.resp.BarsInfoResp;
import com.example.entity.resp.QuanTieListPageResp;
import com.example.enums.CommonEnum;
import com.example.enums.QuanEnum;
import com.example.service.*;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/24 16:35
 */
@Service
public class QuanFacade {

    @Resource
    QuanUserBarFollowsService quanUserBarFollowsService;
    @Resource
    QuanTieWatchService quanTieWatchService;
    @Resource
    QuanTieFavoriteService quanTieFavoriteService;
    @Resource
    QuanTieCommentService quanTieCommentService;
    @Resource
    QuanBarsService quanBarsService;

    public Boolean createBar(QuanBarCreateReq req, Long userId) {
        List<QuanBars> q = quanBarsService.selectByName(req.getName());
        if (!CollectionUtils.isEmpty(q)) {
            throw new ValidationException("圈子名称已存在");
        }
        QuanBars e = new QuanBars();
        //TODO yang 状态先设置为直接审核通过，后续加了审核流程，再变状态
        e.setStatus(QuanEnum.BarStatusEnums.AUDIT_PASS.getCode());
        e.setName(req.getName());
        e.setFirstCategory(req.getFirstCategory());
        e.setSecondCategory(req.getSecondCategory());
        e.setDescription(req.getDescription());
        e.setAvatar(req.getAvatar());
        e.setFollowerCount(0L);
        e.setPostCount(0L);
        e.setCreatedAt(new Date());
        e.setCreatedBy(userId);
        e.setModifiedAt(new Date());
        e.setModifiedBy(userId);
        return quanBarsService.save(e);
    }

    public List<BarsInfoResp> getBarsByCategory(Integer categoryId) {
        if (categoryId == null) {
            return new ArrayList<>(1);
        }
        List<QuanBars> list = quanBarsService.selectByFirstCategory(categoryId);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>(1);
        }
        return list.stream().sorted((o1, o2) -> o2.getFollowerCount().compareTo(o1.getFollowerCount()))
                .map(v -> {
                    BarsInfoResp barsByCategoryResp = new BarsInfoResp();
                    barsByCategoryResp.setId(v.getId());
                    barsByCategoryResp.setName(v.getName());
                    barsByCategoryResp.setAvatar(v.getAvatar());
                    barsByCategoryResp.setFollowerCount(v.getFollowerCount());
                    barsByCategoryResp.setPostCount(v.getPostCount());
                    return barsByCategoryResp;
                }).collect(Collectors.toList());
    }

    public BarsInfoResp getBarInfo(Long barId) {
        if (barId == null) {
            return null;
        }
        QuanBars v = quanBarsService.getById(barId);
        if (v == null) {
            return null;
        }

        BarsInfoResp barsByCategoryResp = new BarsInfoResp();
        barsByCategoryResp.setId(v.getId());
        barsByCategoryResp.setName(v.getName());
        barsByCategoryResp.setAvatar(v.getAvatar());
        barsByCategoryResp.setFollowerCount(v.getFollowerCount());
        barsByCategoryResp.setPostCount(v.getPostCount());
        barsByCategoryResp.setDescription(v.getDescription());
        barsByCategoryResp.setFirstCategoryName(CommonEnum.IndustryCategory.getNameByCode(v.getFirstCategory()));
        barsByCategoryResp.setSecondCategoryName(CommonEnum.IndustryCategory.getNameByCode(v.getSecondCategory()));
        return barsByCategoryResp;
    }

    @Transactional
    public Boolean followBar(Long userId, Long barId) {
        QuanUserBarFollows f = quanUserBarFollowsService.selectByBarIdAndUserId(barId, userId);
        if (f != null) {
            throw new ValidationException("已关注");
        }

        Boolean insert = quanUserBarFollowsService.insert(barId, userId);
        //异步更新圈子关注数
        if (Boolean.TRUE.equals(insert)) {
            AsyncTaskUtil.execute(() -> updateBarFollowCount(barId));
        }
        return insert;
    }

    public void updateBarFollowCount(Long barId) {
        QuanBars bar = quanBarsService.getById(barId);
        if (bar == null) {
            return;
        }
        Long followerCount = quanUserBarFollowsService.selectCountByBarId(barId);
        bar.setFollowerCount(followerCount);
        quanBarsService.updateById(bar);
    }

    public Boolean unFollowBar(Long userId, Long barId) {
        QuanUserBarFollows f = quanUserBarFollowsService.selectByBarIdAndUserId(barId, userId);
        if (f == null) {
            return true;
        }
        Boolean r = quanUserBarFollowsService.removeById(f.getId());
        //异步更新圈子关注数
        if (Boolean.TRUE.equals(r)) {
            AsyncTaskUtil.execute(() -> updateBarFollowCount(barId));
        }
        return r;
    }

    public Boolean isFollowBar(Long userId, Long barId) {
        QuanUserBarFollows f = quanUserBarFollowsService.selectByBarIdAndUserId(barId, userId);
        return f != null;
    }

    public Page<BarsInfoResp> getRelationBar(BarRelationPageReq req) {
        Page<BarsInfoResp> p = Page.of(req.getPage() - 1, req.getSize());
        List<BarsInfoResp> result = new ArrayList<>();
        Set<Long> barIds = new HashSet<>();
        QuanBars bar = quanBarsService.selectById(req.getBarId());
        if (bar != null) {
            Integer secondCategory = bar.getSecondCategory();
            Page<QuanBars> bars = quanBarsService.selectBySecondCategory(Page.of(req.getPage(), req.getSize()), secondCategory);

            //根据二级类目先获取
            if (!CollectionUtils.isEmpty(bars.getRecords())) {
                for (QuanBars b : bars.getRecords()) {
                    BarsInfoResp barsInfoResp = new BarsInfoResp();
                    barsInfoResp.setId(b.getId());
                    barsInfoResp.setName(b.getName());
                    barsInfoResp.setAvatar(b.getAvatar());
                    barsInfoResp.setFollowerCount(b.getFollowerCount());
                    barsInfoResp.setPostCount(b.getPostCount());
                    barsInfoResp.setDescription(b.getDescription());
                    if (result.size() < 5) {
                        if (barIds.add(b.getId())) {
                            result.add(barsInfoResp);
                        }
                    } else {
                        break;
                    }
                }
                if (result.size() > 5) {
                    p.setTotal(bars.getTotal());
                    p.setRecords(result);
                    return p;
                }
            }

            if (result.size() < 5) {
                //根据第一分类获取
                Integer firstCategory = bar.getFirstCategory();
                Page<QuanBars> barByFirstCategory = quanBarsService.selectPageByFirstCategory(Page.of(req.getPage(), req.getSize()), firstCategory);
                p.setTotal(bars.getTotal());
                if (!CollectionUtils.isEmpty(barByFirstCategory.getRecords())) {
                    for (QuanBars b : barByFirstCategory.getRecords()) {
                        BarsInfoResp barsInfoResp = new BarsInfoResp();
                        barsInfoResp.setId(b.getId());
                        barsInfoResp.setName(b.getName());
                        barsInfoResp.setAvatar(b.getAvatar());
                        barsInfoResp.setFollowerCount(b.getFollowerCount());
                        barsInfoResp.setPostCount(b.getPostCount());
                        barsInfoResp.setDescription(b.getDescription());
                        if (result.size() < 5) {
                            if (barIds.add(b.getId())) {
                                result.add(barsInfoResp);
                            }
                        } else {
                            break;
                        }
                    }
                    if (result.size() > 5) {
                        p.setTotal(bars.getTotal());
                        p.setRecords(result);
                        return p;
                    }
                }
            }
        }

        if (result.size() < 5) {
            Page<QuanBars> suijiBars = quanBarsService.selectFollowBars(Page.of(req.getPage(), req.getSize()));
            p.setTotal(suijiBars.getTotal());
            if (!CollectionUtils.isEmpty(suijiBars.getRecords())) {
                for (QuanBars b : suijiBars.getRecords()) {
                    BarsInfoResp barsInfoResp = new BarsInfoResp();
                    barsInfoResp.setId(b.getId());
                    barsInfoResp.setName(b.getName());
                    barsInfoResp.setAvatar(b.getAvatar());
                    barsInfoResp.setFollowerCount(b.getFollowerCount());
                    barsInfoResp.setPostCount(b.getPostCount());
                    barsInfoResp.setDescription(b.getDescription());
                    if (result.size() < 5) {
                        if (barIds.add(b.getId())) {
                            result.add(barsInfoResp);
                        }
                    } else {
                        break;
                    }
                }
                if (result.size() > 5) {
                    p.setTotal(suijiBars.getTotal());
                    p.setRecords(result);
                    return p;
                }
            }
        }
        p.setRecords(result);
        return p;
    }
}
