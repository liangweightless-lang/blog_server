package com.wtls.blog_server.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GroupBuyMember {
    private Long id;
    private Long groupId;
    private Long userId;
    private String orderId;
    private LocalDateTime createTime;
}
