package ru.semstore.userservice.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.semstore.userservice.dto.jwt.JwtAuthDto;

import javax.crypto.SecretKey;
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

    public JwtAuthDto generateAuthToken(String email) {
        JwtAuthDto jwtDto = new JwtAuthDto();
        jwtDto.setToken(generateJwtToken(email));
        jwtDto.setRefreshToken(generateRefreshToken(email));
        return jwtDto;
    }

    public JwtAuthDto refreshBaseToken(String email, String refreshToken) {
        JwtAuthDto jwtDto = new JwtAuthDto();
        jwtDto.setToken(generateJwtToken(email));
        jwtDto.setRefreshToken(refreshToken);
        return jwtDto;
    }

    public SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String generateJwtToken(String email) {
        Date date = Date.from(LocalDateTime.now().plusMinutes(30).atZone(ZoneId.systemDefault()).toInstant());
        return Jwts.builder()
                .subject(email)
                .expiration(date)
                .signWith(getSignInKey())
                .compact();
    }
    private String generateRefreshToken(String email) {
        Date date = Date.from(LocalDateTime.now().plusDays(15).atZone(ZoneId.systemDefault()).toInstant());
        return Jwts.builder()
                .subject(email)
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
        }catch (ExpiredJwtException expEx){
            log.warn("Expired JwtException", expEx);
        }catch (UnsupportedJwtException expEx){
            log.warn("Unsupported JwtException", expEx);
        }catch (MalformedJwtException expEx){
            log.warn("Malformed JwtException", expEx);
        }catch (SecurityException expEx){
            log.warn("Security Exception", expEx);
        }catch (Exception expEx){
            log.warn("Invalid token", expEx);
        }
        return false;
    }
}
