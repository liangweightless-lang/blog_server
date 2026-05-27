package com.wtls.blog_server.controller.product;

import com.wtls.blog_server.common.Result;
import com.wtls.blog_server.entity.product.GroupBuy;
import com.wtls.blog_server.service.product.GroupBuyService;
import com.wtls.blog_server.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.wtls.blog_server.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "*")
@Tag(name = "拼团活动管理", description = "提供拼团发起、参与、列表查询等功能")
public class GroupBuyController {

    @Autowired
    private GroupBuyService groupBuyService;

    private Long getUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("未授权访问，请重新登录");
        }
        Claims claims = JwtUtils.parseToken(authHeader.substring(7));
        Object userId = claims.get("userId");
        if (userId == null) {
            throw new UnauthorizedException("Token无效，缺少用户信息");
        }
        return Long.valueOf(userId.toString());
    }

    @GetMapping("/active")
    @Operation(summary = "获取正在进行中的拼团列表")
    public Result<List<GroupBuy>> getActiveGroups() {
        return Result.success(groupBuyService.getActiveGroups());
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取拼团详情")
    public Result<GroupBuy> getGroupById(@PathVariable Long id) {
        return Result.success(groupBuyService.getGroupById(id));
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "获取拼团成员列表")
    public Result<List<Map<String, Object>>> getGroupMembers(@PathVariable Long id) {
        return Result.success(groupBuyService.getGroupMembers(id));
    }

    @GetMapping
    @Operation(summary = "获取所有拼团记录 (Admin)")
    public Result<List<GroupBuy>> getAllGroups(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Claims claims = JwtUtils.parseToken(token);
        if (!"ADMIN".equals(claims.get("role", String.class))) {
            throw new UnauthorizedException("拒绝访问，只能操作自己的数据");
        }
        return Result.success(groupBuyService.getAllGroups());
    }

    @PostMapping("/start")
    @Operation(summary = "发起新拼团")
    public Result<Map<String, Object>> startGroup(@RequestHeader("Authorization") String authHeader, @RequestBody Map<String, Object> body) {
        Long userId = getUserId(authHeader);
        Long productId = Long.valueOf(body.get("productId").toString());
        String address = body.getOrDefault("address", "").toString();
        GroupBuy gb = groupBuyService.startGroup(userId, productId, address);
        
        // Find the newly created order ID
        String orderId = groupBuyService.getOrderIdForMember(gb.getId(), userId);
        
        return Result.success(Map.of("group", gb, "orderId", orderId));
    }

    @PostMapping("/{groupId}/join")
    @Operation(summary = "加入拼团")
    public Result<Map<String, Object>> joinGroup(@RequestHeader("Authorization") String authHeader, @PathVariable Long groupId, @RequestBody(required = false) Map<String, Object> body) {
        Long userId = getUserId(authHeader);
        String address = (body != null && body.containsKey("address")) ? body.get("address").toString() : "";
        GroupBuy gb = groupBuyService.joinGroup(userId, groupId, address);
        
        // Find the newly created order ID
        String orderId = groupBuyService.getOrderIdForMember(groupId, userId);
        
        return Result.success(Map.of("group", gb, "orderId", orderId));
    }

    @PostMapping("/{groupId}/force-success")
    @Operation(summary = "强制拼团成功 (Admin)")
    public Result<String> forceSuccess(@RequestHeader("Authorization") String authHeader, @PathVariable Long groupId) {
        checkAdmin(authHeader);
        groupBuyService.forceSuccess(groupId);
        return Result.success("Group buy forced success");
    }

    @PostMapping("/{groupId}/force-fail")
    @Operation(summary = "强制拼团失败并退款 (Admin)")
    public Result<String> forceFail(@RequestHeader("Authorization") String authHeader, @PathVariable Long groupId) {
        checkAdmin(authHeader);
        groupBuyService.forceFail(groupId);
        return Result.success("Group buy forced fail and refunded");
    }

    private void checkAdmin(String authHeader) {
        String token = authHeader.substring(7);
        Claims claims = JwtUtils.parseToken(token);
        if (!"ADMIN".equals(claims.get("role", String.class))) {
            throw new UnauthorizedException("拒绝访问，只能操作自己的数据");
        }
    }

    @GetMapping("/me")
    @Operation(summary = "获取我的拼团记录")
    public Result<List<GroupBuy>> getMyGroups(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        return Result.success(groupBuyService.getUserGroups(userId));
    }
}
