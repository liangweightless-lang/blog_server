package com.wtls.blog_server.controller;

import com.wtls.blog_server.entity.ProductOrder;
import com.wtls.blog_server.service.ProductOrderService;
import com.wtls.blog_server.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class ProductOrderController {

    @Autowired
    private ProductOrderService orderService;

    public static class CreateOrderRequest {
        public Long productId;
    }

    private Long getUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Unauthorized");
        }
        String token = authHeader.substring(7);
        Claims claims = JwtUtils.parseToken(token);
        return claims.get("userId", Long.class);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestHeader("Authorization") String authHeader, @RequestBody CreateOrderRequest req) {
        try {
            Long userId = getUserIdFromToken(authHeader);
            ProductOrder order = orderService.createOrder(userId, req.productId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<?> payOrder(@RequestHeader("Authorization") String authHeader, @PathVariable String orderId) {
        try {
            getUserIdFromToken(authHeader); // Just to validate token
            ProductOrder order = orderService.mockPay(orderId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/redeem")
    public ResponseEntity<?> redeem(@RequestHeader("Authorization") String authHeader, @RequestBody CreateOrderRequest req) {
        try {
            Long userId = getUserIdFromToken(authHeader);
            ProductOrder order = orderService.redeemWithPoints(userId, req.productId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
