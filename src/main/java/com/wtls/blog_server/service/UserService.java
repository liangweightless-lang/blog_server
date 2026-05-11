package com.wtls.blog_server.service;

import com.wtls.blog_server.entity.User;
import com.wtls.blog_server.mapper.UserMapper;
import com.wtls.blog_server.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Transactional
    public Map<String, Object> registerOrLogin(String username, String password, String inviteCodeStr) {
        // Validation: Must be 11 digits or 'admin'
        if (!username.equals("admin") && !username.matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("请输入有效的手机号");
        }

        User user = userMapper.findByUsername(username);
        if (user == null) {
            // Register new user
            user = new User();
            user.setUsername(username);
            user.setPassword(password); // Should be hashed in prod
            user.setNickname("User_" + username.substring(0, Math.min(username.length(), 4)));
            user.setAvatarUrl("/img/avatar.png");
            user.setPoints(0);
            user.setInviteCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            user.setRole(username.equals("admin") ? "ADMIN" : "USER");
            
            // Handle invitation logic
            if (inviteCodeStr != null && !inviteCodeStr.isEmpty()) {
                User inviter = userMapper.findByInviteCode(inviteCodeStr);
                if (inviter != null) {
                    user.setInvitedBy(inviter.getId());
                    // Reward inviter immediately or upon order (here we do upon registration for simplicity, 
                    // but the spec says "points when buy". We will implement it when they buy).
                }
            }
            userMapper.insert(user);
        } else {
            // Check password
            if (!user.getPassword().equals(password)) {
                throw new RuntimeException("Invalid credentials");
            }
        }

        String token = JwtUtils.generateToken(user.getId(), user.getUsername());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    public User getUserInfo(Long id) {
        return userMapper.findById(id);
    }
}
