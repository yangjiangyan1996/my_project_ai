
package com.example.entity.cangku.resp;

import lombok.Data;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/10/30 22:31
 */
@Data
public class UnitPageListResp {
    private Long id;
    private Long tenantId;
    private String unitCode;
    private String unitName;
    private String remark;
    private Integer status;

    private java.util.Date createdAt;
    private java.util.Date modifiedAt;
}
