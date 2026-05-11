package com.wtls.blog_server.service.user;

import com.wtls.blog_server.entity.user.User;
import com.wtls.blog_server.mapper.user.UserMapper;
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
            boolean isAdmin = username.equals("admin");
            user.setAvatarUrl(isAdmin ? "/img/admin_avatar.png" : "/img/default_avatar.png");
            user.setPoints(0);
            user.setInviteCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            user.setRole(isAdmin ? "ADMIN" : "USER");
            
            // Handle invitation logic
            if (inviteCodeStr != null && !inviteCodeStr.isEmpty()) {
                User inviter = userMapper.findByInviteCode(inviteCodeStr);
                if (inviter != null) {
                    user.setInvitedBy(inviter.getId());
                    // Reward both inviter and invitee immediately with 50 points
                    user.setPoints(50); // Invitee gets 50
                    userMapper.addPoints(inviter.getId(), 50); // Inviter gets 50
                }
            }
            userMapper.insert(user);
        } else {
            // Check password
            if (!user.getPassword().equals(password)) {
                throw new RuntimeException("Invalid credentials");
            }
        }

        String token = JwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    public User getUserInfo(Long id) {
        return userMapper.findById(id);
    }

    public void updateProfile(User user) {
        userMapper.updateProfile(user);
    }

    public java.util.List<User> getAllUsers() {
        return userMapper.findAll();
    }

    @Transactional
    public void dailyCheckin(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) throw new RuntimeException("User not found");
        
        java.time.LocalDate today = java.time.LocalDate.now();
        if (today.equals(user.getLastCheckinDate())) {
            throw new RuntimeException("今天已经签到过了哦");
        }
        
        // Award 10 points
        userMapper.addPoints(userId, 10);
        userMapper.updateCheckinDate(userId);
    }
}
