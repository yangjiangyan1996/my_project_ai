package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/28 16:30
 */
@TableName("redis")
@Data
public class RedisDTO extends BaseModel{
    @TableId(type = IdType.AUTO)
    private Long id;
    private String k;
    private String v;
    private Date e;
}
