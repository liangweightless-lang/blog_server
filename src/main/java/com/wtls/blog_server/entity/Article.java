package com.wtls.blog_server.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Article {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
