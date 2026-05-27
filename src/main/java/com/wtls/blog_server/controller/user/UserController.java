package com.wtls.blog_server.controller.user;

import com.wtls.blog_server.common.Result;
import com.wtls.blog_server.entity.user.User;
import com.wtls.blog_server.service.user.UserService;
import com.wtls.blog_server.service.auth.CaptchaService;
import com.wtls.blog_server.utils.JwtUtils;
import cn.hutool.core.bean.BeanUtil;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import com.wtls.blog_server.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private CaptchaService captchaService;

    @Schema(description = "登录请求参数")
    public static class LoginRequest {
        @Schema(description = "用户名", example = "admin")
        @NotBlank(message = "用户名不能为空")
        public String username;
        @Schema(description = "密码", example = "123456")
        @NotBlank(message = "密码不能为空")
        public String password;
    }

    @Schema(description = "注册请求参数")
    public static class RegisterRequest {
        @Schema(description = "用户名", example = "13800138000")
        @NotBlank(message = "用户名不能为空")
        public String username;
        @Schema(description = "密码", example = "123456")
        @NotBlank(message = "密码不能为空")
        public String password;
        @Schema(description = "验证码 Key")
        @NotBlank(message = "验证码Key不能为空")
        public String captchaKey;
        @Schema(description = "验证码")
        @NotBlank(message = "验证码不能为空")
        public String captchaCode;
        @Schema(description = "邀请码 (可选)")
        public String inviteCode;
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用手机号和密码登录")
    public Result<?> login(@Valid @RequestBody LoginRequest request) {
        Map<String, Object> result = userService.login(request.username, request.password);
        return Result.success(result);
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新账号，需要验证码")
    public Result<?> register(@Valid @RequestBody RegisterRequest request) {
        if (!captchaService.validateCaptcha(request.captchaKey, request.captchaCode)) {
            return Result.error(400, "验证码错误或已过期");
        }
        Map<String, Object> result = userService.register(request.username, request.password, request.inviteCode);
        return Result.success(result);
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "根据Token获取个人详细资料")
    public Result<User> getMe(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("未授权访问，请重新登录");
        }
        String token = authHeader.substring(7);
        Claims claims = JwtUtils.parseToken(token);
        Object userIdObj = claims.get("userId");
        Long userId = userIdObj != null ? Long.valueOf(userIdObj.toString()) : null;
        User user = userService.getUserInfo(userId);
        return Result.success(user);
    }

    @PostMapping("/checkin")
    @Operation(summary = "每日签到", description = "每天可签到一次，奖励10积分")
    public Result<String> checkin(@RequestHeader("Authorization") String authHeader) {
        Claims claims = JwtUtils.parseToken(authHeader.substring(7));
        Object userIdObj = claims.get("userId");
        Long userId = userIdObj != null ? Long.valueOf(userIdObj.toString()) : null;
        userService.dailyCheckin(userId);
        return Result.success(null, "签到成功，获得10积分！");
    }

    @PutMapping("/profile")
    public Result<User> updateProfile(@RequestHeader("Authorization") String authHeader,
            @RequestBody User profileData) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("未授权访问，请重新登录");
        }
        String token = authHeader.substring(7);
        Claims claims = JwtUtils.parseToken(token);
        Object userIdObj = claims.get("userId");
        Long userId = userIdObj != null ? Long.valueOf(userIdObj.toString()) : null;

        // Fetch current user and update fields selectively
        User user = userService.getUserInfo(userId);
        BeanUtil.copyProperties(profileData, user, "id", "username", "password", "role", "createTime", "points",
                "lastCheckinDate", "invitedBy", "inviteCode");

        userService.updateProfile(user);
        return Result.success(user);
    }

    @GetMapping
    public Result<List<User>> getAllUsers(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("未授权访问，请重新登录");
        }
        String token = authHeader.substring(7);
        Claims claims = JwtUtils.parseToken(token);
        Object userIdObj = claims.get("userId");
        Long userId = userIdObj != null ? Long.valueOf(userIdObj.toString()) : null;
        User admin = userService.getUserInfo(userId);
        if (!"ADMIN".equals(admin.getRole())) {
            throw new UnauthorizedException("禁止访问，仅限管理员");
        }
        return Result.success(userService.getAllUsers());
    }
}
