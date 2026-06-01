package com.retail.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类 - 生成/解析/验证 Token
 */
public class JwtUtil {

    private static final String SECRET = "retail-behavior-analysis-secret-key-2026";
    private static final long EXPIRE = 7 * 24 * 60 * 60 * 1000L; // 7 天

    /**
     * 生成 JWT Token
     */
    public static String generateToken(Long userId, String username, String role, Long storeId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        if (storeId != null) {
            claims.put("storeId", storeId);
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    /**
     * 解析 Token 获取 Claims
     */
    public static Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证 Token 是否有效
     */
    public static boolean validateToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) return false;
        return !claims.getExpiration().before(new Date());
    }

    /**
     * 从 Token 获取用户 ID
     */
    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        if (claims == null) return null;
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }

    /**
     * 从 Token 获取角色
     */
    public static String getRole(String token) {
        Claims claims = parseToken(token);
        if (claims == null) return null;
        return (String) claims.get("role");
    }

    /**
     * 从 Token 获取门店 ID
     */
    public static Long getStoreId(String token) {
        Claims claims = parseToken(token);
        if (claims == null) return null;
        Object storeId = claims.get("storeId");
        if (storeId == null) return null;
        if (storeId instanceof Integer) {
            return ((Integer) storeId).longValue();
        }
        return (Long) storeId;
    }

    /**
     * 从 Token 获取用户名
     */
    public static String getUsername(String token) {
        Claims claims = parseToken(token);
        if (claims == null) return null;
        return (String) claims.get("username");
    }
}
