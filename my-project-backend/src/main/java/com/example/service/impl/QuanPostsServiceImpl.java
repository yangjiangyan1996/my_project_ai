package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.QuanPosts;
import com.example.mapper.QuanPostsMapper;
import com.example.service.QuanPostsService;
import org.springframework.stereotype.Service;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/24 15:55
 */
@Service
public class QuanPostsServiceImpl  extends ServiceImpl<QuanPostsMapper, QuanPosts> implements QuanPostsService {
}
