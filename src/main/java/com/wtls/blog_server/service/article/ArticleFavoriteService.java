package com.wtls.blog_server.service.article;

import com.wtls.blog_server.entity.article.Article;
import com.wtls.blog_server.entity.article.ArticleFavorite;
import com.wtls.blog_server.mapper.article.ArticleFavoriteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticleFavoriteService {

    @Autowired
    private ArticleFavoriteMapper favoriteMapper;

    public boolean toggleFavorite(Long userId, Long articleId) {
        ArticleFavorite existing = favoriteMapper.findByUserIdAndArticleId(userId, articleId);
        if (existing != null) {
            favoriteMapper.delete(userId, articleId);
            return false; // Removed
        } else {
            favoriteMapper.insert(userId, articleId);
            return true; // Added
        }
    }

    public boolean isFavorited(Long userId, Long articleId) {
        if (userId == null) return false;
        return favoriteMapper.findByUserIdAndArticleId(userId, articleId) != null;
    }

    public List<Article> getMyFavorites(Long userId) {
        return favoriteMapper.findFavoriteArticlesByUserId(userId);
    }
}
