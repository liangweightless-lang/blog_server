package com.wtls.blog_server.service;

import com.wtls.blog_server.entity.Product;
import com.wtls.blog_server.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    public List<Product> getAllProducts() {
        return productMapper.findAll();
    }

    public Product getProductById(Long id) {
        return productMapper.findById(id);
    }
}
