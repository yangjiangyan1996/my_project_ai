package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("quan_bars")
public class QuanBars extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer status;
    private String name;
    private Integer categoryId;
    private String description;
    private String avatar;
    private String coverImage;
    private Integer followerCount;
    private Integer postCount;
}