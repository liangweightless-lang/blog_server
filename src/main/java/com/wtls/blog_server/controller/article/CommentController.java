package com.wtls.blog_server.controller.article;

import com.wtls.blog_server.common.Result;
import com.wtls.blog_server.entity.article.Comment;
import com.wtls.blog_server.service.article.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@Tag(name = "评论接口", description = "文章评论的查询与发布")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/article/{articleId}")
    @Operation(summary = "获取文章下的所有评论")
    public Result<List<Comment>> getByArticleId(@PathVariable Long articleId) {
        return Result.success(commentService.getCommentsByArticleId(articleId));
    }

    @PostMapping
    @Operation(summary = "发布评论")
    public Result<Comment> create(@RequestBody Comment comment) {
        commentService.createComment(comment);
        return Result.success(comment);
    }
}
