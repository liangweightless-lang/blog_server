package com.wtls.blog_server.controller.article;

import com.wtls.blog_server.entity.article.Comment;
import com.wtls.blog_server.service.article.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/article/{articleId}")
    public List<Comment> getByArticleId(@PathVariable Long articleId) {
        return commentService.getCommentsByArticleId(articleId);
    }

    @PostMapping
    public Comment create(@RequestBody Comment comment) {
        commentService.createComment(comment);
        return comment;
    }
}
