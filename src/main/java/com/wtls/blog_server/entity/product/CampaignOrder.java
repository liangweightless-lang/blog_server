package com.wtls.blog_server.entity.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CampaignOrder {
    @TableId(type = IdType.INPUT)
    private String id;
    private Long campaignId;
    private Long userId;
    private Integer followNumber; // 跟团号
    private BigDecimal totalAmount;
    private String contactName;
    private String contactPhone;
    private Integer status; // 0: unpaid, 1: paid, 2: picked_up, 3: refunded
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private List<CampaignOrderItem> items;
    
    @TableField(exist = false)
    private String username;
    
    @TableField(exist = false)
    private String avatar;
    
    @TableField(exist = false)
    private GroupBuyCampaign campaign;
}
