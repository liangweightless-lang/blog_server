package com.wtls.blog_server.controller.product;

import com.wtls.blog_server.entity.product.GroupBuy;
import com.wtls.blog_server.service.product.GroupBuyService;
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
    public ResponseEntity<?> startGroup(@RequestHeader("Authorization") String authHeader, @RequestBody Map<String, Object> body) {
        try {
            Long userId = getUserId(authHeader);
            Long productId = Long.valueOf(body.get("productId").toString());
            String address = body.getOrDefault("address", "").toString();
            GroupBuy gb = groupBuyService.startGroup(userId, productId, address);
            return ResponseEntity.ok(gb);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{groupId}/join")
    public ResponseEntity<?> joinGroup(@RequestHeader("Authorization") String authHeader, @PathVariable Long groupId, @RequestBody(required = false) Map<String, Object> body) {
        try {
            Long userId = getUserId(authHeader);
            String address = (body != null && body.containsKey("address")) ? body.get("address").toString() : "";
            GroupBuy gb = groupBuyService.joinGroup(userId, groupId, address);
            return ResponseEntity.ok(gb);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
