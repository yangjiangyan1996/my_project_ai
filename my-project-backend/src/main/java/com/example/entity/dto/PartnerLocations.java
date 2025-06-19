package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/6/18 22:27
 */
@Data
@TableName("partner_locations")
public class PartnerLocations extends BaseModel{
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Double latitude;
    private Double longitude;
    private String skills;
    private String description;
}
