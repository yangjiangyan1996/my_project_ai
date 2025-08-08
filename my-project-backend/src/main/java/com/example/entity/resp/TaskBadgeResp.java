package com.example.entity.resp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/7 19:27
 */
@Data
public class TaskBadgeResp{
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String iconUrl;
    private String colorCode;
    private Integer sortOrder;
}
