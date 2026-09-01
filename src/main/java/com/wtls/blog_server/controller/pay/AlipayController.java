package com.wtls.blog_server.controller.pay;

import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.internal.util.AlipaySignature;
import com.wtls.blog_server.common.Result;
import com.wtls.blog_server.entity.product.ProductOrder;
import com.wtls.blog_server.entity.product.CampaignOrder;
import com.wtls.blog_server.mapper.product.ProductOrderMapper;
import com.wtls.blog_server.mapper.product.CampaignOrderMapper;
import com.wtls.blog_server.service.product.ProductOrderService;
import com.wtls.blog_server.service.product.GroupBuyCampaignService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pay/alipay")
public class AlipayController {

    @Autowired
    private AlipayClient alipayClient;

    @Autowired
    private ProductOrderService orderService;
    
    @Autowired
    private ProductOrderMapper orderMapper;

    @Autowired
    private CampaignOrderMapper campaignOrderMapper;

    @Autowired
    private GroupBuyCampaignService campaignService;

    @Value("${alipay.alipayPublicKey}")
    private String alipayPublicKey;

    @Value("${alipay.notifyUrl}")
    private String notifyUrl;

    @PostMapping("/create")
    @Operation(summary = "唤起支付宝收银台（支持移动端/PC端自适应、普通/团购订单自适应）")
    public Result<String> createPay(HttpServletRequest request, @RequestParam String orderId) {
        // 1. 获取客户端 User-Agent 判断是否为移动端
        String userAgent = request.getHeader("User-Agent");
        boolean isMobile = userAgent != null && (userAgent.toLowerCase().contains("android") 
                || userAgent.toLowerCase().contains("iphone") 
                || userAgent.toLowerCase().contains("mobile")
                || userAgent.toLowerCase().contains("ipad"));

        // 2. 识别并查询订单（普通订单或团购订单）
        ProductOrder productOrder = orderMapper.selectById(orderId);
        CampaignOrder campaignOrder = null;
        if (productOrder == null) {
            campaignOrder = campaignOrderMapper.selectById(orderId);
        }

        if (productOrder == null && campaignOrder == null) {
            return Result.error(400, "订单不存在，无法调起支付");
        }

        String outTradeNo = orderId;
        String totalAmount;
        String subject;
        int status;

        if (productOrder != null) {
            totalAmount = productOrder.getAmount().toString();
            subject = "商品订单 " + outTradeNo;
            status = productOrder.getStatus();
        } else {
            totalAmount = campaignOrder.getTotalAmount().toString();
            subject = "社区团购跟团订单 " + outTradeNo;
            status = campaignOrder.getStatus();
        }

        if (status != 0) {
            return Result.error(400, "订单状态不正确（已支付或已关闭）");
        }

        // 动态计算回跳域名 (支持 test.caibread.com 与 caibread.com 动态匹配)
        String host = request.getHeader("Host");
        String scheme = request.getHeader("X-Forwarded-Proto");
        if (scheme == null || scheme.isEmpty()) {
            scheme = request.getScheme();
        }
        String returnUrl = (host != null && !host.contains("localhost") && !host.contains("127.0.0.1")) 
                ? scheme + "://" + host + "/profile?tab=orders" 
                : "https://caibread.com/profile?tab=orders";

        // 3. 决定调用 Wap 支付还是 Page 网页支付
        if (isMobile) {
            AlipayTradeWapPayRequest wapRequest = new AlipayTradeWapPayRequest();
            wapRequest.setNotifyUrl(notifyUrl);
            wapRequest.setReturnUrl(returnUrl);

            String bizContent = "{\"out_trade_no\":\"" + outTradeNo + "\","
                    + "\"total_amount\":\"" + totalAmount + "\","
                    + "\"subject\":\"" + subject + "\","
                    + "\"product_code\":\"QUICK_WAP_WAY\"}";
            wapRequest.setBizContent(bizContent);

            try {
                String formHtml = alipayClient.pageExecute(wapRequest).getBody();
                return Result.success(formHtml);
            } catch (Exception e) {
                e.printStackTrace();
                return Result.error(500, "生成移动端支付宝支付页面失败");
            }
        } else {
            AlipayTradePagePayRequest pageRequest = new AlipayTradePagePayRequest();
            pageRequest.setNotifyUrl(notifyUrl);
            pageRequest.setReturnUrl(returnUrl);

            String bizContent = "{\"out_trade_no\":\"" + outTradeNo + "\","
                    + "\"total_amount\":\"" + totalAmount + "\","
                    + "\"subject\":\"" + subject + "\","
                    + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}";
            pageRequest.setBizContent(bizContent);

            try {
                String formHtml = alipayClient.pageExecute(pageRequest).getBody();
                return Result.success(formHtml);
            } catch (Exception e) {
                e.printStackTrace();
                return Result.error(500, "生成电脑端支付宝支付页面失败");
            }
        }
    }

    @PostMapping("/notify")
    @Operation(summary = "支付宝支付异步回调验签接口")
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
            // RSA2 签名验证
            boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayPublicKey, "UTF-8", "RSA2");
            if (signVerified) {
                String outTradeNo = params.get("out_trade_no");
                String tradeStatus = params.get("trade_status");

                if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                    // 统一回调处理
                    try {
                        ProductOrder pOrder = orderMapper.selectById(outTradeNo);
                        if (pOrder != null) {
                            orderService.handlePaymentSuccess(outTradeNo);
                            System.out.println("普通商品订单 " + outTradeNo + " 支付回调处理成功");
                        } else {
                            CampaignOrder cOrder = campaignOrderMapper.selectById(outTradeNo);
                            if (cOrder != null) {
                                campaignService.handlePaymentSuccess(outTradeNo);
                                System.out.println("社区团购订单 " + outTradeNo + " 支付回调处理成功");
                            } else {
                                System.err.println("回调订单不存在: " + outTradeNo);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("订单 " + outTradeNo + " 回调处理失败: " + e.getMessage());
                    }
                }
                return "success";
            } else {
                System.err.println("支付宝异步回调验签失败！");
                return "failure";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "failure";
        }
    }
}
