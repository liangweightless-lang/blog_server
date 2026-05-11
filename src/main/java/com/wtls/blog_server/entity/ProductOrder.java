package com.wtls.blog_server.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductOrder {
    private String id;
    private Long userId;
    private Long productId;
    private BigDecimal amount;
    private Integer status; // 0: pending, 1: paid
    private LocalDateTime createTime;
    private LocalDateTime payTime;
}
