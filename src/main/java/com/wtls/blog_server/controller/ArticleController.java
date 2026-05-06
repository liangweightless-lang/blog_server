package com.wtls.blog_server.controller;

import com.wtls.blog_server.entity.Article;
import com.wtls.blog_server.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @GetMapping
    public List<Article> getAll() {
        return articleService.getAllArticles();
    }

    @GetMapping("/{id}")
    public Article getById(@PathVariable Long id) {
        return articleService.getArticleById(id);
    }

    @PostMapping
    public Article create(@RequestBody Article article) {
        articleService.createArticle(article);
        return article;
    }
}
