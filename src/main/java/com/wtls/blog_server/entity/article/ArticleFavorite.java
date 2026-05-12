package com.wtls.blog_server.entity.article;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ArticleFavorite {
    private Long id;
    private Long userId;
    private Long articleId;
    private LocalDateTime createTime;
}
