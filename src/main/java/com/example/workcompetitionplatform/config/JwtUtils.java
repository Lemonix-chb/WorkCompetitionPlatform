package com.example.workcompetitionplatform.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类
 * 提供JWT Token的生成、验证、解析和刷新功能
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Component
public class JwtUtils {

    /**
     * JWT密钥
     */
    @Value("${jwt.secret:workcompetitionplatformsecretkey2026javaproject}")
    private String secret;

    /**
     * JWT Token过期时间（毫秒）
     * 默认为24小时
     */
    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    /**
     * JWT Token刷新时间（毫秒）
     * 默认为7天
     */
    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshExpiration;

    /**
     * JWT Token请求头名称
     */
    @Value("${jwt.header:Authorization}")
    private String tokenHeader;

    /**
     * JWT Token前缀
     */
    @Value("${jwt.prefix:Bearer }")
    private String tokenPrefix;

    /**
     * 生成密钥
     *
     * @return SecretKey实例
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 根据用户信息生成Token
     *
     * @param userDetails 用户详情
     * @return JWT Token
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername(), expiration);
    }

    /**
     * 根据用户信息和额外声明生成Token
     *
     * @param userDetails 用户详情
     * @param extraClaims 额外声明
     * @return JWT Token
     */
    public String generateToken(UserDetails userDetails, Map<String, Object> extraClaims) {
        return createToken(extraClaims, userDetails.getUsername(), expiration);
    }

    /**
     * 生成刷新Token
     *
     * @param userDetails 用户详情
     * @return JWT刷新Token
     */
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername(), refreshExpiration);
    }

    /**
     * 创建Token
     *
     * @param claims 声明
     * @param subject 主题（通常是用户名）
     * @param expirationTime 过期时间（毫秒）
     * @return JWT Token
     */
    private String createToken(Map<String, Object> claims, String subject, Long expirationTime) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从Token中提取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 从Token中提取过期时间
     *
     * @param token JWT Token
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 从Token中提取指定的声明
     *
     * @param token JWT Token
     * @param claimsResolver 声明解析函数
     * @return 声明值
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 从Token中提取所有声明
     *
     * @param token JWT Token
     * @return 所有声明
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证Token是否过期
     *
     * @param token JWT Token
     * @return 是否过期
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 验证Token是否有效
     *
     * @param token JWT Token
     * @param userDetails 用户详情
     * @return 是否有效
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * 验证Token是否有效（仅检查格式和过期时间）
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 判断Token是否可以刷新
     * Token未过期且在刷新期内可以刷新
     *
     * @param token JWT Token
     * @return 是否可以刷新
     */
    public Boolean canTokenBeRefreshed(String token) {
        return !isTokenExpired(token);
    }

    /**
     * 刷新Token
     *
     * @param token 原JWT Token
     * @return 新JWT Token
     */
    public String refreshToken(String token) {
        final Claims claims = getAllClaimsFromToken(token);
        claims.put("refreshed", true);
        return createToken(claims, claims.getSubject(), expiration);
    }

    /**
     * 从请求头中提取Token
     *
     * @param authHeader 请求头Authorization值
     * @return JWT Token（去除前缀）
     */
    public String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith(tokenPrefix)) {
            return authHeader.substring(tokenPrefix.length());
        }
        return null;
    }

    /**
     * 获取Token请求头名称
     *
     * @return 请求头名称
     */
    public String getTokenHeader() {
        return tokenHeader;
    }

    /**
     * 获取Token前缀
     *
     * @return Token前缀
     */
    public String getTokenPrefix() {
        return tokenPrefix;
    }

    /**
     * 获取Token过期时间（毫秒）
     *
     * @return 过期时间
     */
    public Long getExpiration() {
        return expiration;
    }

    /**
     * 获取刷新Token过期时间（毫秒）
     *
     * @return 刷新过期时间
     */
    public Long getRefreshExpiration() {
        return refreshExpiration;
    }
}