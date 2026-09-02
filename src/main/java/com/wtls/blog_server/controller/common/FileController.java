package com.wtls.blog_server.controller.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
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
            
            // 返回标准相对路径 /uploads/yyyy/MM/dd/uuid.ext，自适应当前访问域名（本地/测试服/正式服）
            String relativeUrl = "/uploads/" + datePath + newFilename;
            
            // 工业级全面兼容：同时返回 code、data、url，支持全站各类上传组件的解析要求
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", relativeUrl);
            result.put("url", relativeUrl);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "message", "上传失败: " + e.getMessage(), "error", "上传失败: " + e.getMessage()));
        }
    }
}
