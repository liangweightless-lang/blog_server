package com.wtls.blog_server.mapper.article;

import com.wtls.blog_server.entity.article.Article;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ArticleMapper {

    @Select("SELECT * FROM article ORDER BY create_time DESC")
    List<Article> findAll();

    @Select("SELECT * FROM article WHERE title LIKE CONCAT('%', #{keyword}, '%') " +
            "OR content LIKE CONCAT('%', #{keyword}, '%') ORDER BY create_time DESC")
    List<Article> search(String keyword);

    @Select("SELECT * FROM article WHERE id = #{id}")
    Article findById(Long id);

    @Insert("INSERT INTO article(title, content, cover_url, media_urls, product_id, create_time, update_time) " +
            "VALUES(#{title}, #{content}, #{coverUrl}, #{mediaUrls}, #{productId}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Article article);

    @Update("UPDATE article SET likes_count = likes_count + 1 WHERE id = #{id}")
    void incrementLikes(Long id);

    @Update("UPDATE article SET title=#{title}, content=#{content}, cover_url=#{coverUrl}, " +
            "media_urls=#{mediaUrls}, product_id=#{productId}, update_time=NOW() WHERE id=#{id}")
    void update(Article article);

    @Delete("DELETE FROM article WHERE id=#{id}")
    void delete(Long id);
}
