package com.capstone.landlordInsurance.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    private String SECRET_KEY = "TaK+HaV^uvCHEFsEVfypW#7g9^k*Z8$V";

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // AccessToken has half an hour expiry
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username, 1000 * 60 * 30);
    }

    // Refresh Token has 24 hours expiration time
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username, 1000 * 60 * 60 * 24);
    }

    //My JWT auth is stateless, that means backend does not maintain login session.
    //I am not storing token in backend simply creating and passing to frontend which then stores in localstorage
    private String createToken(Map<String, Object> claims, String subject, long expirationTime) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .header().empty().add("typ", "JWT")
                .and()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime)) // 1 hour expiration time
                .signWith(getSigningKey())
                .compact();
    }

    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }
}
