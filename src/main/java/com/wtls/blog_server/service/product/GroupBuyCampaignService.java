package com.wtls.blog_server.service.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wtls.blog_server.entity.product.CampaignOrder;
import com.wtls.blog_server.entity.product.CampaignOrderItem;
import com.wtls.blog_server.entity.product.CampaignProduct;
import com.wtls.blog_server.entity.product.DeliveryLocation;
import com.wtls.blog_server.entity.product.GroupBuyCampaign;
import com.wtls.blog_server.entity.product.Product;
import com.wtls.blog_server.mapper.product.CampaignOrderItemMapper;
import com.wtls.blog_server.mapper.product.CampaignOrderMapper;
import com.wtls.blog_server.mapper.product.CampaignProductMapper;
import com.wtls.blog_server.mapper.product.DeliveryLocationMapper;
import com.wtls.blog_server.mapper.product.GroupBuyCampaignMapper;
import com.wtls.blog_server.mapper.product.ProductMapper;
import com.wtls.blog_server.mapper.user.UserMapper;
import com.wtls.blog_server.entity.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

    public List<GroupBuyCampaign> getAllCampaigns() {
        QueryWrapper<GroupBuyCampaign> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        List<GroupBuyCampaign> campaigns = campaignMapper.selectList(queryWrapper);
        for (GroupBuyCampaign campaign : campaigns) {
            fillCampaignDetails(campaign);
        }
        return campaigns;
    }

    public GroupBuyCampaign getCampaignById(Long id) {
        GroupBuyCampaign campaign = campaignMapper.selectById(id);
        if (campaign != null) {
            fillCampaignDetails(campaign);
        }
        return campaign;
    }
    
    private void fillCampaignDetails(GroupBuyCampaign campaign) {
        if (campaign.getDeliveryLocationId() != null) {
            campaign.setDeliveryLocation(deliveryLocationMapper.selectById(campaign.getDeliveryLocationId()));
        }
        QueryWrapper<CampaignProduct> productQuery = new QueryWrapper<>();
        productQuery.eq("campaign_id", campaign.getId()).orderByAsc("sort_order");
        List<CampaignProduct> cProducts = campaignProductMapper.selectList(productQuery);
        List<CampaignProduct> validProducts = new java.util.ArrayList<>();
        for (CampaignProduct cp : cProducts) {
            Product p = productMapper.selectById(cp.getProductId());
            if (p != null) {
                cp.setProduct(p);
                validProducts.add(cp);
            }
        }
        campaign.setProducts(validProducts);
        
        // 统计已参团人数 (独立的订单数)
        QueryWrapper<CampaignOrder> orderQuery = new QueryWrapper<>();
        orderQuery.eq("campaign_id", campaign.getId());
        List<CampaignOrder> orders = orderMapper.selectList(orderQuery);
        campaign.setCurrentNum(orders.size());
        
        // 收集参团用户的头像
        List<String> avatars = new java.util.ArrayList<>();
        java.util.Set<Long> userIds = new java.util.HashSet<>();
        for (CampaignOrder order : orders) {
            userIds.add(order.getUserId());
        }
        for (Long uid : userIds) {
            if (avatars.size() >= 5) break; // 最多返回5个头像用于前台展示
            User u = userMapper.selectById(uid);
            if (u != null && u.getAvatarUrl() != null) {
                avatars.add(u.getAvatarUrl());
            }
        }
        campaign.setJoinedAvatars(avatars);
    }

    @Transactional
    public void createCampaign(GroupBuyCampaign campaign) {
        campaign.setCreateTime(LocalDateTime.now());
        campaign.setUpdateTime(LocalDateTime.now());
        if (campaign.getStatus() == null) {
            campaign.setStatus(0);
        }
        campaignMapper.insert(campaign);
        
        if (campaign.getProducts() != null) {
            int sort = 0;
            for (CampaignProduct cp : campaign.getProducts()) {
                cp.setCampaignId(campaign.getId());
                cp.setSortOrder(sort++);
                campaignProductMapper.insert(cp);
            }
        }
    }

    @Transactional
    public void updateCampaign(GroupBuyCampaign campaign) {
        campaign.setUpdateTime(LocalDateTime.now());
        campaignMapper.updateById(campaign);
        
        // Simple strategy: delete all old products and insert new ones
        QueryWrapper<CampaignProduct> delQuery = new QueryWrapper<>();
        delQuery.eq("campaign_id", campaign.getId());
        campaignProductMapper.delete(delQuery);
        
        if (campaign.getProducts() != null) {
            int sort = 0;
            for (CampaignProduct cp : campaign.getProducts()) {
                cp.setCampaignId(campaign.getId());
                cp.setSortOrder(sort++);
                // Clear ID so it can be inserted
                cp.setId(null);
                campaignProductMapper.insert(cp);
            }
        }
    }

    public void updateCampaignStatus(Long id, Integer status) {
        GroupBuyCampaign campaign = new GroupBuyCampaign();
        campaign.setId(id);
        campaign.setStatus(status);
        campaign.setUpdateTime(LocalDateTime.now());
        campaignMapper.updateById(campaign);
    }

    @Transactional
    public void deleteCampaign(Long id) {
        // delete products
        QueryWrapper<CampaignProduct> delQuery = new QueryWrapper<>();
        delQuery.eq("campaign_id", id);
        campaignProductMapper.delete(delQuery);
        // delete campaign
        campaignMapper.deleteById(id);
    }

    // --- Order methods ---

    @Transactional
    public CampaignOrder createOrder(CampaignOrder order) {
        GroupBuyCampaign campaign = campaignMapper.selectById(order.getCampaignId());
        if (campaign == null || campaign.getStatus() == 2 || campaign.getEndTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("该团购活动不存在或已结束！");
        }
        
        order.setId(UUID.randomUUID().toString().replace("-", ""));
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setStatus(0); // unpaid
        
        // Generate follow number for this campaign
        QueryWrapper<CampaignOrder> countQuery = new QueryWrapper<>();
        countQuery.eq("campaign_id", order.getCampaignId());
        long count = orderMapper.selectCount(countQuery);
        order.setFollowNumber((int) count + 1);
        
        orderMapper.insert(order);
        
        if (order.getItems() != null) {
            for (CampaignOrderItem item : order.getItems()) {
                item.setOrderId(order.getId());
                orderItemMapper.insert(item);
            }
        }
        return order;
    }
    
    public List<CampaignOrder> getOrdersByCampaignId(Long campaignId) {
        QueryWrapper<CampaignOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("campaign_id", campaignId).orderByDesc("create_time");
        List<CampaignOrder> orders = orderMapper.selectList(queryWrapper);
        for (CampaignOrder order : orders) {
            QueryWrapper<CampaignOrderItem> itemQuery = new QueryWrapper<>();
            itemQuery.eq("order_id", order.getId());
            order.setItems(orderItemMapper.selectList(itemQuery));
        }
        return orders;
    }
    
    public void updateOrderStatus(String orderId, Integer status) {
        CampaignOrder order = new CampaignOrder();
        order.setId(orderId);
        order.setStatus(status);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }
    
    @Transactional
    public CampaignOrder handlePaymentSuccess(String orderId) {
        CampaignOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 0) {
            throw new RuntimeException("Invalid order or already paid");
        }

        // 1. 更新订单状态为已支付（1）
        order.setStatus(1);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 2. 安全扣减团购商品库存
        QueryWrapper<CampaignOrderItem> itemQuery = new QueryWrapper<>();
        itemQuery.eq("order_id", orderId);
        List<CampaignOrderItem> items = orderItemMapper.selectList(itemQuery);
        for (CampaignOrderItem item : items) {
            QueryWrapper<CampaignProduct> cpQuery = new QueryWrapper<>();
            cpQuery.eq("campaign_id", order.getCampaignId())
                   .eq("product_id", item.getProductId());
            CampaignProduct cp = campaignProductMapper.selectOne(cpQuery);
            if (cp != null && cp.getStockLimit() != -1) {
                if (cp.getStockLimit() < item.getQuantity()) {
                    throw new RuntimeException("商品 " + item.getProductName() + " 库存不足，扣减失败");
                }
                cp.setStockLimit(cp.getStockLimit() - item.getQuantity());
                campaignProductMapper.updateById(cp);
            }
        }
        return order;
    }
    
    public List<CampaignOrder> getMyOrders(Long userId) {
        QueryWrapper<CampaignOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).orderByDesc("create_time");
        List<CampaignOrder> orders = orderMapper.selectList(queryWrapper);
        for (CampaignOrder order : orders) {
            QueryWrapper<CampaignOrderItem> itemQuery = new QueryWrapper<>();
            itemQuery.eq("order_id", order.getId());
            order.setItems(orderItemMapper.selectList(itemQuery));
            
            GroupBuyCampaign c = campaignMapper.selectById(order.getCampaignId());
            if (c != null && c.getDeliveryLocationId() != null) {
                c.setDeliveryLocation(deliveryLocationMapper.selectById(c.getDeliveryLocationId()));
            }
            order.setCampaign(c);
        }
        return orders;
    }

    @Transactional
    public void cancelUnpaidOrder(Long userId, String orderId) {
        CampaignOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("跟团订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("只能删除自己的跟团订单");
        }
        if (order.getStatus() != 0) {
            throw new RuntimeException("只能删除未支付的跟团订单");
        }
        // 删除订单项
        QueryWrapper<CampaignOrderItem> itemQuery = new QueryWrapper<>();
        itemQuery.eq("order_id", orderId);
        orderItemMapper.delete(itemQuery);
        // 删除主订单
        orderMapper.deleteById(orderId);
    }
}
