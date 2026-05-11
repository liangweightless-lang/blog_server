package com.wtls.blog_server.service.product;

import com.wtls.blog_server.entity.product.Product;
import com.wtls.blog_server.mapper.product.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    public List<Product> getAllProducts() {
        return productMapper.selectList(null);
    }

    public Product getProductById(Long id) {
        return productMapper.selectById(id);
    }

    public void createProduct(Product product) {
        productMapper.insert(product);
    }

    public void updateProduct(Product product) {
        productMapper.updateById(product);
    }

    public void deleteProduct(Long id) {
        productMapper.deleteById(id);
    }
}
