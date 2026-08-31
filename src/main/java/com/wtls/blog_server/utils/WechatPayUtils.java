package com.wtls.blog_server.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * 微信支付 V3 签名、验签与 AES-GCM 解密工具类
 */
public class WechatPayUtils {

    /**
     * 生成 V3 授权请求头 Authorization
     */
    public static String buildAuthorization(String mchId, String serialNo, String privateKeyStr,
                                            String method, String canonicalUrl, String body) {
        long timestamp = System.currentTimeMillis() / 1000;
        String nonceStr = RandomUtil.randomString(32);
        String message = buildMessage(method, canonicalUrl, timestamp, nonceStr, body);
        String signature = sign(message, privateKeyStr);

        return "WECHATPAY2-SHA256-RSA2048 "
                + "mchid=\"" + mchId + "\","
                + "nonce_str=\"" + nonceStr + "\","
                + "timestamp=\"" + timestamp + "\","
                + "serial_no=\"" + serialNo + "\","
                + "signature=\"" + signature + "\"";
    }

    /**
     * 构建待签名报文串
     */
    private static String buildMessage(String method, String canonicalUrl, long timestamp, String nonceStr, String body) {
        return method + "\n"
                + canonicalUrl + "\n"
                + timestamp + "\n"
                + nonceStr + "\n"
                + (body == null ? "" : body) + "\n";
    }

    /**
     * 使用商户私钥对报文进行 SHA256withRSA 签名
     */
    public static String sign(String message, String privateKeyStr) {
        try {
            String cleanKey = privateKeyStr
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] keyBytes = Base64.decode(cleanKey);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = kf.generatePrivate(spec);

            Sign sign = new Sign(SignAlgorithm.SHA256withRSA, privateKey, null);
            byte[] signed = sign.sign(message.getBytes(StandardCharsets.UTF_8));
            return Base64.encode(signed);
        } catch (Exception e) {
            throw new RuntimeException("微信支付V3签名失败: " + e.getMessage(), e);
        }
    }

    /**
     * 微信支付 V3 回调报文 AES-256-GCM 解密
     *
     * @param apiV3Key       商户平台设置的 32 字节 APIv3 密钥
     * @param associatedData 附加数据
     * @param nonce          随机串
     * @param ciphertext     Base64 密文
     * @return 解密后的 JSON 字符串
     */
    public static String decryptResource(String apiV3Key, String associatedData, String nonce, String ciphertext) {
        try {
            final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec key = new SecretKeySpec(apiV3Key.getBytes(StandardCharsets.UTF_8), "AES");
            GCMParameterSpec spec = new GCMParameterSpec(128, nonce.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            if (associatedData != null) {
                cipher.updateAAD(associatedData.getBytes(StandardCharsets.UTF_8));
            }
            byte[] decrypted = cipher.doFinal(Base64.decode(ciphertext));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("微信支付V3回调报文解密失败: " + e.getMessage(), e);
        }
    }
}
