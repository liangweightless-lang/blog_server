package com.wtls.blog_server.controller.product;

import com.wtls.blog_server.common.Result;
import com.wtls.blog_server.entity.product.Product;
import com.wtls.blog_server.entity.user.User;
import com.wtls.blog_server.service.product.ProductService;
import com.wtls.blog_server.service.user.UserService;
import com.wtls.blog_server.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
@Tag(name = "商品管理", description = "管理商城的实物与虚拟商品")
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
    @Operation(summary = "获取所有商品列表")
    public Result<List<Product>> getAll() {
        return Result.success(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取商品详情")
    public Result<Product> getById(@PathVariable Long id) {
        return Result.success(productService.getProductById(id));
    }

    @PostMapping
    @Operation(summary = "新增商品 (Admin)")
    public Result<String> create(@RequestHeader("Authorization") String authHeader, @RequestBody Product product) {
        checkAdmin(authHeader);
        productService.createProduct(product);
        return Result.success("Product created");
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新商品信息 (Admin)")
    public Result<String> update(@RequestHeader("Authorization") String authHeader, @PathVariable Long id, @RequestBody Product product) {
        checkAdmin(authHeader);
        product.setId(id);
        productService.updateProduct(product);
        return Result.success("Product updated");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除商品 (Admin)")
    public Result<String> delete(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        checkAdmin(authHeader);
        productService.deleteProduct(id);
        return Result.success("Product deleted");
    }
}
