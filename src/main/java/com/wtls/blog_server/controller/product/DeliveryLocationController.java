package com.wtls.blog_server.controller.product;

import com.wtls.blog_server.common.Result;
import com.wtls.blog_server.entity.product.DeliveryLocation;
import com.wtls.blog_server.exception.UnauthorizedException;
import com.wtls.blog_server.service.product.DeliveryLocationService;
import com.wtls.blog_server.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery-locations")
@CrossOrigin(origins = "*")
@Tag(name = "提货点管理", description = "管理团购的自提地址")
public class DeliveryLocationController {

    @Autowired
    private DeliveryLocationService service;

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
    @Operation(summary = "获取所有提货点 (管理员用)")
    public Result<List<DeliveryLocation>> getAll(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        checkAdmin(authHeader);
        return Result.success(service.getAllLocations());
    }
    
    @GetMapping("/active")
    @Operation(summary = "获取启用中的提货点")
    public Result<List<DeliveryLocation>> getActive() {
        return Result.success(service.getActiveLocations());
    }

    @PostMapping
    @Operation(summary = "新增提货点 (Admin)")
    public Result<String> create(@RequestHeader("Authorization") String authHeader, @RequestBody DeliveryLocation location) {
        checkAdmin(authHeader);
        service.createLocation(location);
        return Result.success("Location created");
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新提货点 (Admin)")
    public Result<String> update(@RequestHeader("Authorization") String authHeader, @PathVariable Long id, @RequestBody DeliveryLocation location) {
        checkAdmin(authHeader);
        location.setId(id);
        service.updateLocation(location);
        return Result.success("Location updated");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除提货点 (Admin)")
    public Result<String> delete(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        checkAdmin(authHeader);
        service.deleteLocation(id);
        return Result.success("Location deleted");
    }
}
