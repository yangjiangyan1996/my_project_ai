package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_follow")
public class UserFollow extends BaseModel{
    @TableId(type = IdType.AUTO)
    private Long id;
    //粉丝id
    private Long followerId;
    //主播ID
    private Long followeeId;
    private Integer isMutual;
    private String remark;
}