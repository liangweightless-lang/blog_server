package com.wtls.blog_server.service.product;

import com.wtls.blog_server.entity.product.Product;
import com.wtls.blog_server.entity.product.ProductOrder;
import com.wtls.blog_server.entity.user.User;
import com.wtls.blog_server.mapper.product.ProductMapper;
import com.wtls.blog_server.mapper.product.ProductOrderMapper;
import com.wtls.blog_server.mapper.user.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public ProductOrder createOrder(Long userId, Long productId, String address, String type) {
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new RuntimeException("Product not found");
        }

        ProductOrder order = new ProductOrder();
        order.setId(UUID.randomUUID().toString().replace("-", ""));
        order.setUserId(userId);
        order.setProductId(productId);
        // Use group price if it's a group order
        order.setAmount("GROUP".equals(type) && product.getGroupPrice() != null ? product.getGroupPrice() : product.getPrice());
        order.setStatus(0); // Pending
        order.setShippingAddress(address);
        order.setOrderType(type);

        orderMapper.insert(order);
        return order;
    }

    @Transactional
    public ProductOrder mockPay(String orderId) {
        ProductOrder order = orderMapper.findById(orderId);
        if (order == null || order.getStatus() != 0) {
            throw new RuntimeException("Invalid order or already paid");
        }

        // Update order status
        orderMapper.updateStatus(orderId, 1);

        // Handle points and referral reward
        User buyer = userMapper.findById(order.getUserId());
        if (buyer.getInvitedBy() != null) {
            // Reward the inviter with 100 points
            userMapper.addPoints(buyer.getInvitedBy(), 100);
        }

        return orderMapper.findById(orderId);
    }
    @Transactional
    public ProductOrder redeemWithPoints(Long userId, Long productId) {
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new RuntimeException("Product not found");
        }

        // 1000 points for a free item
        int pointsNeeded = 1000;
        int rows = userMapper.deductPoints(userId, pointsNeeded);
        if (rows == 0) {
            throw new RuntimeException("积分不足，兑换失败 (需1000积分)");
        }

        ProductOrder order = new ProductOrder();
        order.setId("POINTS_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10));
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
        return orderMapper.findAll();
    }

    public java.util.List<ProductOrder> getUserOrders(Long userId) {
        return orderMapper.findByUserId(userId);
    }

    public void shipOrder(String orderId) {
        orderMapper.updateStatus(orderId, 2); // 2: Shipped
    }
}
