package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.base.RespBean;
import com.example.entity.dto.Projects;
import com.example.entity.req.ProjectListReq;
import com.example.entity.req.TaohuaRequest;
import com.example.entity.req.TargetPerson;
import com.example.entity.resp.SuanTaoHuaVO;
import com.example.entity.resp.TargetVO;
import com.example.service.ProjectService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth/project/")
public class FuyeController {

    @Resource
    private ProjectService projectService;

    @PostMapping("/show")
    public RespBean<Page<Projects>> showHotFuye(@RequestBody ProjectListReq req) {
        Page hotFuyeProjects = projectService.getHotFuyeProjects(Page.of(req.getPage() - 1, req.getSize()));
        return RespBean.success(hotFuyeProjects);
    }
}