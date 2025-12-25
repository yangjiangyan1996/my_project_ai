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
 * @Date 2025/12/25 11:12
 */
//ck_stock_take_item
@Data
@TableName("ck_stock_take_item")
public class StockTaskItem  extends BaseModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    //租户ID
    private Long tenantId;
    //盘点单ID
    private Long stockTakeId;
    //盘点任务ID(关联任务分配)
    private Long taskId;
    //产品ID
    private Long productId;
    //产品SKU
    private String productSku;
    //产品名称
    private String productName;
    //仓库ID
    private Long warehouseId;
    //仓库编码
    private String warehouseCode;
    //货架ID
    private Long shelfId;
    //货架编码
    private String shelfCode;
    //库位编码
    private String locationCode;
    //批次号
    private String batchNo;
    //生产日期
    private Date productionDate;
    //有效期至
    private Date expiryDate;
    //库存类型:1-普通库存,2-批次库存,3-货架库存
    private Integer inventoryType;
    //盘点方式:1-人工盘点,2-RFID,3-视觉识别,4-自动化设备
    private Integer countMethod;
    //RFID标签号
    private String rfidTag;
    //系统库存数量
    private BigDecimal systemQuantity;
    //系统锁定数量
    private BigDecimal systemLockedQuantity;
    //系统可用数量
    private BigDecimal systemAvailableQuantity;
    //系统单价
    private BigDecimal systemUnitPrice;
    //系统总金额
    private BigDecimal systemTotalAmount;
    //初盘数量
    private BigDecimal firstCountQuantity;
    //初盘人ID
    private Long firstCounterId;
    //初盘人姓名
    private String firstCounterName;
    //初盘时间
    private Date firstCountTime;
    //初盘设备
    private String firstCountDevice;
    //初盘备注
    private String firstCountRemark;
    //复盘数量
    private BigDecimal secondCountQuantity;
    //复盘人ID
    private Long secondCounterId;
    //复盘人姓名
    private String secondCounterName;
    //复盘时间
    private Date secondCountTime;
    //复盘设备
    private String secondCountDevice;
    //复盘备注
    private String secondCountRemark;
    //最终确认数量
    private BigDecimal finalCountQuantity;
    //最终确认人ID
    private Long finalCounterId;
    //最终确认人姓名
    private String finalCounterName;
    //最终确认时间
    private Date finalCountTime;
    //数量差异(实际-系统)
    private BigDecimal quantityDifference;
    //差异金额
    private BigDecimal differenceAmount;
    //差异率
    private BigDecimal differenceRate;
    //差异等级:0-无差异,1-微小差异,2-一般差异,3-重大差异
    private Integer differenceLevel;
    //差异原因:1-录入错误,2-漏盘,3-偷盗丢失,4-损坏未报,5-自然损耗,6-系统错误,7-多盘重复,8-单位换算错误,9-盘点时在途,99-其他
    private Integer differenceReason;
    //差异说明
    private String differenceRemark;
    //是否已调整:0-否,1-是
    private Boolean isAdjusted;
    //调整数量
    private BigDecimal adjustQuantity;
    //调整单明细ID
    private Long adjustOrderItemId;
    //调整备注
    private String adjustRemark;
    //调整时间
    private Date adjustTime;
    //盘点状态:0-待盘点,1-已初盘,2-已复盘,3-已确认,4-差异待处理,5-已调整
    private Integer countStatus;
    //审核状态:0-未审核,1-审核通过,2-审核驳回
    private Integer verifyStatus;
}
