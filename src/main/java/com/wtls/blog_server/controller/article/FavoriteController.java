package com.wtls.blog_server.controller.article;

import com.wtls.blog_server.common.Result;
import com.wtls.blog_server.entity.article.Article;
import com.wtls.blog_server.service.article.ArticleFavoriteService;
import com.wtls.blog_server.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@Tag(name = "收藏接口", description = "文章收藏管理")
public class FavoriteController {

    @Autowired
    private ArticleFavoriteService favoriteService;

    private Long getUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        Claims claims = JwtUtils.parseToken(authHeader.substring(7));
        return claims.get("userId", Long.class);
    }

    @Data
    public static class FavoriteRequest {
        private Long articleId;
    }

    @PostMapping("/toggle")
    @Operation(summary = "切换收藏状态")
    public Result<Boolean> toggleFavorite(@RequestHeader("Authorization") String authHeader, @RequestBody FavoriteRequest request) {
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) return Result.error("未登录");
        if (request == null || request.getArticleId() == null) return Result.error("参数错误");
        boolean status = favoriteService.toggleFavorite(userId, request.getArticleId());
        return Result.success(status);
    }

    @GetMapping("/status/{articleId}")
    @Operation(summary = "检查收藏状态")
    public Result<Boolean> getStatus(@RequestHeader(value = "Authorization", required = false) String authHeader, @PathVariable Long articleId) {
        Long userId = getUserIdFromToken(authHeader);
        return Result.success(favoriteService.isFavorited(userId, articleId));
    }

    @GetMapping("/me")
    @Operation(summary = "获取我的收藏列表")
    public Result<List<Article>> getMyFavorites(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) return Result.error("未登录");
        return Result.success(favoriteService.getMyFavorites(userId));
    }
}
