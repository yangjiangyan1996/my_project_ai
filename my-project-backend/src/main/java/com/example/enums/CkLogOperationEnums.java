package com.example.enums;

import lombok.Getter;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/12/25 09:41
 */
public class CkLogOperationEnums {

    /**
     * {@link com.example.annotations.LogOperation}
     * 这个类的枚举
     */
    @Getter
    public enum CkLogOperationEnum {
        PRODUCT("产品管理", "/api/auth/product"),
        INBOUND("入库管理", "/api/auth/inbound"),
        OUTBOUND("出库管理", "/api/auth/outbound"),
        CUSTOMER("客户管理", "/api/auth/customer"),
        WAREHOUSE("仓库管理", "/api/auth/warehouse"),
        SUPPLIER("供应商管理", "/api/auth/supplier"),
        UNIT("单位管理", "/api/auth/unit"),
        RECOMMEND("推荐规则管理", "/api/auth/recommend"),
        SHELF("货架管理", "/api/auth/shelf"),
        SKU("sku管理", "/api/auth/sku"),
        USER("用户管理", "/api/auth/user"),
        TENANT("机构管理", "/api/auth/tenant");
        private String name;
        private String uri;
        CkLogOperationEnum(String name, String uri) {
            this.name = name;
            this.uri = uri;
        }
    }
}
