package com.wtls.blog_server.entity.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("group_buy")
public class GroupBuy {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private Long initiatorId;
    private Integer requiredNum;
    private Integer currentNum;
    private Integer status; // 0: forming, 1: success, 2: failed/expired
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
    
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String productName;
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String initiatorNickname;
}
