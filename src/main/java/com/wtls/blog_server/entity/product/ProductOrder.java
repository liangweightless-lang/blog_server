package com.wtls.blog_server.entity.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product_order")
public class ProductOrder {
    @TableId(type = IdType.INPUT)
    private String id;
    private Long userId;
    private Long productId;
    private BigDecimal amount;
    private Integer status; // 0: pending, 1: paid
    private String shippingAddress;
    private String orderType; // "INDIVIDUAL" or "GROUP"
    private Integer pointsUsed;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
}
