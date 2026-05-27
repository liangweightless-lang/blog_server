package com.wtls.blog_server.service.user;

import com.wtls.blog_server.entity.user.User;
import com.wtls.blog_server.mapper.user.UserMapper;
import com.wtls.blog_server.utils.JwtUtils;
import com.wtls.blog_server.exception.BusinessException;
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

    public Map<String, Object> login(String username, String password) {
        if (!username.equals("admin") && !username.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("请输入有效的手机号");
        }
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new BusinessException("账号不存在，请先注册");
        }
        if (!user.getPassword().equals(password)) {
            throw new BusinessException("密码错误");
        }
        String token = JwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    @Transactional
    public Map<String, Object> register(String username, String password, String inviteCodeStr) {
        if (!username.equals("admin") && !username.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("请输入有效的手机号");
        }
        User user = userMapper.findByUsername(username);
        if (user != null) {
            throw new BusinessException("该手机号已注册，请直接登录");
        }
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
        if (user == null) throw new BusinessException("User not found");
        
        java.time.LocalDate today = java.time.LocalDate.now();
        if (today.equals(user.getLastCheckinDate())) {
            throw new BusinessException("今天已经签到过了哦");
        }
        
        // Award 10 points
        userMapper.addPoints(userId, 10);
        userMapper.updateCheckinDate(userId);
    }
}
