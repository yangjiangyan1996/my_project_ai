package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("quan_tie_favorite")
@AllArgsConstructor
@NoArgsConstructor
public class QuanTieFavorite extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long tieId;

    public QuanTieFavorite(Long id, Long userId, Long tieId) {
        this.id = id;
        this.userId = userId;
        this.tieId = tieId;
    }

    //查询sql的字段
    @TableField(exist = false)
    private Integer count;
}