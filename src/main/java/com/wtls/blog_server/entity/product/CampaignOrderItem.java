package com.wtls.blog_server.entity.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CampaignOrderItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderId;
    private Long productId;
    private String productName;
    private String productImage;
    private String specs;
    private BigDecimal price;
    private Integer quantity;
}
