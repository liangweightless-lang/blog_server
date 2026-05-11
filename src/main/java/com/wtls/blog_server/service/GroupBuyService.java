package com.wtls.blog_server.service;

import com.wtls.blog_server.entity.GroupBuy;
import com.wtls.blog_server.entity.GroupBuyMember;
import com.wtls.blog_server.entity.ProductOrder;
import com.wtls.blog_server.mapper.GroupBuyMapper;
import com.wtls.blog_server.mapper.ProductOrderMapper;
import com.wtls.blog_server.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class GroupBuyService {

    @Autowired
    private GroupBuyMapper groupBuyMapper;

    @Autowired
    private ProductOrderService orderService;

    @Autowired
    private UserMapper userMapper;

    public List<GroupBuy> getActiveGroups() {
        return groupBuyMapper.findActiveGroups();
    }

    @Transactional
    public GroupBuy startGroup(Long userId, Long productId, String address) {
        // Logic: Monday only (Optional: comment out if you want to test any day)
        // LocalDateTime now = LocalDateTime.now();
        // if (now.getDayOfWeek() != DayOfWeek.MONDAY) {
        //    throw new RuntimeException("拼团活动仅在周一开启！");
        // }

        // Create the initiator's order first
        ProductOrder order = orderService.createOrder(userId, productId, address, "GROUP");
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
        
        return gb;
    }

    @Transactional
    public GroupBuy joinGroup(Long userId, Long groupId, String address) {
        GroupBuy gb = groupBuyMapper.findById(groupId);
        if (gb == null || gb.getStatus() != 0 || gb.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("该拼团已失效或已结束");
        }
        
        if (groupBuyMapper.checkMember(groupId, userId) > 0) {
            throw new RuntimeException("您已经参加过该拼团了");
        }

        // Create the participant's order
        ProductOrder order = orderService.createOrder(userId, gb.getProductId(), address, "GROUP");
        
        GroupBuyMember member = new GroupBuyMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setOrderId(order.getId());
        groupBuyMapper.insertMember(member);
        
        groupBuyMapper.incrementCount(groupId);
        
        // Check if successful
        GroupBuy updated = groupBuyMapper.findById(groupId);
        if (updated.getCurrentNum() >= updated.getRequiredNum()) {
            groupBuyMapper.updateStatus(groupId, 1); // Success
            
            // Reward initiator: "赠送一个" -> give 500 points (example) or enough points to buy next one
            userMapper.addPoints(updated.getInitiatorId(), 500);
        }
        
        return updated;
    }
}
