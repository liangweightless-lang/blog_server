package com.wtls.blog_server.controller.product;

import com.wtls.blog_server.common.Result;
import com.wtls.blog_server.entity.product.ProductCategory;
import com.wtls.blog_server.exception.UnauthorizedException;
import com.wtls.blog_server.service.product.ProductCategoryService;
import com.wtls.blog_server.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-categories")
@CrossOrigin(origins = "*")
@Tag(name = "商品分类管理", description = "管理商城的商品分类")
public class ProductCategoryController {

    @Autowired
    private ProductCategoryService categoryService;

    private void checkAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("未授权访问，请重新登录");
        }
        Claims claims = JwtUtils.parseToken(authHeader.substring(7));
        String role = claims.get("role", String.class);
        if (!"ADMIN".equals(role)) {
            throw new UnauthorizedException("权限不足，需要管理员权限");
        }
    }

    @GetMapping
    @Operation(summary = "获取所有商品分类")
    public Result<List<ProductCategory>> getAll() {
        return Result.success(categoryService.getAllCategories());
    }

    @PostMapping
    @Operation(summary = "新增商品分类 (Admin)")
    public Result<String> create(@RequestHeader("Authorization") String authHeader, @RequestBody ProductCategory category) {
        checkAdmin(authHeader);
        categoryService.createCategory(category);
        return Result.success("Category created");
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新商品分类 (Admin)")
    public Result<String> update(@RequestHeader("Authorization") String authHeader, @PathVariable Long id, @RequestBody ProductCategory category) {
        checkAdmin(authHeader);
        category.setId(id);
        categoryService.updateCategory(category);
        return Result.success("Category updated");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除商品分类 (Admin)")
    public Result<String> delete(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        checkAdmin(authHeader);
        categoryService.deleteCategory(id);
        return Result.success("Category deleted");
    }

    @PostMapping("/sort")
    @Operation(summary = "更新分类排序 (Admin)")
    public Result<String> updateSortOrders(@RequestHeader("Authorization") String authHeader, @RequestBody List<ProductCategory> categories) {
        checkAdmin(authHeader);
        categoryService.updateSortOrders(categories);
        return Result.success("Categories sorted");
    }
}
