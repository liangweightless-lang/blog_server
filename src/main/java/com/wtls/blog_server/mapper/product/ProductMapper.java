package com.wtls.blog_server.mapper.product;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wtls.blog_server.entity.product.Product;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    @Select("<script>" +
            "SELECT p.*, c.name as categoryName FROM product p " +
            "LEFT JOIN product_category c ON p.category_id = c.id " +
            "<where>" +
            "  <if test='status != null'> p.status = #{status} </if>" +
            "</where> " +
            "ORDER BY (CASE WHEN p.stock = 0 THEN 1 ELSE 0 END) ASC, p.is_top DESC, p.id DESC" +
            "</script>")
    List<Product> findAll(@Param("status") Integer status);

    @Select("SELECT * FROM product WHERE id = #{id}")
    Product findById(Long id);

    @Update("UPDATE product SET stock = CASE WHEN stock = -1 THEN -1 ELSE stock - #{count} END WHERE id = #{id} AND (stock = -1 OR stock >= #{count})")
    int reduceStock(@Param("id") Long id, @Param("count") int count);

    @Update("UPDATE product SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE product SET is_top = #{isTop} WHERE id = #{id}")
    int updateTop(@Param("id") Long id, @Param("isTop") Integer isTop);
}

