package com.wtls.blog_server.service.notice;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wtls.blog_server.entity.product.CampaignOrder;
import com.wtls.blog_server.entity.product.CampaignOrderItem;
import com.wtls.blog_server.entity.product.GroupBuyCampaign;
import com.wtls.blog_server.entity.product.Product;
import com.wtls.blog_server.entity.product.ProductOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 订单微信服务号即时通知服务 (基于 PushPlus)
 * 当用户完成付款时，秒级推送到店长/合伙人的个人微信或运营群
 */
@Slf4j
@Service
public class OrderNoticeService {

    @Value("${pushplus.enabled:true}")
    private boolean enabled;

    @Value("${pushplus.token:}")
    private String token;

    @Value("${pushplus.topic:}")
    private String topic;

    private static final String PUSHPLUS_URL = "http://www.pushplus.plus/send";

    /**
     * 发送普通商城订单支付通知
     */
    public void sendProductOrderNotice(ProductOrder order, Product product) {
        if (!enabled || StrUtil.isBlank(token)) {
            log.info("PushPlus 微信通知未启用或未配置 token，跳过普通订单 {} 的微信推送", order.getId());
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                String title = String.format("🛒【新商城订单】¥%s - %s", 
                        order.getAmount(), 
                        product != null ? product.getName() : "商品");

                StringBuilder html = new StringBuilder();
                html.append("<div style='font-family:-apple-system,BlinkMacSystemFont,sans-serif;padding:12px;color:#333;'>");
                html.append("<h2 style='color:#FF5A34;margin-top:0;'>🎉 收到新商城订单</h2>");
                html.append("<table style='width:100%;border-collapse:collapse;font-size:14px;'>");
                html.append("<tr><td style='padding:6px 0;color:#888;width:80px;'>订单号：</td><td style='font-weight:600;'>").append(order.getId()).append("</td></tr>");
                html.append("<tr><td style='padding:6px 0;color:#888;'>商品名称：</td><td style='color:#1D2129;font-weight:600;'>").append(product != null ? product.getName() : "商品").append("</td></tr>");
                if (StrUtil.isNotBlank(order.getSelectedSpec())) {
                    html.append("<tr><td style='padding:6px 0;color:#888;'>选择规格：</td><td>").append(order.getSelectedSpec()).append("</td></tr>");
                }
                html.append("<tr><td style='padding:6px 0;color:#888;'>购买数量：</td><td>× ").append(order.getQuantity() != null ? order.getQuantity() : 1).append("</td></tr>");
                html.append("<tr><td style='padding:6px 0;color:#888;'>实付金额：</td><td style='color:#FF5A34;font-size:16px;font-weight:bold;'>¥ ").append(order.getAmount()).append("</td></tr>");
                html.append("<tr><td style='padding:6px 0;color:#888;'>提货/送达：</td><td style='color:#0084FF;font-weight:600;'>").append(StrUtil.blankToDefault(order.getShippingAddress(), "未填写")).append("</td></tr>");
                html.append("<tr><td style='padding:6px 0;color:#888;'>顾客电话：</td><td><a href='tel:").append(order.getContactPhone()).append("'>").append(StrUtil.blankToDefault(order.getContactPhone(), "未填写")).append("</a></td></tr>");
                if (StrUtil.isNotBlank(order.getRemark())) {
                    html.append("<tr><td style='padding:6px 0;color:#888;'>顾客备注：</td><td style='color:#FF7D00;'>").append(order.getRemark()).append("</td></tr>");
                }
                html.append("<tr><td style='padding:6px 0;color:#888;'>下单时间：</td><td>").append(DateUtil.formatDateTime(new Date())).append("</td></tr>");
                html.append("</table>");
                html.append("<div style='margin-top:16px;padding:8px 12px;background:#F7F8FA;border-radius:6px;font-size:12px;color:#86909C;'>请及时安排备料及校内配送/自提。</div>");
                html.append("</div>");

                doSend(title, html.toString());
            } catch (Exception e) {
                log.error("发送普通商城订单微信通知异常, orderId={}", order.getId(), e);
            }
        });
    }

    /**
     * 发送快团订单支付通知
     */
    public void sendCampaignOrderNotice(CampaignOrder order, GroupBuyCampaign campaign, List<CampaignOrderItem> items) {
        if (!enabled || StrUtil.isBlank(token)) {
            log.info("PushPlus 微信通知未启用或未配置 token，跳过快团订单 {} 的微信推送", order.getId());
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                String campaignTitle = campaign != null ? campaign.getTitle() : "快团活动";
                String followNo = order.getFollowNumber() != null ? "#" + order.getFollowNumber() : "";
                String title = String.format("🔥【新跟团单 %s】¥%s - %s", 
                        followNo, 
                        order.getTotalAmount(), 
                        campaignTitle);

                StringBuilder html = new StringBuilder();
                html.append("<div style='font-family:-apple-system,BlinkMacSystemFont,sans-serif;padding:12px;color:#333;'>");
                html.append("<h2 style='color:#FF5A34;margin-top:0;'>🎉 收到新跟团订单 ").append(followNo).append("</h2>");
                html.append("<table style='width:100%;border-collapse:collapse;font-size:14px;'>");
                html.append("<tr><td style='padding:6px 0;color:#888;width:80px;'>跟团序号：</td><td style='color:#FF5A34;font-size:18px;font-weight:800;'>").append(followNo).append("</td></tr>");
                html.append("<tr><td style='padding:6px 0;color:#888;'>快团活动：</td><td style='font-weight:600;'>").append(campaignTitle).append("</td></tr>");
                
                // 商品明细
                if (CollUtil.isNotEmpty(items)) {
                    StringBuilder itemSummary = new StringBuilder();
                    for (CampaignOrderItem it : items) {
                        if (itemSummary.length() > 0) itemSummary.append("<br/>");
                        itemSummary.append("• ").append(it.getProductName())
                                   .append(StrUtil.isNotBlank(it.getSpecs()) ? " (" + it.getSpecs() + ")" : "")
                                   .append(" × ").append(it.getQuantity());
                    }
                    html.append("<tr><td style='padding:6px 0;color:#888;vertical-align:top;'>商品条目：</td><td style='color:#1D2129;font-weight:600;'>").append(itemSummary).append("</td></tr>");
                }

                html.append("<tr><td style='padding:6px 0;color:#888;'>实付金额：</td><td style='color:#FF5A34;font-size:16px;font-weight:bold;'>¥ ").append(order.getTotalAmount()).append("</td></tr>");
                
                String pickupLocation = "团长指定提货点";
                if (campaign != null && campaign.getDeliveryLocation() != null) {
                    pickupLocation = campaign.getDeliveryLocation().getName();
                }
                html.append("<tr><td style='padding:6px 0;color:#888;'>提货地点：</td><td style='color:#0084FF;font-weight:600;'>").append(pickupLocation).append("</td></tr>");
                html.append("<tr><td style='padding:6px 0;color:#888;'>顾客联系：</td><td><a href='tel:").append(order.getContactPhone()).append("'>").append(StrUtil.blankToDefault(order.getContactPhone(), "未填写")).append("</a> (").append(StrUtil.blankToDefault(order.getContactName(), "顾客")).append(")</td></tr>");
                if (StrUtil.isNotBlank(order.getRemark())) {
                    html.append("<tr><td style='padding:6px 0;color:#888;'>顾客备注：</td><td style='color:#FF7D00;'>").append(order.getRemark()).append("</td></tr>");
                }
                html.append("<tr><td style='padding:6px 0;color:#888;'>下单时间：</td><td>").append(DateUtil.formatDateTime(new Date())).append("</td></tr>");
                html.append("</table>");
                html.append("<div style='margin-top:16px;padding:8px 12px;background:#FFF7E8;border-radius:6px;font-size:12px;color:#FF7D00;'>核销时请根据跟团号或顾客手机号进行配货与核销。</div>");
                html.append("</div>");

                doSend(title, html.toString());
            } catch (Exception e) {
                log.error("发送快团订单微信通知异常, orderId={}", order.getId(), e);
            }
        });
    }

    /**
     * 底层发送逻辑
     */
    private void doSend(String title, String contentHtml) {
        JSONObject body = new JSONObject();
        body.set("token", token.trim());
        body.set("title", title);
        body.set("content", contentHtml);
        body.set("template", "html");
        if (StrUtil.isNotBlank(topic)) {
            body.set("topic", topic.trim());
        }

        try {
            HttpResponse resp = HttpRequest.post(PUSHPLUS_URL)
                    .body(body.toString())
                    .timeout(5000)
                    .execute();
            if (resp.isOk()) {
                log.info("PushPlus 微信通知发送成功: title={}", title);
            } else {
                log.warn("PushPlus 微信通知响应非200: status={}, body={}", resp.getStatus(), resp.body());
            }
        } catch (Exception e) {
            log.error("PushPlus 请求网络超时或异常: {}", e.getMessage());
        }
    }
}
