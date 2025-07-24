package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("quan_posts")
public class QuanPosts  extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String title;
    private String content;
    private Integer barId;
    private Integer userId;
    private Integer status;
    private Integer viewCount;
    private Integer commentCount;
    private Integer likeCount;
}