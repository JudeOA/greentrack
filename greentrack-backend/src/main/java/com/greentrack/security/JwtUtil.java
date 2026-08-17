package com.greentrack.security;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component @Slf4j
public class JwtUtil {
    @Value("${app.jwt.secret}") private String jwtSecret;
    @Value("${app.jwt.expiration-ms}") private long jwtExpirationMs;
    @Value("${app.jwt.refresh-expiration-ms}") private long refreshExpirationMs;

    private static final String TYPE_CLAIM = "typ";
    private static final String ACCESS_TYPE = "access";
    private static final String REFRESH_TYPE = "refresh";
    private static final String ROLE_CLAIM = "role";

    public String generateToken(UserDetails u) {
        // Include the user's role (e.g. "ADMIN"/"COLLECTOR"/"CITIZEN") in the token
        String role = u.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
                .findFirst().orElse("");
        Map<String,Object> claims = new HashMap<>();
        claims.put(TYPE_CLAIM, ACCESS_TYPE);
        claims.put(ROLE_CLAIM, role);
        return buildToken(claims, u.getUsername(), jwtExpirationMs);
    }

    public String generateRefreshToken(UserDetails u) {
        return buildToken(Map.of(TYPE_CLAIM, REFRESH_TYPE), u.getUsername(), refreshExpirationMs);
    }

    private String buildToken(Map<String,Object> claims, String subject, long exp) {
        return Jwts.builder().claims(claims).subject(subject)
                .issuedAt(new Date()).expiration(new Date(System.currentTimeMillis()+exp))
                .signWith(getSigningKey()).compact();
    }
    public boolean isTokenValid(String token, UserDetails u) {
        try {
            return extractUsername(token).equals(u.getUsername()) && !isTokenExpired(token)
                    && ACCESS_TYPE.equals(extractClaim(token, c -> c.get(TYPE_CLAIM, String.class)));
        }
        catch (JwtException e) { return false; }
    }
    public boolean isRefreshToken(String token) {
        try { return REFRESH_TYPE.equals(extractClaim(token, c -> c.get(TYPE_CLAIM, String.class))) && !isTokenExpired(token); }
        catch (JwtException e) { return false; }
    }
    public boolean isTokenExpired(String t) { return extractExpiration(t).before(new Date()); }
    public String extractUsername(String t) { return extractClaim(t, Claims::getSubject); }
    public Date extractExpiration(String t) { return extractClaim(t, Claims::getExpiration); }
    public <T> T extractClaim(String t, Function<Claims,T> r) { return r.apply(extractAllClaims(t)); }
    private Claims extractAllClaims(String t) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(t).getPayload();
    }
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
}
