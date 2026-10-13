package com.aeroops.springdemo.service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JwtService {
    private final SecretKey secretKey;
    private final int expiry;

    JwtService(@Value("${jwt.secret}") String jwtSecret, @Value("${jwt.expiration}") int expiry) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expiry = expiry;
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();

        Date expiration = Date.from(
                Instant.now().plus(expiry, ChronoUnit.MILLIS)
        );
        return Jwts.builder().setSubject(username).setExpiration(expiration).signWith(secretKey).compact();
    }

    public boolean validateToken(String token) {
        try
        {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        }
        catch(JwtException ex)
        {
            return false;
        }
    }

    public String extractUsername(String token)
    {
        String username = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getSubject();
        return username;
    }
}