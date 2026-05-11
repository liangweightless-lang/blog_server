package com.wtls.blog_server.controller;

import com.wtls.blog_server.entity.GroupBuy;
import com.wtls.blog_server.service.GroupBuyService;
import com.wtls.blog_server.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "*")
public class GroupBuyController {

    @Autowired
    private GroupBuyService groupBuyService;

    private Long getUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Unauthorized");
        }
        Claims claims = JwtUtils.parseToken(authHeader.substring(7));
        return claims.get("userId", Long.class);
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveGroups() {
        return ResponseEntity.ok(groupBuyService.getActiveGroups());
    }

    @PostMapping("/start")
    public ResponseEntity<?> startGroup(@RequestHeader("Authorization") String authHeader, @RequestBody Map<String, Long> body) {
        try {
            Long userId = getUserId(authHeader);
            Long productId = body.get("productId");
            GroupBuy gb = groupBuyService.startGroup(userId, productId);
            return ResponseEntity.ok(gb);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{groupId}/join")
    public ResponseEntity<?> joinGroup(@RequestHeader("Authorization") String authHeader, @PathVariable Long groupId) {
        try {
            Long userId = getUserId(authHeader);
            GroupBuy gb = groupBuyService.joinGroup(userId, groupId);
            return ResponseEntity.ok(gb);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
