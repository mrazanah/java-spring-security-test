package com.example.authentication.jwt;

import com.example.authentication.domain.CustomUserDetails;
import com.example.authentication.exception.JwtProcessingException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {
    @Value("${jwt.expiration}")
    private int jwtExpirationInMinutes;

    private final Key key;

    public JwtTokenProvider(@Value("${jwt.secret}") String jwtSecret) {
        this.key = new SecretKeySpec(jwtSecret.getBytes(), SignatureAlgorithm.HS512.getJcaName());
    }

    public String generateToken(Authentication authentication) {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMinutes*60*1000L);

        return Jwts.builder()
                .setSubject(user.getUser().getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public void expireToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody().setExpiration(new Date());
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            handleException("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            handleException("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            handleException("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            handleException("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            handleException("JWT claims string is empty.");
        }
        return false;
    }
    private void handleException(String message) {
        log.error(message);
        throw new JwtProcessingException(message);
    }
}

