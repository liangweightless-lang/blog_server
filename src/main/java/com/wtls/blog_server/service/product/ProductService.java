package com.wtls.blog_server.service.product;

import cn.hutool.core.util.ObjUtil;
import com.wtls.blog_server.entity.product.Product;
import com.wtls.blog_server.mapper.product.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    public List<Product> getAllProducts() {
        return productMapper.findAll(null);
    }

    public List<Product> getProductsByStatus(Integer status) {
        return productMapper.findAll(status);
    }

    public Product getProductById(Long id) {
        return productMapper.selectById(id);
    }

    public void createProduct(Product product) {
        product.setStatus(ObjUtil.defaultIfNull(product.getStatus(), 1)); // 默认上架
        product.setDeliveryFee(ObjUtil.defaultIfNull(product.getDeliveryFee(), BigDecimal.ZERO));
        product.setIsTop(ObjUtil.defaultIfNull(product.getIsTop(), 0));
        productMapper.insert(product);
    }

    public void updateProduct(Product product) {
        productMapper.updateById(product);
    }

    public void updateProductStatus(Long id, Integer status) {
        productMapper.updateStatus(id, status);
    }

    public void updateProductTop(Long id, Integer isTop) {
        productMapper.updateTop(id, isTop);
    }

    public void deleteProduct(Long id) {
        productMapper.deleteById(id);
    }
}
