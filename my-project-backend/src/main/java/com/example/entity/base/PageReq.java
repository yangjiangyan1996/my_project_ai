package com.example.entity.base;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/19 14:27
 */
@Data
public class PageReq {
    int page;
    int size;
}
