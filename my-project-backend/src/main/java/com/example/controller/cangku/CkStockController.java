package com.example.controller.cangku;

import com.example.Facade.CkStockFacade;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/25 12:04
 */
@RestController
@Slf4j
@RequestMapping("/api/auth/stock")
public class CkStockController {
    @Resource
    private CkStockFacade stockFacade;
}
