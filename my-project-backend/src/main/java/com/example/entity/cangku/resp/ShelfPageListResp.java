package com.example.entity.cangku.resp;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/30 01:41
 */
@Data
public class ShelfPageListResp {
    private Long id;
    //租户ID
    private Long tenantId;
    //顶层货架的名称
    private String sheliveName;
    //货架编码
    private String shelfCode;
    //货架名称
    private String shelfName;
    //仓库ID
    private Long warehouseId;
    //区域（A区、B区等）
    private String area;
    //排
    private String rowN;
    //列
    private String columnN;
    //层
    private String layer;
    //容量
    private double capacity;
    //可用容量
    private BigDecimal availableCapacity;
    //容量单位
    private String capacityUnit;
    //状态：0-禁用，1-启用
    private Integer status;
    //排序
    private Integer sortOrder;
    //备注
    private String remark;

    private String warehouseName;

    //区域信息
    private List<ShelfZoneVO> zones;


    @Data
    public static class  ShelfZoneVO {
        private Long id;
        private String zoneCode;
        private String zoneName;
        private String area;
        private String rowN;
        private String columnN;
        private String layer;
        private BigDecimal capacity;
        private String capacityUnit;
        private Integer sortOrder;
        private Integer status;
        private String remark;
        private Long parentId; // 所属货架ID
    }

}
