package com.abdoul.gentech_fintech.Util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {
    @Value("${api.secret.key}")
    private String secretKey;

    public String createAccessToken (Long id){
        return Jwts.builder().signWith(Keys.hmacShaKeyFor(secretKey.getBytes())).subject(String.valueOf(id)).issuedAt(new Date()).expiration(new Date (System.currentTimeMillis() + 60 * 20 * 1000)).compact();
    }

    public String extractIdFromToken (String token){
        return Jwts.parser().verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes())).build().parseSignedClaims(token).getPayload().getSubject();
    }

    public boolean isTokenValid (String token){
        try{
            Jwts.parser().verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes())).build().parseSignedClaims(token);
            return true;
        }
        catch (Exception ex){
            return false;
        }
    }

    public String createRefresh(){
        return UUID.randomUUID().toString();
    }

}
