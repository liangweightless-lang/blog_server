package com.wtls.blog_server.mapper.article;

import com.wtls.blog_server.entity.article.Article;
import com.wtls.blog_server.entity.article.ArticleFavorite;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ArticleFavoriteMapper {

    @Select("SELECT * FROM article_favorite WHERE user_id = #{userId} AND article_id = #{articleId}")
    ArticleFavorite findByUserIdAndArticleId(@Param("userId") Long userId, @Param("articleId") Long articleId);

    @Insert("INSERT INTO article_favorite(user_id, article_id, create_time) VALUES(#{userId}, #{articleId}, NOW())")
    void insert(@Param("userId") Long userId, @Param("articleId") Long articleId);

    @Delete("DELETE FROM article_favorite WHERE user_id = #{userId} AND article_id = #{articleId}")
    void delete(@Param("userId") Long userId, @Param("articleId") Long articleId);

    @Select("SELECT a.* FROM article a INNER JOIN article_favorite f ON a.id = f.article_id WHERE f.user_id = #{userId} ORDER BY f.create_time DESC")
    List<Article> findFavoriteArticlesByUserId(Long userId);
}
