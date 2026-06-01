package com.wtls.blog_server.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtils {
    // 使用固定的密钥，避免每次重启服务器导致 Token 失效
    private static final String SECRET = "blog_server_jwt_secret_key_needs_to_be_long_enough_for_hs256_!@#";
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    private static final long EXPIRATION_TIME = 86400000; // 1 day

    public static String generateToken(Long userId, String username, String role) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("role", role)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    public static Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (io.jsonwebtoken.JwtException e) {
            throw new com.wtls.blog_server.exception.UnauthorizedException("登录状态已过期或无效，请重新登录");
        }
    }
}
