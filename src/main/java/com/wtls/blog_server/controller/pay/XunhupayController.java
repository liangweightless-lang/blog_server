package com.wtls.blog_server.controller.pay;

import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.wtls.blog_server.common.Result;
import com.wtls.blog_server.entity.product.CampaignOrder;
import com.wtls.blog_server.entity.product.ProductOrder;
import com.wtls.blog_server.mapper.product.CampaignOrderMapper;
import com.wtls.blog_server.mapper.product.ProductOrderMapper;
import com.wtls.blog_server.service.product.GroupBuyCampaignService;
import com.wtls.blog_server.service.product.ProductOrderService;
import com.wtls.blog_server.utils.XunhupayUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pay/xunhupay")
@CrossOrigin(origins = "*")
@Tag(name = "虎皮椒个人支付模块", description = "支持支付宝免营业执照个人全自动收款与回调")
public class XunhupayController {

    @Autowired
    private ProductOrderMapper orderMapper;

    @Autowired
    private CampaignOrderMapper campaignOrderMapper;

    @Autowired
    private ProductOrderService orderService;

    @Autowired
    private GroupBuyCampaignService campaignService;

    @Value("${xunhupay.appid:201906186957}")
    private String appid;

    @Value("${xunhupay.appsecret:927ae4ec2b4ef368815dd14ea4f68fea}")
    private String appsecret;

    @Value("${xunhupay.gateway:https://api.xunhupay.com/payment/do.html}")
    private String gatewayUrl;

    @Value("${xunhupay.notifyUrl:https://caibread.com/api/pay/xunhupay/notify}")
    private String notifyUrl;

    @Value("${xunhupay.returnUrl:https://caibread.com/profile}")
    private String returnUrl;

    /**
     * 创建虎皮椒支付宝支付订单
     */
    @PostMapping("/create")
    @Operation(summary = "创建虎皮椒支付宝支付", description = "返回支付跳转URL与二维码URL")
    public Result<Map<String, Object>> createPay(HttpServletRequest request, @RequestParam String orderId) {
        // 1. 查询普通订单或团购订单
        ProductOrder productOrder = orderMapper.selectById(orderId);
        CampaignOrder campaignOrder = null;
        if (productOrder == null) {
            campaignOrder = campaignOrderMapper.selectById(orderId);
        }

        if (productOrder == null && campaignOrder == null) {
            return Result.error(400, "订单不存在，无法发起支付");
        }

        String totalFee;
        String title;
        int status;

        if (productOrder != null) {
            totalFee = String.format("%.2f", productOrder.getAmount().doubleValue());
            title = "小柴包商品订单-" + orderId;
            status = productOrder.getStatus();
        } else {
            totalFee = String.format("%.2f", campaignOrder.getTotalAmount().doubleValue());
            title = "小柴包团购订单-" + orderId;
            status = campaignOrder.getStatus();
        }

        if (status != 0) {
            return Result.error(400, "订单已支付或已取消");
        }

        // 2. 动态自适应当前访问域名（支持 test.caibread.com 与 caibread.com 自动匹配）
        String host = request.getHeader("Host");
        String scheme = request.getHeader("X-Forwarded-Proto");
        if (scheme == null || scheme.isEmpty()) {
            scheme = request.getScheme();
        }
        
        String dynamicNotifyUrl = notifyUrl.trim();
        String dynamicReturnUrl = returnUrl.trim();
        if (host != null && !host.contains("localhost") && !host.contains("127.0.0.1")) {
            dynamicNotifyUrl = scheme + "://" + host + "/api/pay/xunhupay/notify";
            dynamicReturnUrl = scheme + "://" + host + "/profile";
        }

        // 3. 组装虎皮椒统一下单参数
        Map<String, String> params = new HashMap<>();
        params.put("version", "1.1");
        params.put("appid", appid.trim());
        params.put("trade_order_id", orderId);
        params.put("total_fee", totalFee);
        params.put("title", title);
        params.put("time", String.valueOf(System.currentTimeMillis() / 1000));
        params.put("notify_url", dynamicNotifyUrl);
        params.put("return_url", dynamicReturnUrl);
        params.put("type", "alipay");
        params.put("nonce_str", IdUtil.fastSimpleUUID());

        try {
            JsonNode respNode = XunhupayUtils.sendPayRequest(gatewayUrl, params, appsecret.trim());
            int errcode = respNode.path("errcode").asInt(-1);
            if (errcode == 0) {
                Map<String, Object> data = new HashMap<>();
                data.put("orderId", orderId);
                data.put("payUrl", respNode.path("url").asText());
                data.put("qrUrl", respNode.path("url_qrcode").asText());
                data.put("channel", "XUNHUPAY_ALIPAY");
                return Result.success(data);
            } else {
                String errmsg = respNode.path("errmsg").asText("虎皮椒统一下单失败");
                return Result.error(500, "调起支付失败: " + errmsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "调用虎皮椒支付网关异常: " + e.getMessage());
        }
    }

    /**
     * 虎皮椒异步回调接口 (POST)
     */
    @PostMapping("/notify")
    @Operation(summary = "虎皮椒支付异步通知回调", description = "接收并验签虎皮椒支付结果，自动流转订单状态")
    public String notifyCall(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        try {
            // 1. 虎皮椒 MD5 签名校验
            boolean signVerified = XunhupayUtils.verifySignature(params, appsecret.trim());
            if (!signVerified) {
                System.err.println("虎皮椒异步回调签名验证失败！收到参数: " + params);
                return "failure";
            }

            // 2. 判断支付状态 (OD 表示支付成功并完成)
            String tradeOrderId = params.get("trade_order_id");
            String status = params.get("status");

            if ("OD".equalsIgnoreCase(status)) {
                // 统一回调处理
                ProductOrder pOrder = orderMapper.selectById(tradeOrderId);
                if (pOrder != null) {
                    orderService.handlePaymentSuccess(tradeOrderId);
                    System.out.println("虎皮椒支付成功: 普通订单 " + tradeOrderId + " 已自动流转为已支付");
                } else {
                    CampaignOrder cOrder = campaignOrderMapper.selectById(tradeOrderId);
                    if (cOrder != null) {
                        campaignService.handlePaymentSuccess(tradeOrderId);
                        System.out.println("虎皮椒支付成功: 团购订单 " + tradeOrderId + " 已自动流转为已支付");
                    } else {
                        System.err.println("虎皮椒回调订单不存在: " + tradeOrderId);
                    }
                }
                return "success";
            } else {
                System.out.println("虎皮椒通知订单 " + tradeOrderId + " 状态为非支付成功: " + status);
                return "success";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "failure";
        }
    }

    /**
     * 查询订单支付状态 (供前端收银台轮询)
     */
    @GetMapping("/query")
    @Operation(summary = "查询订单支付状态")
    public Result<Map<String, Object>> queryStatus(@RequestParam String orderId) {
        Map<String, Object> map = new HashMap<>();
        ProductOrder pOrder = orderMapper.selectById(orderId);
        if (pOrder != null) {
            map.put("paid", pOrder.getStatus() == 1 || pOrder.getStatus() == 3);
            map.put("status", pOrder.getStatus());
            return Result.success(map);
        }

        CampaignOrder cOrder = campaignOrderMapper.selectById(orderId);
        if (cOrder != null) {
            map.put("paid", cOrder.getStatus() == 1 || cOrder.getStatus() == 2);
            map.put("status", cOrder.getStatus());
            return Result.success(map);
        }

        return Result.error(404, "订单不存在");
    }
}
