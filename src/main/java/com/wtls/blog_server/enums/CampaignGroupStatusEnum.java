package com.wtls.blog_server.enums;

import lombok.Getter;

/**
 * 社区快团成团进度状态枚举
 */
@Getter
public enum CampaignGroupStatusEnum {

    IN_PROGRESS(0, "拼团中"),
    SUCCESS(1, "已成团"),
    FAILED(2, "拼团失败");

    private final int code;
    private final String desc;

    CampaignGroupStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public boolean matches(Integer status) {
        return status != null && this.code == status;
    }

    public static CampaignGroupStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CampaignGroupStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }
}
