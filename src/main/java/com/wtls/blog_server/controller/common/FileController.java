package com.wtls.blog_server.controller.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {
    @org.springframework.beans.factory.annotation.Value("${app.file.base-url}")
    private String baseUrl;

    private final String uploadDir = System.getProperty("user.dir") + "/uploads/";

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "文件为空"));
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
            String url = baseUrl + "/uploads/" + datePath + newFilename;
            return ResponseEntity.ok(java.util.Map.of("url", url));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(java.util.Map.of("error", "上传失败: " + e.getMessage()));
        }
    }
}
