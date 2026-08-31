package com.wtls.blog_server.utils;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 虎皮椒 (Xunhupay) 支付签名与请求工具类
 */
@Component
public class XunhupayUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 生成虎皮椒 MD5 签名
     * 规则:
     * 1. 除去 hash 字段以及空值字段
     * 2. 将剩余字段按照 key ASCII 升序排列，拼接为 key1=value1&key2=value2...
     * 3. 在末尾追加 appsecret: key1=value1&key2=value2appsecret
     * 4. 计算 MD5 (32位小写)
     */
    public static String generateSignature(Map<String, String> params, String appSecret) {
        // 过滤空值和 hash 键
        Map<String, String> sortedParams = new TreeMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value != null && !value.trim().isEmpty() && !"hash".equalsIgnoreCase(key)) {
                sortedParams.put(key, value.trim());
            }
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }

        // 虎皮椒标准：在末尾直接拼接 appSecret
        sb.append(appSecret);

        return DigestUtil.md5Hex(sb.toString()).toLowerCase();
    }

    /**
     * 验证虎皮椒异步回调签名
     */
    public static boolean verifySignature(Map<String, String> params, String appSecret) {
        String receivedHash = params.get("hash");
        if (receivedHash == null || receivedHash.trim().isEmpty()) {
            return false;
        }
        String calculatedHash = generateSignature(params, appSecret);
        return receivedHash.equalsIgnoreCase(calculatedHash);
    }

    /**
     * 发送 POST 请求到虎皮椒统一下单网关
     */
    public static JsonNode sendPayRequest(String gatewayUrl, Map<String, String> params, String appSecret) {
        // 计算 hash 签名并放入参数
        String hash = generateSignature(params, appSecret);
        Map<String, Object> formParams = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            formParams.put(entry.getKey(), entry.getValue());
        }
        formParams.put("hash", hash);

        try {
            // 发送表单 POST 请求 (超时时间 10 秒)
            String responseStr = HttpUtil.post(gatewayUrl, formParams, 10000);
            return objectMapper.readTree(responseStr);
        } catch (Exception e) {
            throw new RuntimeException("调用虎皮椒支付网关失败: " + e.getMessage(), e);
        }
    }
}
