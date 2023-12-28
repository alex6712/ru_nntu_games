package ru.nntu.games.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import ru.nntu.games.security.services.GamesUserDetails;

import java.security.Key;
import java.util.Date;

@Component
public class JWTUtils {
    private static final Logger logger = LoggerFactory.getLogger(JWTUtils.class);

    @Value("${games.app.jwtSecret}")
    private String jwtSecret;

    @Value("${games.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    public String generateJWT(@NotNull Authentication authentication) {
        GamesUserDetails userPrincipal = (GamesUserDetails) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Contract(" -> new")
    private @NotNull Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String getUserNameFromJWT(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJWT(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(token);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT string is empty: {}", e.getMessage());
        }

        return false;
    }
}
