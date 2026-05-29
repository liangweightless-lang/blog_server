package com.wtls.blog_server.controller.product;

import com.wtls.blog_server.common.Result;
import com.wtls.blog_server.entity.product.CampaignOrder;
import com.wtls.blog_server.entity.product.GroupBuyCampaign;
import com.wtls.blog_server.exception.UnauthorizedException;
import com.wtls.blog_server.service.product.GroupBuyCampaignService;
import com.wtls.blog_server.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
@CrossOrigin(origins = "*")
@Tag(name = "快团团模式社区团购", description = "管理团长发起的社区团购活动与订单")
public class GroupBuyCampaignController {

    @Autowired
    private GroupBuyCampaignService service;

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

    private Long getUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("未授权访问");
        }
        Claims claims = JwtUtils.parseToken(authHeader.substring(7));
        return claims.get("userId", Long.class);
    }

    // --- Campaign API ---

    @GetMapping
    @Operation(summary = "获取所有团购活动")
    public Result<List<GroupBuyCampaign>> getAll() {
        return Result.success(service.getAllCampaigns());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取单个团购活动详情")
    public Result<GroupBuyCampaign> getById(@PathVariable Long id) {
        return Result.success(service.getCampaignById(id));
    }

    @PostMapping
    @Operation(summary = "发起新团购 (Admin)")
    public Result<String> create(@RequestHeader("Authorization") String authHeader, @RequestBody GroupBuyCampaign campaign) {
        checkAdmin(authHeader);
        service.createCampaign(campaign);
        return Result.success("Campaign created");
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新团购活动 (Admin)")
    public Result<String> update(@RequestHeader("Authorization") String authHeader, @PathVariable Long id, @RequestBody GroupBuyCampaign campaign) {
        checkAdmin(authHeader);
        campaign.setId(id);
        service.updateCampaign(campaign);
        return Result.success("Campaign updated");
    }
    
    @PutMapping("/{id}/status")
    @Operation(summary = "更新团购状态 (Admin)")
    public Result<String> updateStatus(@RequestHeader("Authorization") String authHeader, @PathVariable Long id, @RequestParam Integer status) {
        checkAdmin(authHeader);
        service.updateCampaignStatus(id, status);
        return Result.success("Status updated");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除团购活动 (Admin)")
    public Result<String> delete(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        checkAdmin(authHeader);
        service.deleteCampaign(id);
        return Result.success("Campaign deleted");
    }

    // --- Order API ---

    @PostMapping("/{campaignId}/orders")
    @Operation(summary = "用户跟团下单")
    public Result<CampaignOrder> createOrder(@RequestHeader("Authorization") String authHeader, @PathVariable Long campaignId, @RequestBody CampaignOrder order) {
        order.setUserId(getUserIdFromToken(authHeader));
        order.setCampaignId(campaignId);
        return Result.success(service.createOrder(order));
    }

    @GetMapping("/{campaignId}/orders")
    @Operation(summary = "获取团购活动下的所有订单 (Admin)")
    public Result<List<CampaignOrder>> getOrdersByCampaign(@RequestHeader("Authorization") String authHeader, @PathVariable Long campaignId) {
        checkAdmin(authHeader);
        return Result.success(service.getOrdersByCampaignId(campaignId));
    }

    @GetMapping("/orders/my")
    @Operation(summary = "获取我参与的团购订单")
    public Result<List<CampaignOrder>> getMyOrders(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        return Result.success(service.getMyOrders(userId));
    }

    @PutMapping("/orders/{orderId}/status")
    @Operation(summary = "更新订单状态/核销 (Admin)")
    public Result<String> updateOrderStatus(@RequestHeader("Authorization") String authHeader, @PathVariable String orderId, @RequestParam Integer status) {
        checkAdmin(authHeader);
        service.updateOrderStatus(orderId, status);
        return Result.success("Order status updated");
    }
}
