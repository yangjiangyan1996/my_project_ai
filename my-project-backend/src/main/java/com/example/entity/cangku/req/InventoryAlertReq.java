package com.example.entity.cangku.req;

import com.example.entity.base.PageReq;
import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/27 21:30
 */
@Data
public class InventoryAlertReq  extends PageReq {
    Long warehouseId;
    Long userId;
    Long tenantId;

}
