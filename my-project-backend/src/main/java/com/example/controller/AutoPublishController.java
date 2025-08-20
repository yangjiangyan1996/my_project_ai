package com.example.controller;

import com.example.config.AsyncTaskUtil;
import com.example.entity.RestBean;
import com.example.entity.base.ProjectPreviewDTO;
import com.example.service.AutoProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "自动发布管理")
@RestController
@RequestMapping("/api/unauth/auto")
public class AutoPublishController {

    @Resource
    AutoProjectService autoProjectService;

    @Operation(summary = "手动触发自动发布项目")
    @GetMapping("/publish")
    public RestBean<Long> autoPublishProject() {
        AsyncTaskUtil.execute(() -> autoProjectService.autoPublishProject());
        return RestBean.success();
    }
}