package com.wtls.blog_server.controller.common;

import com.wtls.blog_server.common.Result;
import com.wtls.blog_server.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import com.wtls.blog_server.exception.UnauthorizedException;
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
            throw new UnauthorizedException("未授权访问，请重新登录");
        }
        Claims claims = JwtUtils.parseToken(authHeader.substring(7));
        String role = claims.get("role", String.class);
        if (!"ADMIN".equals(role)) {
            throw new UnauthorizedException("权限不足，需要管理员权限");
        }
    }

    /**
     * 将包含绝对域名的 URL 归一化为相对路径，避免跨环境加载错乱
     */
    private String normalizeUrl(Object urlObj) {
        if (urlObj == null) return "";
        String url = String.valueOf(urlObj).trim();
        int uploadIndex = url.indexOf("/uploads/");
        if (uploadIndex >= 0) {
            return url.substring(uploadIndex);
        }
        return url;
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
                
                // 归一化图片路径为相对路径
                if (map.containsKey("avatarUrl")) {
                    map.put("avatarUrl", normalizeUrl(map.get("avatarUrl")));
                }
                if (map.containsKey("wechatQrUrl")) {
                    map.put("wechatQrUrl", normalizeUrl(map.get("wechatQrUrl")));
                }
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

            // 保存前自动归一化为相对路径
            if (config.containsKey("avatarUrl")) {
                config.put("avatarUrl", normalizeUrl(config.get("avatarUrl")));
            }
            if (config.containsKey("wechatQrUrl")) {
                config.put("wechatQrUrl", normalizeUrl(config.get("wechatQrUrl")));
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
