// ProductCategory.java
package com.example.entity.cangku.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.dto.BaseModel;
import lombok.Data;

/**
 * 产品分类实体类
 * 使用MyBatis-Plus注解进行数据库映射
 */

@Data
@TableName("ck_product_category")
public class ProductCategory extends BaseModel {
    /**
     * 主键ID，自增类型
     */

    @TableId(type = IdType.AUTO)
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

    private Long parentId;
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
    /**
     * 备注
     */

    private String remark;
}