package com.wtls.blog_server.mapper;

import com.wtls.blog_server.entity.Article;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ArticleMapper {

    @Select("SELECT * FROM article ORDER BY create_time DESC")
    List<Article> findAll();

    @Select("SELECT * FROM article WHERE id = #{id}")
    Article findById(Long id);

    @Insert("INSERT INTO article(title, content, create_time, update_time) VALUES(#{title}, #{content}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Article article);
}
