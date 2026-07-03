package com.substring.foodies.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    private static final long EXPIRATION_TIME = 15*60*1000;
    private static final long EXPIRATION_TIME_REFRESH_TOKEN = 7*24*60*60*1000;

    private static final String REFRESH_TOKEN_TYPE="refresh_token";
    private static final String ACCESS_TOKEN_TYPE="access_token";

    @Value("${jwt.secret}")
    private String SECRET;

    public String generateToken(String username, boolean isAccessToken)
    {
        long expTime = isAccessToken ? EXPIRATION_TIME: EXPIRATION_TIME_REFRESH_TOKEN;
        String tokenType = isAccessToken ? ACCESS_TOKEN_TYPE : REFRESH_TOKEN_TYPE;
        Map<String, Object> claims = new HashMap<>();

        // This is basically done to identify if the token is refresh or access type.
        claims.put("typ", tokenType);

        String token = Jwts.builder()
                .subject(username)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expTime))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()), SignatureAlgorithm.HS256)
                .compact();

        return token;
    }


    public String getUsername(String token) {
        // Create a JWT parser builder
        String userName = Jwts.parser()
                // Set the secret key used to validate the JWT signature
                .setSigningKey(SECRET.getBytes())
                // Build the JWT parser
                .build()
                // Parse the signed JWT (JWS) and validate its signature
                .parseClaimsJws(token)
                // Extract the payload (claims) from the JWT
                .getBody()
                // Get the subject (username) from the claims
                .getSubject();

        // Return the extracted username
        return userName;
    }

    public boolean validateItem(String token)
    {
        try{
            Jwts.parser().setSigningKey(SECRET.getBytes()).build().parseClaimsJws(token);
            return true;
        }
        catch (JwtException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isTokenValid(String token) {
        Date expirationDate = Jwts.parser()
                .setSigningKey(SECRET.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();

        return expirationDate.after(new Date());
    }


    public boolean isRefreshToken(String token)
    {
        Claims body = Jwts.parser()
                .setSigningKey(SECRET.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();

        String myToken = (String) body.get("typ");
        return myToken.equals(REFRESH_TOKEN_TYPE);
    }

    public boolean isAccessToken(String token)
    {
        Claims body = Jwts.parser()
                .setSigningKey(SECRET.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();

        String myToken = (String) body.get("typ");
        return myToken.equals(ACCESS_TOKEN_TYPE);
    }
}


