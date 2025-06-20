package com.example.controller;

import com.example.entity.base.RespBean;
import com.example.entity.resp.EnumResp;
import com.example.enums.ProjectEnum;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/20 14:31
 */
@RestController
@RequestMapping("/api/auth/common/")
public class CommonController {

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

}
