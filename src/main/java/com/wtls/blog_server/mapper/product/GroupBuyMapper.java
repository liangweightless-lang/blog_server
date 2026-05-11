package com.wtls.blog_server.mapper.product;

import com.wtls.blog_server.entity.product.GroupBuy;
import com.wtls.blog_server.entity.GroupBuyMember;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface GroupBuyMapper {
    @Insert("INSERT INTO group_buy(product_id, initiator_id, required_num, current_num, status, expire_time) " +
            "VALUES(#{productId}, #{initiatorId}, #{requiredNum}, 1, 0, #{expireTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(GroupBuy groupBuy);

    @Select("SELECT gb.*, p.name as productName, u.nickname as initiatorNickname " +
            "FROM group_buy gb " +
            "JOIN product p ON gb.product_id = p.id " +
            "JOIN user u ON gb.initiator_id = u.id " +
            "WHERE gb.status = 0 AND gb.expire_time > NOW()")
    List<GroupBuy> findActiveGroups();

    @Select("SELECT * FROM group_buy WHERE id = #{id}")
    GroupBuy findById(Long id);

    @Update("UPDATE group_buy SET current_num = current_num + 1 WHERE id = #{id}")
    void incrementCount(Long id);

    @Update("UPDATE group_buy SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Insert("INSERT INTO group_buy_member(group_id, user_id, order_id) VALUES(#{groupId}, #{userId}, #{orderId})")
    void insertMember(GroupBuyMember member);

    @Select("SELECT COUNT(*) FROM group_buy_member WHERE group_id = #{groupId} AND user_id = #{userId}")
    int checkMember(@Param("groupId") Long groupId, @Param("userId") Long userId);
}
