package com.wtls.blog_server.mapper;

import com.wtls.blog_server.entity.ProductOrder;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProductOrderMapper {
    @Insert("INSERT INTO product_order(id, user_id, product_id, amount, status, shipping_address, order_type, create_time) " +
            "VALUES(#{id}, #{userId}, #{productId}, #{amount}, #{status}, #{shippingAddress}, #{orderType}, NOW())")
    void insert(ProductOrder order);

    @Update("UPDATE product_order SET status = #{status}, pay_time = CASE WHEN #{status}=1 THEN NOW() ELSE pay_time END WHERE id = #{id}")
    void updateStatus(@Param("id") String id, @Param("status") Integer status);

    @Select("SELECT * FROM product_order WHERE id = #{id}")
    ProductOrder findById(String id);

    @Select("SELECT * FROM product_order WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<ProductOrder> findByUserId(Long userId);

    @Select("SELECT * FROM product_order ORDER BY create_time DESC")
    List<ProductOrder> findAll();
}
