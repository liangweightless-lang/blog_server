package com.wtls.blog_server.controller.pay;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wtls.blog_server.common.Result;
import com.wtls.blog_server.entity.product.CampaignOrder;
import com.wtls.blog_server.entity.product.ProductOrder;
import com.wtls.blog_server.mapper.product.CampaignOrderMapper;
import com.wtls.blog_server.mapper.product.ProductOrderMapper;
import com.wtls.blog_server.service.product.GroupBuyCampaignService;
import com.wtls.blog_server.service.product.ProductOrderService;
import com.wtls.blog_server.utils.WechatPayUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pay/wechat")
@Tag(name = "微信支付", description = "微信支付 V3 统一下单、扫码支付、H5支付与回调通知")
public class WechatPayController {

    @Autowired
    private ProductOrderService orderService;

    @Autowired
    private ProductOrderMapper orderMapper;

    @Autowired
    private CampaignOrderMapper campaignOrderMapper;

    @Autowired
    private GroupBuyCampaignService campaignService;

    @Value("${wechat.appId:}")
    private String appId;

    @Value("${wechat.mchId:}")
    private String mchId;

    @Value("${wechat.apiV3Key:}")
    private String apiV3Key;

    @Value("${wechat.privateKey:}")
    private String privateKey;

    @Value("${wechat.mchSerialNo:}")
    private String mchSerialNo;

    @Value("${wechat.notifyUrl:https://caibread.com/api/pay/wechat/notify}")
    private String notifyUrl;

    @PostMapping("/create")
    @Operation(summary = "创建微信支付（支持PC端扫码与移动端H5自适应）")
    public Result<Map<String, Object>> createPay(HttpServletRequest request, @RequestParam String orderId) {
        // 1. 判断是否为移动端
        String userAgent = request.getHeader("User-Agent");
        boolean isMobile = userAgent != null && (userAgent.toLowerCase().contains("android")
                || userAgent.toLowerCase().contains("iphone")
                || userAgent.toLowerCase().contains("mobile")
                || userAgent.toLowerCase().contains("ipad"));

        // 2. 查询普通订单或团购订单
        ProductOrder productOrder = orderMapper.selectById(orderId);
        CampaignOrder campaignOrder = null;
        if (productOrder == null) {
            campaignOrder = campaignOrderMapper.selectById(orderId);
        }

        if (productOrder == null && campaignOrder == null) {
            return Result.error(400, "订单不存在，无法调起微信支付");
        }

        String description;
        BigDecimal amount;
        int status;

        if (productOrder != null) {
            description = "小柴包商品订单 " + orderId;
            amount = productOrder.getAmount();
            status = productOrder.getStatus();
        } else {
            description = "小柴包跟团订单 " + orderId;
            amount = campaignOrder.getTotalAmount();
            status = campaignOrder.getStatus();
        }

        if (status != 0) {
            return Result.error(400, "订单状态不正确（已支付或已关闭）");
        }

        // 金额转换为微信支付单位：分
        int totalFen = amount.multiply(new BigDecimal("100")).intValue();
        if (totalFen <= 0) totalFen = 1; // 兜底

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("orderId", orderId);
        resultMap.put("amount", amount.toString());
        resultMap.put("isMobile", isMobile);

        // 3. 检查微信支付商户配置是否已完整配置
        boolean hasConfig = StrUtil.isNotBlank(appId) && StrUtil.isNotBlank(mchId) 
                && StrUtil.isNotBlank(privateKey) && StrUtil.isNotBlank(mchSerialNo);

        if (!hasConfig) {
            // 未配置真实商户密钥时，提供标准的演示体验 / 二维码模拟链接
            if (isMobile) {
                resultMap.put("payType", "H5");
                resultMap.put("h5_url", "weixin://wxpay/bizpayurl?pr=mock_" + orderId);
            } else {
                resultMap.put("payType", "NATIVE");
                resultMap.put("code_url", "weixin://wxpay/bizpayurl?pr=mock_" + orderId);
            }
            resultMap.put("mock", true);
            return Result.success(resultMap);
        }

        // 4. 调用微信支付 V3 API
        try {
            if (isMobile) {
                // 移动端 H5 支付
                String url = "https://api.mch.weixin.qq.com/v3/pay/transactions/h5";
                JSONObject reqBody = new JSONObject();
                reqBody.set("appid", appId);
                reqBody.set("mchid", mchId);
                reqBody.set("description", description);
                reqBody.set("out_trade_no", orderId);
                reqBody.set("notify_url", notifyUrl);

                JSONObject amountObj = new JSONObject();
                amountObj.set("total", totalFen);
                amountObj.set("currency", "CNY");
                reqBody.set("amount", amountObj);

                JSONObject sceneInfo = new JSONObject();
                sceneInfo.set("payer_client_ip", getClientIp(request));
                JSONObject h5Info = new JSONObject();
                h5Info.set("type", "Wap");
                sceneInfo.set("h5_info", h5Info);
                reqBody.set("scene_info", sceneInfo);

                String bodyStr = reqBody.toString();
                String authHeader = WechatPayUtils.buildAuthorization(mchId, mchSerialNo, privateKey, "POST", "/v3/pay/transactions/h5", bodyStr);

                HttpResponse response = HttpRequest.post(url)
                        .header("Authorization", authHeader)
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .body(bodyStr)
                        .execute();

                JSONObject resJson = JSONUtil.parseObj(response.body());
                if (response.isOk() && resJson.containsKey("h5_url")) {
                    resultMap.put("payType", "H5");
                    resultMap.put("h5_url", resJson.getStr("h5_url"));
                    return Result.success(resultMap);
                } else {
                    return Result.error(500, "调用微信H5支付失败: " + resJson.getStr("message", response.body()));
                }
            } else {
                // PC 端 Native 扫码支付
                String url = "https://api.mch.weixin.qq.com/v3/pay/transactions/native";
                JSONObject reqBody = new JSONObject();
                reqBody.set("appid", appId);
                reqBody.set("mchid", mchId);
                reqBody.set("description", description);
                reqBody.set("out_trade_no", orderId);
                reqBody.set("notify_url", notifyUrl);

                JSONObject amountObj = new JSONObject();
                amountObj.set("total", totalFen);
                amountObj.set("currency", "CNY");
                reqBody.set("amount", amountObj);

                String bodyStr = reqBody.toString();
                String authHeader = WechatPayUtils.buildAuthorization(mchId, mchSerialNo, privateKey, "POST", "/v3/pay/transactions/native", bodyStr);

                HttpResponse response = HttpRequest.post(url)
                        .header("Authorization", authHeader)
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .body(bodyStr)
                        .execute();

                JSONObject resJson = JSONUtil.parseObj(response.body());
                if (response.isOk() && resJson.containsKey("code_url")) {
                    resultMap.put("payType", "NATIVE");
                    resultMap.put("code_url", resJson.getStr("code_url"));
                    return Result.success(resultMap);
                } else {
                    return Result.error(500, "调用微信Native支付失败: " + resJson.getStr("message", response.body()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "发起微信支付异常: " + e.getMessage());
        }
    }

    @GetMapping("/query")
    @Operation(summary = "查询订单支付状态（用于扫码页面轮询）")
    public Result<Map<String, Object>> queryStatus(@RequestParam String orderId) {
        ProductOrder productOrder = orderMapper.selectById(orderId);
        CampaignOrder campaignOrder = null;
        if (productOrder == null) {
            campaignOrder = campaignOrderMapper.selectById(orderId);
        }

        if (productOrder == null && campaignOrder == null) {
            return Result.error(404, "订单不存在");
        }

        int status = (productOrder != null) ? productOrder.getStatus() : campaignOrder.getStatus();
        Map<String, Object> res = new HashMap<>();
        res.put("orderId", orderId);
        res.put("status", status);
        res.put("paid", status == 1);

        return Result.success(res);
    }

    @PostMapping("/notify")
    @Operation(summary = "微信支付 V3 异步回调通知")
    public void notifyCall(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("application/json;charset=UTF-8");
        try {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String notifyJsonStr = sb.toString();
            if (StrUtil.isBlank(notifyJsonStr)) {
                response.setStatus(400);
                response.getWriter().write("{\"code\":\"FAIL\",\"message\":\"请求体为空\"}");
                return;
            }

            JSONObject notifyObj = JSONUtil.parseObj(notifyJsonStr);
            String eventType = notifyObj.getStr("event_type");

            if ("TRANSACTION.SUCCESS".equalsIgnoreCase(eventType) && notifyObj.containsKey("resource")) {
                JSONObject resource = notifyObj.getJSONObject("resource");
                String algorithm = resource.getStr("algorithm");
                String ciphertext = resource.getStr("ciphertext");
                String associatedData = resource.getStr("associated_data");
                String nonce = resource.getStr("nonce");

                if ("AEAD_AES_256_GCM".equalsIgnoreCase(algorithm) && StrUtil.isNotBlank(apiV3Key)) {
                    String decrypted = WechatPayUtils.decryptResource(apiV3Key, associatedData, nonce, ciphertext);
                    JSONObject payData = JSONUtil.parseObj(decrypted);
                    String outTradeNo = payData.getStr("out_trade_no");
                    String tradeState = payData.getStr("trade_state");

                    if ("SUCCESS".equalsIgnoreCase(tradeState)) {
                        // 幂等更新订单
                        ProductOrder pOrder = orderMapper.selectById(outTradeNo);
                        if (pOrder != null) {
                            orderService.handlePaymentSuccess(outTradeNo);
                            System.out.println("微信支付普通订单 " + outTradeNo + " 回调成功");
                        } else {
                            CampaignOrder cOrder = campaignOrderMapper.selectById(outTradeNo);
                            if (cOrder != null) {
                                campaignService.handlePaymentSuccess(outTradeNo);
                                System.out.println("微信支付社区团购订单 " + outTradeNo + " 回调成功");
                            }
                        }
                    }
                }
            }

            response.setStatus(200);
            response.getWriter().write("{\"code\":\"SUCCESS\",\"message\":\"成功\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            try {
                response.getWriter().write("{\"code\":\"FAIL\",\"message\":\"" + e.getMessage() + "\"}");
            } catch (Exception ignored) {}
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return (ip != null && ip.contains(",")) ? ip.split(",")[0].trim() : ip;
    }
}
