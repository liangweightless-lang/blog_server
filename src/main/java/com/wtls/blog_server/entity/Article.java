package com.wtls.blog_server.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Article {
    private Long id;
    private String title;
    private String content;
    private String coverUrl;
    private String mediaUrls;
    private Integer likesCount;
    private Long productId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
