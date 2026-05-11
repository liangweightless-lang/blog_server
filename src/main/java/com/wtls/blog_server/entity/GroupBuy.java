package com.wtls.blog_server.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GroupBuy {
    private Long id;
    private Long productId;
    private Long initiatorId;
    private Integer requiredNum;
    private Integer currentNum;
    private Integer status; // 0: forming, 1: success, 2: failed/expired
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
    
    // Extra fields for display
    private String productName;
    private String initiatorNickname;
}
