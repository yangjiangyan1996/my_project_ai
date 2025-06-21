package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("project_favorite")
public class ProjectFavorite extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long projectId;
}