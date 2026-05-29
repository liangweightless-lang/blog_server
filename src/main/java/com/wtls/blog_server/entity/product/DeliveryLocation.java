package com.wtls.blog_server.entity.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DeliveryLocation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String address;
    private String contactName;
    private String contactPhone;
    private Integer status; // 1: active, 0: inactive
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
