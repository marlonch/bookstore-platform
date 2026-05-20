package com.hub.bookstoreorderservice.support;

import io.jsonwebtoken.Jwts;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Generates RS256-signed JWTs for integration tests.
 * Uses the dev RSA private key at {@code classpath:keys/private.pem} (test resources only).
 */
public final class TestJwtFactory {

    private static final RSAPrivateKey PRIVATE_KEY = loadPrivateKey();

    private TestJwtFactory() {}

    /**
     * Builds a valid RS256 token for the given username and roles.
     *
     * @param username the JWT subject
     * @param roles    authority strings (e.g. "ROLE_USER", "ADMINISTRATOR")
     * @return a signed compact JWT
     */
    public static String token(String username, String... roles) {
        Instant now = Instant.now();
        return build(username, now, now.plusSeconds(3600), List.of(roles));
    }

    public static String expiredToken(String username, String... roles) {
        Instant past = Instant.now().minusSeconds(7200);
        return build(username, past, past.plusSeconds(3600), List.of(roles));
    }

    public static RSAPrivateKey privateKey() {
        return PRIVATE_KEY;
    }

    private static String build(String username, Instant issuedAt, Instant expiration, List<String> roles) {
        return Jwts.builder()
                .subject(username)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiration))
                .claim("roles", roles)
                .signWith(PRIVATE_KEY, Jwts.SIG.RS256)
                .compact();
    }

    private static RSAPrivateKey loadPrivateKey() {
        try {
            ClassPathResource resource = new ClassPathResource("keys/private.pem");
            String pem = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            String base64 = pem.replaceAll("-----[^-]+-----", "").replaceAll("\\s", "");
            byte[] der = Base64.getDecoder().decode(base64);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load test private key", e);
        }
    }
}
