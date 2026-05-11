package com.wtls.blog_server.controller.user;

import com.wtls.blog_server.common.Result;
import com.wtls.blog_server.entity.user.User;
import com.wtls.blog_server.service.user.UserService;
import com.wtls.blog_server.utils.JwtUtils;
import cn.hutool.core.bean.BeanUtil;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@Tag(name = "用户管理", description = "用户登录、个人中心、签到相关接口")
public class UserController {

    @Autowired
    private UserService userService;

    @Schema(description = "登录请求参数")
    public static class LoginRequest {
        @Schema(description = "用户名", example = "admin")
        @NotBlank(message = "用户名不能为空")
        public String username;
        @Schema(description = "密码", example = "123456")
        @NotBlank(message = "密码不能为空")
        public String password;
        @Schema(description = "邀请码 (可选)")
        public String inviteCode;
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录/注册", description = "如果用户不存在则自动注册")
    public Result<?> login(@Valid @RequestBody LoginRequest request) {
        Map<String, Object> result = userService.registerOrLogin(request.username, request.password, request.inviteCode);
        return Result.success(result);
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "根据Token获取个人详细资料")
    public Result<User> getMe(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Unauthorized");
        }
        String token = authHeader.substring(7);
        Claims claims = JwtUtils.parseToken(token);
        Long userId = claims.get("userId", Long.class);
        User user = userService.getUserInfo(userId);
        return Result.success(user);
    }

    @PostMapping("/checkin")
    @Operation(summary = "每日签到", description = "每天可签到一次，奖励10积分")
    public Result<String> checkin(@RequestHeader("Authorization") String authHeader) {
        Claims claims = JwtUtils.parseToken(authHeader.substring(7));
        Long userId = claims.get("userId", Long.class);
        userService.dailyCheckin(userId);
        return Result.success("签到成功，获得10积分！");
    }

    @PutMapping("/profile")
    public Result<User> updateProfile(@RequestHeader("Authorization") String authHeader, @RequestBody User profileData) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Unauthorized");
        }
        String token = authHeader.substring(7);
        Claims claims = JwtUtils.parseToken(token);
        Long userId = claims.get("userId", Long.class);
        
        // Fetch current user and update fields selectively
        User user = userService.getUserInfo(userId);
        BeanUtil.copyProperties(profileData, user, "id", "username", "password", "role", "createTime", "points", "lastCheckinDate", "invitedBy", "inviteCode");
        
        userService.updateProfile(user);
        return Result.success(user);
    }

    @GetMapping
    public Result<List<User>> getAllUsers(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Unauthorized");
        }
        String token = authHeader.substring(7);
        Claims claims = JwtUtils.parseToken(token);
        Long userId = claims.get("userId", Long.class);
        User admin = userService.getUserInfo(userId);
        if (!"ADMIN".equals(admin.getRole())) {
            throw new RuntimeException("Forbidden: Admin only");
        }
        return Result.success(userService.getAllUsers());
    }
}
