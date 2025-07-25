package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("quan_bar_tie")
public class QuanBarTie extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private String avatar;
    private Long barId;
    private Integer status;
}