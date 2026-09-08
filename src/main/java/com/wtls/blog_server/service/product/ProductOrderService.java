package com.wtls.blog_server.service.product;

import com.wtls.blog_server.entity.product.Product;
import com.wtls.blog_server.entity.product.ProductOrder;
import com.wtls.blog_server.entity.user.User;
import com.wtls.blog_server.mapper.product.ProductMapper;
import com.wtls.blog_server.mapper.product.ProductOrderMapper;
import com.wtls.blog_server.mapper.user.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wtls.blog_server.exception.BusinessException;
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
    public ProductOrder createOrder(Long userId, Long productId, String address, String type, Integer pointsToUse, String spec, Integer quantity, String contactPhone, String remark) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("Product not found");
        }
        
        int buyCount = (quantity != null && quantity > 0) ? quantity : 1;

        // 校验库存 (stock == -1 表示不限量)
        if (product.getStock() != null && product.getStock() != -1 && product.getStock() < buyCount) {
            throw new BusinessException("商品库存不足，剩余库存: " + product.getStock());
        }

        java.math.BigDecimal unitPrice = "GROUP".equals(type) && product.getGroupPrice() != null ? product.getGroupPrice() : product.getPrice();
        java.math.BigDecimal deliveryFee = product.getDeliveryFee() != null ? product.getDeliveryFee() : java.math.BigDecimal.ZERO;
        
        // 总金额 = 单价 * 数量 + 配送费
        java.math.BigDecimal originalAmount = unitPrice.multiply(new java.math.BigDecimal(buyCount)).add(deliveryFee);
        java.math.BigDecimal deduction = java.math.BigDecimal.ZERO;
        
        int actualPointsToUse = 0;
        if (pointsToUse != null && pointsToUse > 0) {
            User user = userMapper.findById(userId);
            if (user.getPoints() < pointsToUse) {
                throw new BusinessException("积分余额不足");
            }
            
            // 100 points = 1 Yuan
            deduction = new java.math.BigDecimal(pointsToUse).divide(new java.math.BigDecimal(100), 2, java.math.RoundingMode.HALF_UP);
            
            // Ensure deduction doesn't exceed amount (at least 0.01 left)
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
        order.setQuantity(buyCount);
        order.setAmount(originalAmount.subtract(deduction));
        order.setPointsUsed(actualPointsToUse);
        order.setStatus(0); // Pending
        order.setShippingAddress(address);
        order.setContactPhone(contactPhone);
        order.setRemark(remark);
        order.setDeliveryFee(deliveryFee);
        order.setOrderType(type);
        order.setSelectedSpec(spec);

        if (actualPointsToUse > 0) {
            userMapper.deductPoints(userId, actualPointsToUse);
        }

        orderMapper.insert(order);
        return order;
    }

    // 重载方法兼容老接口
    public ProductOrder createOrder(Long userId, Long productId, String address, String type, Integer pointsToUse, String spec) {
        return createOrder(userId, productId, address, type, pointsToUse, spec, 1, null, null);
    }

    @Transactional
    public ProductOrder mockPay(String orderId) {
        ProductOrder order = handlePaymentSuccess(orderId);
        return order;
    }

    @Transactional
    public ProductOrder handlePaymentSuccess(String orderId) {
        ProductOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 0) {
            throw new BusinessException("Invalid order or already paid");
        }

        int count = order.getQuantity() != null && order.getQuantity() > 0 ? order.getQuantity() : 1;
        // 扣减库存
        int rows = productMapper.reduceStock(order.getProductId(), count);
        if (rows == 0) {
            throw new BusinessException("支付失败：商品库存不足");
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
            throw new BusinessException("Product not found");
        }

        if (product.getStock() != null && product.getStock() <= 0) {
            throw new BusinessException("商品库存不足，无法兑换");
        }

        // 1000 points for a free item
        int pointsNeeded = 1000;
        int rows = userMapper.deductPoints(userId, pointsNeeded);
        if (rows == 0) {
            throw new BusinessException("积分不足，兑换失败 (需1000积分)");
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

    @Transactional
    public void cancelUnpaidOrder(Long userId, String orderId) {
        ProductOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("只能删除自己的订单");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("只能删除未支付的订单");
        }
        // 如果使用了积分，安全归还积分
        if (order.getPointsUsed() != null && order.getPointsUsed() > 0) {
            userMapper.addPoints(userId, order.getPointsUsed());
        }
        orderMapper.deleteById(orderId);
    }
}
