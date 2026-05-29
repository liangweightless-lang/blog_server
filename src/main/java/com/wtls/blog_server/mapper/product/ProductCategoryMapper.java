package com.wtls.blog_server.mapper.product;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wtls.blog_server.entity.product.ProductCategory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {
}
