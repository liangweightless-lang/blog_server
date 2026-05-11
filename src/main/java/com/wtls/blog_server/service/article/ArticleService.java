package com.wtls.blog_server.service.article;

import com.wtls.blog_server.entity.article.Article;
import com.wtls.blog_server.mapper.article.ArticleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    public List<Article> getAllArticles() {
        return articleMapper.findAll();
    }

    public Article getArticleById(Long id) {
        return articleMapper.findById(id);
    }

    public void createArticle(Article article) {
        articleMapper.insert(article);
    }

    public void likeArticle(Long id) {
        articleMapper.incrementLikes(id);
    }

    public void updateArticle(Article article) {
        articleMapper.update(article);
    }

    public void deleteArticle(Long id) {
        articleMapper.delete(id);
    }
}
