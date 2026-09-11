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
    private Integer status; // 1: 上架销售中, 0: 已下架仓库中
    private Integer stock;
    private String specs; // JSON format
    private Long categoryId;
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String categoryName;
    private BigDecimal deliveryFee; // 配送费
    private Integer isTop; // 0: 否, 1: 是(置顶推荐)
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
