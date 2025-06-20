package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.base.RespBean;
import com.example.entity.dto.Projects;
import com.example.entity.req.ProjectListReq;
import com.example.entity.req.TaohuaRequest;
import com.example.entity.req.TargetPerson;
import com.example.entity.resp.EnumResp;
import com.example.entity.resp.SuanTaoHuaVO;
import com.example.entity.resp.TargetVO;
import com.example.enums.ProjectEnum;
import com.example.service.ProjectService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/auth/project/")
public class FuyeController {


    @Resource
    private ProjectService projectService;

    @GetMapping("/category")
    public RespBean<List<EnumResp>> getAllCategories() {
        List<EnumResp> collect = Arrays.stream(ProjectEnum.ProjectCategoryEnum.values()).map(v -> {
                    EnumResp r = new EnumResp();
                    r.setCode(v.getCode());
                    r.setDesc(v.getName());
                    return r;
                }).sorted(Comparator.comparingInt(EnumResp::getCode))
                .collect(Collectors.toList());
        return RespBean.success(collect);
    }

    @GetMapping("/difficulty")
    public RespBean<List<EnumResp>> getAllDifficulties() {
        List<EnumResp> collect = Arrays.stream(ProjectEnum.ProjectDifficultyEnum.values()).map(v -> {
                    EnumResp r = new EnumResp();
                    r.setCode(v.getCode());
                    r.setDesc(v.getName());
                    return r;
                }).sorted(Comparator.comparingInt(EnumResp::getCode))
                .collect(Collectors.toList());
        return RespBean.success(collect);
    }

    @PostMapping("/show")
    public RespBean<Page<Projects>> showHotFuye(@RequestBody ProjectListReq req) {
        Page hotFuyeProjects = projectService.getHotFuyeProjects(Page.of(req.getPage() - 1, req.getSize()));
        return RespBean.success(hotFuyeProjects);
    }
}