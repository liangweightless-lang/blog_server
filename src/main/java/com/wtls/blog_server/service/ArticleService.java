package com.wtls.blog_server.service;

import com.wtls.blog_server.entity.Article;
import com.wtls.blog_server.mapper.ArticleMapper;
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
}
