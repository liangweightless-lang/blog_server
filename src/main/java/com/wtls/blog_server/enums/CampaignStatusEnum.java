package com.wtls.blog_server.enums;

import lombok.Getter;

/**
 * 社区快团活动状态枚举
 */
@Getter
public enum CampaignStatusEnum {

    DRAFT(0, "未开始/草稿"),
    ACTIVE(1, "进行中"),
    ENDED(2, "已结束/结团");

    private final int code;
    private final String desc;

    CampaignStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public boolean matches(Integer status) {
        return status != null && this.code == status;
    }

    public static CampaignStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CampaignStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }
}
