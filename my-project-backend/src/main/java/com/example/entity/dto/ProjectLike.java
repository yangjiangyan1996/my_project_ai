package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("project_like")
@AllArgsConstructor
@NoArgsConstructor
public class ProjectLike extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long projectId;
}