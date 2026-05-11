package com.wtls.blog_server.controller.product;

import com.wtls.blog_server.entity.product.ProductOrder;
import com.wtls.blog_server.service.product.ProductOrderService;
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
        public String address;
        public String type; // INDIVIDUAL, GROUP, etc.
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
            ProductOrder order = orderService.createOrder(userId, req.productId, req.address, req.type == null ? "INDIVIDUAL" : req.type);
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

    @GetMapping("/me")
    public ResponseEntity<?> getMyOrders(@RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = getUserIdFromToken(authHeader);
            return ResponseEntity.ok(orderService.getUserOrders(userId));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllOrders(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Claims claims = JwtUtils.parseToken(token);
            String role = claims.get("role", String.class);
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(403).body(Map.of("error", "Access Denied"));
            }
            return ResponseEntity.ok(orderService.getAllOrders());
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{orderId}/ship")
    public ResponseEntity<?> shipOrder(@RequestHeader("Authorization") String authHeader, @PathVariable String orderId) {
        try {
            String token = authHeader.substring(7);
            Claims claims = JwtUtils.parseToken(token);
            if (!"ADMIN".equals(claims.get("role", String.class))) {
                return ResponseEntity.status(403).body(Map.of("error", "Access Denied"));
            }
            orderService.shipOrder(orderId);
            return ResponseEntity.ok(Map.of("message", "Order shipped"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
