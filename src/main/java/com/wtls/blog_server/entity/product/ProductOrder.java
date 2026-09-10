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
    private Integer quantity; // 购买数量
    private BigDecimal amount;
    private Integer status; // 0: pending, 1: paid
    private String shippingAddress;
    private String contactPhone; // 联系手机号
    private String remark; // 顾客备注
    private String orderType; // "INDIVIDUAL" or "GROUP"
    private Integer pointsUsed;
    private BigDecimal deliveryFee; // 配送费
    private String selectedSpec;
    private LocalDateTime createTime;
    private LocalDateTime payTime;

    /**
     * 关联扩展字段（非数据库持久化字段，由业务层动态装配）
     */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String productName;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String productImage;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private BigDecimal productOriginalPrice;
}

