package com.wtls.blog_server.enums;

import lombok.Getter;

/**
 * 社区快团跟团订单生命周期状态枚举
 */
@Getter
public enum CampaignOrderStatusEnum {

    UNPAID(0, "待付款"),
    PAID_PENDING_PICKUP(1, "待提货"),
    COMPLETED(2, "已提货完成"),
    CANCELLED(3, "已失效/已取消");

    private final int code;
    private final String desc;

    CampaignOrderStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 判断状态是否匹配，防 Null 安全
     */
    public boolean matches(Integer status) {
        return status != null && this.code == status;
    }

    /**
     * 根据 code 获取对应枚举，未找到返回 null
     */
    public static CampaignOrderStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CampaignOrderStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    /**
     * 判断是否属于已付款有效状态（1: 待提货, 2: 已提货完成）
     */
    public static boolean isPaid(Integer status) {
        return PAID_PENDING_PICKUP.matches(status) || COMPLETED.matches(status);
    }
}
