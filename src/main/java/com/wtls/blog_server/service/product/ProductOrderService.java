package com.wtls.blog_server.service.product;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wtls.blog_server.entity.product.Product;
import com.wtls.blog_server.entity.product.ProductOrder;
import com.wtls.blog_server.entity.user.User;
import com.wtls.blog_server.exception.BusinessException;
import com.wtls.blog_server.mapper.product.ProductMapper;
import com.wtls.blog_server.mapper.product.ProductOrderMapper;
import com.wtls.blog_server.mapper.user.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 普通商品订单与积分商城服务层
 * 负责普通/拼团商品下单结算、积分抵扣、库存扣减、支付回调履约及订单生命周期流转
 */
@Service
public class ProductOrderService {

    @Autowired
    private ProductOrderMapper orderMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 创建商品订单（支持单买/拼团、规格、件数、积分抵扣、配送费计算）
     *
     * @param userId       下单用户ID
     * @param productId    商品ID
     * @param address      收货地址
     * @param type         订单类型 (INDIVIDUAL: 单独购买, GROUP: 拼团购买)
     * @param pointsToUse  使用积分抵扣数量
     * @param spec         所选商品规格
     * @param quantity     购买数量
     * @param contactPhone 预留联系电话
     * @param remark       买家留言备注
     * @return 刚创建的待支付订单实体
     */
    @Transactional
    public ProductOrder createOrder(Long userId, Long productId, String address, String type, 
                                   Integer pointsToUse, String spec, Integer quantity, 
                                   String contactPhone, String remark) {
        Product product = productMapper.selectById(productId);
        if (ObjUtil.isNull(product)) {
            throw new BusinessException("未找到对应商品");
        }
        
        int buyCount = Math.max(1, ObjUtil.defaultIfNull(quantity, 1));

        // 1. 校验商品库存 (stock == -1 代表库存无限)
        if (ObjUtil.isNotNull(product.getStock()) && !ObjUtil.equals(product.getStock(), -1) && product.getStock() < buyCount) {
            throw new BusinessException("商品库存不足，当前剩余库存: " + product.getStock());
        }

        // 2. 计算计价单价与运费
        BigDecimal unitPrice = StrUtil.equals("GROUP", type) && ObjUtil.isNotNull(product.getGroupPrice()) 
                ? product.getGroupPrice() 
                : product.getPrice();
        BigDecimal deliveryFee = ObjUtil.defaultIfNull(product.getDeliveryFee(), BigDecimal.ZERO);
        
        // 应付原总价 = 单价 * 数量 + 配送费
        BigDecimal originalAmount = unitPrice.multiply(BigDecimal.valueOf(buyCount)).add(deliveryFee);
        BigDecimal deduction = BigDecimal.ZERO;
        
        // 3. 计算积分抵扣 (规则：100积分抵扣 1元人民币，至多抵扣至0.01元)
        int actualPointsToUse = 0;
        int inputPoints = NumberUtil.nullToZero(pointsToUse);
        if (inputPoints > 0) {
            User user = userMapper.findById(userId);
            if (ObjUtil.isNull(user) || NumberUtil.nullToZero(user.getPoints()) < inputPoints) {
                throw new BusinessException("可用积分余额不足");
            }
            
            deduction = BigDecimal.valueOf(inputPoints).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            
            // 确保抵扣后至少保留 0.01 元支付金额
            if (deduction.compareTo(originalAmount) >= 0) {
                deduction = originalAmount.subtract(new BigDecimal("0.01"));
                actualPointsToUse = deduction.multiply(BigDecimal.valueOf(100)).intValue();
            } else {
                actualPointsToUse = inputPoints;
            }
        }

        // 4. 构建并保存订单
        ProductOrder order = new ProductOrder();
        order.setId(IdUtil.fastSimpleUUID());
        order.setUserId(userId);
        order.setProductId(productId);
        order.setQuantity(buyCount);
        order.setAmount(originalAmount.subtract(deduction));
        order.setPointsUsed(actualPointsToUse);
        order.setStatus(0); // 0: 待支付
        order.setShippingAddress(address);
        order.setContactPhone(contactPhone);
        order.setRemark(remark);
        order.setDeliveryFee(deliveryFee);
        order.setOrderType(type);
        order.setSelectedSpec(spec);

        // 5. 冻结/预扣抵扣的积分
        if (actualPointsToUse > 0) {
            userMapper.deductPoints(userId, actualPointsToUse);
        }

        orderMapper.insert(order);
        return order;
    }

    /**
     * 兼容老接口的重载下单方法
     */
    public ProductOrder createOrder(Long userId, Long productId, String address, String type, Integer pointsToUse, String spec) {
        return createOrder(userId, productId, address, type, pointsToUse, spec, 1, null, null);
    }

    /**
     * 模拟支付成功（方便调试与测试环境核验）
     *
     * @param orderId 订单编号
     * @return 支付完成后的订单实体
     */
    @Transactional
    public ProductOrder mockPay(String orderId) {
        return handlePaymentSuccess(orderId);
    }

    /**
     * 核心支付成功履约逻辑：原子扣减商品库存、变更订单状态、发放推广人佣金/积分奖励
     *
     * @param orderId 订单编号
     * @return 更新后的订单实体
     */
    @Transactional
    public ProductOrder handlePaymentSuccess(String orderId) {
        ProductOrder order = orderMapper.selectById(orderId);
        if (ObjUtil.isNull(order) || !ObjUtil.equals(order.getStatus(), 0)) {
            throw new BusinessException("订单无效或已完成支付");
        }

        // 1. 原子扣减商品主库存
        int count = Math.max(1, NumberUtil.nullToZero(order.getQuantity()));
        int rows = productMapper.reduceStock(order.getProductId(), count);
        if (rows == 0) {
            throw new BusinessException("支付失败：商品库存不足");
        }

        // 2. 更新订单状态为已支付待发货/自提 (1)
        orderMapper.updateStatus(orderId, 1);

        // 3. 处理邀请人推荐奖励积分
        User buyer = userMapper.findById(order.getUserId());
        if (ObjUtil.isNotNull(buyer) && ObjUtil.isNotNull(buyer.getInvitedBy())) {
            userMapper.addPoints(buyer.getInvitedBy(), 100);
        }

        return orderMapper.selectById(orderId);
    }

    /**
     * 纯积分兑换商品
     *
     * @param userId    兑换用户ID
     * @param productId 兑换商品ID
     * @return 积分兑换订单
     */
    @Transactional
    public ProductOrder redeemWithPoints(Long userId, Long productId) {
        Product product = productMapper.selectById(productId);
        if (ObjUtil.isNull(product)) {
            throw new BusinessException("未找到对应商品");
        }

        if (ObjUtil.isNotNull(product.getStock()) && product.getStock() <= 0) {
            throw new BusinessException("商品库存不足，无法兑换");
        }

        // 固定消耗 1000 积分兑换商品
        int pointsNeeded = 1000;
        int rows = userMapper.deductPoints(userId, pointsNeeded);
        if (rows == 0) {
            throw new BusinessException("积分不足，兑换失败 (需 " + pointsNeeded + " 积分)");
        }
        
        // 扣减商品物理库存
        productMapper.reduceStock(productId, 1);

        // 构建已支付的积分兑换订单
        ProductOrder order = new ProductOrder();
        order.setId("POINTS_" + IdUtil.fastSimpleUUID().substring(0, 10));
        order.setUserId(userId);
        order.setProductId(productId);
        order.setAmount(BigDecimal.ZERO);
        order.setStatus(1); // 自动置为已付款
        order.setPayTime(LocalDateTime.now());
        order.setOrderType("INDIVIDUAL");

        orderMapper.insert(order);
        return order;
    }

    /**
     * 后台查询全量普通商品订单列表（按创建时间倒序）
     *
     * @return 订单列表
     */
    public List<ProductOrder> getAllOrders() {
        List<ProductOrder> orders = orderMapper.selectList(
            new LambdaQueryWrapper<ProductOrder>()
                .orderByDesc(ProductOrder::getCreateTime)
        );
        if (CollUtil.isNotEmpty(orders)) {
            orders.forEach(this::fillOrderProductDetails);
        }
        return CollUtil.defaultIfEmpty(orders, new ArrayList<>());
    }

    /**
     * 查询指定用户的所有普通商品订单
     *
     * @param userId 用户ID
     * @return 个人订单列表
     */
    public List<ProductOrder> getUserOrders(Long userId) {
        List<ProductOrder> orders = orderMapper.selectList(
            new LambdaQueryWrapper<ProductOrder>()
                .eq(ProductOrder::getUserId, userId)
                .orderByDesc(ProductOrder::getCreateTime)
        );
        if (CollUtil.isNotEmpty(orders)) {
            orders.forEach(this::fillOrderProductDetails);
        }
        return CollUtil.defaultIfEmpty(orders, new ArrayList<>());
    }

    /**
     * 根据主键查询单个订单详情并装配商品信息
     *
     * @param orderId 订单ID
     * @return 订单实体
     */
    public ProductOrder getOrderById(String orderId) {
        ProductOrder order = orderMapper.selectById(orderId);
        fillOrderProductDetails(order);
        return order;
    }

    /**
     * 为订单实体动态补全商品信息（商品名称、商品封面图、商品原单价）
     *
     * @param order 订单实体
     */
    private void fillOrderProductDetails(ProductOrder order) {
        if (ObjUtil.isNull(order) || ObjUtil.isNull(order.getProductId())) {
            return;
        }
        Product product = productMapper.selectById(order.getProductId());
        if (ObjUtil.isNotNull(product)) {
            order.setProductName(product.getName());
            order.setProductImage(product.getImage());
            order.setProductOriginalPrice(product.getPrice());
        }
    }

    /**
     * 商家发货/核销
     *
     * @param orderId 订单编号
     */
    public void shipOrder(String orderId) {
        // 0: 待支付, 1: 已支付, 2: 已取消, 3: 已发货/已完成
        orderMapper.updateStatus(orderId, 3);
    }

    /**
     * 用户删除或取消未支付/已失效的订单，并退还预扣积分
     *
     * @param userId  用户ID
     * @param orderId 订单编号
     */
    @Transactional
    public void cancelUnpaidOrder(Long userId, String orderId) {
        ProductOrder order = orderMapper.selectById(orderId);
        if (ObjUtil.isNull(order)) {
            throw new BusinessException("订单不存在");
        }
        if (!ObjUtil.equals(order.getUserId(), userId)) {
            throw new BusinessException("只能操作属于自己的订单");
        }
        // 仅允许删除未支付(0) 或 已取消(2) 的历史订单
        if (!ObjUtil.equals(order.getStatus(), 0) && !ObjUtil.equals(order.getStatus(), 2)) {
            throw new BusinessException("只能删除未支付或已取消的订单");
        }
        // 若订单属于未支付且使用了积分抵扣，安全归还被冻结的积分
        if (ObjUtil.equals(order.getStatus(), 0)) {
            int usedPoints = NumberUtil.nullToZero(order.getPointsUsed());
            if (usedPoints > 0) {
                userMapper.addPoints(userId, usedPoints);
            }
        }
        orderMapper.deleteById(orderId);
    }
}

