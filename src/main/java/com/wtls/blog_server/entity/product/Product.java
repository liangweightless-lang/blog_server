package com.wtls.blog_server.entity.product;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Product {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal groupPrice;
    private String image;
    private Boolean isDigital;
    private Integer stock;
    private String specs; // JSON format
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
