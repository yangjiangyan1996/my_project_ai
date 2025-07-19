package com.example.controller;

import com.example.entity.base.RespBean;
import com.example.entity.resp.EnumResp;
import com.example.enums.CommonEnum;
import com.example.enums.ProjectEnum;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RequestMapping("/api/auth/common/")
public class CommonController {

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

    @GetMapping("/industrys")
    public RespBean<List<EnumResp>> getIndustrys() {
        List<EnumResp> collect = Arrays.stream(CommonEnum.IndustryEnum.values()).map(v -> {
                    EnumResp r = new EnumResp();
                    r.setCode(v.getCode());
                    r.setDesc(v.getName());
                    return r;
                }).sorted(Comparator.comparingInt(EnumResp::getCode))
                .collect(Collectors.toList());
        return RespBean.success(collect);
    }


    @GetMapping("/getLabels")
    public RespBean<List<EnumResp>> getLabels() {
        List<EnumResp> collect = Arrays.stream(CommonEnum.LabelEnums.values()).map(v -> {
                    EnumResp r = new EnumResp();
                    r.setCode(v.getCode());
                    r.setDesc(v.getName());
                    return r;
                }).sorted(Comparator.comparingInt(EnumResp::getCode))
                .collect(Collectors.toList());
        return RespBean.success(collect);
    }

    @GetMapping("/getUserType")
    public RespBean<List<EnumResp>> getUserType() {
        List<EnumResp> collect = Arrays.stream(CommonEnum.UserTypeEnum.values()).map(v -> {
                    EnumResp r = new EnumResp();
                    r.setCode(v.getCode());
                    r.setDesc(v.getName());
                    return r;
                }).sorted(Comparator.comparingInt(EnumResp::getCode))
                .collect(Collectors.toList());
        return RespBean.success(collect);
    }

    @GetMapping("/category")
    public RespBean<List<EnumResp>> getAllCategories() {
        try {
            CommonEnum.IndustryCategory[] values = CommonEnum.IndustryCategory.values();
            List<EnumResp> collect = Arrays.stream(values).map(v -> {
                        EnumResp r = new EnumResp();
                        r.setCode(v.getCode());
                        r.setDesc(v.getName());
                        if (v.getSubCategories() != null && v.getSubCategories().length > 0) {
                            CommonEnum.IndustryCategory.IndustrySubCategory[] sub = v.getSubCategories();
                            List<EnumResp> subList = Arrays.stream(sub).map(s -> {
                                EnumResp z = new EnumResp();
                                z.setCode(s.getCode());
                                z.setDesc(s.getName());
                                return z;
                            }).collect(Collectors.toList());
                            r.setSubs(subList);

                        }
                        return r;
                    }).sorted(Comparator.comparingInt(EnumResp::getCode))
                    .collect(Collectors.toList());
            return RespBean.success(collect);
        } catch (ValidationException e) {
            log.error("CommonController#getAllCategories,", e);
            return RespBean.failure(999, e.getMessage());
        } catch (Exception e) {
            log.error("CommonController#getAllCategories, error", e);
            return RespBean.failure(999, e.getMessage());
        }

    }


}
