package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Images;
import com.example.entity.dto.UserFollow;
import com.example.mapper.ImagesMapper;
import com.example.mapper.UserFollowMapper;
import com.example.service.ImagesService;
import com.example.service.UserFollowService;
import org.springframework.stereotype.Service;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/24 21:11
 */
@Service
public class ImagesServiceImpl extends ServiceImpl<ImagesMapper, Images> implements ImagesService {
}
