package com.wtls.blog_server.controller;

import com.wtls.blog_server.entity.User;
import com.wtls.blog_server.service.UserService;
import com.wtls.blog_server.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    public static class LoginRequest {
        public String username;
        public String password;
        public String inviteCode;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Map<String, Object> result = userService.registerOrLogin(request.username, request.password, request.inviteCode);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        String token = authHeader.substring(7);
        try {
            Claims claims = JwtUtils.parseToken(token);
            Long userId = claims.get("userId", Long.class);
            User user = userService.getUserInfo(userId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid Token"));
        }
    }
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String authHeader, @RequestBody User profileData) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        String token = authHeader.substring(7);
        try {
            Claims claims = JwtUtils.parseToken(token);
            Long userId = claims.get("userId", Long.class);
            
            // Fetch current user and update fields
            User user = userService.getUserInfo(userId);
            user.setNickname(profileData.getNickname());
            user.setAvatarUrl(profileData.getAvatarUrl());
            user.setAddress(profileData.getAddress());
            user.setWechatId(profileData.getWechatId());
            user.setAge(profileData.getAge());
            user.setGender(profileData.getGender());
            
            userService.updateProfile(user);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid Token or Update Failed"));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        String token = authHeader.substring(7);
        try {
            Claims claims = JwtUtils.parseToken(token);
            Long userId = claims.get("userId", Long.class);
            User admin = userService.getUserInfo(userId);
            if (!"ADMIN".equals(admin.getRole())) {
                return ResponseEntity.status(403).body(Map.of("error", "Forbidden: Admin only"));
            }
            return ResponseEntity.ok(userService.getAllUsers());
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid Token"));
        }
    }
}
