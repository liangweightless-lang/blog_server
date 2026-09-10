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
    private Integer groupStatus; // 0: 拼团中, 1: 拼团成功(已成团/无限制), 2: 拼团失败(已结束且人数未达标)
    
    @TableField(exist = false)
    private String groupStatusText; // 如: "已成团", "拼团中(差4人)", "拼团失败(人数未达标)"
    
    @TableField(exist = false)
    private DeliveryLocation deliveryLocation;
    
    @TableField(exist = false)
    private List<CampaignProduct> products;
}
