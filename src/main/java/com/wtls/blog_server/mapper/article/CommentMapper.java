package com.wtls.blog_server.mapper.article;

import com.wtls.blog_server.entity.article.Comment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface CommentMapper {
    @Select("SELECT * FROM comment WHERE article_id = #{articleId} ORDER BY create_time ASC")
    List<Comment> findByArticleId(Long articleId);

    @Insert("INSERT INTO comment(article_id, author_name, content, create_time) VALUES(#{articleId}, #{authorName}, #{content}, NOW())")
    void insert(Comment comment);
}
