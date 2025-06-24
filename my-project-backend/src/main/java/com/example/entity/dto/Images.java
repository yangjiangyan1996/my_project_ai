package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/24 21:10 存储图片，后续使用图片服务器
 */
@Data
@TableName("forum_posts")
public class Images extends BaseModel{
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String url;
    private String img;
}
