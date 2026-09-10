package com.wtls.blog_server.service.product;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wtls.blog_server.entity.product.GroupBuy;
import com.wtls.blog_server.entity.product.GroupBuyMember;
import com.wtls.blog_server.entity.product.ProductOrder;
import com.wtls.blog_server.exception.BusinessException;
import com.wtls.blog_server.mapper.product.GroupBuyMapper;
import com.wtls.blog_server.mapper.product.ProductOrderMapper;
import com.wtls.blog_server.mapper.user.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 拼团核心业务服务层
 * 负责发起拼团、参与拼团、成团判定、超时自动结算及后台人工介入等
 */
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

    /**
     * 获取所有进行中的拼团活动列表
     *
     * @return 包含发起人信息的拼团列表
     */
    public List<GroupBuy> getActiveGroups() {
        return groupBuyMapper.findActiveGroupsWithNames();
    }

    /**
     * 发起拼团活动
     *
     * @param userId    发起人用户ID
     * @param productId 商品ID
     * @param address   收货地址
     * @return 发起成功的拼团实体
     */
    @Transactional
    public GroupBuy startGroup(Long userId, Long productId, String address) {
        // 1. 先为团长创建对应的待拼团订单
        ProductOrder order = orderService.createOrder(userId, productId, address, "GROUP", 0, null);
        
        // 2. 初始化拼团记录（默认成团人数8人，24小时有效）
        GroupBuy gb = new GroupBuy();
        gb.setProductId(productId);
        gb.setInitiatorId(userId);
        gb.setRequiredNum(8);
        gb.setExpireTime(LocalDateTime.now().plusHours(24));
        groupBuyMapper.insert(gb);
        
        // 3. 将团长加入团成员列表
        GroupBuyMember member = new GroupBuyMember();
        member.setGroupId(gb.getId());
        member.setUserId(userId);
        member.setOrderId(order.getId());
        groupBuyMapper.insertMember(member);
        
        log.info("[拼团] 用户 {} 发起了拼团, GroupID: {}, 关联订单: {}", userId, gb.getId(), order.getId());
        return gb;
    }

    /**
     * 参与已有的拼团活动
     *
     * @param userId  参团用户ID
     * @param groupId 拼团记录ID
     * @param address 收货地址
     * @return 更新后的拼团实体
     */
    @Transactional
    public GroupBuy joinGroup(Long userId, Long groupId, String address) {
        GroupBuy gb = groupBuyMapper.selectById(groupId);
        // 校验拼团是否存在、是否进行中、是否过期
        if (ObjUtil.isNull(gb) || !ObjUtil.equals(gb.getStatus(), 0) || gb.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("该拼团已失效或已结束");
        }
        
        // 防止重复参团
        if (groupBuyMapper.checkMember(groupId, userId) > 0) {
            throw new BusinessException("您已经参加过该拼团了");
        }

        // 1. 创建参团成员订单
        ProductOrder order = orderService.createOrder(userId, gb.getProductId(), address, "GROUP", 0, null);
        
        // 2. 插入拼团成员记录
        GroupBuyMember member = new GroupBuyMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setOrderId(order.getId());
        groupBuyMapper.insertMember(member);
        
        // 3. 自增当前参团人数
        groupBuyMapper.incrementCount(groupId);
        
        // 4. 检查是否达到成团人数阈值
        GroupBuy updated = groupBuyMapper.selectById(groupId);
        if (NumberUtil.nullToZero(updated.getCurrentNum()) >= NumberUtil.nullToZero(updated.getRequiredNum())) {
            groupBuyMapper.updateStatus(groupId, 1); // 1: 拼团成功
            
            // 奖励发起人积分
            userMapper.addPoints(updated.getInitiatorId(), 500);
            log.info("[拼团] 拼团达成! GroupID: {}, 已发放成团奖励至团长 {}", groupId, updated.getInitiatorId());
        }
        
        log.info("[拼团] 用户 {} 成功加入拼团 {}, 当前进度: {}/{}", 
                userId, groupId, updated.getCurrentNum(), updated.getRequiredNum());
        return updated;
    }

    /**
     * 查询指定用户在某个拼团中的订单编号
     *
     * @param groupId 拼团ID
     * @param userId  用户ID
     * @return 订单ID
     */
    public String getOrderIdForMember(Long groupId, Long userId) {
        return groupBuyMapper.getOrderIdForMember(groupId, userId);
    }

    /**
     * 获取指定用户参与过的所有拼团列表
     *
     * @param userId 用户ID
     * @return 拼团记录列表
     */
    public List<GroupBuy> getUserGroups(Long userId) {
        return groupBuyMapper.findByUserIdWithNames(userId);
    }

    /**
     * 获取系统全量拼团记录
     *
     * @return 拼团记录列表
     */
    public List<GroupBuy> getAllGroups() {
        return groupBuyMapper.findAllWithNames();
    }

    /**
     * 根据主键查询拼团信息
     *
     * @param id 拼团ID
     * @return 拼团实体
     */
    public GroupBuy getGroupById(Long id) {
        return groupBuyMapper.selectById(id);
    }

    /**
     * 查询指定拼团的所有参团成员详情
     *
     * @param groupId 拼团ID
     * @return 包含用户昵称头像等信息的映射列表
     */
    public List<Map<String, Object>> getGroupMembers(Long groupId) {
        List<Map<String, Object>> members = groupBuyMapper.findMemberDetailsByGroupId(groupId);
        return CollUtil.defaultIfEmpty(members, Collections.emptyList());
    }

    /**
     * 管理员强制成团（手动标记成功）
     *
     * @param groupId 拼团ID
     */
    @Transactional
    public void forceSuccess(Long groupId) {
        GroupBuy gb = groupBuyMapper.selectById(groupId);
        if (ObjUtil.isNull(gb) || !ObjUtil.equals(gb.getStatus(), 0)) {
            throw new BusinessException("拼团不存在或已处于非进行状态");
        }
        
        // 1. 更新拼团状态为成功 (1)
        gb.setStatus(1);
        groupBuyMapper.updateById(gb);
        
        // 2. 流转所有已支付成员订单为已发货/待收货状态 (3)
        List<GroupBuyMember> members = groupBuyMapper.findMembersByGroupId(groupId);
        if (CollUtil.isNotEmpty(members)) {
            for (GroupBuyMember member : members) {
                ProductOrder order = orderMapper.findById(member.getOrderId());
                if (ObjUtil.isNotNull(order) && ObjUtil.equals(order.getStatus(), 1)) {
                    order.setStatus(3);
                    orderMapper.updateStatus(order.getId(), 3);
                }
            }
        }
        log.warn("[ADMIN] 管理员强制拼团成功: GroupID={}", groupId);
    }

    /**
     * 管理员强制取消拼团并执行退款退积分
     *
     * @param groupId 拼团ID
     */
    @Transactional
    public void forceFail(Long groupId) {
        GroupBuy gb = groupBuyMapper.selectById(groupId);
        if (ObjUtil.isNull(gb) || !ObjUtil.equals(gb.getStatus(), 0)) {
            throw new BusinessException("拼团不存在或已处于非进行状态");
        }
        
        // 1. 更新拼团状态为失败 (2)
        gb.setStatus(2);
        groupBuyMapper.updateById(gb);
        
        // 2. 取消所有成员订单并原路退回扣除的积分
        List<GroupBuyMember> members = groupBuyMapper.findMembersByGroupId(groupId);
        if (CollUtil.isNotEmpty(members)) {
            for (GroupBuyMember member : members) {
                ProductOrder order = orderMapper.findById(member.getOrderId());
                if (ObjUtil.isNotNull(order)) {
                    orderMapper.updateStatus(order.getId(), 2); // 2: 已取消/失败
                    int points = NumberUtil.nullToZero(order.getPointsUsed());
                    if (points > 0) {
                        userMapper.addPoints(order.getUserId(), points);
                        log.info("[ADMIN] 强制拼团失败，已退还用户 {} 积分: {}", order.getUserId(), points);
                    }
                }
            }
        }
        log.warn("[ADMIN] 管理员强制拼团失败并退还资产: GroupID={}", groupId);
    }

    /**
     * 定时任务/自动结算：检查所有已过期的拼团活动
     * 将超时未成团的活动自动关闭，取消成员订单并退还抵扣积分
     */
    @Transactional
    public void checkExpiredGroups() {
        List<GroupBuy> expiredGroups = groupBuyMapper.selectList(
            new LambdaQueryWrapper<GroupBuy>()
                .eq(GroupBuy::getStatus, 0)
                .lt(GroupBuy::getExpireTime, LocalDateTime.now())
        );
        if (CollUtil.isEmpty(expiredGroups)) {
            return;
        }

        for (GroupBuy gb : expiredGroups) {
            // 1. 更新拼团状态为已失败 (2)
            groupBuyMapper.updateStatus(gb.getId(), 2);
            
            // 2. 依次取消参团成员订单并归还积分
            List<GroupBuyMember> members = groupBuyMapper.findMembersByGroupId(gb.getId());
            if (CollUtil.isNotEmpty(members)) {
                for (GroupBuyMember member : members) {
                    orderMapper.updateStatus(member.getOrderId(), 2);
                    
                    ProductOrder order = orderMapper.findById(member.getOrderId());
                    if (ObjUtil.isNotNull(order)) {
                        int points = NumberUtil.nullToZero(order.getPointsUsed());
                        if (points > 0) {
                            userMapper.addPoints(order.getUserId(), points);
                            log.info("[拼团结算] 超时结算退回用户 {} 积分: {}", order.getUserId(), points);
                        }
                    }
                }
            }
            log.warn("[拼团结算] 拼团超时未达标，自动结算失败: GroupID={}", gb.getId());
        }
    }
}
