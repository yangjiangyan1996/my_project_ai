package com.example.entity.resp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

/**
 * @Author YangJian
 * @Description 副业项目详情实体
 * @Email 1776080295@qq.com
 * @Date 2025/6/18 22:55
 */
@Data
public class ProjectsDetailResp {
    private Long id;
    private String name;
    private Long createdBy;
    private Long secrecyId;
    private Long projectsId;
    private String imageUrl;
    private String steps;
    private String tools;
    private String timePerDay;
    private String incomeEstimate;
    private String targetAudience;
    private String riskWarning;
//    private Integer isRemote;
//    private Integer isFreeEntry;
    private String tags;
    private String creatorName;
    private Boolean myLike;
    private Boolean myFavorite;
    private Long likeCount;
    private Long favoriteCount;
    private Boolean followed;
    private Integer applyStatus;
    private Integer memberNum;
}