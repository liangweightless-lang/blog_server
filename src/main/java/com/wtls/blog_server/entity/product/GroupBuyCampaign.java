package com.wtls.blog_server.entity.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GroupBuyCampaign {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String intro;
    private Long deliveryLocationId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime deliveryTime;
    private Integer targetNum; // 目标参团人数
    private Integer status; // 0: pending, 1: active, 2: ended
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    private Integer currentNum; // 已参团人数
    
    @TableField(exist = false)
    private List<String> joinedAvatars; // 已参团用户的头像
    
    @TableField(exist = false)
    private DeliveryLocation deliveryLocation;
    
    @TableField(exist = false)
    private List<CampaignProduct> products;
}
