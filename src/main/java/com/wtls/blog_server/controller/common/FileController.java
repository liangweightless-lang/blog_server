package com.wtls.blog_server.controller.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;

@RestController
@RequestMapping({"/api/common", "/api/files"})
@CrossOrigin(origins = "*")
public class FileController {

    private final String uploadDir = System.getProperty("user.dir") + "/uploads/";

    @Value("${app.file.base-url:}")
    private String configuredBaseUrl;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "文件为空", "error", "文件为空"));
        }
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;
            String datePath = LocalDate.now().toString().replace("-", "/") + "/";
            File dir = new File(uploadDir + datePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File dest = new File(dir, newFilename);
            file.transferTo(dest);
            
            // 1. 标准相对路径 /uploads/yyyy/MM/dd/uuid.ext
            String relativeUrl = "/uploads/" + datePath + newFilename;
            
            // 2. 动态智能解析客户端当前实际访问的域名 (支持 Nginx 反向代理、测试服 test.caibread.com 与生产服)
            String proto = request.getHeader("X-Forwarded-Proto");
            if (proto == null || proto.isEmpty()) {
                proto = request.getScheme();
            }
            String host = request.getHeader("X-Forwarded-Host");
            if (host == null || host.isEmpty()) {
                host = request.getHeader("Host");
            }
            if (host == null || host.isEmpty()) {
                host = request.getServerName() + (request.getServerPort() == 80 || request.getServerPort() == 443 ? "" : ":" + request.getServerPort());
            }

            String domain;
            if (host != null && !host.isEmpty() && !host.contains("localhost") && !host.contains("127.0.0.1")) {
                domain = proto + "://" + host;
            } else if (configuredBaseUrl != null && !configuredBaseUrl.trim().isEmpty()) {
                domain = configuredBaseUrl.trim().replaceAll("/+$", "");
            } else {
                domain = proto + "://" + host;
            }

            String fullUrl = domain + relativeUrl;
            
            // 3. 工业级全面兼容：同时返回 code、data、url，并提供完整绝对地址与相对地址
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", fullUrl); // 默认返回绝对可访问地址，彻底杜绝 App 回显 404
            result.put("url", fullUrl);
            result.put("relativePath", relativeUrl);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "message", "上传失败: " + e.getMessage(), "error", "上传失败: " + e.getMessage()));
        }
    }
}
