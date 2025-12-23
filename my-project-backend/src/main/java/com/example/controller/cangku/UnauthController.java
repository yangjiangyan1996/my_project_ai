package com.example.controller.cangku;

import com.example.Facade.CkTenantFacade;
import com.example.entity.RestBean;
import com.example.entity.base.RespBean;
import com.example.entity.cangku.resp.CkTenantUnauthResp;
import com.example.entity.vo.request.ConfirmResetVO;
import com.example.entity.vo.request.EmailResetVO;
import com.example.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Supplier;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/23 17:18
 */
@RestController
@Slf4j
@RequestMapping("/api/unauth/")
public class UnauthController {
    @Resource
    AccountService accountService;
    @Resource
    private CkTenantFacade tenantFacade;

    /**
     * 执行密码重置确认，检查验证码是否正确
     *
     * @param vo 密码重置信息
     * @return 是否操作成功
     */
    @PostMapping("/reset-confirm")
    @Operation(summary = "密码重置确认")
    public RestBean<Void> resetConfirm(@RequestBody @Valid ConfirmResetVO vo) {
        return this.messageHandle(() -> accountService.resetConfirm(vo));
    }

    /**
     * 执行密码重置操作
     *
     * @param vo 密码重置信息
     * @return 是否操作成功
     */
    @PostMapping("/reset-password")
    @Operation(summary = "密码重置操作")
    public RestBean<Void> resetPassword(@RequestBody @Valid EmailResetVO vo) {
        return this.messageHandle(() ->
                accountService.resetEmailAccountPassword(vo));
    }

    /**
     * 针对于返回值为String作为错误信息的方法进行统一处理
     *
     * @param action 具体操作
     * @param <T>    响应结果类型
     * @return 响应结果
     */
    private <T> RestBean<T> messageHandle(Supplier<String> action) {
        String message = action.get();
        if (message == null)
            return RestBean.success();
        else
            return RestBean.failure(400, message);
    }

    @GetMapping("/tenant/getTenantList")
    public RespBean<List<CkTenantUnauthResp>> getTenantList() {
        try {
            List<CkTenantUnauthResp> result = tenantFacade.getTenantList();
            return RespBean.success(result);
        } catch (Exception e) {
            log.error("UnauthController#getTenantList,req:{}", e);
            return RespBean.failure(999, "系统异常，请联系管理员");
        }
    }
}
