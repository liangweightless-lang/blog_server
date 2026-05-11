package com.wtls.blog_server.controller.product;

import com.wtls.blog_server.entity.product.Product;
import com.wtls.blog_server.entity.user.User;
import com.wtls.blog_server.service.product.ProductService;
import com.wtls.blog_server.service.user.UserService;
import com.wtls.blog_server.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    private void checkAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Unauthorized");
        }
        Claims claims = JwtUtils.parseToken(authHeader.substring(7));
        Long userId = claims.get("userId", Long.class);
        User user = userService.getUserInfo(userId);
        if (user == null || !"ADMIN".equals(user.getRole())) {
            throw new RuntimeException("Admin access required");
        }
    }

    @GetMapping
    public List<Product> getAll() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestHeader("Authorization") String authHeader, @RequestBody Product product) {
        try {
            checkAdmin(authHeader);
            productService.createProduct(product);
            return ResponseEntity.ok(Map.of("message", "Product created"));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@RequestHeader("Authorization") String authHeader, @PathVariable Long id, @RequestBody Product product) {
        try {
            checkAdmin(authHeader);
            product.setId(id);
            productService.updateProduct(product);
            return ResponseEntity.ok(Map.of("message", "Product updated"));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        try {
            checkAdmin(authHeader);
            productService.deleteProduct(id);
            return ResponseEntity.ok(Map.of("message", "Product deleted"));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }
}
