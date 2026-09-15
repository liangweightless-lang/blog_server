package com.wtls.blog_server.enums;

import lombok.Getter;

/**
 * 商城普通商品订单状态枚举
 */
@Getter
public enum ProductOrderStatusEnum {

    UNPAID(0, "待付款"),
    PAID_PENDING_SHIP(1, "待发货/自提"),
    CANCELLED(2, "已取消/关闭"),
    COMPLETED(3, "已交付完成");

    private final int code;
    private final String desc;

    ProductOrderStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public boolean matches(Integer status) {
        return status != null && this.code == status;
    }

    public static ProductOrderStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProductOrderStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    public static boolean isPaid(Integer status) {
        return PAID_PENDING_SHIP.matches(status) || COMPLETED.matches(status);
    }
}
