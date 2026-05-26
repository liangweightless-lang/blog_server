package com.wtls.blog_server.controller.pay;

import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.internal.util.AlipaySignature;
import com.wtls.blog_server.common.Result;
import com.wtls.blog_server.entity.product.ProductOrder;
import com.wtls.blog_server.mapper.product.ProductOrderMapper;
import com.wtls.blog_server.service.product.ProductOrderService;
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

    @Value("${alipay.alipayPublicKey}")
    private String alipayPublicKey;

    @Value("${alipay.notifyUrl}")
    private String notifyUrl;

    @PostMapping("/create")
    @Operation(summary = "唤起支付宝电脑网站收银台")
    public Result<String> createPay(@RequestParam String orderId) {
        ProductOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 0) {
            return Result.error(400, "无效的订单或状态不正确");
        }

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(notifyUrl);

        // 这里仅为了回跳前端页面演示，真实场景可以配置为一个回跳前端支付结果页面的地址
        request.setReturnUrl("http://localhost:8080/user/orders");

        String outTradeNo = order.getId();
        String totalAmount = order.getAmount().toString();
        String subject = "Blog_Server Order " + outTradeNo;

        // JSON 参数
        String bizContent = "{\"out_trade_no\":\"" + outTradeNo + "\","
                + "\"total_amount\":\"" + totalAmount + "\","
                + "\"subject\":\"" + subject + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}";
        
        request.setBizContent(bizContent);

        try {
            // 调用 SDK 生成表单 HTML
            String formHtml = alipayClient.pageExecute(request).getBody();
            return Result.success(formHtml);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "生成支付宝订单失败");
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
                    // 业务处理：状态变更为已支付、增加积分等
                    // 注意幂等性，handlePaymentSuccess 里有 order.getStatus() != 0 的校验
                    try {
                        orderService.handlePaymentSuccess(outTradeNo);
                        System.out.println("订单 " + outTradeNo + " 支付回调处理成功");
                    } catch (Exception e) {
                        System.err.println("订单已处理或处理失败: " + e.getMessage());
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
