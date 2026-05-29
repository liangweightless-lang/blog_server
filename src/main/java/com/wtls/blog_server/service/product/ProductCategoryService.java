package com.wtls.blog_server.service.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.wtls.blog_server.entity.product.Product;
import com.wtls.blog_server.entity.product.ProductCategory;
import com.wtls.blog_server.mapper.product.ProductCategoryMapper;
import com.wtls.blog_server.mapper.product.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductCategoryService {

    @Autowired
    private ProductCategoryMapper categoryMapper;

    @Autowired
    private ProductMapper productMapper;

    public List<ProductCategory> getAllCategories() {
        QueryWrapper<ProductCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sort_order").orderByAsc("id");
        return categoryMapper.selectList(queryWrapper);
    }

    public void createCategory(ProductCategory category) {
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        categoryMapper.insert(category);
    }

    public void updateCategory(ProductCategory category) {
        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.updateById(category);
    }

    public void deleteCategory(Long id) {
        // 先将该分类下的商品分类设为null（未分类）
        UpdateWrapper<Product> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("category_id", id).set("category_id", null);
        productMapper.update(null, updateWrapper);
        
        // 再删除分类
        categoryMapper.deleteById(id);
    }

    public void updateSortOrders(List<ProductCategory> categories) {
        for (int i = 0; i < categories.size(); i++) {
            ProductCategory cat = categories.get(i);
            cat.setSortOrder(i);
            cat.setUpdateTime(LocalDateTime.now());
            categoryMapper.updateById(cat);
        }
    }
}
