package com.wtls.blog_server.entity.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("group_buy_campaign_product")
public class CampaignProduct {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long campaignId;
    private Long productId;
    private BigDecimal groupPrice;
    private Integer stockLimit;
    private Integer sortOrder;
    
    @TableField(exist = false)
    private Product product;
}
