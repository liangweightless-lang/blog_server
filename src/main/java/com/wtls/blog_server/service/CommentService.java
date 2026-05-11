package com.wtls.blog_server.service;

import com.wtls.blog_server.entity.Comment;
import com.wtls.blog_server.mapper.CommentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    private CommentMapper commentMapper;

    public List<Comment> getCommentsByArticleId(Long articleId) {
        return commentMapper.findByArticleId(articleId);
    }

    public void createComment(Comment comment) {
        if (comment.getAuthorName() == null || comment.getAuthorName().isEmpty()) {
            comment.setAuthorName("热心网友");
        }
        commentMapper.insert(comment);
    }
}
