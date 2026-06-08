package com.tamdao.auth.auth_app.security;

import com.tamdao.auth.auth_app.entity.Role;
import com.tamdao.auth.auth_app.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Getter
@Setter
public class JwtService {

    private final SecretKey key;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;
    private final String issuer;


    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-ttl-seconds}") long accessTtlSeconds,
            @Value("${security.jwt.refresh-ttl-seconds}") long refreshTtlSeconds,
            @Value("${security.jwt.issuer}") String issuer) {
        if (secret==null || secret.length()<32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters long");

        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
        this.issuer = issuer;
    }

    //gerenate access token
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        List<String> roles = user.getRoles() == null
                ?
                List.of() :
                user.getRoles()
                        .stream()
                        .map(Role::getName)
                        .toList();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTtlSeconds)))
                .claims(
                    Map.of(
                            "email", user.getEmail(),
                            "roles", roles,
                            "typ", "access"
                    )
                )
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    //generate refresh token
    public String generateRefreshToken(User user, String jti) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(jti)
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshTtlSeconds)))
                .claim("typ", "refresh")
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    // parse the token
    public Jws<Claims> parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    }

    public boolean isAccessToken(String token) {
        Claims c = parse(token).getPayload();
        return "access".equals(c.get("typ"));
    }

    public boolean isRefreshToken(String token) {
        Claims c = parse(token).getPayload();
        return "refresh".equals(c.get("typ"));
    }

    public UUID getUserIdFromToken(String token) {
        Claims c = parse(token).getPayload();
        return UUID.fromString(c.getSubject());
    }

     public String getJtiFromToken(String token) {
        Claims c = parse(token).getPayload();
        return c.getId();
    }

    public List<String> getRolesFromToken(String token) {
        Claims c = parse(token).getPayload();
        return (List<String>) c.get("roles");
    }

    public String getEmailFromToken(String token) {
        Claims c = parse(token).getPayload();
        return (String) c.get("email");
    }
}
