package com.bank.app.configuration;

import java.io.Serializable;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenUtil implements Serializable {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${expiration_delay}")
    private int delaiExpiration;

    public String getToken() {
        String token = null;
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            token = (String) authentication.getCredentials();
        }
        return token;
    }

    // retrieve username from jwt token
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    // retrieve expiration date from jwt token
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    public Object getByClaim(String token, String key) {
        return getAllClaimsFromToken(token).get(key);
    }

    // for retrieving any information from token we will need the secret key
    private Claims getAllClaimsFromToken(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // check if the token has expired
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    // while creating the token -
    // 1. Define claims of the token, like Issuer, Expiration, Subject, and the ID
    // 2. Sign the JWT using the HS512 algorithm and secret key.
    // 3. According to JWS Compact
    // Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
    // compaction of the JWT to a URL-safe string
    public String doGenerateToken(Map<String, Object> claims, String subject) {
        try {
            return Jwts
                    .builder()
                    .setClaims(claims)
                    .setSubject(subject)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + delaiExpiration))
                    .signWith(getSignInKey(), SignatureAlgorithm.HS512)
                    .compact();

        } catch (Exception e) {
            return null;
        }
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validate(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token);
            return true;
        } catch (SignatureException ex) {
            // logger.error("Invalid JWT signature - {}", ex.getMessage());
            // } catch (MalformedJwtException ex) {
            // logger.error("Invalid JWT token - {}", ex.getMessage());
            // } catch (ExpiredJwtException ex) {
            // logger.error("Expired JWT token - {}", ex.getMessage());
            // } catch (UnsupportedJwtException ex) {
            // logger.error("Unsupported JWT token - {}", ex.getMessage());
            // } catch (IllegalArgumentException ex) {
            // logger.error("JWT claims string is empty - {}", ex.getMessage());
            return false;
        }
    }

}
