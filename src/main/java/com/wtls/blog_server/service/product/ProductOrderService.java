package com.wtls.blog_server.service.product;

import com.wtls.blog_server.entity.product.Product;
import com.wtls.blog_server.entity.product.ProductOrder;
import com.wtls.blog_server.entity.user.User;
import com.wtls.blog_server.mapper.product.ProductMapper;
import com.wtls.blog_server.mapper.product.ProductOrderMapper;
import com.wtls.blog_server.mapper.user.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.hutool.core.util.IdUtil;
import java.util.UUID;

@Service
public class ProductOrderService {

    @Autowired
    private ProductOrderMapper orderMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Transactional
    public ProductOrder createOrder(Long userId, Long productId, String address, String type, Integer pointsToUse, String spec) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("Product not found");
        }
        
        // 校验库存
        if (product.getStock() != null && product.getStock() <= 0) {
            throw new RuntimeException("商品库存不足");
        }

        java.math.BigDecimal originalAmount = "GROUP".equals(type) && product.getGroupPrice() != null ? product.getGroupPrice() : product.getPrice();
        java.math.BigDecimal deduction = java.math.BigDecimal.ZERO;
        
        int actualPointsToUse = 0;
        if (pointsToUse != null && pointsToUse > 0) {
            User user = userMapper.findById(userId);
            if (user.getPoints() < pointsToUse) {
                throw new RuntimeException("积分余额不足");
            }
            
            // 100 points = 1 Yuan
            deduction = new java.math.BigDecimal(pointsToUse).divide(new java.math.BigDecimal(100), 2, java.math.RoundingMode.HALF_UP);
            
            // Ensure deduction doesn't exceed amount (at least 0.01 left?)
            if (deduction.compareTo(originalAmount) >= 0) {
                deduction = originalAmount.subtract(new java.math.BigDecimal("0.01"));
                actualPointsToUse = deduction.multiply(new java.math.BigDecimal(100)).intValue();
            } else {
                actualPointsToUse = pointsToUse;
            }
        }

        ProductOrder order = new ProductOrder();
        order.setId(IdUtil.fastSimpleUUID());
        order.setUserId(userId);
        order.setProductId(productId);
        order.setAmount(originalAmount.subtract(deduction));
        order.setPointsUsed(actualPointsToUse);
        order.setStatus(0); // Pending
        order.setShippingAddress(address);
        order.setOrderType(type);
        order.setSelectedSpec(spec);

        if (actualPointsToUse > 0) {
            userMapper.deductPoints(userId, actualPointsToUse);
        }

        orderMapper.insert(order);
        return order;
    }

    @Transactional
    public ProductOrder mockPay(String orderId) {
        ProductOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 0) {
            throw new RuntimeException("Invalid order or already paid");
        }

        // 扣减库存
        int rows = productMapper.reduceStock(order.getProductId(), 1);
        if (rows == 0) {
            throw new RuntimeException("支付失败：商品库存不足");
        }

        // Update order status
        orderMapper.updateStatus(orderId, 1);

        // Handle points and referral reward
        User buyer = userMapper.findById(order.getUserId());
        if (buyer.getInvitedBy() != null) {
            // Reward the inviter with 100 points
            userMapper.addPoints(buyer.getInvitedBy(), 100);
        }

        return orderMapper.selectById(orderId);
    }
    @Transactional
    public ProductOrder redeemWithPoints(Long userId, Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("Product not found");
        }

        if (product.getStock() != null && product.getStock() <= 0) {
            throw new RuntimeException("商品库存不足，无法兑换");
        }

        // 1000 points for a free item
        int pointsNeeded = 1000;
        int rows = userMapper.deductPoints(userId, pointsNeeded);
        if (rows == 0) {
            throw new RuntimeException("积分不足，兑换失败 (需1000积分)");
        }
        
        // 扣减库存
        productMapper.reduceStock(productId, 1);

        ProductOrder order = new ProductOrder();
        order.setId("POINTS_" + IdUtil.fastSimpleUUID().substring(0, 10));
        order.setUserId(userId);
        order.setProductId(productId);
        order.setAmount(java.math.BigDecimal.ZERO);
        order.setStatus(1); // Auto paid
        order.setPayTime(java.time.LocalDateTime.now());
        order.setOrderType("INDIVIDUAL");

        orderMapper.insert(order);
        return order;
    }

    public java.util.List<ProductOrder> getAllOrders() {
        return orderMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductOrder>()
                .orderByDesc(ProductOrder::getCreateTime)
        );
    }

    public java.util.List<ProductOrder> getUserOrders(Long userId) {
        return orderMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductOrder>()
                .eq(ProductOrder::getUserId, userId)
                .orderByDesc(ProductOrder::getCreateTime)
        );
    }

    public void shipOrder(String orderId) {
        orderMapper.updateStatus(orderId, 2); // 2: Shipped
    }
}
