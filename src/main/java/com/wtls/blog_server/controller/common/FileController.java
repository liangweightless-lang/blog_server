package com.wtls.blog_server.controller.common;

import com.wtls.blog_server.service.common.TencentCosService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private TencentCosService cosService;

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
            String relativeUrl = "/uploads/" + datePath + newFilename;
            String fullUrl;

            // 1. 如果开启了腾讯云对象存储 COS，直接推送到 COS (零带宽损耗、秒级分发、无状态持久化)
            if (cosService.isCosEnabled()) {
                String keyPath = "uploads/" + datePath + newFilename;
                fullUrl = cosService.uploadFile(file, keyPath);
            } else {
                // 2. 本地存储兼容与兜底
                File dir = new File(uploadDir + datePath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File dest = new File(dir, newFilename);
                file.transferTo(dest);

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
                fullUrl = domain + relativeUrl;
            }

            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", fullUrl);
            result.put("url", fullUrl);
            result.put("relativePath", relativeUrl);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "message", "上传失败: " + e.getMessage(), "error", "上传失败: " + e.getMessage()));
        }
    }
}
