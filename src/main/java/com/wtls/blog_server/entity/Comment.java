package com.wtls.blog_server.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Comment {
    private Long id;
    private Long articleId;
    private String authorName;
    private String content;
    private LocalDateTime createTime;
}
