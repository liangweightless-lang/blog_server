package com.wtls.blog_server.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String password; // In a real system, this should be hashed
    private String nickname;
    private String avatarUrl;
    private Integer points;
    private String inviteCode;
    private Long invitedBy;
    private String role;
    private LocalDateTime createTime;
}
