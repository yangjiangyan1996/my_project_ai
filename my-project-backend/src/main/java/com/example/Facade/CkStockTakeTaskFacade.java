package com.example.Facade;

import com.example.service.CkStockTakeTaskService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/28 13:08
 */
@Component
public class CkStockTakeTaskFacade {
    @Resource
    private CkStockTakeTaskService ckStockTakeTaskService;
}
