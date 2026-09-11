package com.wtls.blog_server.service.product;

import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wtls.blog_server.entity.product.CampaignProduct;
import com.wtls.blog_server.entity.product.Product;
import com.wtls.blog_server.entity.product.ProductOrder;
import com.wtls.blog_server.exception.BusinessException;
import com.wtls.blog_server.mapper.product.CampaignProductMapper;
import com.wtls.blog_server.mapper.product.ProductMapper;
import com.wtls.blog_server.mapper.product.ProductOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductOrderMapper productOrderMapper;

    @Autowired
    private CampaignProductMapper campaignProductMapper;

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
        // 1. 校验普通订单是否存在
        QueryWrapper<ProductOrder> orderQuery = new QueryWrapper<>();
        orderQuery.eq("product_id", id);
        long orderCount = productOrderMapper.selectCount(orderQuery);
        if (orderCount > 0) {
            throw new BusinessException("该商品已产生过 " + orderCount + " 笔商城订单，为保护用户交易记录，不支持彻底删除！请使用【下架】功能。");
        }

        // 2. 校验是否被快团活动关联
        QueryWrapper<CampaignProduct> campaignQuery = new QueryWrapper<>();
        campaignQuery.eq("product_id", id);
        long campaignCount = campaignProductMapper.selectCount(campaignQuery);
        if (campaignCount > 0) {
            throw new BusinessException("该商品当前已被快团活动关联，无法直接删除！请先在快团活动中移除该商品，或使用【下架】功能。");
        }

        productMapper.deleteById(id);
    }
}

