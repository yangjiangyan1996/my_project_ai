package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.Projects;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/18 22:54
 */
public interface ProjectService extends IService<Projects>, UserDetailsService {
    Page getHotFuyeProjects(Page<Projects> pageable);
}
