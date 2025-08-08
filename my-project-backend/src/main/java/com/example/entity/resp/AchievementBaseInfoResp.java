package com.example.entity.resp;

import lombok.Data;

import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/6 19:17
 */
@Data
public class AchievementBaseInfoResp {
    //当前积分
    private Long points;
    //今日获得积分
    private Long todayPoints;
    //本月获得积分
    private Long monthPoints;
    //勋章
    private List<TaskBadgeResp> badges;
}
