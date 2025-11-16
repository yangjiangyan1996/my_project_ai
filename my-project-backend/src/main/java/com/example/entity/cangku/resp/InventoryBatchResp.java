package com.example.entity.cangku.resp;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/11/7 00:43
 */
@Data
public class InventoryBatchResp {
    String batchNo;
    BigDecimal quantity;
    List<ShelfInfo> shelfList;

    @Data
    public static class ShelfInfo {
        Long shelfId;
        String shelfName;
        BigDecimal quantity;
    }
}
