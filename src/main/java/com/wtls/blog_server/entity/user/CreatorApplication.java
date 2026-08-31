package com.wtls.blog_server.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("creator_application")
public class CreatorApplication {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String brandName;
    private String contactPhone;
    private String wechatId;
    private String intro;
    private String credentialsUrl;
    private Integer status; // 0-待审核, 1-已通过, 2-已驳回
    private String rejectReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
