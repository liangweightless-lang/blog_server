package com.wtls.blog_server.mapper.product;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wtls.blog_server.entity.product.Product;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    @Select("SELECT * FROM product ORDER BY id ASC")
    List<Product> findAll();

    @Select("SELECT * FROM product WHERE id = #{id}")
    Product findById(Long id);

    @Update("UPDATE product SET stock = stock - #{count} WHERE id = #{id} AND stock >= #{count}")
    int reduceStock(@Param("id") Long id, @Param("count") int count);
}

