package com.hub.bookstoreorderservice.adapters.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Validates RS256-signed JWTs using the RSA public key.
 * <p>
 * order-service never holds the private key; signature verification with
 * the public key is sufficient for stateless authentication. Token revocation
 * is not checked — see design spec for the known limitation and mitigation.
 */
@Slf4j
@Component
public class JwtValidator {

    @Value("${app.jwt.public-key-path}")
    private Resource publicKeyResource;

    private RSAPublicKey publicKey;

    @PostConstruct
    void init() {
        publicKey = loadPublicKey(publicKeyResource);
    }

    /**
     * Parses and validates a JWT, returning its claims.
     *
     * @throws JwtException if the token is invalid or the RS256 signature does not verify
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("JWT validation failed: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Loads an RSA public key from an X.509 PEM resource
     * ({@code -----BEGIN PUBLIC KEY-----}).
     */
    private RSAPublicKey loadPublicKey(Resource resource) {
        try {
            String pem = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            String base64 = pem.replaceAll("-----[^-]+-----", "").replaceAll("\\s", "");
            byte[] der = Base64.getDecoder().decode(base64);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(der));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load RSA public key", e);
        }
    }
}
