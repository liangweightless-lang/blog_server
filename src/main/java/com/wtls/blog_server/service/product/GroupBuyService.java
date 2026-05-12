package com.wtls.blog_server.service.product;

import com.wtls.blog_server.entity.product.GroupBuy;
import com.wtls.blog_server.entity.product.GroupBuyMember;
import com.wtls.blog_server.entity.product.ProductOrder;
import com.wtls.blog_server.mapper.product.GroupBuyMapper;
import com.wtls.blog_server.mapper.product.ProductOrderMapper;
import com.wtls.blog_server.mapper.user.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class GroupBuyService {

    @Autowired
    private GroupBuyMapper groupBuyMapper;

    @Autowired
    private ProductOrderService orderService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductOrderMapper orderMapper;

    public List<GroupBuy> getActiveGroups() {
        return groupBuyMapper.findActiveGroupsWithNames();
    }

    @Transactional
    public GroupBuy startGroup(Long userId, Long productId, String address) {
        // Logic: Monday only (Optional: comment out if you want to test any day)
        // LocalDateTime now = LocalDateTime.now();
        // if (now.getDayOfWeek() != DayOfWeek.MONDAY) {
        //    throw new RuntimeException("拼团活动仅在周一开启！");
        // }

        // Create the initiator's order first
        ProductOrder order = orderService.createOrder(userId, productId, address, "GROUP", 0, null);
        // Note: For initiator, we might want to charge them later or mark as 'Pending Group'
        
        GroupBuy gb = new GroupBuy();
        gb.setProductId(productId);
        gb.setInitiatorId(userId);
        gb.setRequiredNum(8);
        gb.setExpireTime(LocalDateTime.now().plusHours(24)); // 24h limit
        
        groupBuyMapper.insert(gb);
        
        GroupBuyMember member = new GroupBuyMember();
        member.setGroupId(gb.getId());
        member.setUserId(userId);
        member.setOrderId(order.getId());
        groupBuyMapper.insertMember(member);
        
        log.info("[拼团] 用户 {} 发起了拼团, GroupID: {}, 关联订单: {}", userId, gb.getId(), order.getId());
        return gb;
    }

    @Transactional
    public GroupBuy joinGroup(Long userId, Long groupId, String address) {
        GroupBuy gb = groupBuyMapper.selectById(groupId);
        if (gb == null || gb.getStatus() != 0 || gb.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("该拼团已失效或已结束");
        }
        
        if (groupBuyMapper.checkMember(groupId, userId) > 0) {
            throw new RuntimeException("您已经参加过该拼团了");
        }

        // Create the participant's order
        ProductOrder order = orderService.createOrder(userId, gb.getProductId(), address, "GROUP", 0, null);
        
        GroupBuyMember member = new GroupBuyMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setOrderId(order.getId());
        groupBuyMapper.insertMember(member);
        
        groupBuyMapper.incrementCount(groupId);
        
        // Check if successful
        GroupBuy updated = groupBuyMapper.selectById(groupId);
        if (updated.getCurrentNum() >= updated.getRequiredNum()) {
            groupBuyMapper.updateStatus(groupId, 1); // Success
            
            // Reward initiator: "赠送一个" -> give 500 points (example) or enough points to buy next one
            userMapper.addPoints(updated.getInitiatorId(), 500);
            log.info("[拼团] 拼团成功! GroupID: {}, 已奖励发起人 {}", groupId, updated.getInitiatorId());
        }
        
        log.info("[拼团] 用户 {} 成功加入拼团 {}, 当前人数: {}/{}", userId, groupId, updated.getCurrentNum(), updated.getRequiredNum());
        return updated;
    }

    public List<GroupBuy> getUserGroups(Long userId) {
        return groupBuyMapper.findByUserIdWithNames(userId);
    }

    public List<GroupBuy> getAllGroups() {
        return groupBuyMapper.findAllWithNames();
    }

    public GroupBuy getGroupById(Long id) {
        return groupBuyMapper.selectById(id);
    }

    @Transactional
    public void forceSuccess(Long groupId) {
        GroupBuy gb = groupBuyMapper.selectById(groupId);
        if (gb == null || gb.getStatus() != 0) {
            throw new RuntimeException("Group is not active or already finished");
        }
        
        gb.setStatus(1); // Success
        groupBuyMapper.updateById(gb);
        
        // Complete all orders
        List<GroupBuyMember> members = groupBuyMapper.findMembersByGroupId(groupId);
        for (GroupBuyMember member : members) {
            ProductOrder order = orderMapper.findById(member.getOrderId());
            if (order != null && order.getStatus() == 1) { // Paid
                order.setStatus(3); // Shipped or Finished
                orderMapper.updateStatus(order.getId(), 3);
            }
        }
        log.warn("[ADMIN] 手动强制拼团成功: GroupID={}", groupId);
    }

    @Transactional
    public void forceFail(Long groupId) {
        GroupBuy gb = groupBuyMapper.selectById(groupId);
        if (gb == null || gb.getStatus() != 0) {
            throw new RuntimeException("Group is not active or already finished");
        }
        
        gb.setStatus(2); // Failed
        groupBuyMapper.updateById(gb);
        
        // Refund and cancel
        List<GroupBuyMember> members = groupBuyMapper.findMembersByGroupId(groupId);
        for (GroupBuyMember member : members) {
            ProductOrder order = orderMapper.findById(member.getOrderId());
            if (order != null) {
                orderMapper.updateStatus(order.getId(), 2); // Cancelled
                if (order.getPointsUsed() != null && order.getPointsUsed() > 0) {
                    userMapper.addPoints(order.getUserId(), order.getPointsUsed());
                }
            }
        }
        log.warn("[ADMIN] 手动强制拼团失败并退款: GroupID={}", groupId);
    }

    @Transactional
    public void checkExpiredGroups() {
        List<GroupBuy> expiredGroups = groupBuyMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<GroupBuy>()
                .eq(GroupBuy::getStatus, 0)
                .lt(GroupBuy::getExpireTime, LocalDateTime.now())
        );
        for (GroupBuy gb : expiredGroups) {
            // 1. Update group status to Failed (2)
            groupBuyMapper.updateStatus(gb.getId(), 2);
            
            // 2. Handle members
            List<GroupBuyMember> members = groupBuyMapper.findMembersByGroupId(gb.getId());
            for (GroupBuyMember member : members) {
                // Cancel the order
                orderMapper.updateStatus(member.getOrderId(), 2); // 2: Cancelled/Failed
                
                // Refund points if any
                ProductOrder order = orderMapper.findById(member.getOrderId());
                if (order != null && order.getPointsUsed() != null && order.getPointsUsed() > 0) {
                    userMapper.addPoints(order.getUserId(), order.getPointsUsed());
                    log.info("[拼团结算] 退回用户 {} 积分: {}", order.getUserId(), order.getPointsUsed());
                }
            }
            log.warn("[拼团结算] 拼团已过期自动结算为失败: GroupID={}", gb.getId());
        }
    }
}
