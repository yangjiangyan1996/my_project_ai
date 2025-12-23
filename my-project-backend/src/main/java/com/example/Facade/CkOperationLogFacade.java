package com.example.Facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.cangku.dto.OperationLog;
import com.example.entity.cangku.req.OperationLogListPageReq;
import com.example.entity.cangku.resp.OperationLogPageListResp;
import com.example.service.CkOperationLogService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/23 13:49
 */
@Service
public class CkOperationLogFacade {
    @Resource
    CkOperationLogService ckOperationLogService;

    public Page<OperationLogPageListResp> pageList(Page<OperationLog> page, OperationLogListPageReq req) {
        Page<OperationLog> list = ckOperationLogService.getPage(page, req);
        if (list.getRecords().isEmpty()) {
            return Page.of(req.getPage() - 1, req.getSize());
        }
        List<OperationLogPageListResp> collect = list.getRecords().stream().map(v -> {
            OperationLogPageListResp p = new OperationLogPageListResp();
            BeanUtils.copyProperties(v, p);
            return p;
        }).collect(Collectors.toList());

        Page<OperationLogPageListResp> result = Page.of(req.getPage() - 1, req.getSize());
        result.setTotal(list.getTotal());
        result.setRecords(collect);
        return result;
    }
}
