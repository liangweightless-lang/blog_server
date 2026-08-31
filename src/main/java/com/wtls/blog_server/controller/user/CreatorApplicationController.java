package com.wtls.blog_server.controller.user;

import com.wtls.blog_server.common.Result;
import com.wtls.blog_server.entity.user.CreatorApplication;
import com.wtls.blog_server.entity.user.User;
import com.wtls.blog_server.exception.BusinessException;
import com.wtls.blog_server.exception.UnauthorizedException;
import com.wtls.blog_server.mapper.user.CreatorApplicationMapper;
import com.wtls.blog_server.mapper.user.UserMapper;
import com.wtls.blog_server.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/creator")
@CrossOrigin(origins = "*")
@Tag(name = "主理人申请与审批模块", description = "支持普通用户申请入驻主理人与超管一键审批")
public class CreatorApplicationController {

    @Autowired
    private CreatorApplicationMapper applicationMapper;

    @Autowired
    private UserMapper userMapper;

    private Long getUserIdFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("未登录，请先登录");
        }
        Claims claims = JwtUtils.parseToken(authHeader.substring(7));
        return claims.get("userId", Long.class);
    }

    private void checkAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("未授权访问，请重新登录");
        }
        Claims claims = JwtUtils.parseToken(authHeader.substring(7));
        String role = claims.get("role", String.class);
        if (!"ADMIN".equals(role)) {
            throw new UnauthorizedException("权限不足，需要管理员权限");
        }
    }

    /**
     * 普通用户提交申请成为主理人
     */
    @PostMapping("/apply")
    @Operation(summary = "提交主理人入驻申请")
    public Result<String> apply(@RequestHeader("Authorization") String authHeader,
                                @RequestBody CreatorApplication request) {
        Long userId = getUserIdFromHeader(authHeader);
        User user = userMapper.findById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        if ("ADMIN".equals(user.getRole()) || "CREATOR".equals(user.getRole())) {
            return Result.error(400, "您已经是主理人或管理员，无需重复申请");
        }

        // 检查是否有审核中的申请
        CreatorApplication latest = applicationMapper.selectLatestByUserId(userId);
        if (latest != null && latest.getStatus() == 0) {
            return Result.error(400, "您已有正在审核中的申请，请耐心等待管理员审核");
        }

        if (request.getBrandName() == null || request.getBrandName().trim().isEmpty()) {
            return Result.error(400, "主理人/品牌名称不能为空");
        }
        if (request.getContactPhone() == null || request.getContactPhone().trim().isEmpty()) {
            return Result.error(400, "联系电话不能为空");
        }
        if (request.getIntro() == null || request.getIntro().trim().isEmpty()) {
            return Result.error(400, "主理人简介说明不能为空");
        }

        CreatorApplication app = new CreatorApplication();
        app.setUserId(userId);
        app.setBrandName(request.getBrandName().trim());
        app.setContactPhone(request.getContactPhone().trim());
        app.setWechatId(request.getWechatId() != null ? request.getWechatId().trim() : "");
        app.setIntro(request.getIntro().trim());
        app.setCredentialsUrl(request.getCredentialsUrl());
        app.setStatus(0); // 待审核

        applicationMapper.insert(app);
        return Result.success("申请提交成功，请等待管理员审核");
    }

    /**
     * 查询当前用户的申请状态
     */
    @GetMapping("/my-status")
    @Operation(summary = "查询我的主理人申请状态")
    public Result<Map<String, Object>> getMyStatus(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromHeader(authHeader);
        User user = userMapper.findById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        CreatorApplication latest = applicationMapper.selectLatestByUserId(userId);
        Map<String, Object> map = new HashMap<>();
        map.put("role", user.getRole());
        map.put("isCreator", "CREATOR".equals(user.getRole()) || "ADMIN".equals(user.getRole()));
        map.put("application", latest);
        return Result.success(map);
    }

    /**
     * 管理员获取所有申请列表
     */
    @GetMapping("/admin/list")
    @Operation(summary = "管理员获取主理人申请列表")
    public Result<List<Map<String, Object>>> getAdminList(@RequestHeader("Authorization") String authHeader) {
        checkAdmin(authHeader);
        List<Map<String, Object>> list = applicationMapper.selectAllWithUserInfo();
        return Result.success(list);
    }

    /**
     * 管理员审核通过
     */
    @PostMapping("/admin/{id}/approve")
    @Operation(summary = "管理员审核通过主理人申请")
    public Result<String> approve(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        checkAdmin(authHeader);
        CreatorApplication app = applicationMapper.selectById(id);
        if (app == null) {
            return Result.error(404, "申请记录不存在");
        }

        app.setStatus(1); // 已通过
        applicationMapper.updateById(app);

        // 将用户角色升级为 CREATOR
        User user = userMapper.findById(app.getUserId());
        if (user != null && !"ADMIN".equals(user.getRole())) {
            user.setRole("CREATOR");
            userMapper.updateById(user);
        }

        return Result.success("已通过该主理人申请，用户已成功升级为主理人角色！");
    }

    /**
     * 管理员驳回申请
     */
    @PostMapping("/admin/{id}/reject")
    @Operation(summary = "管理员驳回主理人申请")
    public Result<String> reject(@RequestHeader("Authorization") String authHeader,
                                 @PathVariable Long id,
                                 @RequestBody(required = false) Map<String, String> body) {
        checkAdmin(authHeader);
        CreatorApplication app = applicationMapper.selectById(id);
        if (app == null) {
            return Result.error(404, "申请记录不存在");
        }

        String reason = body != null ? body.get("reason") : "资料不符合入驻要求";
        app.setStatus(2); // 已驳回
        app.setRejectReason(reason);
        applicationMapper.updateById(app);

        return Result.success("已驳回该主理人申请");
    }
}
