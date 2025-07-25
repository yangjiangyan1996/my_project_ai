package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("quan_bars")
public class QuanBars extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer status;
    private String name;
    private Integer firstCategory;
    private Integer secondCategory;
    private String description;
    private String avatar;
    private Long followerCount;
    private Long postCount;
}