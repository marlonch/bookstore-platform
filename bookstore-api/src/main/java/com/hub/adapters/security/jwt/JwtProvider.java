package com.hub.adapters.security.jwt;

import com.hub.application.auth.port.in.command.TokenGenerationCommand;
import com.hub.application.auth.port.out.TokenGeneratorPort;
import com.hub.domain.identity.Role;
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
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates and validates RS256-signed JWTs.
 * <p>
 * Signs tokens with the RSA private key (bookstore-api only). Other services
 * verify signatures using only the public key, so the private key never leaves
 * this service.
 */
@Slf4j
@Component
public class JwtProvider implements TokenGeneratorPort {

    @Value("${app.jwt.private-key-path}")
    private Resource privateKeyResource;

    @Value("${app.jwt.public-key-path}")
    private Resource publicKeyResource;

    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    @PostConstruct
    void init() throws Exception {
        privateKey = loadPrivateKey(privateKeyResource);
        publicKey = loadPublicKey(publicKeyResource);
    }

    @Override
    public String generate(TokenGenerationCommand command) {
        List<String> roleNames = command.roles().stream()
                .map(Role::name).collect(Collectors.toList());

        return Jwts.builder()
                .subject(command.username())
                .id(command.tokenId())
                .claim("userId", command.userId())
                .claim("roles", roleNames)
                .issuedAt(new Date())
                .expiration(Date.from(command.expiresAt()))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    /**
     * Parses and validates a JWT, returning its claims.
     *
     * @throws JwtException if the token is invalid or the signature does not verify
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
     * Loads an RSA private key from a PKCS#8 PEM resource
     * ({@code -----BEGIN PRIVATE KEY-----}).
     */
    private RSAPrivateKey loadPrivateKey(Resource resource) throws Exception {
        byte[] der = decodePem(resource);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(der));
    }

    /**
     * Loads an RSA public key from an X.509 PEM resource
     * ({@code -----BEGIN PUBLIC KEY-----}).
     */
    private RSAPublicKey loadPublicKey(Resource resource) throws Exception {
        byte[] der = decodePem(resource);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(der));
    }

    /** Strips PEM headers/footers and decodes the Base64 DER payload. */
    private byte[] decodePem(Resource resource) throws Exception {
        String pem = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        String base64 = pem.replaceAll("-----[^-]+-----", "").replaceAll("\\s", "");
        return Base64.getDecoder().decode(base64);
    }
}