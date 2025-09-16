package org.ebndrnk.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

/**
 * Utility component responsible for validating JWT tokens.
 * <p>
 * Parses and verifies the signature of a given JWT using a shared secret key.
 */
@Component
public class JwtTokenValidator {


    @Value("${secret.key}")
    private String secret;

    /**
     * Retrieves the signing key used to verify JWT signatures.
     *
     * @return the signing {@link Key}
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Validates and parses a JWT token.
     *
     * @param token the JWT token as a string
     * @return the {@link Claims} extracted from the token if it is valid
     * @throws RuntimeException if the token is invalid or cannot be parsed
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            throw new JwtException("Invalid JWT token", e);
        }
    }
}

