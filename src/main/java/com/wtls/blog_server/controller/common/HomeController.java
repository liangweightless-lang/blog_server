package com.wtls.blog_server.controller.common;

import com.wtls.blog_server.common.Result;
import com.wtls.blog_server.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.web.bind.annotation.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/home")
@CrossOrigin(origins = "*")
public class HomeController {

    private final String configPath = System.getProperty("user.dir") + "/uploads/home-config.json";

    private void checkAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Unauthorized");
        }
        Claims claims = JwtUtils.parseToken(authHeader.substring(7));
        String role = claims.get("role", String.class);
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("Admin access required");
        }
    }

    @GetMapping("/config")
    public Result<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();
        File file = new File(configPath);
        if (file.exists()) {
            try {
                String content = new String(Files.readAllBytes(Paths.get(configPath)), StandardCharsets.UTF_8);
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String, Object> map = mapper.readValue(content, Map.class);
                return Result.success(map);
            } catch (IOException e) {
                // fallback to default
            }
        }
        
        // 默认预设配置值
        config.put("avatarUrl", "/img/avatar.png");
        config.put("authorName", "小柴包");
        config.put("authorBio", "记录灵感，探索生活美学。在这里分享品牌的成长脉络，以及创作者的生活方式碎片。");
        config.put("tags", new String[]{"生活方式", "独立品牌", "创作手记"});
        config.put("wechatQrUrl", "");
        return Result.success(config);
    }

    @PostMapping("/config")
    public Result<String> saveConfig(@RequestHeader("Authorization") String authHeader, @RequestBody Map<String, Object> config) {
        checkAdmin(authHeader);
        try {
            File dir = new File(System.getProperty("user.dir") + "/uploads");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String json = mapper.writeValueAsString(config);
            Files.write(Paths.get(configPath), json.getBytes(StandardCharsets.UTF_8));
            return Result.success("Config saved successfully");
        } catch (IOException e) {
            return Result.error(500, "保存失败: " + e.getMessage());
        }
    }
}
