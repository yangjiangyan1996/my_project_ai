package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/25 11:06
 */
//ck_stock_take
@Data
@TableName("ck_stock_take")
public class StockTask extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private String orderNo;
    //盘点任务名称
    private String takeName;
    //盘点方式:1-动态盘点(业务照常),2-静态盘点(停止出入库)
    private Integer takeType;
    //盘点策略:1-全库盘点,2-区域盘点,3-按产品分类,4-循环盘点,5-随机抽盘
    private Integer takeStrategy;
    //周期类型:1-日盘,2-周盘,3-月盘,4-季度盘
    private Integer cycleType;
    //仓库ID
    private Long warehouseId;
    //盘点区域编码(JSON数组)
    private String areaCodes;
    //审批状态:0-未提交,1-审核中,2-已批准,3-已驳回
    private Integer approvalStatus;
    //执行状态:0-未开始,1-进行中,2-已完成,3-已暂停,9-已取消
    private Integer executeStatus;
    //调整状态:0-未调整,1-调整中,2-已调整
    private Integer adjustStatus;
    //计划开始时间
    private Date planStartTime;
    //计划结束时间
    private Date planEndTime;
    //实际开始时间
    private Date actualStartTime;
    //实际结束时间
    private Date actualEndTime;
    //总盘点项数
    private Integer totalItems;
    //已完成项数
    private Integer completedItems;
    //准确项数(无差异)
    private Integer accuracyItems;
    //差异项数
    private Integer differItems;
    //系统总数量
    private BigDecimal totalSystemQuantity;
    //实际总数量
    private BigDecimal totalActualQuantity;
    //总差异数量
    private BigDecimal totalDifferenceQuantity;
    //总差异金额
    private BigDecimal totalDifferenceAmount;
    //允许差异率(%)
    private BigDecimal toleranceRate;
    //是否需要审批:0-否,1-是
    private Boolean requireApproval;
    //是否需要调整:0-否,1-是
    private Boolean requireAdjust;
    //库存调整单ID
    private Long adjustOrderId;
    //调整单号
    private String adjustOrderNo;
    //发起人ID
    private Long initiatorId;
    //审核人ID
    private Long auditorId;
    //审核时间
    private Date auditTime;
    //审核意见
    private String auditOpinion;
    //备注
    private String remark;
}
