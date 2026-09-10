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
import com.wtls.blog_server.exception.BusinessException;
import com.wtls.blog_server.mapper.product.CampaignOrderItemMapper;
import com.wtls.blog_server.mapper.product.CampaignOrderMapper;
import com.wtls.blog_server.mapper.product.CampaignProductMapper;
import com.wtls.blog_server.mapper.product.DeliveryLocationMapper;
import com.wtls.blog_server.mapper.product.GroupBuyCampaignMapper;
import com.wtls.blog_server.mapper.product.ProductMapper;
import com.wtls.blog_server.mapper.user.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 团购活动（Campaign）业务服务层
 * 负责团购活动生命周期管理、活动商品关联、成团状态动态计算、跟团订单与库存结算等
 */
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
        boolean isActive = ObjUtil.equals(campaign.getStatus(), 1);
        boolean isExpired = ObjUtil.isNotNull(campaign.getEndTime()) && campaign.getEndTime().isBefore(LocalDateTime.now());
        if (isActive && isExpired) {
            campaign.setStatus(2);
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
                  .ne("status", 3); // 排除已取消/退款
        List<CampaignOrder> orders = orderMapper.selectList(orderQuery);
        long validPaidCount = orders.stream()
                .filter(o -> ObjUtil.isNotNull(o.getStatus()) && o.getStatus() >= 1)
                .count();
        int currentNum = (int) validPaidCount;
        campaign.setCurrentNum(currentNum);
        
        // 5. 动态计算成团状态 (扁平清晰语义)
        int target = ObjUtil.defaultIfNull(campaign.getTargetNum(), 0);
        boolean isEnded = ObjUtil.equals(campaign.getStatus(), 2);
        if (target <= 0) {
            campaign.setGroupStatus(1);
            campaign.setGroupStatusText(isEnded ? "已结团" : "火热拼团中");
        } else if (currentNum >= target) {
            campaign.setGroupStatus(1);
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
            if (ObjUtil.isNotNull(order.getStatus()) && order.getStatus() >= 1) {
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
            campaign.setStatus(0); // 默认待上架/草稿
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
        if (ObjUtil.isNull(campaign) || ObjUtil.equals(campaign.getStatus(), 2) || campaign.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("该团购活动不存在或已结束！");
        }
        
        order.setId(IdUtil.fastSimpleUUID());
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setStatus(0); // 0: 待支付
        
        // 生成当前团购活动下的跟团排号序号
        QueryWrapper<CampaignOrder> countQuery = new QueryWrapper<>();
        countQuery.eq("campaign_id", order.getCampaignId());
        long count = orderMapper.selectCount(countQuery);
        order.setFollowNumber((int) count + 1);
        
        orderMapper.insert(order);
        
        if (CollUtil.isNotEmpty(order.getItems())) {
            for (CampaignOrderItem item : order.getItems()) {
                item.setOrderId(order.getId());
                orderItemMapper.insert(item);
            }
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
        if (ObjUtil.isNull(order) || !ObjUtil.equals(order.getStatus(), 0)) {
            throw new BusinessException("订单无效或已完成支付");
        }

        // 1. 更新订单状态为已支付（1）
        order.setStatus(1);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 2. 校验并扣减团购专属库存
        QueryWrapper<CampaignOrderItem> itemQuery = new QueryWrapper<>();
        itemQuery.eq("order_id", orderId);
        List<CampaignOrderItem> items = orderItemMapper.selectList(itemQuery);
        if (CollUtil.isNotEmpty(items)) {
            for (CampaignOrderItem item : items) {
                QueryWrapper<CampaignProduct> cpQuery = new QueryWrapper<>();
                cpQuery.eq("campaign_id", order.getCampaignId())
                       .eq("product_id", item.getProductId());
                CampaignProduct cp = campaignProductMapper.selectOne(cpQuery);
                if (ObjUtil.isNotNull(cp) && !ObjUtil.equals(cp.getStockLimit(), -1)) {
                    if (cp.getStockLimit() < item.getQuantity()) {
                        throw new BusinessException("商品 " + item.getProductName() + " 库存不足，扣减失败");
                    }
                    cp.setStockLimit(cp.getStockLimit() - item.getQuantity());
                    campaignProductMapper.updateById(cp);
                }
            }
        }
        return order;
    }
    
    /**
     * 查询个人用户的所有跟团订单，并挂载对应团购活动详情与商品条目
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
                }
                order.setCampaign(c);
            }
        }
        return CollUtil.defaultIfEmpty(orders, new ArrayList<>());
    }

    /**
     * 用户删除或取消个人未支付/已失效的跟团订单
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
        if (!ObjUtil.equals(order.getUserId(), userId)) {
            throw new BusinessException("只能删除自己的跟团订单");
        }
        // 允许删除未支付(0) 或 已取消/已退款(3) 的跟团订单
        if (!ObjUtil.equals(order.getStatus(), 0) && !ObjUtil.equals(order.getStatus(), 3)) {
            throw new BusinessException("只能删除未支付或已取消的跟团订单");
        }
        // 1. 删除订单商品项
        QueryWrapper<CampaignOrderItem> itemQuery = new QueryWrapper<>();
        itemQuery.eq("order_id", orderId);
        orderItemMapper.delete(itemQuery);

        // 2. 删除主订单
        orderMapper.deleteById(orderId);
    }
}

