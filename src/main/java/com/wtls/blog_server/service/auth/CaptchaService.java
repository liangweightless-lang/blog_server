package com.wtls.blog_server.service.auth;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class CaptchaService {

    // 默认缓存 5 分钟 (300,000 毫秒)
    private static final TimedCache<String, String> captchaCache = CacheUtil.newTimedCache(300000);

    static {
        // 每 1 分钟清理一次过期缓存
        captchaCache.schedulePrune(60000);
    }

    /**
     * 生成验证码并缓存
     */
    public Map<String, String> generateCaptcha() {
        // 创建干扰线验证码 (宽, 高, 字符数, 干扰线数)
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 30);
        String code = lineCaptcha.getCode();
        String base64Image = lineCaptcha.getImageBase64Data();

        String captchaKey = UUID.randomUUID().toString().replace("-", "");
        
        // 存入缓存
        captchaCache.put(captchaKey, code);

        Map<String, String> result = new HashMap<>();
        result.put("captchaKey", captchaKey);
        result.put("captchaImage", base64Image);
        return result;
    }

    /**
     * 校验验证码
     * @param captchaKey   前端传来的 key
     * @param captchaCode  前端传来的 输入验证码
     * @return boolean
     */
    public boolean validateCaptcha(String captchaKey, String captchaCode) {
        if (captchaKey == null || captchaCode == null) {
            return false;
        }
        String cachedCode = captchaCache.get(captchaKey, false); // 不更新最后访问时间
        if (cachedCode == null) {
            return false;
        }
        // 核销验证码，防止重放攻击
        captchaCache.remove(captchaKey);
        
        return cachedCode.equalsIgnoreCase(captchaCode.trim());
    }
}
