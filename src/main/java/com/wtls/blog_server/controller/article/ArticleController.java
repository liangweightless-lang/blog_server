package com.wtls.blog_server.controller.article;

import com.wtls.blog_server.entity.article.Article;
import com.wtls.blog_server.entity.user.User;
import com.wtls.blog_server.service.article.ArticleService;
import com.wtls.blog_server.service.user.UserService;
import com.wtls.blog_server.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/articles")
@CrossOrigin(origins = "*")
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
        Long userId = claims.get("userId", Long.class);
        User user = userService.getUserInfo(userId);
        if (user == null || !"ADMIN".equals(user.getRole())) {
            throw new RuntimeException("Admin access required");
        }
    }

    @GetMapping
    public List<Article> getAll() {
        return articleService.getAllArticles();
    }

    @GetMapping("/{id}")
    public Article getById(@PathVariable Long id) {
        return articleService.getArticleById(id);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestHeader("Authorization") String authHeader, @RequestBody Article article) {
        try {
            checkAdmin(authHeader);
            articleService.createArticle(article);
            return ResponseEntity.ok(article);
        } catch (Exception e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@RequestHeader("Authorization") String authHeader, @PathVariable Long id, @RequestBody Article article) {
        try {
            checkAdmin(authHeader);
            article.setId(id);
            articleService.updateArticle(article);
            return ResponseEntity.ok(Map.of("message", "Article updated"));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        try {
            checkAdmin(authHeader);
            articleService.deleteArticle(id);
            return ResponseEntity.ok(Map.of("message", "Article deleted"));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/like")
    public void likeArticle(@PathVariable Long id) {
        articleService.likeArticle(id);
    }
}
