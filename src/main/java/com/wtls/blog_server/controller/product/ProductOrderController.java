package com.wtls.blog_server.controller.product;

import com.wtls.blog_server.common.Result;
import com.wtls.blog_server.entity.product.ProductOrder;
import com.wtls.blog_server.service.product.ProductOrderService;
import com.wtls.blog_server.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.wtls.blog_server.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
@Tag(name = "订单与拼团管理", description = "处理商品下单、支付、兑换及拼团状态查询")
public class ProductOrderController {

    @Autowired
    private ProductOrderService orderService;

    @Schema(description = "创建订单请求参数")
    public static class CreateOrderRequest {
        @Schema(description = "商品ID", example = "1")
        @NotNull(message = "产品ID不能为空")
        public Long productId;
        @Schema(description = "收货地址", example = "北京市朝阳区...")
        @NotBlank(message = "收货地址不能为空")
        public String address;
        @Schema(description = "订单类型", example = "GROUP", allowableValues = {"INDIVIDUAL", "GROUP"})
        public String type; // INDIVIDUAL, GROUP, etc.
        @Schema(description = "使用的积分数量", example = "100")
        public Integer pointsToUse;
        @Schema(description = "选中的规格", example = "标准,去冰")
        public String spec;
        @Schema(description = "购买数量", example = "1")
        public Integer quantity;
        @Schema(description = "联系手机号", example = "13800000000")
        public String contactPhone;
        @Schema(description = "顾客备注", example = "放门口自提")
        public String remark;
    }

    @PostMapping("/create")
    @Operation(summary = "创建新订单", description = "支持个人购买和发起拼团")
    public Result<ProductOrder> createOrder(@RequestHeader("Authorization") String authHeader, @Valid @RequestBody CreateOrderRequest req) {
        Long userId = JwtUtils.getUserIdFromHeader(authHeader);
        ProductOrder order = orderService.createOrder(userId, req.productId, req.address, req.type == null ? "INDIVIDUAL" : req.type, req.pointsToUse, req.spec, req.quantity, req.contactPhone, req.remark);
        return Result.success(order);
    }

    @PostMapping("/{orderId}/pay")
    @Operation(summary = "模拟支付", description = "将订单状态从待支付更新为已支付")
    public Result<ProductOrder> payOrder(@RequestHeader("Authorization") String authHeader, @PathVariable String orderId) {
        JwtUtils.getUserIdFromHeader(authHeader); // 校验登录状态
        ProductOrder order = orderService.mockPay(orderId);
        return Result.success(order);
    }

    @PostMapping("/redeem")
    public Result<ProductOrder> redeem(@RequestHeader("Authorization") String authHeader, @Valid @RequestBody CreateOrderRequest req) {
        Long userId = JwtUtils.getUserIdFromHeader(authHeader);
        ProductOrder order = orderService.redeemWithPoints(userId, req.productId);
        return Result.success(order);
    }

    @GetMapping("/me")
    public Result<List<ProductOrder>> getMyOrders(@RequestHeader("Authorization") String authHeader) {
        Long userId = JwtUtils.getUserIdFromHeader(authHeader);
        return Result.success(orderService.getUserOrders(userId));
    }

    @GetMapping
    public Result<List<ProductOrder>> getAllOrders(@RequestHeader("Authorization") String authHeader) {
        JwtUtils.checkAdminOrCreator(authHeader);
        return Result.success(orderService.getAllOrders());
    }

    @PostMapping("/{orderId}/ship")
    public Result<String> shipOrder(@RequestHeader("Authorization") String authHeader, @PathVariable String orderId) {
        JwtUtils.checkAdminOrCreator(authHeader);
        orderService.shipOrder(orderId);
        return Result.success("Order shipped");
    }

    @PostMapping("/{orderId}/confirm-pay")
    @Operation(summary = "管理员或主理人手动确认收款", description = "核对微信商家码到账后，将订单手动流转为已支付")
    public Result<String> confirmPay(@RequestHeader("Authorization") String authHeader, @PathVariable String orderId) {
        JwtUtils.checkAdminOrCreator(authHeader);
        orderService.handlePaymentSuccess(orderId);
        return Result.success("订单已成功确认为已支付状态");
    }

    @DeleteMapping("/{orderId}")
    @Operation(summary = "取消或删除订单", description = "用户取消未支付订单或删除已取消订单")
    public Result<String> deleteUnpaidOrder(@RequestHeader("Authorization") String authHeader, @PathVariable String orderId) {
        Long userId = JwtUtils.getUserIdFromHeader(authHeader);
        orderService.cancelUnpaidOrder(userId, orderId);
        return Result.success("订单已成功取消/删除");
    }
}
