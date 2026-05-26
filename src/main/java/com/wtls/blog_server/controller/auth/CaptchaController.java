package com.wtls.blog_server.controller.auth;

import com.wtls.blog_server.common.Result;
import com.wtls.blog_server.service.auth.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Tag(name = "安全验证管理", description = "验证码等安全接口")
public class CaptchaController {

    @Autowired
    private CaptchaService captchaService;

    @GetMapping("/captcha")
    @Operation(summary = "获取图形验证码", description = "返回一个 Base64 格式的图形验证码和 key，有效时间 5 分钟")
    public Result<Map<String, String>> getCaptcha() {
        return Result.success(captchaService.generateCaptcha());
    }
}
