// CkProductCategoryServiceImpl.java
package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.cangku.dto.ProductCategory;
import com.example.mapper.CkProductCategoryMapper;
import com.example.service.CkProductCategoryService;
import org.springframework.stereotype.Service;

@Service
public class CkProductCategoryServiceImpl extends ServiceImpl<CkProductCategoryMapper, ProductCategory> implements CkProductCategoryService {

}