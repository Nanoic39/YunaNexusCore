package cc.nanoic.yuna.common.security.utils;

import cc.nanoic.yuna.common.security.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    /**
     * 生成 token
     * @param userId 用户ID
     * @param username 用户名
     * @return token
     */
    public String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        return generateToken(claims);
    }

    /**
     * 生成 token
     * @param claims 数据声明
     * @return token
     */
    private String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(generateExpirationDate())
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 从 token 中获取类型 (access/refresh)
     */
    public String getTokenType(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return (String) claims.get("type");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 token 中获取用户名
     */
    public String getUsernameFromToken(String token) {
        String username;
        try {
            Claims claims = getClaimsFromToken(token);
            username = (String) claims.get("username");
        } catch (Exception e) {
            username = null;
        }
        return username;
    }

    /**
     * 从 token 中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Long userId;
        try {
            Claims claims = getClaimsFromToken(token);
            Object idObj = claims.get("userId");
            if (idObj instanceof Integer) {
                userId = ((Integer) idObj).longValue();
            } else {
                userId = (Long) idObj;
            }
        } catch (Exception e) {
            userId = null;
        }
        return userId;
    }

    /**
     * 判断 token 是否失效
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 验证 token 是否有效 (包含类型校验)
     * @param token 令牌
     * @param username 用户名 (可为null，若为null则不校验用户名)
     * @param expectedType 期望的令牌类型 (access / refresh)
     */
    public boolean validateToken(String token, String username, String expectedType) {
        // 1. 基础校验
        if (isTokenExpired(token)) {
            return false;
        }

        // 2. 类型校验
        String type = getTokenType(token);
        if (type == null || !type.equals(expectedType)) {
            return false;
        }

        // 3. 用户名校验 (如果传入了用户名)
        if (username != null) {
            String tokenUsername = getUsernameFromToken(token);
            return tokenUsername != null && tokenUsername.equals(username);
        }

        return true;
    }

    /**
     * 从 token 中获取数据声明
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 生成 token 过期时间
     * @return 过期时间
     */
    private Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + jwtProperties.getAccessExpiration() * 1000);
    }

    /**
     * 从配置中获取签名密钥
     * @return 签名密钥
     */
    private Key getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}