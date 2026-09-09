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

    @Select("SELECT * FROM article ORDER BY is_top DESC, create_time DESC")
    List<Article> findAll();

    @Select("SELECT * FROM article WHERE title LIKE CONCAT('%', #{keyword}, '%') " +
            "OR content LIKE CONCAT('%', #{keyword}, '%') ORDER BY is_top DESC, create_time DESC")
    List<Article> search(String keyword);

    @Select("SELECT * FROM article WHERE id = #{id}")
    Article findById(Long id);

    @Insert("INSERT INTO article(title, content, cover_url, media_urls, is_top, product_id, tags, location, create_time, update_time) " +
            "VALUES(#{title}, #{content}, #{coverUrl}, #{mediaUrls}, COALESCE(#{isTop}, 0), #{productId}, #{tags}, #{location}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Article article);

    @Update("UPDATE article SET likes_count = likes_count + 1 WHERE id = #{id}")
    void incrementLikes(Long id);

    @Update("UPDATE article SET title=#{title}, content=#{content}, cover_url=#{coverUrl}, " +
            "media_urls=#{mediaUrls}, is_top=COALESCE(#{isTop}, is_top), product_id=#{productId}, tags=#{tags}, location=#{location}, update_time=NOW() WHERE id=#{id}")
    void update(Article article);

    @Update("UPDATE article SET is_top = #{isTop}, update_time = NOW() WHERE id = #{id}")
    void updateTop(@org.apache.ibatis.annotations.Param("id") Long id, @org.apache.ibatis.annotations.Param("isTop") Integer isTop);

    @Delete("DELETE FROM article WHERE id=#{id}")
    void delete(Long id);
}
