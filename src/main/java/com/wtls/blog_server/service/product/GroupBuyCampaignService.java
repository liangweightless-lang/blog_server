package com.wtls.blog_server.service.product;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wtls.blog_server.entity.product.CampaignOrder;
import com.wtls.blog_server.entity.product.CampaignOrderItem;
import com.wtls.blog_server.entity.product.CampaignProduct;
import com.wtls.blog_server.entity.product.GroupBuyCampaign;
import com.wtls.blog_server.entity.product.Product;
import com.wtls.blog_server.entity.user.User;
import com.wtls.blog_server.enums.CampaignGroupStatusEnum;
import com.wtls.blog_server.enums.CampaignOrderStatusEnum;
import com.wtls.blog_server.enums.CampaignStatusEnum;
import com.wtls.blog_server.enums.UserRoleEnum;
import com.wtls.blog_server.exception.BusinessException;
import com.wtls.blog_server.mapper.product.CampaignOrderItemMapper;
import com.wtls.blog_server.mapper.product.CampaignOrderMapper;
import com.wtls.blog_server.mapper.product.CampaignProductMapper;
import com.wtls.blog_server.mapper.product.DeliveryLocationMapper;
import com.wtls.blog_server.mapper.product.GroupBuyCampaignMapper;
import com.wtls.blog_server.mapper.product.ProductMapper;
import com.wtls.blog_server.mapper.user.UserMapper;
import com.wtls.blog_server.service.notice.OrderNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

/**
 * 团购活动（Campaign）业务服务层
 * 负责团购活动生命周期管理、活动商品关联、成团状态动态计算、跟团订单与库存结算等
 */
@Slf4j
@Service
public class GroupBuyCampaignService {

    @Autowired
    private GroupBuyCampaignMapper campaignMapper;

    @Autowired
    private CampaignProductMapper campaignProductMapper;

    @Autowired
    private CampaignOrderMapper orderMapper;

    @Autowired
    private CampaignOrderItemMapper orderItemMapper;
    
    @Autowired
    private DeliveryLocationMapper deliveryLocationMapper;
    
    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderNoticeService orderNoticeService;

    @Autowired(required = false)
    private com.wtls.blog_server.utils.RedisUtils redisUtils;

    /**
     * 查询所有团购活动列表（按创建时间倒序），并补全自提点、活动商品与成团人数详情
     *
     * @return 团购活动详情列表
     */
    public List<GroupBuyCampaign> getAllCampaigns() {
        QueryWrapper<GroupBuyCampaign> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        List<GroupBuyCampaign> campaigns = campaignMapper.selectList(queryWrapper);
        if (CollUtil.isNotEmpty(campaigns)) {
            for (GroupBuyCampaign campaign : campaigns) {
                fillCampaignDetails(campaign);
            }
        }
        return CollUtil.defaultIfEmpty(campaigns, new ArrayList<>());
    }

    /**
     * 根据主键查询单个团购活动详情
     *
     * @param id 活动ID
     * @return 团购活动详情（包含商品、成团进度等）
     */
    public GroupBuyCampaign getCampaignById(Long id) {
        GroupBuyCampaign campaign = campaignMapper.selectById(id);
        if (ObjUtil.isNotNull(campaign)) {
            fillCampaignDetails(campaign);
        }
        return campaign;
    }
    
    /**
     * 补全团购活动关联信息（自提点、商品列表、成团人数、动态拼团状态及参团用户头像）
     *
     * @param campaign 活动实体
     */
    private void fillCampaignDetails(GroupBuyCampaign campaign) {
        if (ObjUtil.isNull(campaign)) {
            return;
        }

        // 1. 活动到期自动流转为已结束状态 (2)
        boolean isActive = CampaignStatusEnum.ACTIVE.matches(campaign.getStatus());
        boolean isExpired = ObjUtil.isNotNull(campaign.getEndTime()) && campaign.getEndTime().isBefore(LocalDateTime.now());
        if (isActive && isExpired) {
            campaign.setStatus(CampaignStatusEnum.ENDED.getCode());
            campaign.setUpdateTime(LocalDateTime.now());
            campaignMapper.updateById(campaign);
        }

        // 2. 装配自提点信息
        if (ObjUtil.isNotNull(campaign.getDeliveryLocationId())) {
            campaign.setDeliveryLocation(deliveryLocationMapper.selectById(campaign.getDeliveryLocationId()));
        }

        // 3. 装配活动包含的商品信息
        QueryWrapper<CampaignProduct> productQuery = new QueryWrapper<>();
        productQuery.eq("campaign_id", campaign.getId()).orderByAsc("sort_order");
        List<CampaignProduct> cProducts = campaignProductMapper.selectList(productQuery);
        List<CampaignProduct> validProducts = new ArrayList<>();
        if (CollUtil.isNotEmpty(cProducts)) {
            for (CampaignProduct cp : cProducts) {
                Product p = productMapper.selectById(cp.getProductId());
                if (ObjUtil.isNotNull(p)) {
                    cp.setProduct(p);
                    validProducts.add(cp);
                }
            }
        }
        campaign.setProducts(validProducts);
        
        // 4. 统计已参团人数 (排除已取消订单，优先统计已支付的有效订单)
        QueryWrapper<CampaignOrder> orderQuery = new QueryWrapper<>();
        orderQuery.eq("campaign_id", campaign.getId())
                  .ne("status", CampaignOrderStatusEnum.CANCELLED.getCode()); // 排除已取消/退款
        List<CampaignOrder> orders = orderMapper.selectList(orderQuery);
        long validPaidCount = orders.stream()
                .filter(o -> CampaignOrderStatusEnum.isPaid(o.getStatus()))
                .count();
        int currentNum = (int) validPaidCount;
        campaign.setCurrentNum(currentNum);
        
        // 5. 动态计算成团状态 (标准枚举语义)
        int target = ObjUtil.defaultIfNull(campaign.getTargetNum(), 0);
        boolean isEnded = CampaignStatusEnum.ENDED.matches(campaign.getStatus());
        if (target <= 0) {
            campaign.setGroupStatus(CampaignGroupStatusEnum.SUCCESS.getCode());
            campaign.setGroupStatusText(isEnded ? "已结团" : "火热拼团中");
        } else if (currentNum >= target) {
            campaign.setGroupStatus(CampaignGroupStatusEnum.SUCCESS.getCode());
            campaign.setGroupStatusText("已成团");
        } else if (isEnded) {
            campaign.setGroupStatus(2);
            campaign.setGroupStatusText("拼团失败(未达标)");
        } else {
            campaign.setGroupStatus(0);
            campaign.setGroupStatusText("拼团中(差" + (target - currentNum) + "人)");
        }
        
        // 6. 收集前台展示的参团用户头像（至多前5位）
        List<String> avatars = new ArrayList<>();
        Set<Long> userIds = new HashSet<>();
        for (CampaignOrder order : orders) {
            if (CampaignOrderStatusEnum.isPaid(order.getStatus())) {
                userIds.add(order.getUserId());
            }
        }
        for (Long uid : userIds) {
            if (avatars.size() >= 5) {
                break;
            }
            User u = userMapper.selectById(uid);
            if (ObjUtil.isNotNull(u) && ObjUtil.isNotNull(u.getAvatarUrl())) {
                avatars.add(u.getAvatarUrl());
            }
        }
        campaign.setJoinedAvatars(avatars);
    }

    /**
     * 新建团购活动及其包含的商品明细
     *
     * @param campaign 活动实体
     */
    @Transactional
    public void createCampaign(GroupBuyCampaign campaign) {
        campaign.setCreateTime(LocalDateTime.now());
        campaign.setUpdateTime(LocalDateTime.now());
        if (ObjUtil.isNull(campaign.getStatus())) {
            campaign.setStatus(CampaignStatusEnum.DRAFT.getCode()); // 默认待上架/草稿
        }
        campaignMapper.insert(campaign);
        
        if (CollUtil.isNotEmpty(campaign.getProducts())) {
            int sort = 0;
            for (CampaignProduct cp : campaign.getProducts()) {
                cp.setCampaignId(campaign.getId());
                cp.setSortOrder(sort++);
                campaignProductMapper.insert(cp);
            }
        }
    }

    /**
     * 更新团购活动及其包含的商品配置
     *
     * @param campaign 活动实体
     */
    @Transactional
    public void updateCampaign(GroupBuyCampaign campaign) {
        campaign.setUpdateTime(LocalDateTime.now());
        campaignMapper.updateById(campaign);
        
        // 商品列表覆盖策略：先删除旧商品，再批量重新插入
        QueryWrapper<CampaignProduct> delQuery = new QueryWrapper<>();
        delQuery.eq("campaign_id", campaign.getId());
        campaignProductMapper.delete(delQuery);
        
        if (CollUtil.isNotEmpty(campaign.getProducts())) {
            int sort = 0;
            for (CampaignProduct cp : campaign.getProducts()) {
                cp.setCampaignId(campaign.getId());
                cp.setSortOrder(sort++);
                cp.setId(null); // 清空ID以便重新插入
                campaignProductMapper.insert(cp);
            }
        }
    }

    /**
     * 变更活动状态（如上架 1、下架/结团 2、草稿 0）
     *
     * @param id     活动ID
     * @param status 目标状态
     */
    public void updateCampaignStatus(Long id, Integer status) {
        GroupBuyCampaign campaign = new GroupBuyCampaign();
        campaign.setId(id);
        campaign.setStatus(status);
        campaign.setUpdateTime(LocalDateTime.now());
        campaignMapper.updateById(campaign);
    }

    /**
     * 删除团购活动及商品关联
     *
     * @param id 活动ID
     */
    @Transactional
    public void deleteCampaign(Long id) {
        // 校验：若活动下尚有待提货或待支付的有效订单，严禁删除活动（彻底防止死锁孤儿订单）
        QueryWrapper<CampaignOrder> orderCheckQuery = new QueryWrapper<>();
        orderCheckQuery.eq("campaign_id", id).in("status", 
                CampaignOrderStatusEnum.UNPAID.getCode(), 
                CampaignOrderStatusEnum.PAID_PENDING_PICKUP.getCode());
        long pendingOrdersCount = orderMapper.selectCount(orderCheckQuery);
        if (pendingOrdersCount > 0) {
            throw new com.wtls.blog_server.exception.BusinessException(
                "该活动下尚有 " + pendingOrdersCount + " 笔待提货或待支付订单，无法直接删除！请先核销或关闭订单，或将活动标记为【已结束】。"
            );
        }

        // 1. 删除关联商品记录
        QueryWrapper<CampaignProduct> delQuery = new QueryWrapper<>();
        delQuery.eq("campaign_id", id);
        campaignProductMapper.delete(delQuery);

        // 2. 删除活动主体
        campaignMapper.deleteById(id);
    }

    // --- 跟团订单业务方法 ---

    /**
     * 用户提交跟团订单
     *
     * @param order 跟团订单实体
     * @return 创建成功并附带订单ID的订单实体
     */
    @Transactional
    public CampaignOrder createOrder(CampaignOrder order) {
        GroupBuyCampaign campaign = campaignMapper.selectById(order.getCampaignId());
        if (ObjUtil.isNull(campaign) || CampaignStatusEnum.ENDED.matches(campaign.getStatus()) || campaign.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("该团购活动不存在或已结束！");
        }
        if (CollUtil.isEmpty(order.getItems())) {
            throw new BusinessException("订单商品明细不能为空！");
        }
        
        order.setId(IdUtil.fastSimpleUUID());
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setStatus(CampaignOrderStatusEnum.UNPAID.getCode()); // 0: 待支付
        
        // 1. 服务端严格重算总金额，防止前端恶意篡改金额（安全防线）
        BigDecimal calculatedTotal = BigDecimal.ZERO;
        for (CampaignOrderItem item : order.getItems()) {
            if (item.getProductId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BusinessException("商品条目或数量非法！");
            }
            // 查询活动配置的价格（若配置了团购专属价则用专属价，否则取原商品价格）
            QueryWrapper<CampaignProduct> cpQuery = new QueryWrapper<>();
            cpQuery.eq("campaign_id", order.getCampaignId()).eq("product_id", item.getProductId());
            CampaignProduct cp = campaignProductMapper.selectOne(cpQuery);
            
            Product product = productMapper.selectById(item.getProductId());
            if (product == null) {
                throw new BusinessException("所选商品不存在！");
            }
            
            BigDecimal unitPrice = (cp != null && cp.getGroupPrice() != null) ? cp.getGroupPrice() : product.getPrice();
            if (unitPrice == null) {
                unitPrice = BigDecimal.ZERO;
            }
            
            item.setPrice(unitPrice);
            item.setProductName(product.getName());
            item.setProductImage(product.getImage());
            calculatedTotal = calculatedTotal.add(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        order.setTotalAmount(calculatedTotal);
        
        // 2. 生成当前团购活动下的跟团排号序号（优先使用 Redis 原子递增防并发重复，同时做 DB 计数校准兜底）
        int nextFollowNumber = 1;
        QueryWrapper<CampaignOrder> countQuery = new QueryWrapper<>();
        countQuery.eq("campaign_id", order.getCampaignId());
        long dbOrderCount = orderMapper.selectCount(countQuery);
        
        if (redisUtils != null) {
            try {
                String seqKey = "campaign:follow_seq:" + order.getCampaignId();
                long redisSeq = redisUtils.incr(seqKey, 1);
                // 若 Redis 序列比 DB 当前已落库总数小（如刚重启或缓存清理），以 DB 为准校准并同步回 Redis
                if (redisSeq <= dbOrderCount) {
                    redisSeq = dbOrderCount + 1;
                    redisUtils.set(seqKey, redisSeq);
                }
                nextFollowNumber = (int) redisSeq;
            } catch (Exception e) {
                log.warn("Redis 生成跟团序号异常，降级为 DB 计数值: {}", e.getMessage());
                nextFollowNumber = (int) dbOrderCount + 1;
            }
        } else {
            nextFollowNumber = (int) dbOrderCount + 1;
        }
        order.setFollowNumber(nextFollowNumber);
        
        orderMapper.insert(order);
        
        for (CampaignOrderItem item : order.getItems()) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }
        return order;
    }
    
    /**
     * 查询指定团购活动下的所有订单及商品明细
     *
     * @param campaignId 活动ID
     * @return 订单列表
     */
    public List<CampaignOrder> getOrdersByCampaignId(Long campaignId) {
        QueryWrapper<CampaignOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("campaign_id", campaignId).orderByDesc("create_time");
        List<CampaignOrder> orders = orderMapper.selectList(queryWrapper);
        if (CollUtil.isNotEmpty(orders)) {
            for (CampaignOrder order : orders) {
                QueryWrapper<CampaignOrderItem> itemQuery = new QueryWrapper<>();
                itemQuery.eq("order_id", order.getId());
                order.setItems(orderItemMapper.selectList(itemQuery));
            }
        }
        return CollUtil.defaultIfEmpty(orders, new ArrayList<>());
    }
    
    /**
     * 更新跟团订单状态
     *
     * @param orderId 订单ID
     * @param status  目标状态 (0:待支付, 1:已支付待自提, 2:已核销自提, 3:已取消)
     */
    public void updateOrderStatus(String orderId, Integer status) {
        CampaignOrder order = new CampaignOrder();
        order.setId(orderId);
        order.setStatus(status);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }
    
    /**
     * 支付成功回调处理：更新订单状态为已支付，并原子扣减团购商品活动库存
     *
     * @param orderId 订单ID
     * @return 处理后的订单
     */
    @Transactional
    public CampaignOrder handlePaymentSuccess(String orderId) {
        CampaignOrder order = orderMapper.selectById(orderId);
        if (ObjUtil.isNull(order) || !CampaignOrderStatusEnum.UNPAID.matches(order.getStatus())) {
            throw new BusinessException("订单无效或已完成支付");
        }

        // 1. 更新订单状态为已支付待提货
        order.setStatus(CampaignOrderStatusEnum.PAID_PENDING_PICKUP.getCode());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 2. 原子扣减团购专属库存（防止高并发超卖与负库存）
        QueryWrapper<CampaignOrderItem> itemQuery = new QueryWrapper<>();
        itemQuery.eq("order_id", orderId);
        List<CampaignOrderItem> items = orderItemMapper.selectList(itemQuery);
        if (CollUtil.isNotEmpty(items)) {
            for (CampaignOrderItem item : items) {
                QueryWrapper<CampaignProduct> cpQuery = new QueryWrapper<>();
                cpQuery.eq("campaign_id", order.getCampaignId())
                       .eq("product_id", item.getProductId());
                CampaignProduct cp = campaignProductMapper.selectOne(cpQuery);
                if (ObjUtil.isNotNull(cp)) {
                    int affectedRows = campaignProductMapper.reduceStock(cp.getId(), item.getQuantity());
                    if (affectedRows == 0) {
                        log.warn("团购活动商品库存扣减失败（可能已售罄）：campaignId={}, productId={}, quantity={}", 
                                order.getCampaignId(), item.getProductId(), item.getQuantity());
                    }
                }
            }
        }

        // 3. 异步推送微信跟团提醒给店长/团长
        GroupBuyCampaign campaign = campaignMapper.selectById(order.getCampaignId());
        orderNoticeService.sendCampaignOrderNotice(order, campaign, items);

        return order;
    }
    
    /**
     * 查询个人用户的所有跟团订单，并挂载对应团购活动详情与商品条目
     * 具备自动自愈机制：若检测到活动已被删除的孤儿脏数据，自动纠偏为已失效(3)，消除小红点死锁
     *
     * @param userId 用户ID
     * @return 个人跟团订单列表
     */
    public List<CampaignOrder> getMyOrders(Long userId) {
        QueryWrapper<CampaignOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).orderByDesc("create_time");
        List<CampaignOrder> orders = orderMapper.selectList(queryWrapper);
        if (CollUtil.isNotEmpty(orders)) {
            for (CampaignOrder order : orders) {
                QueryWrapper<CampaignOrderItem> itemQuery = new QueryWrapper<>();
                itemQuery.eq("order_id", order.getId());
                order.setItems(orderItemMapper.selectList(itemQuery));
                
                GroupBuyCampaign c = campaignMapper.selectById(order.getCampaignId());
                if (ObjUtil.isNotNull(c)) {
                    fillCampaignDetails(c);
                } else {
                    // 孤儿订单自动自愈机制：活动已被删除，导致订单处于无主死锁状态
                    // 若状态仍为待支付或待提货，自动流转为已失效/取消并持久化更新到数据库
                    if (CampaignOrderStatusEnum.UNPAID.matches(order.getStatus()) || 
                        CampaignOrderStatusEnum.PAID_PENDING_PICKUP.matches(order.getStatus())) {
                        order.setStatus(CampaignOrderStatusEnum.CANCELLED.getCode());
                        order.setUpdateTime(LocalDateTime.now());
                        orderMapper.updateById(order);
                    }
                }
                order.setCampaign(c);
            }
        }
        return CollUtil.defaultIfEmpty(orders, new ArrayList<>());
    }

    /**
     * 用户删除或取消个人未支付/已失效的跟团订单，或管理员清理孤儿订单
     *
     * @param userId  用户ID
     * @param orderId 订单ID
     */
    @Transactional
    public void cancelUnpaidOrder(Long userId, String orderId) {
        CampaignOrder order = orderMapper.selectById(orderId);
        if (ObjUtil.isNull(order)) {
            throw new BusinessException("跟团订单不存在");
        }

        // 检查操作人是否为管理员 (ADMIN / CREATOR 拥有运维特权)
        User currentUser = userMapper.selectById(userId);
        boolean isAdmin = ObjUtil.isNotNull(currentUser) && UserRoleEnum.isAdminOrCreator(currentUser.getRole());

        if (!ObjUtil.equals(order.getUserId(), userId) && !isAdmin) {
            throw new BusinessException("只能删除自己的跟团订单");
        }

        // 检查是否为孤儿订单 (关联的活动已被删除)
        GroupBuyCampaign c = campaignMapper.selectById(order.getCampaignId());
        boolean isOrphan = (c == null);

        // 如果既不是孤儿订单、也不是管理员清理，则仅允许删除未支付或已取消的跟团订单
        if (!isOrphan && !isAdmin && 
            !CampaignOrderStatusEnum.UNPAID.matches(order.getStatus()) && 
            !CampaignOrderStatusEnum.CANCELLED.matches(order.getStatus())) {
            throw new BusinessException("只能删除未支付或已取消的跟团订单");
        }

        // 1. 删除订单商品项
        QueryWrapper<CampaignOrderItem> itemQuery = new QueryWrapper<>();
        itemQuery.eq("order_id", orderId);
        orderItemMapper.delete(itemQuery);

        // 2. 删除主订单
        orderMapper.deleteById(orderId);
    }

    /**
     * 一键清理所有孤儿跟团订单（活动已被删除的脏数据）
     *
     * @return 清理的孤儿订单数量
     */
    @Transactional
    public int cleanAllOrphanOrders() {
        List<CampaignOrder> allOrders = orderMapper.selectList(null);
        int cleanedCount = 0;
        if (CollUtil.isNotEmpty(allOrders)) {
            for (CampaignOrder order : allOrders) {
                if (ObjUtil.isNull(order.getCampaignId()) || campaignMapper.selectById(order.getCampaignId()) == null) {
                    // 删除孤儿订单关联条目
                    QueryWrapper<CampaignOrderItem> itemQuery = new QueryWrapper<>();
                    itemQuery.eq("order_id", order.getId());
                    orderItemMapper.delete(itemQuery);
                    // 删除订单
                    orderMapper.deleteById(order.getId());
                    cleanedCount++;
                }
            }
        }
        return cleanedCount;
    }

    private static final java.util.concurrent.ConcurrentHashMap<String, Long> LAST_NOTIFY_TIME_MAP = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 用户前端确认已完成付款，主动触发企业微信通知管理员进行核实与核销
     *
     * @param userId  当前登录用户ID
     * @param orderId 跟团订单ID
     */
    public void notifyUserPaid(Long userId, String orderId) {
        CampaignOrder order = orderMapper.selectById(orderId);
        if (ObjUtil.isNull(order)) {
            throw new BusinessException("跟团订单不存在");
        }
        if (!ObjUtil.equals(order.getUserId(), userId)) {
            throw new BusinessException("无权操作他人的订单");
        }

        // 已经核销(COMPLETED)或已取消(CANCELLED)不再触发通知
        if (CampaignOrderStatusEnum.COMPLETED.matches(order.getStatus()) || 
            CampaignOrderStatusEnum.CANCELLED.matches(order.getStatus())) {
            return;
        }

        // 防刷防抖：30秒内同一订单只触发一次推送
        long now = System.currentTimeMillis();
        Long lastTime = LAST_NOTIFY_TIME_MAP.get(orderId);
        if (lastTime != null && (now - lastTime) < 30000) {
            return;
        }
        LAST_NOTIFY_TIME_MAP.put(orderId, now);

        // 获取活动与商品明细
        GroupBuyCampaign campaign = campaignMapper.selectById(order.getCampaignId());
        QueryWrapper<CampaignOrderItem> itemQuery = new QueryWrapper<>();
        itemQuery.eq("order_id", orderId);
        List<CampaignOrderItem> items = orderItemMapper.selectList(itemQuery);

        // 触发推送，isUserPaidConfirmation = true 标明待手动核销
        orderNoticeService.sendCampaignOrderNotice(order, campaign, items, true);
    }
}

