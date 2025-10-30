// ProductCategory.java
package com.example.entity.cangku.resp;

import lombok.Data;

import java.util.List;

/**
 * 产品分类实体类
 * 使用MyBatis-Plus注解进行数据库映射
 */

@Data
public class ProductCategoryResp  {
    /**
     * 主键ID，自增类型
     */
    private Long id;
    /**
     * 分类编码
     */
    private String categoryCode;
    /**
     * 分类名称
     */
    private String categoryName;
    /**
     * 父级分类ID
     */
    private String parentCode;
    /**
     * 分类层级
     */
    private Integer level;
    /**
     * 排序序号
     */
    private Integer sortOrder;
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    List<ProductCategoryResp> children;
}