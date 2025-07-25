package com.example.Facade;

import com.example.entity.dto.QuanBars;
import com.example.entity.req.QuanBarCreateReq;
import com.example.entity.resp.BarsInfoResp;
import com.example.enums.QuanEnum;
import com.example.service.QuanBarsService;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
        e.setFollowerCount(0);
        e.setPostCount(0);
        e.setCreatedAt(new Date());
        e.setCreatedBy(userId);
        e.setModifiedAt(new Date());
        e.setModifiedBy(userId);
        return quanBarsService.save(e);
    }

    public List<BarsInfoResp> getBarsByCategory(Long categoryId) {
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
        return barsByCategoryResp;
    }
}
