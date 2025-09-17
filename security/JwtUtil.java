package com.example.booking.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtUtil {
    private final String secret;
    private final long expirationMs;
    private final Key key;

    public JwtUtil(org.springframework.core.env.Environment env) {
        this.secret = env.getProperty("jwt.secret", "changeme");
        this.expirationMs = Long.parseLong(env.getProperty("jwt.expiration-ms", "86400000"));
        this.key = Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    public String generateToken(String username, Set<String> roles) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validate(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String getUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
                .getBody().getSubject();
    }

    @SuppressWarnings("unchecked")
    public Set<String> getRoles(String token) {
        Object claim = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
                .getBody().get("roles");
        if (claim instanceof List) {
            return ((List<Object>) claim).stream().map(Object::toString).collect(Collectors.toSet());
        }
        return Set.of();
    }
}
