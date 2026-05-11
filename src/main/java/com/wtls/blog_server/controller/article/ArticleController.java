package com.wtls.blog_server.controller.article;

import com.wtls.blog_server.common.Result;
import com.wtls.blog_server.entity.article.Article;
import com.wtls.blog_server.service.article.ArticleService;
import com.wtls.blog_server.service.user.UserService;
import com.wtls.blog_server.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@Tag(name = "文章接口", description = "博客文章的查询与管理")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserService userService;

    private void checkAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Unauthorized");
        }
        Claims claims = JwtUtils.parseToken(authHeader.substring(7));
        String role = claims.get("role", String.class);
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("Admin access required");
        }
    }

    @GetMapping
    @Operation(summary = "获取所有文章")
    public Result<List<Article>> getAll() {
        return Result.success(articleService.getAllArticles());
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取文章详情")
    public Result<Article> getById(@PathVariable Long id) {
        return Result.success(articleService.getArticleById(id));
    }

    @PostMapping
    @Operation(summary = "发布文章 (Admin)")
    public Result<Article> create(@RequestHeader("Authorization") String authHeader, @RequestBody Article article) {
        checkAdmin(authHeader);
        articleService.createArticle(article);
        return Result.success(article);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新文章 (Admin)")
    public Result<String> update(@RequestHeader("Authorization") String authHeader, @PathVariable Long id, @RequestBody Article article) {
        checkAdmin(authHeader);
        article.setId(id);
        articleService.updateArticle(article);
        return Result.success("Article updated");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除文章 (Admin)")
    public Result<String> delete(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        checkAdmin(authHeader);
        articleService.deleteArticle(id);
        return Result.success("Article deleted");
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "点赞文章")
    public Result<Void> likeArticle(@PathVariable Long id) {
        articleService.likeArticle(id);
        return Result.success(null);
    }
}
