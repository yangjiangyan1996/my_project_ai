// ProductCategory.java
package com.example.entity.cangku.resp;

import lombok.Data;

/**
 * 产品分类实体类
 * 使用MyBatis-Plus注解进行数据库映射
 */

@Data
public class UnitResp {
    /**
     * 主键ID，自增类型
     */
    private Long id;
    /**
     * 分类编码
     */
    private String unitCode;
    /**
     * 分类名称
     */
    private String unitName;

}