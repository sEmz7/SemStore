package ru.semstore.userservice.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.semstore.userservice.dto.jwt.JwtAuthDto;
import ru.semstore.userservice.model.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
@Slf4j
public class JwtService {
    private final String jwtSecret;

    public JwtService(@Value("${JWT_SECRET}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public JwtAuthDto generateAuthToken(User user) {
        JwtAuthDto jwtDto = new JwtAuthDto();
        jwtDto.setToken(generateJwtToken(user));
        jwtDto.setRefreshToken(generateRefreshToken(user));
        return jwtDto;
    }

    public JwtAuthDto refreshBaseToken(User user, String refreshToken) {
        JwtAuthDto jwtDto = new JwtAuthDto();
        jwtDto.setToken(generateJwtToken(user));
        jwtDto.setRefreshToken(refreshToken);
        return jwtDto;
    }

    public SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    private String generateJwtToken(User user) {
        Date date = Date.from(LocalDateTime.now().plusMinutes(30).atZone(ZoneId.systemDefault()).toInstant());
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("id", user.getId())
                .expiration(date)
                .signWith(getSignInKey())
                .compact();
    }

    private String generateRefreshToken(User user) {
        Date date = Date.from(LocalDateTime.now().plusDays(30).atZone(ZoneId.systemDefault()).toInstant());
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("id", user.getId())
                .expiration(date)
                .signWith(getSignInKey())
                .compact();
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return true;
        } catch (ExpiredJwtException expEx) {
            log.warn("Expired JwtException: {}", expEx.getMessage());
        } catch (UnsupportedJwtException expEx) {
            log.warn("Unsupported JwtException: {}", expEx.getMessage());
        } catch (MalformedJwtException expEx) {
            log.warn("Malformed JwtException: {}", expEx.getMessage());
        } catch (SecurityException expEx) {
            log.warn("Security Exception: {}", expEx.getMessage());
        } catch (Exception expEx) {
            log.warn("Invalid token: {}", expEx.getMessage());
        }
        return false;
    }
}
