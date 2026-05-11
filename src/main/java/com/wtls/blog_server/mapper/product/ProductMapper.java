package com.wtls.blog_server.mapper.product;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wtls.blog_server.entity.product.Product;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProductMapper {

    @Select("SELECT * FROM product ORDER BY id ASC")
    List<Product> findAll();

    @Select("SELECT * FROM product WHERE id = #{id}")
    Product findById(Long id);

    @Insert("INSERT INTO product(name, description, price, image, is_digital, create_time, update_time) " +
            "VALUES(#{name}, #{description}, #{price}, #{image}, #{isDigital}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Product product);

    @Update("UPDATE product SET name=#{name}, description=#{description}, price=#{price}, image=#{image}, is_digital=#{isDigital}, update_time=NOW() WHERE id=#{id}")
    void update(Product product);

    @Delete("DELETE FROM product WHERE id=#{id}")
    void delete(Long id);
}
