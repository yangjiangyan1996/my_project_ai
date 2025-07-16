package com.example.entity.req;

import com.example.entity.base.PageReq;
import lombok.Data;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/7/16 10:26
 */
@Data
public class ShowHotProjectListPageReq extends PageReq {
    private List<Integer> categoryIds;
}
