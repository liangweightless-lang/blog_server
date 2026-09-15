package com.wtls.blog_server.enums;

import lombok.Getter;

/**
 * 系统用户角色权限枚举
 */
@Getter
public enum UserRoleEnum {

    USER("USER", "普通注册用户"),
    CREATOR("CREATOR", "烘焙创作官/小柴包酱"),
    ADMIN("ADMIN", "超级管理员");

    private final String role;
    private final String desc;

    UserRoleEnum(String role, String desc) {
        this.role = role;
        this.desc = desc;
    }

    public boolean matches(String roleStr) {
        return this.role.equalsIgnoreCase(roleStr);
    }

    public static boolean isAdmin(String roleStr) {
        return ADMIN.matches(roleStr);
    }

    public static boolean isAdminOrCreator(String roleStr) {
        return ADMIN.matches(roleStr) || CREATOR.matches(roleStr);
    }
}
