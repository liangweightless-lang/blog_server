package com.wtls.blog_server.service;

import com.wtls.blog_server.entity.Product;
import com.wtls.blog_server.entity.ProductOrder;
import com.wtls.blog_server.entity.User;
import com.wtls.blog_server.mapper.ProductMapper;
import com.wtls.blog_server.mapper.ProductOrderMapper;
import com.wtls.blog_server.mapper.UserMapper;
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
    public ProductOrder createOrder(Long userId, Long productId) {
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new RuntimeException("Product not found");
        }

        ProductOrder order = new ProductOrder();
        order.setId(UUID.randomUUID().toString().replace("-", ""));
        order.setUserId(userId);
        order.setProductId(productId);
        order.setAmount(product.getPrice());
        order.setStatus(0); // Pending

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
}
