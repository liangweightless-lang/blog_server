package com.wtls.blog_server.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final String uploadDir = System.getProperty("user.dir") + "/uploads/";

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "Failed to upload empty file";
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
            return "/uploads/" + datePath + newFilename;
        } catch (IOException e) {
            e.printStackTrace();
            return "Upload failed";
        }
    }
}
