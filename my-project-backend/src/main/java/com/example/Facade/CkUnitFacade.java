package com.example.Facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.cangku.dto.Unit;
import com.example.entity.cangku.req.UnitCreateReq;
import com.example.entity.cangku.req.UnitDeleteReq;
import com.example.entity.cangku.req.UnitListPageReq;
import com.example.entity.cangku.req.UnitUpdateStatusReq;
import com.example.entity.cangku.resp.UnitPageListResp;
import com.example.service.CkUnitService;
import jakarta.annotation.Resource;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/26 15:41
 */
@Service
@Slf4j
public class CkUnitFacade {
    @Resource
    CkUnitService unitService;

    public Page<UnitPageListResp> pageList(Page<Unit> page, UnitListPageReq req) {
        Page<Unit> list = unitService.getPage(page, req);
        if (list.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }
        List<UnitPageListResp> collect = list.getRecords().stream().map(v -> {
            UnitPageListResp p = new UnitPageListResp();
            BeanUtils.copyProperties(v, p);
            return p;
        }).collect(Collectors.toList());

        Page<UnitPageListResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;
    }

    public Boolean delete(UnitDeleteReq req) {
        Unit p = unitService.getById(req.getId());
        if (p == null) {
            throw new ValidationException("单位不存在");
        }

        Unit save = new Unit();
        save.setId(req.getId());
        save.setIsDeleted(1);
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());
        return unitService.updateById(save);
    }

    public Boolean create(UnitCreateReq req) {
        if (req == null) {
            throw new ValidationException("参数错误");
        }

        List<Unit> list = unitService.selectByCodeOrName(req.getTenantId(), req.getUnitCode(), req.getUnitName());
        if (!CollectionUtils.isEmpty(list)) {
            throw new ValidationException("单位已存在");
        }
        Unit s = new Unit();
        s.setTenantId(req.getTenantId());
        s.setStatus(req.getStatus());
        s.setRemark(req.getRemark());
        s.setUnitCode(req.getUnitCode());
        s.setUnitName(req.getUnitName());
        s.setModifiedAt(new Date());
        s.setModifiedBy(req.getUserId());
        s.setCreatedAt(new Date());
        s.setCreatedBy(req.getUserId());
        return unitService.save(s);
    }


    public Boolean update(UnitCreateReq req) {
        Unit wh = unitService.getById(req.getId());
        if (wh == null) {
            throw new ValidationException("单位不存在，请新增！");
        }
        List<Unit> s = unitService.selectByCodeOrName(req.getTenantId(), req.getUnitCode(), req.getUnitName());
        if (CollectionUtils.isEmpty(s)) {
            for (Unit unit : s) {
                if (unit.getId().equals(req.getId())) {
                    continue;
                }
                if (unit.getUnitCode().equals(req.getUnitCode()) || unit.getUnitName().equals(req.getUnitName())) {
                    throw new ValidationException("单位已存在!");
                }
            }
        }

        wh.setUnitName(req.getUnitName());
        wh.setUnitCode(req.getUnitCode());
        wh.setRemark(req.getRemark());
        wh.setStatus(req.getStatus());
        wh.setModifiedAt(new Date());
        wh.setModifiedBy(req.getUserId());

        return unitService.updateById(wh);
    }


    public Boolean updateStatus(UnitUpdateStatusReq req) {
        Unit p = unitService.getById(req.getId());
        if (p == null) {
            throw new ValidationException("单位不存在");
        }

        Unit save = new Unit();
        save.setId(req.getId());
        save.setStatus(req.getStatus());
        save.setModifiedAt(new Date());
        save.setModifiedBy(req.getUserId());
        return unitService.updateById(save);
    }
}
