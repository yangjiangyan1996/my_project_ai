package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("project_favorite")
@AllArgsConstructor
@NoArgsConstructor
public class ProjectFavorite extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long projectId;

    public ProjectFavorite(Long id, Long userId, Long projectId) {
        this.id = id;
        this.userId = userId;
        this.projectId = projectId;
    }

    //查询sql的字段
    @TableField(exist = false)
    private Integer favoriteCount;
}